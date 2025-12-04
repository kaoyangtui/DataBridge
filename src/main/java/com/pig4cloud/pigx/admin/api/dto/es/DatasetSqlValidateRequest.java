package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据集 SQL 校验入参
 */
@Data
@Schema(name = "DatasetSqlValidateRequest", description = "数据集 SQL 校验入参")
public class DatasetSqlValidateRequest {

    @Schema(description = "数据集ID，编辑时可传，用于做一些差异校验", example = "1")
    private Long datasetId;

    @Schema(description = "SQL 文本（必填）")
    private String sqlText;

    @Schema(description = "增量依据字段（可选，用于顺便校验字段是否存在）", example = "update_time")
    private String incrementField;

    @Schema(description = "主键/唯一标识字段（可选，用于校验字段是否存在）", example = "app_number")
    private String primaryField;

    // 如果你后面要支持多数据源，可以加一个：
    // private String datasourceCode;
}
