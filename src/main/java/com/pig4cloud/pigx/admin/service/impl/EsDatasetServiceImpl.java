package com.pig4cloud.pigx.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.api.dto.es.*;
import com.pig4cloud.pigx.admin.entity.EsDataset;
import com.pig4cloud.pigx.admin.entity.EsDatasetField;
import com.pig4cloud.pigx.admin.enums.es.EsDatasetStatusEnum;
import com.pig4cloud.pigx.admin.enums.es.LogicFieldTypeEnum;
import com.pig4cloud.pigx.admin.mapper.EsDatasetMapper;
import com.pig4cloud.pigx.admin.service.EsDatasetFieldService;
import com.pig4cloud.pigx.admin.service.EsDatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ES 通用查询 - 数据集定义 Service 实现
 * <p>
 * 说明：
 * - Entity/Mapper 由 pigx 代码生成器生成
 * - 这里在其基础上实现业务方法：分页、详情、保存、SQL 校验等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsDatasetServiceImpl extends ServiceImpl<EsDatasetMapper, EsDataset>
        implements EsDatasetService {

    private final EsDatasetFieldService datasetFieldService;
    private final DataSource dataSource; // 用于 SQL 校验 & 字段预览

    // ========== 分页查询 ==========

    @Override
    public IPage<DatasetPageItemVO> pageDatasets(DatasetPageRequest request) {
        Page<EsDataset> page = new Page<>(
                request.getCurrent() == null ? 1L : request.getCurrent(),
                request.getSize() == null ? 20L : request.getSize()
        );

        LambdaQueryWrapper<EsDataset> wrapper = Wrappers.lambdaQuery(EsDataset.class)
                .eq(EsDataset::getDelFlag, "0");

        if (StrUtil.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w.like(EsDataset::getDatasetName, request.getKeyword())
                    .or()
                    .like(EsDataset::getDatasetCode, request.getKeyword()));
        }

        if (StrUtil.isNotBlank(request.getBizModule())) {
            wrapper.eq(EsDataset::getBizModule, request.getBizModule());
        }

        if (StrUtil.isNotBlank(request.getEsIndex())) {
            wrapper.like(EsDataset::getEsIndex, request.getEsIndex());
        }

        if (request.getStatus() != null) {
            wrapper.eq(EsDataset::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(EsDataset::getCreateTime);

        IPage<EsDataset> entityPage = this.page(page, wrapper);

        Page<DatasetPageItemVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());

        List<DatasetPageItemVO> items = entityPage.getRecords()
                .stream()
                .map(this::convertToPageItemVO)
                .collect(Collectors.toList());

        voPage.setRecords(items);
        return voPage;
    }

    private DatasetPageItemVO convertToPageItemVO(EsDataset ds) {
        DatasetPageItemVO vo = new DatasetPageItemVO();
        vo.setId(ds.getId());
        vo.setDatasetCode(ds.getDatasetCode());
        vo.setDatasetName(ds.getDatasetName());
        vo.setBizModule(ds.getBizModule());
        vo.setTags(ds.getTags());
        vo.setEsIndex(ds.getEsIndex());
        vo.setStatus(ds.getStatus());
        vo.setStatusName(Optional.ofNullable(EsDatasetStatusEnum.of(ds.getStatus()))
                .map(EsDatasetStatusEnum::getName)
                .orElse("-"));
        vo.setLastFullSyncTime(ds.getLastFullSyncTime());
        vo.setLastIncrementSyncTime(ds.getLastIncrementSyncTime());
        vo.setRemark(ds.getRemark());
        // lastIncrementStatus 可以后续从任务表聚合，这里暂留空
        vo.setLastIncrementStatus(null);
        return vo;
    }

    // ========== 详情查询 ==========

    @Override
    public DatasetDetailVO getDatasetDetail(Long id) {
        EsDataset ds = this.getById(id);
        if (ds == null || "1".equals(ds.getDelFlag())) {
            return null;
        }

        DatasetDetailVO vo = new DatasetDetailVO();
        BeanUtil.copyProperties(ds, vo);

        vo.setStatusName(Optional.ofNullable(EsDatasetStatusEnum.of(ds.getStatus()))
                .map(EsDatasetStatusEnum::getName)
                .orElse("-"));

        // 查询字段配置
        List<EsDatasetField> fields = datasetFieldService.list(
                Wrappers.lambdaQuery(EsDatasetField.class)
                        .eq(EsDatasetField::getDatasetId, id)
                        .eq(EsDatasetField::getDelFlag, "0")
                        .orderByAsc(EsDatasetField::getListOrder)
        );

        List<DatasetFieldVO> fieldList = fields.stream().map(field -> {
            DatasetFieldVO fv = new DatasetFieldVO();
            BeanUtil.copyProperties(field, fv);
            return fv;
        }).collect(Collectors.toList());

        vo.setFieldList(fieldList);
        return vo;
    }

    // ========== 保存/更新 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateDataset(DatasetSaveRequest request) {
        EsDataset entity = buildDatasetEntity(request);
        boolean isNew = (entity.getId() == null);

        if (isNew) {
            entity.setDelFlag("0");
            if (entity.getCreateTime() == null) {
                entity.setCreateTime(LocalDateTime.now());
            }
        }
        entity.setUpdateTime(LocalDateTime.now());

        this.saveOrUpdate(entity);
        Long datasetId = entity.getId();

        // 字段配置处理
        if (CollUtil.isNotEmpty(request.getFieldList())) {
            // 简单策略：逻辑删除旧的，重新插入新的
            datasetFieldService.remove(
                    Wrappers.lambdaQuery(EsDatasetField.class)
                            .eq(EsDatasetField::getDatasetId, datasetId)
            );

            List<EsDatasetField> toSave = request.getFieldList().stream()
                    .map(reqField -> buildDatasetFieldEntity(reqField, datasetId))
                    .collect(Collectors.toList());

            datasetFieldService.saveBatch(toSave);
        }

        return true;
    }

    private EsDataset buildDatasetEntity(DatasetSaveRequest request) {
        EsDataset entity = new EsDataset();
        // 注意：id 在 Controller 已经 set 好（新增为 null，编辑为 pathVariable）
        entity.setId(request.getId());
        entity.setDatasetCode(request.getDatasetCode());
        entity.setDatasetName(request.getDatasetName());
        entity.setBizModule(request.getBizModule());
        entity.setTags(request.getTags());
        entity.setEsIndex(request.getEsIndex());
        entity.setPrimaryField(request.getPrimaryField());
        entity.setIncrementField(request.getIncrementField());
        entity.setSqlText(request.getSqlText());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        // 其他字段（tenantId、createBy 等）可根据你们的 LoginContext 补充
        return entity;
    }

    private EsDatasetField buildDatasetFieldEntity(DatasetFieldSaveRequest req, Long datasetId) {
        EsDatasetField field = new EsDatasetField();
        // 新增时我们强制置空 id
        field.setId(null);
        field.setDatasetId(datasetId);

        field.setFieldCode(req.getFieldCode());
        field.setFieldName(req.getFieldName());
        field.setFieldDesc(req.getFieldDesc());
        field.setLogicType(req.getLogicType());
        field.setEsType(req.getEsType());
        field.setEsAnalyzer(req.getEsAnalyzer());
        field.setIsAnalyzed(defaultInt(req.getIsAnalyzed(), 0));
        field.setEsAggregatable(defaultInt(req.getEsAggregatable(), 1));
        field.setEsDocValues(defaultInt(req.getEsDocValues(), 1));

        field.setListVisible(defaultInt(req.getListVisible(), 1));
        field.setListOrder(defaultInt(req.getListOrder(), 100));
        field.setListTitle(StrUtil.firstNonBlank(req.getListTitle(), req.getFieldName()));
        field.setSortable(defaultInt(req.getSortable(), 0));
        field.setDefaultSort(defaultInt(req.getDefaultSort(), 0));
        field.setDefaultSortDir(req.getDefaultSortDir());

        field.setFilterable(defaultInt(req.getFilterable(), 0));
        field.setFilterControl(req.getFilterControl());
        field.setFilterOperator(req.getFilterOperator());
        field.setIsCommonFilter(defaultInt(req.getIsCommonFilter(), 1));

        field.setExportable(defaultInt(req.getExportable(), 1));
        field.setExportTitle(StrUtil.firstNonBlank(req.getExportTitle(), req.getFieldName()));
        field.setExportOrder(defaultInt(req.getExportOrder(), 100));

        field.setDelFlag("0");
        field.setCreateTime(LocalDateTime.now());
        field.setUpdateTime(LocalDateTime.now());
        return field;
    }

    private int defaultInt(Integer value, int defaultVal) {
        return value == null ? defaultVal : value;
    }

    // ========== SQL 校验 & 字段预览 ==========

    @Override
    public SqlValidateResultVO validateSql(DatasetSqlValidateRequest request) {
        SqlValidateResultVO result = new SqlValidateResultVO();

        String rawSql = request.getSqlText();
        if (StrUtil.isBlank(rawSql)) {
            result.setSuccess(false);
            result.setErrorMessage("SQL 不能为空");
            return result;
        }

        // 包一层子查询 + LIMIT 防止全表扫爆内存
        String wrappedSql = "SELECT * FROM (" + rawSql + ") t LIMIT 50";
        log.info("Validate dataset SQL, wrappedSql=\n{}", wrappedSql);

        List<FieldPreviewVO> fieldPreviewList = new ArrayList<>();
        int rowCount = 0;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(wrappedSql)) {

            boolean hasResultSet = ps.execute();
            if (!hasResultSet) {
                // 说明 SQL 没有返回结果集（例如 DML），对我们的场景来说是不合法的
                result.setSuccess(false);
                result.setErrorMessage("SQL 未返回结果集，请确认是 SELECT 查询");
                return result;
            }

            try (ResultSet rs = ps.getResultSet()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                // 字段结构
                for (int i = 1; i <= columnCount; i++) {
                    String colLabel = meta.getColumnLabel(i);
                    String colName = meta.getColumnName(i);
                    String fieldName = StrUtil.emptyToDefault(colLabel, colName);

                    int jdbcType = meta.getColumnType(i);
                    String jdbcTypeName = meta.getColumnTypeName(i);
                    String logicType = inferLogicType(jdbcType);

                    FieldPreviewVO fp = new FieldPreviewVO();
                    fp.setFieldName(fieldName);
                    fp.setDbType(jdbcTypeName);
                    fp.setLogicType(logicType);
                    fieldPreviewList.add(fp);
                }

                // 统计样例行数（最多 50 行）
                while (rs.next()) {
                    rowCount++;
                }
            }

            // 校验主键字段 & 增量字段是否存在
            Set<String> fieldNameSet = fieldPreviewList.stream()
                    .map(fp -> fp.getFieldName().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());

            List<String> errors = new ArrayList<>();
            if (StrUtil.isNotBlank(request.getPrimaryField())
                    && !fieldNameSet.contains(request.getPrimaryField().toLowerCase(Locale.ROOT))) {
                errors.add("主键字段不存在: " + request.getPrimaryField());
            }
            if (StrUtil.isNotBlank(request.getIncrementField())
                    && !fieldNameSet.contains(request.getIncrementField().toLowerCase(Locale.ROOT))) {
                errors.add("增量依据字段不存在: " + request.getIncrementField());
            }

            if (!errors.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage(StrUtil.join("；", errors));
            } else {
                result.setSuccess(true);
            }

            result.setSampleRowCount(rowCount);
            result.setFields(fieldPreviewList);
            return result;
        } catch (Exception e) {
            log.error("Validate dataset SQL error", e);
            result.setSuccess(false);
            // 为了安全，不把全部堆栈抛给前端，只给 message
            result.setErrorMessage(e.getMessage());
            result.setFields(Collections.emptyList());
            return result;
        }
    }

    /**
     * 根据 JDBC 类型推断逻辑字段类型
     */
    private String inferLogicType(int jdbcType) {
        switch (jdbcType) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.REAL:
                return LogicFieldTypeEnum.NUMBER.getCode();

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case -101: // Oracle TIMESTAMP WITH TIME ZONE 等特殊类型，可以按需扩展
                return LogicFieldTypeEnum.DATE.getCode();

            case Types.BIT:
            case Types.BOOLEAN:
                return LogicFieldTypeEnum.BOOLEAN.getCode();

            default:
                return LogicFieldTypeEnum.STRING.getCode();
        }
    }
}
