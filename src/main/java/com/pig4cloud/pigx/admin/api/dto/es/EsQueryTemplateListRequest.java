package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询模板列表入参
 */
@Data
@Schema(name = "EsQueryTemplateListRequest", description = "查询模板列表入参")
public class EsQueryTemplateListRequest {

    @Schema(description = "数据集ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long datasetId;

    @Schema(description = "模板类型：1-个人 2-公共（可选，不传则不按类型过滤）", example = "1")
    private Integer templateType;

    @Schema(description = "仅查看当前用户的个人模板（仅在 templateType=1 时生效）", example = "true")
    private Boolean onlyMine;

    @Schema(description = "关键字（按模板名称模糊查询，可选）", example = "近一年授权")
    private String keyword;
}
