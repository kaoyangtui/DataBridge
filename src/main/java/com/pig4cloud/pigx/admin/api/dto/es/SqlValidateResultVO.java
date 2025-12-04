package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * SQL 校验结果 VO
 */
@Data
@Schema(name = "SqlValidateResultVO", description = "数据集 SQL 校验结果")
public class SqlValidateResultVO {

    @Schema(description = "是否校验成功")
    private Boolean success;

    @Schema(description = "错误信息（失败时）")
    private String errorMessage;

    @Schema(description = "样例数据条数（可选）")
    private Integer sampleRowCount;

    @Schema(description = "字段预览列表（仅在 success=true 时返回）")
    private List<FieldPreviewVO> fields;
}
