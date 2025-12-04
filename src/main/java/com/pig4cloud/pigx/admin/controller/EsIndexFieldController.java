package com.pig4cloud.pigx.admin.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.admin.entity.EsIndexField;
import com.pig4cloud.pigx.admin.service.EsIndexFieldService;
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
 * ES 索引字段定义
 *
 * @author pigx
 * @date 2025-12-03 22:24:39
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/esIndexField" )
@Tag(description = "esIndexField" , name = "ES 索引字段定义管理" )
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EsIndexFieldController {

    private final  EsIndexFieldService esIndexFieldService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param esIndexField ES 索引字段定义
     * @return
     */
    @Operation(summary = "分页查询" , description = "分页查询" )
    @GetMapping("/page" )
    @PreAuthorize("@pms.hasPermission('admin_esIndexField_view')" )
    public R getEsIndexFieldPage(@ParameterObject Page page, @ParameterObject EsIndexField esIndexField) {
        LambdaQueryWrapper<EsIndexField> wrapper = Wrappers.lambdaQuery();
        return R.ok(esIndexFieldService.page(page, wrapper));
    }


    /**
     * 通过id查询ES 索引字段定义
     * @param id id
     * @return R
     */
    @Operation(summary = "通过id查询" , description = "通过id查询" )
    @GetMapping("/{id}" )
    @PreAuthorize("@pms.hasPermission('admin_esIndexField_view')" )
    public R getById(@PathVariable("id" ) Long id) {
        return R.ok(esIndexFieldService.getById(id));
    }

    /**
     * 新增ES 索引字段定义
     * @param esIndexField ES 索引字段定义
     * @return R
     */
    @Operation(summary = "新增ES 索引字段定义" , description = "新增ES 索引字段定义" )
    @SysLog("新增ES 索引字段定义" )
    @PostMapping
    @PreAuthorize("@pms.hasPermission('admin_esIndexField_add')" )
    public R save(@RequestBody EsIndexField esIndexField) {
        return R.ok(esIndexFieldService.save(esIndexField));
    }

    /**
     * 修改ES 索引字段定义
     * @param esIndexField ES 索引字段定义
     * @return R
     */
    @Operation(summary = "修改ES 索引字段定义" , description = "修改ES 索引字段定义" )
    @SysLog("修改ES 索引字段定义" )
    @PutMapping
    @PreAuthorize("@pms.hasPermission('admin_esIndexField_edit')" )
    public R updateById(@RequestBody EsIndexField esIndexField) {
        return R.ok(esIndexFieldService.updateById(esIndexField));
    }

    /**
     * 通过id删除ES 索引字段定义
     * @param ids id列表
     * @return R
     */
    @Operation(summary = "通过id删除ES 索引字段定义" , description = "通过id删除ES 索引字段定义" )
    @SysLog("通过id删除ES 索引字段定义" )
    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('admin_esIndexField_del')" )
    public R removeById(@RequestBody Long[] ids) {
        return R.ok(esIndexFieldService.removeBatchByIds(CollUtil.toList(ids)));
    }


    /**
     * 导出excel 表格
     * @param esIndexField 查询条件
   	 * @param ids 导出指定ID
     * @return excel 文件流
     */
    @ResponseExcel
    @GetMapping("/export")
    @PreAuthorize("@pms.hasPermission('admin_esIndexField_export')" )
    public List<EsIndexField> export(EsIndexField esIndexField, Long[] ids) {
        return esIndexFieldService.list(Wrappers.lambdaQuery(esIndexField).in(ArrayUtil.isNotEmpty(ids), EsIndexField::getId, ids));
    }
}