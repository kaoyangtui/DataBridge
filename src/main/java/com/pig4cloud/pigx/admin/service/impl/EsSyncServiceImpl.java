package com.pig4cloud.pigx.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncResultVO;
import com.pig4cloud.pigx.admin.entity.EsDataset;
import com.pig4cloud.pigx.admin.entity.EsDatasetField;
import com.pig4cloud.pigx.admin.service.EsDatasetFieldService;
import com.pig4cloud.pigx.admin.service.EsDatasetService;
import com.pig4cloud.pigx.admin.service.EsIndexService;
import com.pig4cloud.pigx.admin.service.EsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsSyncServiceImpl implements EsSyncService {

    private final EsDatasetService esDatasetService;
    private final EsDatasetFieldService esDatasetFieldService;
    private final EsIndexService esIndexService;
    private final ElasticsearchClient esClient;
    private final DataSource dataSource;

    /** bulk 最大重试次数（仅重试可重试的失败 item） */
    private static final int BULK_MAX_RETRY = 3;

    /** errors 只保留前 N 条，避免爆内存 */
    private static final int MAX_ERROR_KEEP = 50;

    /**
     * 接口方法：保持不改（默认使用 dataset.commitBatch）
     */
    @Override
    public EsSyncResultVO fullSync(Long datasetId, boolean recreateIndex) {
        return this.fullSyncInternal(datasetId, recreateIndex, null);
    }

    /**
     * ⭐做法A：实现类新增重载（不改接口）
     * overrideBatchSize != null 时，优先使用该值覆盖 dataset.commitBatch
     */
    public EsSyncResultVO fullSyncWithBatch(Long datasetId, boolean recreateIndex, Integer overrideBatchSize) {
        return this.fullSyncInternal(datasetId, recreateIndex, overrideBatchSize);
    }

    /**
     * 内部实现：batchSize 优先使用 overrideBatchSize
     */
    private EsSyncResultVO fullSyncInternal(Long datasetId, boolean recreateIndex, Integer overrideBatchSize) {
        EsDataset ds = esDatasetService.getById(datasetId);
        if (ds == null || "1".equals(ds.getDelFlag())) {
            throw new IllegalArgumentException("数据集不存在或已删除");
        }
        if (StrUtil.isBlank(ds.getEsIndex())) {
            throw new IllegalArgumentException("数据集未配置 ES 索引");
        }
        if (StrUtil.isBlank(ds.getSqlText())) {
            throw new IllegalArgumentException("数据集未配置 SQL");
        }
        if (StrUtil.isBlank(ds.getPrimaryField())) {
            throw new IllegalArgumentException("数据集未配置 primaryField（用于 ES _id）");
        }

        // 1) 字段配置（用于：白名单/别名/类型 normalize）
        List<EsDatasetField> fieldRows = esDatasetFieldService.list(
                Wrappers.lambdaQuery(EsDatasetField.class)
                        .eq(EsDatasetField::getDatasetId, datasetId)
        );
        if (CollUtil.isEmpty(fieldRows)) {
            throw new IllegalArgumentException("当前数据集未配置字段(fieldList)，请先在数据集管理里生成字段配置");
        }

        FieldRuleSet rules = buildRules(fieldRows);

        // 2) 建/重建索引
        if (recreateIndex) {
            esIndexService.recreateIndexForDataset(datasetId, true);
        }

        // 3) batchSize：override > dataset.commitBatch > 默认 2000
        int batchSize;
        if (overrideBatchSize != null) {
            batchSize = overrideBatchSize;
        } else {
            batchSize = (ds.getCommitBatch() == null ? 2000 : ds.getCommitBatch());
        }
        batchSize = Math.max(100, Math.min(batchSize, 10000));

        String wrappedSql = "SELECT * FROM (" + ds.getSqlText() + ") t";
        String index = ds.getEsIndex();

        EsSyncResultVO vo = new EsSyncResultVO();
        vo.setDatasetId(datasetId);
        vo.setIndex(index);

        long readTotal = 0;
        long success = 0;
        long fail = 0;
        List<String> errors = new ArrayList<>();

        String primaryField = ds.getPrimaryField();
        String pipelineId = ds.getEsPipelineId();

        log.info("全量同步开始 datasetId={}, index={}, batchSize={}, overrideBatchSize={}, primaryField={}, pipelineId={}, sql=\n{}",
                datasetId, index, batchSize, overrideBatchSize, primaryField, pipelineId, wrappedSql);

        try (Connection conn = dataSource.getConnection()) {

            boolean isMySql = isMySql(conn);
            // MySQL 真流式：需要配合驱动；这里尽量做到“能流就流”
            if (isMySql) {
                try {
                    conn.setAutoCommit(false);
                } catch (Exception ignore) {
                    // ignore
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    wrappedSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

                // fetchSize：MySQL 里 MIN_VALUE 通常会启用 streaming；其他库按 batchSize
                if (isMySql) {
                    ps.setFetchSize(Integer.MIN_VALUE);
                } else {
                    ps.setFetchSize(batchSize);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    List<BulkOperation> ops = new ArrayList<>(batchSize);
                    List<String> opIds = new ArrayList<>(batchSize);

                    while (rs.next()) {
                        readTotal++;

                        Map<String, Object> doc = new HashMap<>(colCount);
                        Object idValFromRow = null;

                        for (int i = 1; i <= colCount; i++) {
                            String label = meta.getColumnLabel(i);
                            String name = meta.getColumnName(i);
                            String col = StrUtil.blankToDefault(label, name);
                            if (StrUtil.isBlank(col)) {
                                continue;
                            }

                            Object rawVal = rs.getObject(i);

                            // 主键值尽量从行里直接抓（不依赖字段配置）
                            if (idValFromRow == null && equalsIgnoreCase(col, primaryField)) {
                                idValFromRow = rawVal;
                            }

                            // 字段白名单：字段配置不存在/禁用 => 不写入
                            FieldRule r = rules.match(col);
                            if (r == null || !r.enabled) {
                                continue;
                            }

                            Object normVal = normalizeValue(r, rawVal);
                            doc.put(r.esField, normVal);
                        }

                        // 主键 fallback：如果上面没抓到，再从 doc 里尝试捞
                        Object idVal = idValFromRow;
                        if (idVal == null) {
                            idVal = getCaseInsensitive(doc, primaryField);
                        }
                        if (idVal == null) {
                            fail++;
                            addErr(errors, "第 " + readTotal + " 行缺少主键字段 " + primaryField);
                            continue;
                        }

                        String esId = String.valueOf(idVal);

                        // upsert=0 => create；否则 index（幂等覆盖）
                        if (ds.getEsUpsert() != null && ds.getEsUpsert() == 0) {
                            ops.add(BulkOperation.of(b -> b.create(c -> c.index(index).id(esId).document(doc))));
                        } else {
                            ops.add(BulkOperation.of(b -> b.index(i -> i.index(index).id(esId).document(doc))));
                        }
                        opIds.add(esId);

                        if (ops.size() >= batchSize) {
                            long[] sf = flushBulkWithRetry(index, pipelineId, ops, opIds, errors);
                            success += sf[0];
                            fail += sf[1];
                            ops.clear();
                            opIds.clear();
                        }
                    }

                    if (!ops.isEmpty()) {
                        long[] sf = flushBulkWithRetry(index, pipelineId, ops, opIds, errors);
                        success += sf[0];
                        fail += sf[1];
                        ops.clear();
                        opIds.clear();
                    }
                }
            }

            // 4) 更新数据集同步时间（按你表字段）
            EsDataset upd = new EsDataset();
            upd.setId(ds.getId());
            upd.setLastFullSyncTime(LocalDateTime.now());
            esDatasetService.updateById(upd);

        } catch (Exception e) {
            log.error("全量同步失败 datasetId=" + datasetId, e);
            throw new RuntimeException("全量同步失败：" + e.getMessage(), e);
        }

        vo.setReadTotal(readTotal);
        vo.setSuccess(success);
        vo.setFail(fail);
        vo.setErrors(errors);

        log.info("全量同步结束 datasetId={}, index={}, readTotal={}, success={}, fail={}, keptErrors={}",
                datasetId, index, readTotal, success, fail, errors.size());
        return vo;
    }

    /**
     * bulk：对可重试失败 item（429/502/503/504 等）做最多 BULK_MAX_RETRY 次重试
     * 返回：{success, fail}
     */
    private long[] flushBulkWithRetry(String index,
                                     String pipelineId,
                                     List<BulkOperation> ops,
                                     List<String> opIds,
                                     List<String> errors) throws Exception {

        long success = 0;
        long fail = 0;

        List<BulkOperation> curOps = ops;
        List<String> curIds = opIds;

        for (int attempt = 1; attempt <= BULK_MAX_RETRY; attempt++) {

            BulkRequest.Builder br = new BulkRequest.Builder();
            br.operations(curOps);
            if (StrUtil.isNotBlank(pipelineId)) {
                br.pipeline(pipelineId);
            }

            var resp = esClient.bulk(br.build());

            if (!resp.errors()) {
                success += curOps.size();
                return new long[]{success, fail};
            }

            // 逐条分析：可重试的收集起来重试；不可重试直接 fail
            List<BulkOperation> retryOps = new ArrayList<>();
            List<String> retryIds = new ArrayList<>();

            for (int i = 0; i < resp.items().size(); i++) {
                var item = resp.items().get(i);
                var err = item.error();

                if (err == null) {
                    success++;
                    continue;
                }

                int status = ObjectUtil.defaultIfNull(item.status(), 500);
                String id = (curIds != null && i < curIds.size()) ? curIds.get(i) : "UNKNOWN_ID";
                String reason = err.reason();

                if (isRetryableStatus(status) && attempt < BULK_MAX_RETRY) {
                    retryOps.add(curOps.get(i));
                    retryIds.add(id);
                    continue;
                }

                fail++;
                addErr(errors, "bulk failed index=" + index + ", id=" + id + ", status=" + status + ", reason=" + reason);
            }

            if (retryOps.isEmpty()) {
                return new long[]{success, fail};
            }

            // 退避重试
            long sleepMs = 200L * (1L << (attempt - 1)); // 200,400,800...
            TimeUnit.MILLISECONDS.sleep(sleepMs);

            curOps = retryOps;
            curIds = retryIds;
        }

        // 理论上不会走到这里（上面 attempt < MAX 才收集 retry）
        fail += curOps.size();
        return new long[]{success, fail};
    }

    private static boolean isRetryableStatus(int status) {
        return status == 429 || status == 502 || status == 503 || status == 504;
    }

    private static void addErr(List<String> errors, String msg) {
        if (errors == null) {
            return;
        }
        if (errors.size() >= MAX_ERROR_KEEP) {
            return;
        }
        errors.add(msg);
    }

    private static boolean isMySql(Connection conn) {
        try {
            String driver = conn.getMetaData().getDriverName();
            return driver != null && driver.toLowerCase().contains("mysql");
        } catch (Exception e) {
            return false;
        }
    }

    // --------------------- 字段规则 & normalize ---------------------

    /** 字段规则集合：支持别名/大小写匹配/下划线匹配 */
    private static class FieldRuleSet {
        private final Map<String, FieldRule> byKey = new HashMap<>();

        FieldRule match(String column) {
            if (StrUtil.isBlank(column)) {
                return null;
            }
            FieldRule r = byKey.get(column);
            if (r != null) {
                return r;
            }
            r = byKey.get(column.toLowerCase(Locale.ROOT));
            if (r != null) {
                return r;
            }
            r = byKey.get(toSnake(column));
            if (r != null) {
                return r;
            }
            return byKey.get(toSnake(column).toLowerCase(Locale.ROOT));
        }

        void putKeys(FieldRule r, String... keys) {
            for (String k : keys) {
                if (StrUtil.isBlank(k)) {
                    continue;
                }
                byKey.putIfAbsent(k, r);
                byKey.putIfAbsent(k.toLowerCase(Locale.ROOT), r);
                String s = toSnake(k);
                byKey.putIfAbsent(s, r);
                byKey.putIfAbsent(s.toLowerCase(Locale.ROOT), r);
            }
        }
    }

    /** 单字段规则：esField + type + array + separator + dateFormat + enabled */
    private static class FieldRule {
        String column;      // DB列/别名（fieldCode/columnName）
        String esField;     // ES字段名（esField/fieldCode）
        String type;        // 字段类型
        boolean array;      // 是否数组
        String separator;   // 分隔符（默认 ;）
        String dateFormat;  // 日期格式（可空）
        boolean enabled;    // 是否启用（默认 true）
    }

    /** 从 EsDatasetField 列表构建规则（尽量兼容不同字段命名） */
    private static FieldRuleSet buildRules(List<EsDatasetField> fields) {
        FieldRuleSet set = new FieldRuleSet();

        for (EsDatasetField f : fields) {
            FieldRule r = new FieldRule();

            String fieldCode = firstNonBlank(
                    getStr(f, "fieldCode"),
                    getStr(f, "columnCode"),
                    getStr(f, "colCode"),
                    getStr(f, "fieldName"),
                    getStr(f, "name")
            );

            String esField = firstNonBlank(
                    getStr(f, "esField"),
                    getStr(f, "esFieldCode"),
                    getStr(f, "esFieldName"),
                    fieldCode
            );

            r.column = fieldCode;
            r.esField = esField;

            r.type = firstNonBlank(
                    getStr(f, "fieldType"),
                    getStr(f, "esType"),
                    getStr(f, "type")
            );

            r.array = firstNonNullBool(f, false,
                    "isArray", "array", "arrayFlag", "multi", "multiple");

            r.separator = firstNonBlank(
                    getStr(f, "separator"),
                    getStr(f, "splitSeparator"),
                    getStr(f, "split")
            );
            if (StrUtil.isBlank(r.separator)) {
                r.separator = ";";
            }

            r.dateFormat = firstNonBlank(
                    getStr(f, "dateFormat"),
                    getStr(f, "format")
            );

            r.enabled = firstNonNullEnabled(f, true,
                    "enable", "enabled", "status", "isEnable", "isEnabled");

            if (StrUtil.isBlank(r.column)) {
                continue;
            }

            String alias = firstNonBlank(getStr(f, "alias"), getStr(f, "columnName"));
            set.putKeys(r, r.column, alias, r.esField);
        }

        return set;
    }

    /** 按规则做 value normalize，尽量让 ES 序列化与 mapping 友好 */
    private static Object normalizeValue(FieldRule r, Object raw) {
        if (raw == null) {
            return null;
        }

        if (raw instanceof Clob) {
            return clobToString((Clob) raw);
        }

        if (raw instanceof Blob) {
            return blobToBase64((Blob) raw);
        }

        if (raw instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) raw);
        }

        if (raw instanceof java.sql.Array) {
            try {
                Object arr = ((java.sql.Array) raw).getArray();
                if (arr instanceof Object[]) {
                    return Arrays.asList((Object[]) arr);
                }
                return arr;
            } catch (Exception e) {
                return String.valueOf(raw);
            }
        }

        if (r != null && r.array) {
            if (raw instanceof Collection) {
                List<Object> list = new ArrayList<>();
                for (Object o : (Collection<?>) raw) {
                    if (o == null) {
                        continue;
                    }
                    String s = String.valueOf(o).trim();
                    if (StrUtil.isBlank(s)) {
                        continue;
                    }
                    list.add(s);
                }
                return list;
            }
            if (raw instanceof String) {
                String s = ((String) raw).trim();
                if (StrUtil.isBlank(s)) {
                    return Collections.emptyList();
                }
                String[] parts = s.split(java.util.regex.Pattern.quote(r.separator));
                List<String> list = new ArrayList<>(parts.length);
                for (String p : parts) {
                    if (StrUtil.isBlank(p)) {
                        continue;
                    }
                    list.add(p.trim());
                }
                return list;
            }
        }

        if (raw instanceof Timestamp) {
            return formatDateTime(((Timestamp) raw).toInstant(), r);
        }
        if (raw instanceof java.sql.Date) {
            return formatDate(((java.sql.Date) raw).toLocalDate(), r);
        }
        if (raw instanceof java.util.Date) {
            Instant ins = Instant.ofEpochMilli(((java.util.Date) raw).getTime());
            return formatDateTime(ins, r);
        }
        if (raw instanceof LocalDateTime) {
            return formatDateTime(((LocalDateTime) raw).atZone(ZoneId.systemDefault()).toInstant(), r);
        }
        if (raw instanceof LocalDate) {
            return formatDate((LocalDate) raw, r);
        }

        if (raw instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) raw;
            String t = (r == null ? null : StrUtil.toUpperCase(StrUtil.blankToDefault(r.type, "")));
            if (StrUtil.containsAny(t, "LONG", "INTEGER", "INT", "SHORT")) {
                return bd.longValue();
            }
            if (StrUtil.containsAny(t, "DOUBLE", "FLOAT", "DECIMAL")) {
                return bd.doubleValue();
            }
            return bd.stripTrailingZeros().toPlainString();
        }

        String t = (r == null ? null : StrUtil.toUpperCase(StrUtil.blankToDefault(r.type, "")));
        if (StrUtil.containsAny(t, "BOOL", "BOOLEAN")) {
            return toBool(raw);
        }

        return raw;
    }

    private static String formatDateTime(Instant ins, FieldRule r) {
        if (ins == null) {
            return null;
        }
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime ldt = LocalDateTime.ofInstant(ins, zone);
        if (r != null && StrUtil.isNotBlank(r.dateFormat)) {
            try {
                return ldt.format(DateTimeFormatter.ofPattern(r.dateFormat));
            } catch (Exception ignore) {
                // ignore
            }
        }
        return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static String formatDate(LocalDate d, FieldRule r) {
        if (d == null) {
            return null;
        }
        if (r != null && StrUtil.isNotBlank(r.dateFormat)) {
            try {
                return d.format(DateTimeFormatter.ofPattern(r.dateFormat));
            } catch (Exception ignore) {
                // ignore
            }
        }
        return d.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static Boolean toBool(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue() != 0;
        }
        String s = String.valueOf(raw).trim();
        if (StrUtil.isBlank(s)) {
            return null;
        }
        if ("1".equals(s)) {
            return true;
        }
        if ("0".equals(s)) {
            return false;
        }
        if ("true".equalsIgnoreCase(s) || "y".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)) {
            return true;
        }
        if ("false".equalsIgnoreCase(s) || "n".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)) {
            return false;
        }
        return null;
    }

    private static String clobToString(Clob clob) {
        try (Reader reader = clob.getCharacterStream()) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[2048];
            int n;
            while ((n = reader.read(buf)) > 0) {
                sb.append(buf, 0, n);
                if (sb.length() > 2_000_000) {
                    break;
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String blobToBase64(Blob blob) {
        try {
            byte[] bytes = blob.getBytes(1, (int) Math.min(blob.length(), 2_000_000));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getCaseInsensitive(Map<String, Object> map, String key) {
        if (map == null || StrUtil.isBlank(key)) {
            return null;
        }
        if (map.containsKey(key)) {
            return map.get(key);
        }
        String lk = key.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().toLowerCase(Locale.ROOT).equals(lk)) {
                return e.getValue();
            }
        }
        return null;
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.equalsIgnoreCase(b) || toSnake(a).equalsIgnoreCase(toSnake(b));
    }

    /** camelCase -> snake_case（用于列名匹配增强） */
    private static String toSnake(String s) {
        if (StrUtil.isBlank(s)) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        char[] arr = s.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char c = arr[i];
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // -------- 兼容读取 EsDatasetField 的属性（字段名不一致也能跑） --------

    private static String getStr(Object bean, String prop) {
        try {
            Object v = BeanUtil.getProperty(bean, prop);
            if (v == null) {
                return null;
            }
            return String.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    private static String firstNonBlank(String... arr) {
        if (arr == null) {
            return null;
        }
        for (String s : arr) {
            if (StrUtil.isNotBlank(s)) {
                return s;
            }
        }
        return null;
    }

    private static boolean firstNonNullBool(Object bean, boolean def, String... props) {
        for (String p : props) {
            try {
                Object v = BeanUtil.getProperty(bean, p);
                if (v == null) {
                    continue;
                }
                if (v instanceof Boolean) {
                    return (Boolean) v;
                }
                if (v instanceof Number) {
                    return ((Number) v).intValue() != 0;
                }
                String s = String.valueOf(v).trim();
                if (StrUtil.isBlank(s)) {
                    continue;
                }
                if ("1".equals(s)) {
                    return true;
                }
                if ("0".equals(s)) {
                    return false;
                }
                if ("true".equalsIgnoreCase(s)) {
                    return true;
                }
                if ("false".equalsIgnoreCase(s)) {
                    return false;
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
        return def;
    }

    private static boolean firstNonNullEnabled(Object bean, boolean def, String... props) {
        for (String p : props) {
            try {
                Object v = BeanUtil.getProperty(bean, p);
                if (v == null) {
                    continue;
                }
                if (v instanceof Boolean) {
                    return (Boolean) v;
                }
                if (v instanceof Number) {
                    return ((Number) v).intValue() != 0;
                }
                String s = String.valueOf(v).trim();
                if (StrUtil.isBlank(s)) {
                    continue;
                }
                if ("1".equals(s)) {
                    return true;
                }
                if ("0".equals(s)) {
                    return false;
                }
                if ("Y".equalsIgnoreCase(s) || "YES".equalsIgnoreCase(s) || "TRUE".equalsIgnoreCase(s)) {
                    return true;
                }
                if ("N".equalsIgnoreCase(s) || "NO".equalsIgnoreCase(s) || "FALSE".equalsIgnoreCase(s)) {
                    return false;
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
        return def;
    }
}
