package com.pig4cloud.pigx.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryTemplateListRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryTemplateSaveRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryTemplateVO;
import com.pig4cloud.pigx.admin.entity.EsQueryTemplate;

import java.util.List;

public interface EsQueryTemplateService extends IService<EsQueryTemplate> {
    /**
     * 模板列表
     */
    List<EsQueryTemplateVO> listByDataset(EsQueryTemplateListRequest request);

    /**
     * 保存 / 更新模板
     */
    boolean saveOrUpdateTemplate(EsQueryTemplateSaveRequest request);

    /**
     * 删除模板（软删）
     */
    boolean deleteTemplate(Long id);

    /**
     * 设置为默认模板
     */
    boolean setDefault(Long id);
}