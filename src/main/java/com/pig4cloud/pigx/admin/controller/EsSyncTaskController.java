package com.pig4cloud.pigx.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskCreateRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskDetailVO;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskPageItemVO;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskPageRequest;
import com.pig4cloud.pigx.admin.service.EsSyncTaskService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ES 通用查询 - 同步任务管理
 */
@RestController
@RequestMapping("/es/syncTask")
@RequiredArgsConstructor
@Tag(name = "ES 通用查询 - 同步任务管理")
public class EsSyncTaskController {

    private final EsSyncTaskService esSyncTaskService;

    @PostMapping("/page")
    @Operation(summary = "分页查询同步任务")
    public R<IPage<EsSyncTaskPageItemVO>> page(@RequestBody(required = false) EsSyncTaskPageRequest request) {
        if (request == null) {
            request = new EsSyncTaskPageRequest();
        }
        return R.ok(esSyncTaskService.pageTasks(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询同步任务详情")
    public R<EsSyncTaskDetailVO> detail(@PathVariable("id") Long id) {
        EsSyncTaskDetailVO vo = esSyncTaskService.getTaskDetail(id);
        if (vo == null) {
            return R.failed("任务不存在或已删除");
        }
        return R.ok(vo);
    }

    @PostMapping
    @Operation(summary = "创建同步任务（存量/增量/删除/补偿）")
    public R<Long> create(@RequestBody EsSyncTaskCreateRequest request) {
        Long id = esSyncTaskService.createTask(request);
        return R.ok(id);
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "重试同步任务（仅失败/停止任务可重试）")
    public R<Boolean> retry(@PathVariable("id") Long id) {
        boolean ok = esSyncTaskService.retryTask(id);
        if (!ok) {
            return R.failed("任务不存在、已删除或状态不允许重试");
        }
        return R.ok(true);
    }
}
