package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询模板返回对象
 */
@Data
@Schema(name = "EsQueryTemplateVO", description = "查询模板返回对象")
public class EsQueryTemplateVO {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "数据集ID")
    private Long datasetId;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板类型：1-个人 2-公共")
    private Integer templateType;

    @Schema(description = "拥有者用户ID（公共模板可为空）")
    private Long ownerUserId;

    @Schema(description = "是否默认模板")
    private Boolean isDefault;

    @Schema(description = "显示排序")
    private Integer sortOrder;

    @Schema(description = "查询条件配置 JSON")
    private String filtersJson;

    @Schema(description = "列显示/隐藏与顺序配置 JSON")
    private String columnsJson;

    @Schema(description = "排序字段配置 JSON")
    private String sortsJson;
}
