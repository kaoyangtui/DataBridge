package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询模板列表入参
 */
@Data
@Schema(name = "EsQueryTemplateListRequest", description = "查询模板列表入参")
public class EsQueryTemplateListRequest {

    @Schema(description = "数据集ID", required = true, example = "1")
    private Long datasetId;

    @Schema(description = "模板类型：1-个人 2-公共，空则查询全部", example = "1")
    private Integer templateType;

    @Schema(description = "是否只查询当前用户（仅对个人模板生效），默认 true")
    private Boolean onlyMine;
}
