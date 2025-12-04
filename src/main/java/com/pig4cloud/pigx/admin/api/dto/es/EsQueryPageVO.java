package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 通用查询 - 分页结果
 */
@Data
@Schema(name = "EsQueryPageVO", description = "通用查询 - 分页结果")
public class EsQueryPageVO {

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "每页条数")
    private Long size;

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "记录列表")
    private List<EsQueryHitVO> records;
}
