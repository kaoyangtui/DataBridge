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

    // ========= 基本信息 =========

    @Schema(description = "数据集编码（唯一，不建议频繁修改）", example = "patent_main")
    private String datasetCode;

    @Schema(description = "数据集名称", example = "专利主视图")
    private String datasetName;

    @Schema(description = "所属业务模块/标签", example = "patent")
    private String bizModule;

    @Schema(description = "标签（逗号分隔）", example = "专利,秦淮区")
    private String tags;

    // ========= ES 相关 =========

    @Schema(description = "目标 ES 索引名称（物理索引名或写 alias）", example = "patent_info")
    private String esIndex;

    @Schema(description = "ES pipeline ID，如 split_semicolon_fields；为空则不使用 pipeline", example = "split_semicolon_fields")
    private String esPipelineId;

    @Schema(description = "主键/唯一标识字段（SQL 别名），用于内部逻辑唯一标识", example = "_id")
    private String primaryField;

    @Schema(description = "ES 文档 _id 对应的字段别名，通常与 primaryField 一致", example = "_id")
    private String esIdField;

    @Schema(description = "是否采用 upsert 写入 ES：1-upsert 0-仅 create", example = "1")
    private Integer esUpsert;

    // ========= canal / adapter 相关 =========

    @Schema(description = "adapter dataSourceKey，对应 canal-adapter 的 dataSourceKey", example = "defaultDS")
    private String dataSourceKey;

    @Schema(description = "canal destination，对应 canal 实例名", example = "qhq")
    private String canalDest;

    @Schema(description = "canal groupId，对应 canal 的订阅组", example = "g1")
    private String canalGroup;

    // ========= 增量 / ETL 相关 =========

    @Schema(description = "增量依据字段（如 update_time 或自增ID，对应 SQL 别名）", example = "id")
    private String incrementField;

    @Schema(description = "增量类型：1=时间 2=自增ID", example = "2")
    private Integer incType;

    @Schema(
            description = "ETL 区间模板，用于调用 /etl 时拼接 WHERE 条件；" +
                    "例如：WHERE t.id >= {} AND t.id < {} 或 WHERE t.update_time >= '{}' AND t.update_time < '{}'",
            example = "WHERE t.id >= {} AND t.id < {}"
    )
    private String etlCondition;

    @Schema(description = "ETL 批次大小，对应 canal-adapter 的 commitBatch", example = "5000")
    private Integer commitBatch;

    // ========= SQL & 状态 =========

    @Schema(description = "数据集 SQL 定义（支持多表 join），建议不要带 WHERE，方便区间条件拼接")
    private String sqlText;

    @Schema(description = "状态：0-停用 1-启用", example = "1")
    private Integer status;

    @Schema(description = "备注说明")
    private String remark;

    // ========= 字段配置列表 =========

    @Schema(description = "字段配置列表（可选，若为空则只保存数据集基础信息）")
    private List<DatasetFieldSaveRequest> fieldList;
}
