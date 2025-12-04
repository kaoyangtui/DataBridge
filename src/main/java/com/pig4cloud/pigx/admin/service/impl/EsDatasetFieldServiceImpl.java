package com.pig4cloud.pigx.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.entity.EsDatasetField;
import com.pig4cloud.pigx.admin.mapper.EsDatasetFieldMapper;
import com.pig4cloud.pigx.admin.service.EsDatasetFieldService;
import org.springframework.stereotype.Service;
/**
 * ES 通用查询 - 数据集字段配置表
 *
 * @author pigx
 * @date 2025-12-03 19:53:21
 */
@Service
public class EsDatasetFieldServiceImpl extends ServiceImpl<EsDatasetFieldMapper, EsDatasetField> implements EsDatasetFieldService {
}