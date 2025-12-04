package com.pig4cloud.pigx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import java.time.LocalDateTime;

/**
 * ES 通用查询 - 数据集定义表
 *
 * @author pigx
 * @date 2025-12-03 19:52:42
 */
@Data
@TenantTable
@TableName("t_es_dataset")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ES 通用查询 - 数据集定义表")
public class EsDataset extends Model<EsDataset> {


	/**
	* 主键ID
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="主键ID")
    private Long id;

	/**
	* 数据集编码（唯一）
	*/
    @Schema(description="数据集编码（唯一）")
    private String datasetCode;

	/**
	* 数据集名称
	*/
    @Schema(description="数据集名称")
    private String datasetName;

	/**
	* 所属业务模块/标签（如 patent/enterprise 等）
	*/
    @Schema(description="所属业务模块/标签（如 patent/enterprise 等）")
    private String bizModule;

	/**
	* 标签（逗号分隔，可选）
	*/
    @Schema(description="标签（逗号分隔，可选）")
    private String tags;

	/**
	* 目标 ES 索引名称
	*/
    @Schema(description="目标 ES 索引名称")
    private String esIndex;

	/**
	* 主键/唯一标识字段（SQL 字段别名）
	*/
    @Schema(description="主键/唯一标识字段（SQL 字段别名）")
    private String primaryField;

	/**
	* 增量依据字段（如 update_time 或自增ID）
	*/
    @Schema(description="增量依据字段（如 update_time 或自增ID）")
    private String incrementField;

	/**
	* 数据集 SQL 定义（支持多表 join）
	*/
    @Schema(description="数据集 SQL 定义（支持多表 join）")
    private String sqlText;

	/**
	* 状态：0-停用 1-启用
	*/
    @Schema(description="状态：0-停用 1-启用")
    private Integer status;

	/**
	* 备注
	*/
    @Schema(description="备注")
    private String remark;

	/**
	* 最近一次存量同步时间
	*/
    @Schema(description="最近一次存量同步时间")
    private LocalDateTime lastFullSyncTime;

	/**
	* 最近一次增量同步时间
	*/
    @Schema(description="最近一次增量同步时间")
    private LocalDateTime lastIncrementSyncTime;

	/**
	* 部门ID
	*/
    @Schema(description="部门ID")
    private Long deptId;

	/**
	* 扩展信息 JSON（预留）
	*/
    @Schema(description="扩展信息 JSON（预留）")
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