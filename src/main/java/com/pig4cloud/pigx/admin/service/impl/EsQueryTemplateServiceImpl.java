package com.pig4cloud.pigx.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryTemplateListRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryTemplateSaveRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryTemplateVO;
import com.pig4cloud.pigx.admin.entity.EsQueryTemplate;
import com.pig4cloud.pigx.admin.mapper.EsQueryTemplateMapper;
import com.pig4cloud.pigx.admin.service.EsQueryTemplateService;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ES 通用查询 - 查询模板 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsQueryTemplateServiceImpl
        extends ServiceImpl<EsQueryTemplateMapper, EsQueryTemplate>
        implements EsQueryTemplateService {

    @Override
    public List<EsQueryTemplateVO> listByDataset(EsQueryTemplateListRequest request) {
        if (request.getDatasetId() == null) {
            return CollUtil.newArrayList();
        }

        Long currentUserId = getCurrentUserId();

        var qw = Wrappers.<EsQueryTemplate>lambdaQuery()
                .eq(EsQueryTemplate::getDatasetId, request.getDatasetId())
                .eq(EsQueryTemplate::getDelFlag, "0");

        // 模板类型过滤
        if (request.getTemplateType() != null) {
            qw.eq(EsQueryTemplate::getTemplateType, request.getTemplateType());
        }

        // 个人模板 + onlyMine = true 时，只看自己的
        if (request.getTemplateType() != null
                && request.getTemplateType() == 1
                && Boolean.TRUE.equals(request.getOnlyMine())
                && currentUserId != null) {
            qw.eq(EsQueryTemplate::getOwnerUserId, currentUserId);
        }

        // 关键字（按模板名称模糊）
        if (StrUtil.isNotBlank(request.getKeyword())) {
            qw.like(EsQueryTemplate::getTemplateName, request.getKeyword());
        }

        qw.orderByAsc(EsQueryTemplate::getTemplateType)
                .orderByDesc(EsQueryTemplate::getIsDefault)
                .orderByAsc(EsQueryTemplate::getSortOrder)
                .orderByDesc(EsQueryTemplate::getCreateTime);

        return this.list(qw).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateTemplate(EsQueryTemplateSaveRequest request) {
        if (request.getDatasetId() == null) {
            throw new IllegalArgumentException("datasetId 不能为空");
        }
        if (StrUtil.isBlank(request.getTemplateName())) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        if (request.getTemplateType() == null) {
            throw new IllegalArgumentException("模板类型不能为空");
        }

        Long currentUserId = getCurrentUserId();

        EsQueryTemplate entity;
        boolean isNew = (request.getId() == null);

        if (isNew) {
            entity = new EsQueryTemplate();
            entity.setDelFlag("0");
        } else {
            entity = this.getById(request.getId());
            if (entity == null || "1".equals(entity.getDelFlag())) {
                throw new IllegalArgumentException("模板不存在或已删除");
            }
            // 个人模板只能本人修改
            if (entity.getTemplateType() != null
                    && entity.getTemplateType() == 1
                    && entity.getOwnerUserId() != null
                    && !Objects.equals(entity.getOwnerUserId(), currentUserId)) {
                throw new IllegalStateException("无权修改该模板");
            }
        }

        entity.setDatasetId(request.getDatasetId());
        entity.setTemplateName(request.getTemplateName());
        entity.setTemplateType(request.getTemplateType());
        entity.setSortOrder(request.getSortOrder() == null ? 100 : request.getSortOrder());
        entity.setFiltersJson(request.getFiltersJson());
        entity.setColumnsJson(request.getColumnsJson());
        entity.setSortsJson(request.getSortsJson());

        // 个人模板：ownerUserId = 当前用户
        if (entity.getTemplateType() != null && entity.getTemplateType() == 1) {
            entity.setOwnerUserId(currentUserId);
        } else {
            // 公共模板：ownerUserId 置空
            entity.setOwnerUserId(null);
        }

        // 默认模板标记
        Integer isDefaultFlag = Boolean.TRUE.equals(request.getIsDefault()) ? 1 : 0;
        entity.setIsDefault(isDefaultFlag);

        // 如果是默认模板，先把同一数据集 + 类型 + 拥有者的其他模板的默认取消
        if (isDefaultFlag == 1) {
            this.lambdaUpdate()
                    .eq(EsQueryTemplate::getDatasetId, entity.getDatasetId())
                    .eq(EsQueryTemplate::getTemplateType, entity.getTemplateType())
                    .eq(entity.getTemplateType() != null && entity.getTemplateType() == 1,
                            EsQueryTemplate::getOwnerUserId, entity.getOwnerUserId())
                    .eq(EsQueryTemplate::getDelFlag, "0")
                    .ne(!isNew, EsQueryTemplate::getId, entity.getId())
                    .set(EsQueryTemplate::getIsDefault, 0)
                    .update();
        }

        return this.saveOrUpdate(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Long id) {
        EsQueryTemplate entity = this.getById(id);
        if (entity == null || "1".equals(entity.getDelFlag())) {
            return false;
        }
        Long currentUserId = getCurrentUserId();
        // 个人模板校验拥有者
        if (entity.getTemplateType() != null
                && entity.getTemplateType() == 1
                && entity.getOwnerUserId() != null
                && !Objects.equals(entity.getOwnerUserId(), currentUserId)) {
            throw new IllegalStateException("无权删除该模板");
        }

        entity.setDelFlag("1");
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefault(Long id) {
        EsQueryTemplate entity = this.getById(id);
        if (entity == null || "1".equals(entity.getDelFlag())) {
            throw new IllegalArgumentException("模板不存在或已删除");
        }
        Long currentUserId = getCurrentUserId();
        if (entity.getTemplateType() != null
                && entity.getTemplateType() == 1
                && entity.getOwnerUserId() != null
                && !Objects.equals(entity.getOwnerUserId(), currentUserId)) {
            throw new IllegalStateException("无权操作该模板");
        }

        // 取消其它默认
        this.lambdaUpdate()
                .eq(EsQueryTemplate::getDatasetId, entity.getDatasetId())
                .eq(EsQueryTemplate::getTemplateType, entity.getTemplateType())
                .eq(entity.getTemplateType() != null && entity.getTemplateType() == 1,
                        EsQueryTemplate::getOwnerUserId, entity.getOwnerUserId())
                .eq(EsQueryTemplate::getDelFlag, "0")
                .ne(EsQueryTemplate::getId, entity.getId())
                .set(EsQueryTemplate::getIsDefault, 0)
                .update();

        entity.setIsDefault(1);
        return this.updateById(entity);
    }

    private EsQueryTemplateVO toVO(EsQueryTemplate e) {
        EsQueryTemplateVO vo = new EsQueryTemplateVO();
        vo.setId(e.getId());
        vo.setDatasetId(e.getDatasetId());
        vo.setTemplateName(e.getTemplateName());
        vo.setTemplateType(e.getTemplateType());
        vo.setOwnerUserId(e.getOwnerUserId());
        vo.setIsDefault(e.getIsDefault() != null && e.getIsDefault() == 1);
        vo.setSortOrder(e.getSortOrder());
        vo.setFiltersJson(e.getFiltersJson());
        vo.setColumnsJson(e.getColumnsJson());
        vo.setSortsJson(e.getSortsJson());
        return vo;
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUser().getId();
        } catch (Exception e) {
            log.warn("获取当前登录用户失败，可忽略用于本地调试", e);
            return null;
        }
    }
}
