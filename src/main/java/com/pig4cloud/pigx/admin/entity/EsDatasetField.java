package com.pig4cloud.pigx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import java.time.LocalDateTime;

/**
 * ES 通用查询 - 数据集字段配置表
 *
 * @author pigx
 * @date 2025-12-03 19:53:21
 */
@Data
@TenantTable
@TableName("t_es_dataset_field")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ES 通用查询 - 数据集字段配置表")
public class EsDatasetField extends Model<EsDatasetField> {


	/**
	* 主键ID
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="主键ID")
    private Long id;

	/**
	* 所属数据集ID
	*/
    @Schema(description="所属数据集ID")
    private Long datasetId;

	/**
	* 字段编码/字段名（SQL 别名）
	*/
    @Schema(description="字段编码/字段名（SQL 别名）")
    private String fieldCode;

	/**
	* 字段展示名（例如：申请人）
	*/
    @Schema(description="字段展示名（例如：申请人）")
    private String fieldName;

	/**
	* 字段说明（帮助文本）
	*/
    @Schema(description="字段说明（帮助文本）")
    private String fieldDesc;

	/**
	* 逻辑字段类型：string/number/date/boolean 等
	*/
    @Schema(description="逻辑字段类型：string/number/date/boolean 等")
    private String logicType;

	/**
	* ES 字段类型：keyword/text/integer/long/double/date/boolean 等
	*/
    @Schema(description="ES 字段类型：keyword/text/integer/long/double/date/boolean 等")
    private String esType;

	/**
	* ES 分词器（如有）
	*/
    @Schema(description="ES 分词器（如有）")
    private String esAnalyzer;

	/**
	* 是否分词：0-否 1-是
	*/
    @Schema(description="是否分词：0-否 1-是")
    private Integer isAnalyzed;

	/**
	* 是否允许聚合：0-否 1-是
	*/
    @Schema(description="是否允许聚合：0-否 1-是")
    private Integer esAggregatable;

	/**
	* 是否启用 doc_values：0-否 1-是
	*/
    @Schema(description="是否启用 doc_values：0-否 1-是")
    private Integer esDocValues;

	/**
	* 是否在列表中显示：0-否 1-是
	*/
    @Schema(description="是否在列表中显示：0-否 1-是")
    private Integer listVisible;

	/**
	* 列表显示顺序（越小越靠前）
	*/
    @Schema(description="列表显示顺序（越小越靠前）")
    private Integer listOrder;

	/**
	* 列表列标题（默认字段展示名）
	*/
    @Schema(description="列表列标题（默认字段展示名）")
    private String listTitle;

	/**
	* 是否支持排序：0-否 1-是
	*/
    @Schema(description="是否支持排序：0-否 1-是")
    private Integer sortable;

	/**
	* 是否为默认排序字段：0-否 1-是
	*/
    @Schema(description="是否为默认排序字段：0-否 1-是")
    private Integer defaultSort;

	/**
	* 默认排序方向：ASC/DESC
	*/
    @Schema(description="默认排序方向：ASC/DESC")
    private String defaultSortDir;

	/**
	* 是否为查询条件：0-否 1-是
	*/
    @Schema(description="是否为查询条件：0-否 1-是")
    private Integer filterable;

	/**
	* 控件类型：text/select/multi_select/date/date_range/number_range 等
	*/
    @Schema(description="控件类型：text/select/multi_select/date/date_range/number_range 等")
    private String filterControl;

	/**
	* 查询方式：term/terms/match/match_phrase/range/prefix 等
	*/
    @Schema(description="查询方式：term/terms/match/match_phrase/range/prefix 等")
    private String filterOperator;

	/**
	* 是否常用查询条件：0-否 1-是（用于“常用/更多”区分）
	*/
    @Schema(description="是否常用查询条件：0-否 1-是（用于“常用/更多”区分）")
    private Integer isCommonFilter;

	/**
	* 是否参与导出：0-否 1-是
	*/
    @Schema(description="是否参与导出：0-否 1-是")
    private Integer exportable;

	/**
	* 导出列名（默认字段展示名）
	*/
    @Schema(description="导出列名（默认字段展示名）")
    private String exportTitle;

	/**
	* 导出列顺序
	*/
    @Schema(description="导出列顺序")
    private Integer exportOrder;

	/**
	* 扩展 JSON（前端渲染用的一些额外属性）
	*/
    @Schema(description="扩展 JSON（前端渲染用的一些额外属性）")
    private String extJson;

	/**
	* 创建人ID
	*/
	@TableField(fill = FieldFill.INSERT)
    @Schema(description="创建人ID")
    private Long createBy;

	/**
	* 创建时间
	*/
	@TableField(fill = FieldFill.INSERT)
    @Schema(description="创建时间")
    private LocalDateTime createTime;

	/**
	* 更新人ID
	*/
	@TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description="更新人ID")
    private Long updateBy;

	/**
	* 更新时间
	*/
	@TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description="更新时间")
    private LocalDateTime updateTime;

	/**
	* 删除标记：0-正常 1-删除
	*/
    @TableLogic
	@TableField(fill = FieldFill.INSERT)
    @Schema(description="删除标记：0-正常 1-删除")
    private String delFlag;

	/**
	* 租户ID
	*/
    @Schema(description="租户ID")
    private Long tenantId;
}