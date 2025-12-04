package com.pig4cloud.pigx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import java.time.LocalDateTime;

/**
 * ES 通用查询 - 查询模板表
 *
 * @author pigx
 * @date 2025-12-03 19:54:23
 */
@Data
@TenantTable
@TableName("t_es_query_template")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ES 通用查询 - 查询模板表")
public class EsQueryTemplate extends Model<EsQueryTemplate> {


	/**
	* 主键ID
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="主键ID")
    private Long id;

	/**
	* 数据集ID
	*/
    @Schema(description="数据集ID")
    private Long datasetId;

	/**
	* 模板名称
	*/
    @Schema(description="模板名称")
    private String templateName;

	/**
	* 模板类型：1-个人 2-公共
	*/
    @Schema(description="模板类型：1-个人 2-公共")
    private Integer templateType;

	/**
	* 模板拥有者用户ID（公共模板时可为空或管理员ID）
	*/
    @Schema(description="模板拥有者用户ID（公共模板时可为空或管理员ID）")
    private Long ownerUserId;

	/**
	* 是否默认模板：0-否 1-是
	*/
    @Schema(description="是否默认模板：0-否 1-是")
    private Integer isDefault;

	/**
	* 显示排序
	*/
    @Schema(description="显示排序")
    private Integer sortOrder;

	/**
	* 查询条件配置 JSON
	*/
    @Schema(description="查询条件配置 JSON")
    private String filtersJson;

	/**
	* 列显示/隐藏与顺序配置 JSON
	*/
    @Schema(description="列显示/隐藏与顺序配置 JSON")
    private String columnsJson;

	/**
	* 排序字段配置 JSON
	*/
    @Schema(description="排序字段配置 JSON")
    private String sortsJson;

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