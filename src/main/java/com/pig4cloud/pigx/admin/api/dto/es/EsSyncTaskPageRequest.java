package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 同步任务分页查询入参
 */
@Data
@Schema(name = "EsSyncTaskPageRequest", description = "ES 同步任务分页查询入参")
public class EsSyncTaskPageRequest {

    @Schema(description = "当前页码，从1开始", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "20")
    private Long size = 20L;

    @Schema(description = "数据集ID", example = "1")
    private Long datasetId;

    @Schema(description = "任务类型：full/increment/delete/repair", example = "full")
    private String taskType;

    @Schema(description = "触发方式：manual/schedule/event", example = "manual")
    private String triggerType;

    @Schema(description = "任务状态：init/running/success/failed/stopped", example = "success")
    private String status;

    @Schema(description = "开始时间（创建时间 >=），格式：yyyy-MM-dd HH:mm:ss")
    private String createTimeBegin;

    @Schema(description = "结束时间（创建时间 <=），格式：yyyy-MM-dd HH:mm:ss")
    private String createTimeEnd;
}
