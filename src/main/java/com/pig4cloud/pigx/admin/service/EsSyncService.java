package com.pig4cloud.pigx.admin.service;

import com.pig4cloud.pigx.admin.api.dto.es.EsSyncResultVO;

public interface EsSyncService {

    /**
     * 全量同步：执行 dataset.sqlText，把结果 bulk 写入 dataset.esIndex
     * @param datasetId 数据集ID
     * @param recreateIndex 是否先重建索引（mapping 来自字段配置）
     */
    EsSyncResultVO fullSync(Long datasetId, boolean recreateIndex);
}
