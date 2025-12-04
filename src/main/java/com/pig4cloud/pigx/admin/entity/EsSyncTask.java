package com.pig4cloud.pigx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import java.time.LocalDateTime;

/**
 * ES 通用查询 - 数据同步任务表
 *
 * @author pigx
 * @date 2025-12-03 19:53:52
 */
@Data
@TenantTable
@TableName("t_es_sync_task")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ES 通用查询 - 数据同步任务表")
public class EsSyncTask extends Model<EsSyncTask> {


	/**
	* 主键ID
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="主键ID")
    private Long id;

	/**
	* 任务编号（对外展示）
	*/
    @Schema(description="任务编号（对外展示）")
    private String taskNo;

	/**
	* 数据集ID
	*/
    @Schema(description="数据集ID")
    private Long datasetId;

	/**
	* 任务类型：full-存量 increment-增量 delete-删除 repair-补偿
	*/
    @Schema(description="任务类型：full-存量 increment-增量 delete-删除 repair-补偿")
    private String taskType;

	/**
	* 触发方式：manual-手动 schedule-定时 event-事件
	*/
    @Schema(description="触发方式：manual-手动 schedule-定时 event-事件")
    private String triggerType;

	/**
	* 批处理大小（可选配置）
	*/
    @Schema(description="批处理大小（可选配置）")
    private Integer batchSize;

	/**
	* 增量范围类型：time/id/event 等
	*/
    @Schema(description="增量范围类型：time/id/event 等")
    private String rangeType;

	/**
	* 增量起点（时间/ID/事件标识）
	*/
    @Schema(description="增量起点（时间/ID/事件标识）")
    private String rangeStart;

	/**
	* 增量终点（时间/ID/事件标识）
	*/
    @Schema(description="增量终点（时间/ID/事件标识）")
    private String rangeEnd;

	/**
	* 总处理记录数
	*/
    @Schema(description="总处理记录数")
    private Long totalRows;

	/**
	* 成功记录数
	*/
    @Schema(description="成功记录数")
    private Long successRows;

	/**
	* 失败记录数
	*/
    @Schema(description="失败记录数")
    private Long failedRows;

	/**
	* 任务状态：init/running/success/failed/stopped
	*/
    @Schema(description="任务状态：init/running/success/failed/stopped")
    private String status;

	/**
	* 错误摘要（典型错误示例、错误码统计等）
	*/
    @Schema(description="错误摘要（典型错误示例、错误码统计等）")
    private String errorSummary;

	/**
	* 开始时间
	*/
    @Schema(description="开始时间")
    private LocalDateTime startedAt;

	/**
	* 结束时间
	*/
    @Schema(description="结束时间")
    private LocalDateTime finishedAt;

	/**
	* 扩展 JSON（批次明细、错误样本等）
	*/
    @Schema(description="扩展 JSON（批次明细、错误样本等）")
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
	* 租户ID
	*/
    @Schema(description="租户ID")
    private Long tenantId;
}