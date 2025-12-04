package com.pig4cloud.pigx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * ES 索引定义
 *
 * @author pigx
 * @date 2025-12-03 22:23:56
 */
@Data
@TableName("t_es_index_def")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ES 索引定义")
public class EsIndexDef extends Model<EsIndexDef> {


	/**
	* 主键
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="主键")
    private Long id;

	/**
	* 逻辑索引编码, 如 patent_info_prod
	*/
    @Schema(description="逻辑索引编码, 如 patent_info_prod")
    private String indexCode;

	/**
	* 中文名称
	*/
    @Schema(description="中文名称")
    private String indexName;

	/**
	* ES 集群标识, 如 es_prod
	*/
    @Schema(description="ES 集群标识, 如 es_prod")
    private String esClusterCode;

	/**
	* 物理索引名, 如 patent_info_prod_v1
	*/
    @Schema(description="物理索引名, 如 patent_info_prod_v1")
    private String physicalIndex;

	/**
	* 写 alias
	*/
    @Schema(description="写 alias")
    private String writeAlias;

	/**
	* 读 alias
	*/
    @Schema(description="读 alias")
    private String readAlias;

	/**
	* 分片数
	*/
    @Schema(description="分片数")
    private Integer numberOfShards;

	/**
	* 副本数
	*/
    @Schema(description="副本数")
    private Integer numberOfReplicas;

	/**
	* 刷新间隔
	*/
    @Schema(description="刷新间隔")
    private String refreshInterval;

	/**
	* index.default_pipeline
	*/
    @Schema(description="index.default_pipeline")
    private String defaultPipeline;

	/**
	* 索引 schema 版本号
	*/
    @Schema(description="索引 schema 版本号")
    private Integer version;

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