package com.pig4cloud.pigx.admin.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.admin.entity.EsPipelineDef;
import com.pig4cloud.pigx.admin.service.EsPipelineDefService;
import org.springframework.security.access.prepost.PreAuthorize;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ES Pipeline 定义
 *
 * @author pigx
 * @date 2025-12-03 22:25:08
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/esPipelineDef" )
@Tag(description = "esPipelineDef" , name = "ES Pipeline 定义管理" )
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EsPipelineDefController {

    private final  EsPipelineDefService esPipelineDefService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param esPipelineDef ES Pipeline 定义
     * @return
     */
    @Operation(summary = "分页查询" , description = "分页查询" )
    @GetMapping("/page" )
    @PreAuthorize("@pms.hasPermission('admin_esPipelineDef_view')" )
    public R getEsPipelineDefPage(@ParameterObject Page page, @ParameterObject EsPipelineDef esPipelineDef) {
        LambdaQueryWrapper<EsPipelineDef> wrapper = Wrappers.lambdaQuery();
        return R.ok(esPipelineDefService.page(page, wrapper));
    }


    /**
     * 通过id查询ES Pipeline 定义
     * @param id id
     * @return R
     */
    @Operation(summary = "通过id查询" , description = "通过id查询" )
    @GetMapping("/{id}" )
    @PreAuthorize("@pms.hasPermission('admin_esPipelineDef_view')" )
    public R getById(@PathVariable("id" ) Long id) {
        return R.ok(esPipelineDefService.getById(id));
    }

    /**
     * 新增ES Pipeline 定义
     * @param esPipelineDef ES Pipeline 定义
     * @return R
     */
    @Operation(summary = "新增ES Pipeline 定义" , description = "新增ES Pipeline 定义" )
    @SysLog("新增ES Pipeline 定义" )
    @PostMapping
    @PreAuthorize("@pms.hasPermission('admin_esPipelineDef_add')" )
    public R save(@RequestBody EsPipelineDef esPipelineDef) {
        return R.ok(esPipelineDefService.save(esPipelineDef));
    }

    /**
     * 修改ES Pipeline 定义
     * @param esPipelineDef ES Pipeline 定义
     * @return R
     */
    @Operation(summary = "修改ES Pipeline 定义" , description = "修改ES Pipeline 定义" )
    @SysLog("修改ES Pipeline 定义" )
    @PutMapping
    @PreAuthorize("@pms.hasPermission('admin_esPipelineDef_edit')" )
    public R updateById(@RequestBody EsPipelineDef esPipelineDef) {
        return R.ok(esPipelineDefService.updateById(esPipelineDef));
    }

    /**
     * 通过id删除ES Pipeline 定义
     * @param ids id列表
     * @return R
     */
    @Operation(summary = "通过id删除ES Pipeline 定义" , description = "通过id删除ES Pipeline 定义" )
    @SysLog("通过id删除ES Pipeline 定义" )
    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('admin_esPipelineDef_del')" )
    public R removeById(@RequestBody Long[] ids) {
        return R.ok(esPipelineDefService.removeBatchByIds(CollUtil.toList(ids)));
    }


    /**
     * 导出excel 表格
     * @param esPipelineDef 查询条件
   	 * @param ids 导出指定ID
     * @return excel 文件流
     */
    @ResponseExcel
    @GetMapping("/export")
    @PreAuthorize("@pms.hasPermission('admin_esPipelineDef_export')" )
    public List<EsPipelineDef> export(EsPipelineDef esPipelineDef, Long[] ids) {
        return esPipelineDefService.list(Wrappers.lambdaQuery(esPipelineDef).in(ArrayUtil.isNotEmpty(ids), EsPipelineDef::getId, ids));
    }
}