package com.pig4cloud.pigx.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
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
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsSyncServiceImpl implements EsSyncService {

    private final EsDatasetService esDatasetService;
    private final EsDatasetFieldService esDatasetFieldService;
    private final EsIndexService esIndexService;
    private final ElasticsearchClient esClient;
    private final DataSource dataSource;

    @Override
    public EsSyncResultVO fullSync(Long datasetId, boolean recreateIndex) {
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

        // 1) 确保字段配置存在（至少用来建 mapping，也能提示用户完善）
        List<EsDatasetField> fields = esDatasetFieldService.list(
                Wrappers.lambdaQuery(EsDatasetField.class)
                        .eq(EsDatasetField::getDatasetId, datasetId)
        );
        if (CollUtil.isEmpty(fields)) {
            throw new IllegalArgumentException("当前数据集未配置字段(fieldList)，请先在数据集管理里生成字段配置");
        }

        // 2) 建/重建索引
        if (recreateIndex) {
            esIndexService.recreateIndexForDataset(datasetId, true);
        } else {
            // 如果你希望“索引不存在就创建”，也可以这里做一个 exists 检测再 create
            // 简化：先不强制
        }

        int batchSize = ds.getCommitBatch() == null ? 2000 : ds.getCommitBatch();
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

        log.info("全量同步开始 datasetId={}, index={}, batchSize={}, sql=\n{}", datasetId, index, batchSize, wrappedSql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(wrappedSql,
                     ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

            // MySQL 流式读取（可选，某些驱动需要配合 useCursorFetch 等参数）
            ps.setFetchSize(batchSize);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                List<BulkOperation> ops = new ArrayList<>(batchSize);

                while (rs.next()) {
                    readTotal++;

                    Map<String, Object> doc = new HashMap<>(colCount);
                    for (int i = 1; i <= colCount; i++) {
                        String label = meta.getColumnLabel(i);
                        String name = meta.getColumnName(i);
                        String field = StrUtil.emptyToDefault(label, name);
                        Object val = rs.getObject(i);
                        doc.put(field, val);
                    }

                    Object idVal = doc.get(ds.getPrimaryField());
                    if (idVal == null) {
                        // 没主键直接跳过（也可以 fail++)
                        fail++;
                        if (errors.size() < 20) {
                            errors.add("第 " + readTotal + " 行缺少主键字段 " + ds.getPrimaryField());
                        }
                        continue;
                    }

                    String esId = String.valueOf(idVal);

                    // upsert=1 默认用 index（幂等覆盖）；upsert=0 可以用 create（已存在会报错）
                    if (ds.getEsUpsert() != null && ds.getEsUpsert() == 0) {
                        ops.add(BulkOperation.of(b -> b
                                .create(c -> c.index(index).id(esId).document(doc))
                        ));
                    } else {
                        ops.add(BulkOperation.of(b -> b
                                .index(i -> i.index(index).id(esId).document(doc))
                        ));
                    }

                    // 批量提交
                    if (ops.size() >= batchSize) {
                        long[] sf = flushBulk(index, ds.getEsPipelineId(), ops, errors);
                        success += sf[0];
                        fail += sf[1];
                        ops.clear();
                    }
                }

                // 最后一批
                if (!ops.isEmpty()) {
                    long[] sf = flushBulk(index, ds.getEsPipelineId(), ops, errors);
                    success += sf[0];
                    fail += sf[1];
                    ops.clear();
                }
            }

            // 3) 更新数据集同步时间（可选：你表里有 lastFullSyncTime）
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
        log.info("全量同步结束 datasetId={}, index={}, readTotal={}, success={}, fail={}",
                datasetId, index, readTotal, success, fail);
        return vo;
    }

    private long[] flushBulk(String index, String pipelineId,
                             List<BulkOperation> ops,
                             List<String> errors) throws Exception {

        BulkRequest.Builder br = new BulkRequest.Builder();
        br.operations(ops);

        // 使用 ingest pipeline（比如 split_semicolon_fields）
        if (StrUtil.isNotBlank(pipelineId)) {
            br.pipeline(pipelineId);
        }

        var resp = esClient.bulk(br.build());

        long success = 0;
        long fail = 0;

        if (resp.errors()) {
            // 逐条统计
            for (int i = 0; i < resp.items().size(); i++) {
                var item = resp.items().get(i);
                var err = item.error();
                if (err != null) {
                    fail++;
                    if (errors.size() < 20) {
                        errors.add("bulk item failed: index=" + index + ", reason=" + err.reason());
                    }
                } else {
                    success++;
                }
            }
        } else {
            success = ops.size();
        }

        return new long[]{success, fail};
    }
}
