package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 通用查询 - 单条记录
 */
@Data
@Schema(name = "EsQueryHitVO", description = "通用查询 - 单条记录")
public class EsQueryHitVO {

    @Schema(description = "主键值（dataset.primaryField 对应的字段值）")
    private Object id;

    @Schema(description = "整行数据（字段名 -> 值）")
    private Map<String, Object> source;
}
