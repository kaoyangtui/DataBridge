package com.pig4cloud.pigx.admin.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.admin.entity.SyncTaskLog;
import com.pig4cloud.pigx.admin.service.SyncTaskLogService;
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
 * 同步任务执行日志
 *
 * @author pigx
 * @date 2025-12-03 22:27:37
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/syncTaskLog" )
@Tag(description = "syncTaskLog" , name = "同步任务执行日志管理" )
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SyncTaskLogController {

    private final  SyncTaskLogService syncTaskLogService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param syncTaskLog 同步任务执行日志
     * @return
     */
    @Operation(summary = "分页查询" , description = "分页查询" )
    @GetMapping("/page" )
    @PreAuthorize("@pms.hasPermission('admin_syncTaskLog_view')" )
    public R getSyncTaskLogPage(@ParameterObject Page page, @ParameterObject SyncTaskLog syncTaskLog) {
        LambdaQueryWrapper<SyncTaskLog> wrapper = Wrappers.lambdaQuery();
        return R.ok(syncTaskLogService.page(page, wrapper));
    }


    /**
     * 通过id查询同步任务执行日志
     * @param id id
     * @return R
     */
    @Operation(summary = "通过id查询" , description = "通过id查询" )
    @GetMapping("/{id}" )
    @PreAuthorize("@pms.hasPermission('admin_syncTaskLog_view')" )
    public R getById(@PathVariable("id" ) Long id) {
        return R.ok(syncTaskLogService.getById(id));
    }

    /**
     * 新增同步任务执行日志
     * @param syncTaskLog 同步任务执行日志
     * @return R
     */
    @Operation(summary = "新增同步任务执行日志" , description = "新增同步任务执行日志" )
    @SysLog("新增同步任务执行日志" )
    @PostMapping
    @PreAuthorize("@pms.hasPermission('admin_syncTaskLog_add')" )
    public R save(@RequestBody SyncTaskLog syncTaskLog) {
        return R.ok(syncTaskLogService.save(syncTaskLog));
    }

    /**
     * 修改同步任务执行日志
     * @param syncTaskLog 同步任务执行日志
     * @return R
     */
    @Operation(summary = "修改同步任务执行日志" , description = "修改同步任务执行日志" )
    @SysLog("修改同步任务执行日志" )
    @PutMapping
    @PreAuthorize("@pms.hasPermission('admin_syncTaskLog_edit')" )
    public R updateById(@RequestBody SyncTaskLog syncTaskLog) {
        return R.ok(syncTaskLogService.updateById(syncTaskLog));
    }

    /**
     * 通过id删除同步任务执行日志
     * @param ids id列表
     * @return R
     */
    @Operation(summary = "通过id删除同步任务执行日志" , description = "通过id删除同步任务执行日志" )
    @SysLog("通过id删除同步任务执行日志" )
    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('admin_syncTaskLog_del')" )
    public R removeById(@RequestBody Long[] ids) {
        return R.ok(syncTaskLogService.removeBatchByIds(CollUtil.toList(ids)));
    }


    /**
     * 导出excel 表格
     * @param syncTaskLog 查询条件
   	 * @param ids 导出指定ID
     * @return excel 文件流
     */
    @ResponseExcel
    @GetMapping("/export")
    @PreAuthorize("@pms.hasPermission('admin_syncTaskLog_export')" )
    public List<SyncTaskLog> export(SyncTaskLog syncTaskLog, Long[] ids) {
        return syncTaskLogService.list(Wrappers.lambdaQuery(syncTaskLog).in(ArrayUtil.isNotEmpty(ids), SyncTaskLog::getId, ids));
    }
}