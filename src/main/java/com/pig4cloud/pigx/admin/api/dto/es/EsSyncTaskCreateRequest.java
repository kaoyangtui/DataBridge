package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建 ES 同步任务入参
 */
@Data
@Schema(name = "EsSyncTaskCreateRequest", description = "创建 ES 同步任务入参")
public class EsSyncTaskCreateRequest {

    @Schema(description = "数据集ID", required = true, example = "1")
    private Long datasetId;

    @Schema(description = "任务类型：full/increment/delete/repair", required = true, example = "full")
    private String taskType;

    @Schema(description = "触发方式：manual/schedule/event", required = true, example = "manual")
    private String triggerType;

    @Schema(description = "增量范围类型：time/id/event 等", example = "time")
    private String rangeType;

    @Schema(description = "增量起点（时间/ID/事件标识）", example = "2025-01-01 00:00:00")
    private String rangeStart;

    @Schema(description = "增量终点（时间/ID/事件标识）", example = "2025-01-31 23:59:59")
    private String rangeEnd;

    @Schema(description = "批处理大小", example = "1000")
    private Integer batchSize;
}
