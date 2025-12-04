package com.pig4cloud.pigx.admin.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.admin.entity.SyncCursor;
import com.pig4cloud.pigx.admin.service.SyncCursorService;
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
 * 同步游标
 *
 * @author pigx
 * @date 2025-12-03 22:26:55
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/syncCursor" )
@Tag(description = "syncCursor" , name = "同步游标管理" )
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SyncCursorController {

    private final  SyncCursorService syncCursorService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param syncCursor 同步游标
     * @return
     */
    @Operation(summary = "分页查询" , description = "分页查询" )
    @GetMapping("/page" )
    @PreAuthorize("@pms.hasPermission('admin_syncCursor_view')" )
    public R getSyncCursorPage(@ParameterObject Page page, @ParameterObject SyncCursor syncCursor) {
        LambdaQueryWrapper<SyncCursor> wrapper = Wrappers.lambdaQuery();
        return R.ok(syncCursorService.page(page, wrapper));
    }


    /**
     * 通过id查询同步游标
     * @param id id
     * @return R
     */
    @Operation(summary = "通过id查询" , description = "通过id查询" )
    @GetMapping("/{id}" )
    @PreAuthorize("@pms.hasPermission('admin_syncCursor_view')" )
    public R getById(@PathVariable("id" ) Long id) {
        return R.ok(syncCursorService.getById(id));
    }

    /**
     * 新增同步游标
     * @param syncCursor 同步游标
     * @return R
     */
    @Operation(summary = "新增同步游标" , description = "新增同步游标" )
    @SysLog("新增同步游标" )
    @PostMapping
    @PreAuthorize("@pms.hasPermission('admin_syncCursor_add')" )
    public R save(@RequestBody SyncCursor syncCursor) {
        return R.ok(syncCursorService.save(syncCursor));
    }

    /**
     * 修改同步游标
     * @param syncCursor 同步游标
     * @return R
     */
    @Operation(summary = "修改同步游标" , description = "修改同步游标" )
    @SysLog("修改同步游标" )
    @PutMapping
    @PreAuthorize("@pms.hasPermission('admin_syncCursor_edit')" )
    public R updateById(@RequestBody SyncCursor syncCursor) {
        return R.ok(syncCursorService.updateById(syncCursor));
    }

    /**
     * 通过id删除同步游标
     * @param ids id列表
     * @return R
     */
    @Operation(summary = "通过id删除同步游标" , description = "通过id删除同步游标" )
    @SysLog("通过id删除同步游标" )
    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('admin_syncCursor_del')" )
    public R removeById(@RequestBody Long[] ids) {
        return R.ok(syncCursorService.removeBatchByIds(CollUtil.toList(ids)));
    }


    /**
     * 导出excel 表格
     * @param syncCursor 查询条件
   	 * @param ids 导出指定ID
     * @return excel 文件流
     */
    @ResponseExcel
    @GetMapping("/export")
    @PreAuthorize("@pms.hasPermission('admin_syncCursor_export')" )
    public List<SyncCursor> export(SyncCursor syncCursor, Long[] ids) {
        return syncCursorService.list(Wrappers.lambdaQuery(syncCursor).in(ArrayUtil.isNotEmpty(ids), SyncCursor::getId, ids));
    }
}