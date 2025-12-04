package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据集详情 VO
 */
@Data
@Schema(name = "DatasetDetailVO", description = "数据集详情")
public class DatasetDetailVO {

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

    @Schema(description = "主键/唯一标识字段（SQL 别名）")
    private String primaryField;

    @Schema(description = "增量依据字段（如 update_time）")
    private String incrementField;

    @Schema(description = "数据集 SQL 定义")
    private String sqlText;

    @Schema(description = "状态：0-停用 1-启用")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "最近一次存量同步时间")
    private LocalDateTime lastFullSyncTime;

    @Schema(description = "最近一次增量同步时间")
    private LocalDateTime lastIncrementSyncTime;

    @Schema(description = "字段配置列表")
    private List<DatasetFieldVO> fieldList;
}
