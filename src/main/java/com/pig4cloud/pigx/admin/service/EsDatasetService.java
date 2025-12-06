package com.pig4cloud.pigx.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pigx.admin.api.dto.es.*;
import com.pig4cloud.pigx.admin.entity.EsDataset;

public interface EsDatasetService extends IService<EsDataset> {
    /**
     * 分页查询数据集
     */
    IPage<DatasetPageItemVO> pageDatasets(DatasetPageRequest request);

    /**
     * 查询数据集详情（含字段配置）
     */
    DatasetDetailVO getDatasetDetail(Long id);

    /**
     * 保存或更新数据集（可带字段配置）
     */
    boolean saveOrUpdateDataset(DatasetSaveRequest request);

    /**
     * 校验 SQL 并预览字段
     */
    SqlValidateResultVO validateSql(DatasetSqlValidateRequest request);


    /**
     * 根据数据集配置生成 canal-adapter 的 mapping yml 文本（仅预览）
     */
    String generateAdapterYaml(Long datasetId);
}