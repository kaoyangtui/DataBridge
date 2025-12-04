package com.pig4cloud.pigx.admin.api.dto.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 新建 / 编辑数据集入参
 */
@Data
@Schema(name = "DatasetSaveRequest", description = "新建/编辑数据集的入参")
public class DatasetSaveRequest {

    @Schema(description = "主键ID，新增时为空，编辑时必填")
    private Long id;

    @Schema(description = "数据集编码（唯一，不建议频繁修改）", example = "patent_main")
    private String datasetCode;

    @Schema(description = "数据集名称", example = "专利主视图")
    private String datasetName;

    @Schema(description = "所属业务模块/标签", example = "patent")
    private String bizModule;

    @Schema(description = "标签（逗号分隔）", example = "专利,秦淮区")
    private String tags;

    @Schema(description = "目标 ES 索引名称", example = "patent_main_idx")
    private String esIndex;

    @Schema(description = "主键/唯一标识字段（SQL 别名）", example = "app_number")
    private String primaryField;

    @Schema(description = "增量依据字段（如 update_time 或自增ID）", example = "update_time")
    private String incrementField;

    @Schema(description = "数据集 SQL 定义（支持多表 join）")
    private String sqlText;

    @Schema(description = "状态：0-停用 1-启用", example = "1")
    private Integer status;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "字段配置列表（可选，若为空则只保存数据集基础信息）")
    private List<DatasetFieldSaveRequest> fieldList;
}
