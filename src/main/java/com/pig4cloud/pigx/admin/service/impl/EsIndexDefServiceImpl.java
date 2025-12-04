package com.pig4cloud.pigx.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.entity.EsIndexDef;
import com.pig4cloud.pigx.admin.mapper.EsIndexDefMapper;
import com.pig4cloud.pigx.admin.service.EsIndexDefService;
import org.springframework.stereotype.Service;
/**
 * ES 索引定义
 *
 * @author pigx
 * @date 2025-12-03 22:23:56
 */
@Service
public class EsIndexDefServiceImpl extends ServiceImpl<EsIndexDefMapper, EsIndexDef> implements EsIndexDefService {
}