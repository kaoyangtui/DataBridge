package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 通用查询 - 查询入参
 */
@Data
@Schema(name = "EsQueryRequest", description = "通用查询 - 查询入参")
public class EsQueryRequest {

    @Schema(description = "数据集ID（datasetId 和 datasetCode 至少一个）", example = "1")
    private Long datasetId;

    @Schema(description = "数据集编码（datasetId 和 datasetCode 至少一个）", example = "patent_main")
    private String datasetCode;

    @Schema(description = "分页 - 当前页，从1开始", example = "1")
    private Long current = 1L;

    @Schema(description = "分页 - 每页条数", example = "20")
    private Long size = 20L;

    @Schema(description = "查询条件列表")
    private List<EsQueryConditionDTO> conditions;

    @Schema(description = "排序字段列表")
    private List<EsQuerySortDTO> sorts;

    @Schema(description = "是否返回总数，默认 true")
    private Boolean needTotal = true;
}
