package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据集分页列表项
 */
@Data
@Schema(name = "DatasetPageItemVO", description = "数据集分页列表项")
public class DatasetPageItemVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "数据集编码")
    private String datasetCode;

    @Schema(description = "数据集名称")
    private String datasetName;

    @Schema(description = "所属业务模块/标签")
    private String bizModule;

    @Schema(description = "标签（逗号分隔）")
    private String tags;

    @Schema(description = "ES 索引名称")
    private String esIndex;

    @Schema(description = "状态：0-停用 1-启用")
    private Integer status;

    @Schema(description = "状态名称（枚举转换）")
    private String statusName;

    @Schema(description = "最近一次存量同步时间")
    private LocalDateTime lastFullSyncTime;

    @Schema(description = "最近一次增量同步时间")
    private LocalDateTime lastIncrementSyncTime;

    @Schema(description = "最近一次增量同步状态（可选，从任务表统计）")
    private String lastIncrementStatus;

    @Schema(description = "备注")
    private String remark;
}
