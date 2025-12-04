package com.pig4cloud.pigx.admin.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.admin.entity.EsIndexDef;
import com.pig4cloud.pigx.admin.service.EsIndexDefService;
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
 * ES 索引定义
 *
 * @author pigx
 * @date 2025-12-03 22:23:56
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/esIndexDef" )
@Tag(description = "esIndexDef" , name = "ES 索引定义管理" )
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EsIndexDefController {

    private final  EsIndexDefService esIndexDefService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param esIndexDef ES 索引定义
     * @return
     */
    @Operation(summary = "分页查询" , description = "分页查询" )
    @GetMapping("/page" )
    @PreAuthorize("@pms.hasPermission('admin_esIndexDef_view')" )
    public R getEsIndexDefPage(@ParameterObject Page page, @ParameterObject EsIndexDef esIndexDef) {
        LambdaQueryWrapper<EsIndexDef> wrapper = Wrappers.lambdaQuery();
        return R.ok(esIndexDefService.page(page, wrapper));
    }


    /**
     * 通过id查询ES 索引定义
     * @param id id
     * @return R
     */
    @Operation(summary = "通过id查询" , description = "通过id查询" )
    @GetMapping("/{id}" )
    @PreAuthorize("@pms.hasPermission('admin_esIndexDef_view')" )
    public R getById(@PathVariable("id" ) Long id) {
        return R.ok(esIndexDefService.getById(id));
    }

    /**
     * 新增ES 索引定义
     * @param esIndexDef ES 索引定义
     * @return R
     */
    @Operation(summary = "新增ES 索引定义" , description = "新增ES 索引定义" )
    @SysLog("新增ES 索引定义" )
    @PostMapping
    @PreAuthorize("@pms.hasPermission('admin_esIndexDef_add')" )
    public R save(@RequestBody EsIndexDef esIndexDef) {
        return R.ok(esIndexDefService.save(esIndexDef));
    }

    /**
     * 修改ES 索引定义
     * @param esIndexDef ES 索引定义
     * @return R
     */
    @Operation(summary = "修改ES 索引定义" , description = "修改ES 索引定义" )
    @SysLog("修改ES 索引定义" )
    @PutMapping
    @PreAuthorize("@pms.hasPermission('admin_esIndexDef_edit')" )
    public R updateById(@RequestBody EsIndexDef esIndexDef) {
        return R.ok(esIndexDefService.updateById(esIndexDef));
    }

    /**
     * 通过id删除ES 索引定义
     * @param ids id列表
     * @return R
     */
    @Operation(summary = "通过id删除ES 索引定义" , description = "通过id删除ES 索引定义" )
    @SysLog("通过id删除ES 索引定义" )
    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('admin_esIndexDef_del')" )
    public R removeById(@RequestBody Long[] ids) {
        return R.ok(esIndexDefService.removeBatchByIds(CollUtil.toList(ids)));
    }


    /**
     * 导出excel 表格
     * @param esIndexDef 查询条件
   	 * @param ids 导出指定ID
     * @return excel 文件流
     */
    @ResponseExcel
    @GetMapping("/export")
    @PreAuthorize("@pms.hasPermission('admin_esIndexDef_export')" )
    public List<EsIndexDef> export(EsIndexDef esIndexDef, Long[] ids) {
        return esIndexDefService.list(Wrappers.lambdaQuery(esIndexDef).in(ArrayUtil.isNotEmpty(ids), EsIndexDef::getId, ids));
    }
}