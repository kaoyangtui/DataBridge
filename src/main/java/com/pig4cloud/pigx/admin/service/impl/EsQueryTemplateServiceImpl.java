package com.pig4cloud.pigx.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.entity.EsQueryTemplate;
import com.pig4cloud.pigx.admin.mapper.EsQueryTemplateMapper;
import com.pig4cloud.pigx.admin.service.EsQueryTemplateService;
import org.springframework.stereotype.Service;
/**
 * ES 通用查询 - 查询模板表
 *
 * @author pigx
 * @date 2025-12-03 19:54:23
 */
@Service
public class EsQueryTemplateServiceImpl extends ServiceImpl<EsQueryTemplateMapper, EsQueryTemplate> implements EsQueryTemplateService {
}