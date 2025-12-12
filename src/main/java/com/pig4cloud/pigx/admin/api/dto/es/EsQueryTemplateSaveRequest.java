package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询模板 - 新增/编辑入参
 */
@Data
@Schema(name = "EsQueryTemplateSaveRequest", description = "查询模板保存入参")
public class EsQueryTemplateSaveRequest {

    @Schema(description = "模板ID，新建为空，编辑必填")
    private Long id;

    @Schema(description = "数据集ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long datasetId;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "近一年授权")
    private String templateName;

    @Schema(description = "模板类型：1-个人 2-公共", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer templateType;

    @Schema(description = "是否默认模板：true-默认 false-否", example = "false")
    private Boolean isDefault;

    @Schema(description = "显示排序，默认 100", example = "100")
    private Integer sortOrder;

    @Schema(description = "查询条件配置 JSON 字符串（前端直接 JSON.stringify）")
    private String filtersJson;

    @Schema(description = "列配置 JSON 字符串（前端直接 JSON.stringify）")
    private String columnsJson;

    @Schema(description = "排序配置 JSON 字符串（前端直接 JSON.stringify）")
    private String sortsJson;
}
