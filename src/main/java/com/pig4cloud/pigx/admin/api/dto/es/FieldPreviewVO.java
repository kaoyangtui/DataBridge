package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SQL 字段预览 VO
 */
@Data
@Schema(name = "FieldPreviewVO", description = "SQL 字段预览 VO")
public class FieldPreviewVO {

    @Schema(description = "字段名（SQL 别名）", example = "app_number")
    private String fieldName;

    @Schema(description = "数据库类型（JDBC 类型）", example = "VARCHAR")
    private String dbType;

    @Schema(description = "推断的逻辑类型：string/number/date/boolean", example = "string")
    private String logicType;
}
