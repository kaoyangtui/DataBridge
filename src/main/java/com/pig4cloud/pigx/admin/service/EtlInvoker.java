package com.pig4cloud.pigx.admin.service;

import java.util.Map;

/**
 * 调用 canal-adapter /etl 的小黑盒。
 */
public interface EtlInvoker {

    /**
     * 调用 canal-adapter ETL 接口
     *
     * @param mappingName es8 yml 的 mapping 名（不带 .yml），如 t_patent_info
     * @param params      增量区间参数，例如 {start: "...", end: "..."}，全量可以传 null
     * @return canal-adapter 返回的响应 body（文本）
     */
    String invokeEtl(String mappingName, Map<String, String> params);
}
