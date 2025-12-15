package com.pig4cloud.pigx.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskCreateRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskDetailVO;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskPageItemVO;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskPageRequest;
import com.pig4cloud.pigx.admin.entity.EsSyncTask;

public interface EsSyncTaskService extends IService<EsSyncTask> {

    /**
     * 分页查询同步任务
     */
    IPage<EsSyncTaskPageItemVO> pageTasks(EsSyncTaskPageRequest request);

    /**
     * 查询任务详情
     */
    EsSyncTaskDetailVO getTaskDetail(Long id);

    /**
     * 创建任务（仅生成记录，不直接执行）
     *
     * @return 任务ID
     */
    Long createTask(EsSyncTaskCreateRequest request);

    /**
     * 重试任务（将状态置为 init，清空统计）
     */
    boolean retryTask(Long id);

    /** 启动任务（异步执行），仅允许 init 状态 */
    boolean startTaskAsync(Long id);

    /** 创建并立即启动（返回 taskId） */
    Long createAndStart(EsSyncTaskCreateRequest request);
}