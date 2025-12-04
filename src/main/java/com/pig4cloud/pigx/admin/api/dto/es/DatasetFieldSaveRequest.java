package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据集字段配置 - 保存入参
 */
@Data
@Schema(name = "DatasetFieldSaveRequest", description = "数据集字段配置保存入参")
public class DatasetFieldSaveRequest {

    @Schema(description = "字段记录ID，新增为空，编辑必填")
    private Long id;

    @Schema(description = "字段编码/字段名（SQL 别名）", example = "app_number")
    private String fieldCode;

    @Schema(description = "字段展示名", example = "申请号")
    private String fieldName;

    @Schema(description = "字段说明（帮助文本）")
    private String fieldDesc;

    @Schema(description = "逻辑字段类型：string/number/date/boolean 等", example = "string")
    private String logicType;

    @Schema(description = "ES 字段类型：keyword/text/integer/long/double/date/boolean 等", example = "keyword")
    private String esType;

    @Schema(description = "ES 分词器（如有）", example = "ik_max_word")
    private String esAnalyzer;

    @Schema(description = "是否分词：0-否 1-是", example = "0")
    private Integer isAnalyzed;

    @Schema(description = "是否允许聚合：0-否 1-是", example = "1")
    private Integer esAggregatable;

    @Schema(description = "是否启用 doc_values：0-否 1-是", example = "1")
    private Integer esDocValues;

    // 列表展示 & 排序
    @Schema(description = "是否在列表中显示：0-否 1-是", example = "1")
    private Integer listVisible;

    @Schema(description = "列表显示顺序（越小越靠前）", example = "10")
    private Integer listOrder;

    @Schema(description = "列表列标题（默认字段展示名）", example = "申请号")
    private String listTitle;

    @Schema(description = "是否支持排序：0-否 1-是", example = "1")
    private Integer sortable;

    @Schema(description = "是否为默认排序字段：0-否 1-是", example = "0")
    private Integer defaultSort;

    @Schema(description = "默认排序方向：ASC/DESC", example = "DESC")
    private String defaultSortDir;

    // 查询条件配置
    @Schema(description = "是否为查询条件：0-否 1-是", example = "1")
    private Integer filterable;

    @Schema(description = "查询控件类型：text/select/multi_select/date/date_range/number_range 等", example = "text")
    private String filterControl;

    @Schema(description = "查询方式：term/terms/match/match_phrase/range/prefix 等", example = "term")
    private String filterOperator;

    @Schema(description = "是否常用查询条件：0-否 1-是（常用区 vs 更多条件）", example = "1")
    private Integer isCommonFilter;

    // 导出配置
    @Schema(description = "是否参与导出：0-否 1-是", example = "1")
    private Integer exportable;

    @Schema(description = "导出列名（默认字段展示名）", example = "申请号")
    private String exportTitle;

    @Schema(description = "导出列顺序", example = "10")
    private Integer exportOrder;
}
