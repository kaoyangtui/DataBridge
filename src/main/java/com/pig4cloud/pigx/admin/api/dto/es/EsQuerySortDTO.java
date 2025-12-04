package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用查询 - 排序字段
 */
@Data
@Schema(name = "EsQuerySortDTO", description = "通用查询 - 排序字段")
public class EsQuerySortDTO {

    @Schema(description = "字段编码（对应 EsDatasetField.fieldCode）", example = "pub_date")
    private String fieldCode;

    @Schema(description = "排序方向：ASC/DESC", example = "DESC")
    private String direction;
}
