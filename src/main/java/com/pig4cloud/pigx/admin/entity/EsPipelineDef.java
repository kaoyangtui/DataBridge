package com.pig4cloud.pigx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * ES Pipeline 定义
 *
 * @author pigx
 * @date 2025-12-03 22:25:08
 */
@Data
@TableName("t_es_pipeline_def")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ES Pipeline 定义")
public class EsPipelineDef extends Model<EsPipelineDef> {


	/**
	* 主键
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="主键")
    private Long id;

	/**
	* ES pipeline 名称, 如 split_semicolon_fields
	*/
    @Schema(description="ES pipeline 名称, 如 split_semicolon_fields")
    private String pipelineId;

	/**
	* 中文名称
	*/
    @Schema(description="中文名称")
    private String pipelineName;

	/**
	* ES 集群标识
	*/
    @Schema(description="ES 集群标识")
    private String esClusterCode;

	/**
	* processors 完整 JSON 配置
	*/
    @Schema(description="processors 完整 JSON 配置")
    private String processorsJson;

	/**
	* 1启用,0停用
	*/
    @Schema(description="1启用,0停用")
    private Integer status;

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