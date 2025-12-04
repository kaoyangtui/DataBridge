package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据集分页查询入参
 */
@Data
@Schema(name = "DatasetPageRequest", description = "数据集分页查询入参")
public class DatasetPageRequest {

    @Schema(description = "当前页码，从1开始", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "20")
    private Long size = 20L;

    @Schema(description = "关键词（匹配名称或编码）", example = "patent")
    private String keyword;

    @Schema(description = "所属业务模块/标签", example = "patent")
    private String bizModule;

    @Schema(description = "ES 索引名称", example = "patent_main_idx")
    private String esIndex;

    @Schema(description = "状态：0-停用 1-启用", example = "1")
    private Integer status;
}
