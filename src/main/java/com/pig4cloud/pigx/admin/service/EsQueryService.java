package com.pig4cloud.pigx.admin.service;

import com.pig4cloud.pigx.admin.api.dto.es.EsQueryPageVO;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryRequest;

/**
 * ES 通用查询 - 运行时查询 Service
 */
public interface EsQueryService {

    /**
     * 执行查询
     */
    EsQueryPageVO query(EsQueryRequest request);
}
