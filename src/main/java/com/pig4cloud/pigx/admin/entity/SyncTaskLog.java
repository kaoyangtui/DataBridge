package com.pig4cloud.pigx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 同步任务执行日志
 *
 * @author pigx
 * @date 2025-12-03 22:27:37
 */
@Data
@TableName("t_sync_task_log")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "同步任务执行日志")
public class SyncTaskLog extends Model<SyncTaskLog> {


	/**
	* 主键
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="主键")
    private Long id;

	/**
	* 关联 t_sync_task.id
	*/
    @Schema(description="关联 t_sync_task.id")
    private Long taskId;

	/**
	* 关联 t_sync_dataset.id
	*/
    @Schema(description="关联 t_sync_dataset.id")
    private Long datasetId;

	/**
	* full/inc
	*/
    @Schema(description="full/inc")
    private String jobType;

	/**
	* 开始时间
	*/
    @Schema(description="开始时间")
    private LocalDateTime startTime;

	/**
	* 结束时间
	*/
    @Schema(description="结束时间")
    private LocalDateTime endTime;

	/**
	* 区间起始值
	*/
    @Schema(description="区间起始值")
    private String incStartVal;

	/**
	* 区间结束值
	*/
    @Schema(description="区间结束值")
    private String incEndVal;

	/**
	* 本次处理总数
	*/
    @Schema(description="本次处理总数")
    private Integer totalCount;

	/**
	* 成功数
	*/
    @Schema(description="成功数")
    private Integer successCount;

	/**
	* 失败数
	*/
    @Schema(description="失败数")
    private Integer failCount;

	/**
	* 错误信息摘要
	*/
    @Schema(description="错误信息摘要")
    private String errorMsg;

	/**
	* 创建时间
	*/
	@TableField(fill = FieldFill.INSERT)
    @Schema(description="创建时间")
    private LocalDateTime createTime;
}