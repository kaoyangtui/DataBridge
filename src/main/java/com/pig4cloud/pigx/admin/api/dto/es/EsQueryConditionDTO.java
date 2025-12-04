package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用查询 - 单个条件
 */
@Data
@Schema(name = "EsQueryConditionDTO", description = "通用查询 - 单个条件")
public class EsQueryConditionDTO {

    @Schema(description = "字段编码（对应 EsDatasetField.fieldCode）", example = "applicant_name")
    private String fieldCode;

    @Schema(description = "操作符：term/terms/match/match_phrase/range/prefix 等", example = "match")
    private String operator;

    @Schema(description = "单值（term/match 等使用）", example = "南京大学")
    private String value;

    @Schema(description = "区间起始值（range 使用）", example = "2020-01-01")
    private String from;

    @Schema(description = "区间结束值（range 使用）", example = "2020-12-31")
    private String to;
}
