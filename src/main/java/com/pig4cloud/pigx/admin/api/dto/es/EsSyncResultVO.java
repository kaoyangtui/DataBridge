package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "EsSyncResultVO", description = "同步结果")
public class EsSyncResultVO {

    @Schema(description = "数据集ID")
    private Long datasetId;

    @Schema(description = "索引名")
    private String index;

    @Schema(description = "总读取行数")
    private long readTotal;

    @Schema(description = "写入成功数")
    private long success;

    @Schema(description = "写入失败数")
    private long fail;

    @Schema(description = "错误摘要（最多前 N 条）")
    private List<String> errors;
}
