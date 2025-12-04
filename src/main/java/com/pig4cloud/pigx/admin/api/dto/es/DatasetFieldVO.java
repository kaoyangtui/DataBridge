package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据集字段配置 VO
 */
@Data
@Schema(name = "DatasetFieldVO", description = "数据集字段配置 VO")
public class DatasetFieldVO {

    @Schema(description = "字段记录ID")
    private Long id;

    @Schema(description = "所属数据集ID")
    private Long datasetId;

    @Schema(description = "字段编码/字段名（SQL 别名）")
    private String fieldCode;

    @Schema(description = "字段展示名")
    private String fieldName;

    @Schema(description = "字段说明（帮助文本）")
    private String fieldDesc;

    @Schema(description = "逻辑字段类型：string/number/date/boolean 等")
    private String logicType;

    @Schema(description = "ES 字段类型")
    private String esType;

    @Schema(description = "ES 分词器（如有）")
    private String esAnalyzer;

    @Schema(description = "是否分词：0-否 1-是")
    private Integer isAnalyzed;

    @Schema(description = "是否允许聚合：0-否 1-是")
    private Integer esAggregatable;

    @Schema(description = "是否启用 doc_values：0-否 1-是")
    private Integer esDocValues;

    @Schema(description = "是否在列表中显示：0-否 1-是")
    private Integer listVisible;

    @Schema(description = "列表显示顺序（越小越靠前）")
    private Integer listOrder;

    @Schema(description = "列表列标题（默认字段展示名）")
    private String listTitle;

    @Schema(description = "是否支持排序：0-否 1-是")
    private Integer sortable;

    @Schema(description = "是否为默认排序字段：0-否 1-是")
    private Integer defaultSort;

    @Schema(description = "默认排序方向：ASC/DESC")
    private String defaultSortDir;

    @Schema(description = "是否为查询条件：0-否 1-是")
    private Integer filterable;

    @Schema(description = "查询控件类型")
    private String filterControl;

    @Schema(description = "查询方式：term/terms/match/match_phrase/range/prefix 等")
    private String filterOperator;

    @Schema(description = "是否常用查询条件：0-否 1-是")
    private Integer isCommonFilter;

    @Schema(description = "是否参与导出：0-否 1-是")
    private Integer exportable;

    @Schema(description = "导出列名")
    private String exportTitle;

    @Schema(description = "导出列顺序")
    private Integer exportOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
