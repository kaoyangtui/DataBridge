package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 通用查询 - 导出 Excel 请求
 */
@Data
@Schema(name = "EsExportRequest", description = "通用查询 - 导出 Excel 请求")
public class EsExportRequest extends EsQueryRequest {

    @Schema(description = "导出字段编码列表（不传则按字段配置中 exportable=1 的字段）")
    private List<String> exportFieldCodes;

    @Schema(description = "最多导出多少行，默认 5000，防止一次性拉太多", example = "5000")
    private Integer maxRows;

    @Schema(description = "导出文件名（不含扩展名）", example = "专利列表")
    private String fileName;
}
