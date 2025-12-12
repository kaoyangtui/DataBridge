package com.pig4cloud.pigx.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pigx.admin.entity.EsDataset;
import com.pig4cloud.pigx.admin.entity.EsDatasetField;
import com.pig4cloud.pigx.admin.service.EsDatasetFieldService;
import com.pig4cloud.pigx.admin.service.EsDatasetService;
import com.pig4cloud.pigx.admin.service.EsIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES 索引管理：根据数据集配置创建 / 重建索引
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsIndexServiceImpl implements EsIndexService {

	private final EsDatasetService esDatasetService;
	private final EsDatasetFieldService esDatasetFieldService;
	private final ElasticsearchClient esClient;

	@Override
	public boolean recreateIndexForDataset(Long datasetId, boolean deleteIfExists) {
		EsDataset dataset = esDatasetService.getById(datasetId);
		if (dataset == null || "1".equals(dataset.getDelFlag())) {
			throw new IllegalArgumentException("数据集不存在或已删除");
		}
		if (StrUtil.isBlank(dataset.getEsIndex())) {
			throw new IllegalArgumentException("数据集未配置 ES 索引");
		}

		String index = dataset.getEsIndex();

		// 1) 字段配置
		List<EsDatasetField> fields = esDatasetFieldService.list(
				Wrappers.lambdaQuery(EsDatasetField.class)
						.eq(EsDatasetField::getDatasetId, datasetId)
		);
		if (CollUtil.isEmpty(fields)) {
			throw new IllegalArgumentException("当前数据集未配置字段，请先在数据集管理中配置字段");
		}

		try {
			// 2) 是否存在
			boolean exists = esClient.indices()
					.exists(new ExistsRequest.Builder().index(index).build())
					.value();

			if (exists) {
				if (!deleteIfExists) {
					log.info("ES 索引 {} 已存在，deleteIfExists=false，跳过重建", index);
					return true;
				}
				log.info("删除已存在的索引：{}", index);
				esClient.indices().delete(new DeleteIndexRequest.Builder().index(index).build());
			}

			// 3) 生成 mapping.properties
			Map<String, Property> props = new HashMap<>();
			for (EsDatasetField f : fields) {
				String fieldCode = f.getFieldCode();
				if (StrUtil.isBlank(fieldCode)) {
					continue;
				}
				String esType = StrUtil.blankToDefault(f.getEsType(), "keyword").trim().toLowerCase();
				props.put(fieldCode, buildPropertyFromField(esType, f));
			}

			// 4) 创建索引
			CreateIndexRequest createReq = new CreateIndexRequest.Builder()
					.index(index)
					.mappings(m -> m.properties(props))
					.build();

			esClient.indices().create(createReq);

			log.info("为数据集 {}({}) 创建 ES 索引 {} 成功",
					dataset.getDatasetName(), dataset.getId(), index);
			return true;

		} catch (IOException e) {
			log.error("创建/重建 ES 索引异常，index={}", index, e);
			throw new RuntimeException("创建/重建 ES 索引失败：" + e.getMessage(), e);
		}
	}

	/**
	 * 根据数据集字段配置生成 ES Property
	 * 兼容 elasticsearch-java 8.x：数字类型用 long_/integer/double_ 分支，不使用 number(...)
	 */
	private Property buildPropertyFromField(String esType, EsDatasetField f) {
		// doc_values 默认 true
		boolean docValues = f.getEsDocValues() == null || f.getEsDocValues() == 1;
		// 是否允许聚合/排序（text 默认不适合，keyword/number/date/boolean 一般可）
		boolean aggregatable = f.getEsAggregatable() == null || f.getEsAggregatable() == 1;

		switch (esType) {
			case "text":
				return Property.of(p -> p.text(t -> {
					if (StrUtil.isNotBlank(f.getEsAnalyzer())) {
						t.analyzer(f.getEsAnalyzer());
					}
					/**
					 * text 默认不能聚合/排序：
					 * 如果你希望可聚合/精确过滤，这里给它加一个 keyword 子字段：field.keyword
					 */
					if (aggregatable) {
						t.fields("keyword", Property.of(pp -> pp.keyword(k -> k.ignoreAbove(256))));
					}
					return t;
				}));

			case "keyword":
				return Property.of(p -> p.keyword(k -> {
					k.docValues(docValues);
					k.ignoreAbove(256);
					return k;
				}));

			case "date":
				return Property.of(p -> p.date(d -> {
					d.docValues(docValues);
					return d;
				}));

			case "long":
				return Property.of(p -> p.long_(n -> {
					n.docValues(docValues);
					return n;
				}));

			case "integer":
				return Property.of(p -> p.integer(n -> {
					n.docValues(docValues);
					return n;
				}));

			case "double":
				return Property.of(p -> p.double_(n -> {
					n.docValues(docValues);
					return n;
				}));

			case "boolean":
				return Property.of(p -> p.boolean_(b -> {
					b.docValues(docValues);
					return b;
				}));

			default:
				// 未识别类型，兜底 keyword
				return Property.of(p -> p.keyword(k -> {
					k.docValues(docValues);
					k.ignoreAbove(256);
					return k;
				}));
		}
	}
}
