package com.pig4cloud.pigx.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.entity.EsIndexField;
import com.pig4cloud.pigx.admin.mapper.EsIndexFieldMapper;
import com.pig4cloud.pigx.admin.service.EsIndexFieldService;
import org.springframework.stereotype.Service;
/**
 * ES 索引字段定义
 *
 * @author pigx
 * @date 2025-12-03 22:24:39
 */
@Service
public class EsIndexFieldServiceImpl extends ServiceImpl<EsIndexFieldMapper, EsIndexField> implements EsIndexFieldService {
}