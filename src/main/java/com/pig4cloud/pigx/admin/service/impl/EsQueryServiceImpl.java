package com.pig4cloud.pigx.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pigx.admin.api.dto.es.*;
import com.pig4cloud.pigx.admin.entity.EsDataset;
import com.pig4cloud.pigx.admin.entity.EsDatasetField;
import com.pig4cloud.pigx.admin.service.EsDatasetFieldService;
import com.pig4cloud.pigx.admin.service.EsDatasetService;
import com.pig4cloud.pigx.admin.service.EsQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ES 通用查询 - 查询实现（基于 elasticsearch-java 8.x 客户端）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsQueryServiceImpl implements EsQueryService {

    private final EsDatasetService esDatasetService;
    private final EsDatasetFieldService esDatasetFieldService;
    private final ElasticsearchClient esClient; // 8.x 官方新客户端

    @Override
    public EsQueryPageVO query(EsQueryRequest request) {
        // 1. 找到数据集
        EsDataset dataset = findDataset(request);
        if (dataset == null || "1".equals(dataset.getDelFlag())) {
            throw new IllegalArgumentException("数据集不存在或已删除");
        }

        String index = dataset.getEsIndex();
        if (StrUtil.isBlank(index)) {
            throw new IllegalArgumentException("数据集未配置 ES 索引");
        }

        // 2. 加载字段配置（用于校验 fieldCode & 确定可排序字段）
        List<EsDatasetField> fields = esDatasetFieldService.list(
                Wrappers.lambdaQuery(EsDatasetField.class)
                        .eq(EsDatasetField::getDatasetId, dataset.getId())
                        .eq(EsDatasetField::getDelFlag, "0")
        );
        Map<String, EsDatasetField> fieldMap = fields.stream()
                .collect(Collectors.toMap(EsDatasetField::getFieldCode, f -> f, (a, b) -> a));

        // 3. 构建 ES 查询
        long current = request.getCurrent() == null || request.getCurrent() < 1 ? 1 : request.getCurrent();
        long size = request.getSize() == null || request.getSize() < 1 ? 20 : request.getSize();

        // 3.1 where 条件 -> Query
        Query query = buildQuery(request.getConditions(), fieldMap);

        // 3.2 构建 SearchRequest
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(index)
                .from((int) ((current - 1) * size))
                .size((int) size)
                .query(query);

        // 3.3 排序
        buildSorts(builder, request.getSorts(), fieldMap);

        SearchRequest esRequest = builder.build();

        try {
            // 指定响应的 source 类型为 Map<String, Object>
            SearchResponse<Map<String, Object>> resp =
                    esClient.search(esRequest, (Class<Map<String, Object>>) (Class<?>) Map.class);

            EsQueryPageVO pageVO = new EsQueryPageVO();
            pageVO.setCurrent(current);
            pageVO.setSize(size);

            // total 可能为 null（track_total_hits=false 时），这里做个兜底
            long total = resp.hits().total() != null ? resp.hits().total().value() : 0L;
            pageVO.setTotal(total);

            List<EsQueryHitVO> records = resp.hits().hits().stream()
                    .map(hit -> convertHit(hit, dataset))
                    .collect(Collectors.toList());

            pageVO.setRecords(records);
            return pageVO;
        } catch (IOException e) {
            log.error("ES 通用查询执行异常", e);
            throw new RuntimeException("ES 查询失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据入参找到对应数据集
     */
    private EsDataset findDataset(EsQueryRequest request) {
        if (request.getDatasetId() != null) {
            return esDatasetService.getById(request.getDatasetId());
        }
        if (StrUtil.isNotBlank(request.getDatasetCode())) {
            return esDatasetService.getOne(
                    Wrappers.lambdaQuery(EsDataset.class)
                            .eq(EsDataset::getDatasetCode, request.getDatasetCode())
                            .eq(EsDataset::getDelFlag, "0"),
                    false
            );
        }
        return null;
    }

    /**
     * 将 ES Hit 转为 VO
     */
    private EsQueryHitVO convertHit(Hit<Map<String, Object>> hit, EsDataset dataset) {
        EsQueryHitVO vo = new EsQueryHitVO();

        Map<String, Object> source = hit.source() == null
                ? Collections.emptyMap()
                : hit.source();

        Object idVal = null;
        if (StrUtil.isNotBlank(dataset.getPrimaryField())) {
            idVal = source.get(dataset.getPrimaryField());
        }
        if (idVal == null) {
            idVal = hit.id(); // 兜底用 _id
        }

        vo.setId(idVal);
        vo.setSource(source);
        return vo;
    }

    /**
     * 构建 Query（where 条件）
     */
    private Query buildQuery(List<EsQueryConditionDTO> conditions,
                             Map<String, EsDatasetField> fieldMap) {
        if (CollUtil.isEmpty(conditions)) {
            // 没有条件时，用 match_all
            return Query.of(q -> q.matchAll(m -> m));
        }

        List<Query> mustQueries = new ArrayList<>();

        for (EsQueryConditionDTO c : conditions) {
            if (StrUtil.isBlank(c.getFieldCode())) {
                continue;
            }
            EsDatasetField field = fieldMap.get(c.getFieldCode());
            if (field == null || (field.getFilterable() != null && field.getFilterable() == 0)) {
                // 前端传了一个不允许查询的字段，直接忽略
                continue;
            }

            String op = StrUtil.emptyToDefault(
                    c.getOperator(),
                    StrUtil.emptyToDefault(field.getFilterOperator(), "term")
            );

            String esField = field.getFieldCode(); // 默认 ES 字段名 = fieldCode

            Query q = null;
            switch (op) {
                case "match":
                    if (StrUtil.isNotBlank(c.getValue())) {
                        q = Query.of(builder ->
                                builder.match(m -> m.field(esField).query(c.getValue()))
                        );
                    }
                    break;
                case "match_phrase":
                    if (StrUtil.isNotBlank(c.getValue())) {
                        q = Query.of(builder ->
                                builder.matchPhrase(m -> m.field(esField).query(c.getValue()))
                        );
                    }
                    break;
                case "range":
                    // from/to 可以是日期或数字，这里先用字符串，ES 会自己解析
                    RangeQuery.Builder rangeBuilder = new RangeQuery.Builder().field(esField);
                    if (StrUtil.isNotBlank(c.getFrom())) {
                        rangeBuilder.gte(JsonData.of(c.getFrom()));
                    }
                    if (StrUtil.isNotBlank(c.getTo())) {
                        rangeBuilder.lte(JsonData.of(c.getTo()));
                    }
                    q = Query.of(builder -> builder.range(rangeBuilder.build()));
                    break;
                case "prefix":
                    if (StrUtil.isNotBlank(c.getValue())) {
                        q = Query.of(builder ->
                                builder.prefix(p -> p.field(esField).value(c.getValue()))
                        );
                    }
                    break;
                case "term":
                default:
                    if (StrUtil.isNotBlank(c.getValue())) {
                        q = Query.of(builder ->
                                builder.term(t -> t.field(esField).value(c.getValue()))
                        );
                    }
                    break;
            }

            if (q != null) {
                mustQueries.add(q);
            }
        }

        if (mustQueries.isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        BoolQuery bool = new BoolQuery.Builder()
                .must(mustQueries)
                .build();

        return Query.of(q -> q.bool(bool));
    }

    /**
     * 构建排序
     */
    private void buildSorts(SearchRequest.Builder builder,
                            List<EsQuerySortDTO> sorts,
                            Map<String, EsDatasetField> fieldMap) {
        if (CollUtil.isEmpty(sorts)) {
            // 使用默认排序字段
            fieldMap.values().stream()
                    .filter(f -> f.getDefaultSort() != null && f.getDefaultSort() == 1)
                    .sorted(Comparator.comparing(f -> f.getListOrder() == null ? 100 : f.getListOrder()))
                    .forEach(f -> {
                        String dir = StrUtil.emptyToDefault(f.getDefaultSortDir(), "DESC");
                        SortOrder order = "ASC".equalsIgnoreCase(dir) ? SortOrder.Asc : SortOrder.Desc;
                        builder.sort(s -> s.field(sf -> sf.field(f.getFieldCode()).order(order)));
                    });
            return;
        }

        for (EsQuerySortDTO s : sorts) {
            EsDatasetField field = fieldMap.get(s.getFieldCode());
            if (field == null || field.getSortable() == null || field.getSortable() == 0) {
                continue;
            }
            String dir = StrUtil.emptyToDefault(s.getDirection(), "ASC");
            SortOrder order = "DESC".equalsIgnoreCase(dir) ? SortOrder.Desc : SortOrder.Asc;
            builder.sort(sb -> sb.field(sf -> sf.field(field.getFieldCode()).order(order)));
        }
    }
}
