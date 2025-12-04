package com.pig4cloud.pigx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * ES 索引字段定义
 *
 * @author pigx
 * @date 2025-12-03 22:24:39
 */
@Data
@TableName("t_es_index_field")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ES 索引字段定义")
public class EsIndexField extends Model<EsIndexField> {


	/**
	* 主键
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="主键")
    private Long id;

	/**
	* 关联 t_es_index_def.id
	*/
    @Schema(description="关联 t_es_index_def.id")
    private Long indexId;

	/**
	* 字段名, 如 ipc
	*/
    @Schema(description="字段名, 如 ipc")
    private String fieldName;

	/**
	* ES 类型: keyword/text/date/long/nested 等
	*/
    @Schema(description="ES 类型: keyword/text/date/long/nested 等")
    private String dataType;

	/**
	* 分词器
	*/
    @Schema(description="分词器")
    private String analyzer;

	/**
	* 搜索分词器
	*/
    @Schema(description="搜索分词器")
    private String searchAnalyzer;

	/**
	* 是否多值字段(用于 pipeline 生成)
	*/
    @Schema(description="是否多值字段(用于 pipeline 生成)")
    private Integer isArray;

	/**
	* multi-fields JSON, 如 { "raw": { "type": "keyword" } }
	*/
    @Schema(description="multi-fields JSON")
    private String subFieldsJson;

	/**
	* doc_values
	*/
    @Schema(description="doc_values")
    private Integer docValues;

	/**
	* norms
	*/
    @Schema(description="norms")
    private Integer norms;

	/**
	* enabled
	*/
    @Schema(description="enabled")
    private Integer enabled;

	/**
	* ignore_above
	*/
    @Schema(description="ignore_above")
    private Integer ignoreAbove;

	/**
	* null_value
	*/
    @Schema(description="null_value")
    private String nullValue;

	/**
	* 字段排序
	*/
    @Schema(description="字段排序")
    private Integer sortOrder;

	/**
	* 备注
	*/
    @Schema(description="备注")
    private String remark;

	/**
	* 创建时间
	*/
	@TableField(fill = FieldFill.INSERT)
    @Schema(description="创建时间")
    private LocalDateTime createTime;

	/**
	* 更新时间
	*/
	@TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description="更新时间")
    private LocalDateTime updateTime;
}