package com.pig4cloud.pigx.admin.service;

public interface EsIndexService {

    /**
     * 为指定数据集创建/重建 ES 索引（mapping 来自 t_es_dataset_field）
     *
     * @param datasetId      数据集ID
     * @param deleteIfExists 如果索引已存在，是否先删除再创建
     * @return true 表示成功
     */
    boolean recreateIndexForDataset(Long datasetId, boolean deleteIfExists);

}
