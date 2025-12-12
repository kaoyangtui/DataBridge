package com.pig4cloud.pigx.admin.controller;

import com.pig4cloud.pigx.admin.api.dto.es.EsQueryTemplateListRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryTemplateSaveRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryTemplateVO;
import com.pig4cloud.pigx.admin.service.EsQueryTemplateService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/es/template") // 如果你原来是 /es/template，这里改回去即可
@RequiredArgsConstructor
@Tag(name = "ES 通用查询 - 查询模板")
public class EsQueryTemplateController {

    private final EsQueryTemplateService templateService;

    @PostMapping("/list")
    @Operation(summary = "模板列表")
    public R<List<EsQueryTemplateVO>> list(@RequestBody EsQueryTemplateListRequest request) {
        List<EsQueryTemplateVO> list = templateService.listByDataset(request);
        return R.ok(list);
    }

    @PostMapping("/save")
    @Operation(summary = "保存/更新模板")
    public R<Boolean> save(@RequestBody EsQueryTemplateSaveRequest request) {
        return R.ok(templateService.saveOrUpdateTemplate(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.ok(templateService.deleteTemplate(id));
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "设置为默认模板")
    public R<Boolean> setDefault(@PathVariable("id") Long id) {
        return R.ok(templateService.setDefault(id));
    }
}
