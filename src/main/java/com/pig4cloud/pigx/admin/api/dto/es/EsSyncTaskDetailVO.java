package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ES 同步任务详情 VO
 */
@Data
@Schema(name = "EsSyncTaskDetailVO", description = "ES 同步任务详情 VO")
public class EsSyncTaskDetailVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "任务编号")
    private String taskNo;

    @Schema(description = "数据集ID")
    private Long datasetId;

    @Schema(description = "数据集名称（可选）")
    private String datasetName;

    @Schema(description = "任务类型：full/increment/delete/repair")
    private String taskType;

    @Schema(description = "触发方式：manual/schedule/event")
    private String triggerType;

    @Schema(description = "增量范围类型：time/id/event 等")
    private String rangeType;

    @Schema(description = "增量起点")
    private String rangeStart;

    @Schema(description = "增量终点")
    private String rangeEnd;

    @Schema(description = "批处理大小")
    private Integer batchSize;

    @Schema(description = "总处理记录数")
    private Long totalRows;

    @Schema(description = "成功记录数")
    private Long successRows;

    @Schema(description = "失败记录数")
    private Long failedRows;

    @Schema(description = "任务状态：init/running/success/failed/stopped")
    private String status;

    @Schema(description = "错误摘要")
    private String errorSummary;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "结束时间")
    private LocalDateTime finishedAt;
}
