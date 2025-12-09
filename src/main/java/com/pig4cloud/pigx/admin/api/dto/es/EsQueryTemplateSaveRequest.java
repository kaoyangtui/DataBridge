package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 保存 / 更新查询模板入参
 */
@Data
@Schema(name = "EsQueryTemplateSaveRequest", description = "保存/更新查询模板入参")
public class EsQueryTemplateSaveRequest {

    @Schema(description = "模板ID，新增为空，编辑必填")
    private Long id;

    @Schema(description = "数据集ID", required = true, example = "1")
    private Long datasetId;

    @Schema(description = "模板名称", required = true, example = "按公开日倒序")
    private String templateName;

    @Schema(description = "模板类型：1-个人 2-公共", required = true, example = "1")
    private Integer templateType;

    @Schema(description = "是否默认模板：true-是 false-否", example = "false")
    private Boolean isDefault;

    @Schema(description = "显示排序，越小越靠前", example = "100")
    private Integer sortOrder;

    @Schema(description = "查询条件配置 JSON（前端自己组织）")
    private String filtersJson;

    @Schema(description = "列显示/隐藏与顺序配置 JSON（前端自己组织）")
    private String columnsJson;

    @Schema(description = "排序字段配置 JSON（前端自己组织）")
    private String sortsJson;
}
