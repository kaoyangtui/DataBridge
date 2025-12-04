package com.pig4cloud.pigx.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskCreateRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskDetailVO;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskPageItemVO;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncTaskPageRequest;
import com.pig4cloud.pigx.admin.entity.EsDataset;
import com.pig4cloud.pigx.admin.entity.EsSyncTask;
import com.pig4cloud.pigx.admin.mapper.EsSyncTaskMapper;
import com.pig4cloud.pigx.admin.service.EsDatasetService;
import com.pig4cloud.pigx.admin.service.EsSyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * ES 通用查询 - 同步任务 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsSyncTaskServiceImpl extends ServiceImpl<EsSyncTaskMapper, EsSyncTask>
        implements EsSyncTaskService {

    private final EsDatasetService esDatasetService;

    private static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ========== 分页查询 ==========

    @Override
    public IPage<EsSyncTaskPageItemVO> pageTasks(EsSyncTaskPageRequest request) {
        Page<EsSyncTask> page = new Page<>(
                request.getCurrent() == null ? 1L : request.getCurrent(),
                request.getSize() == null ? 20L : request.getSize()
        );

        LambdaQueryWrapper<EsSyncTask> wrapper = Wrappers.lambdaQuery(EsSyncTask.class);

        if (request.getDatasetId() != null) {
            wrapper.eq(EsSyncTask::getDatasetId, request.getDatasetId());
        }
        if (StrUtil.isNotBlank(request.getTaskType())) {
            wrapper.eq(EsSyncTask::getTaskType, request.getTaskType());
        }
        if (StrUtil.isNotBlank(request.getTriggerType())) {
            wrapper.eq(EsSyncTask::getTriggerType, request.getTriggerType());
        }
        if (StrUtil.isNotBlank(request.getStatus())) {
            wrapper.eq(EsSyncTask::getStatus, request.getStatus());
        }
        if (StrUtil.isNotBlank(request.getCreateTimeBegin())) {
            wrapper.ge(EsSyncTask::getCreateTime,
                    LocalDateTime.parse(request.getCreateTimeBegin(), DT_FORMATTER));
        }
        if (StrUtil.isNotBlank(request.getCreateTimeEnd())) {
            wrapper.le(EsSyncTask::getCreateTime,
                    LocalDateTime.parse(request.getCreateTimeEnd(), DT_FORMATTER));
        }

        wrapper.orderByDesc(EsSyncTask::getCreateTime);

        IPage<EsSyncTask> entityPage = this.page(page, wrapper);

        Page<EsSyncTaskPageItemVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());

        voPage.setRecords(entityPage.getRecords().stream()
                .map(this::convertToPageItemVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    private EsSyncTaskPageItemVO convertToPageItemVO(EsSyncTask task) {
        EsSyncTaskPageItemVO vo = new EsSyncTaskPageItemVO();
        vo.setId(task.getId());
        vo.setTaskNo(task.getTaskNo());
        vo.setDatasetId(task.getDatasetId());

        // 可选：查一下数据集名称
        EsDataset dataset = esDatasetService.getById(task.getDatasetId());
        if (dataset != null) {
            vo.setDatasetName(dataset.getDatasetName());
        }

        vo.setTaskType(task.getTaskType());
        vo.setTriggerType(task.getTriggerType());
        vo.setStatus(task.getStatus());
        vo.setTotalRows(task.getTotalRows());
        vo.setSuccessRows(task.getSuccessRows());
        vo.setFailedRows(task.getFailedRows());
        vo.setCreateTime(task.getCreateTime());
        vo.setStartedAt(task.getStartedAt());
        vo.setFinishedAt(task.getFinishedAt());
        return vo;
    }

    // ========== 详情 ==========

    @Override
    public EsSyncTaskDetailVO getTaskDetail(Long id) {
        EsSyncTask task = this.getById(id);
        if (task == null) {
            return null;
        }

        EsSyncTaskDetailVO vo = new EsSyncTaskDetailVO();
        BeanUtil.copyProperties(task, vo);

        EsDataset dataset = esDatasetService.getById(task.getDatasetId());
        if (dataset != null) {
            vo.setDatasetName(dataset.getDatasetName());
        }

        return vo;
    }

    // ========== 创建任务 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(EsSyncTaskCreateRequest request) {
        EsDataset dataset = esDatasetService.getById(request.getDatasetId());
        if (dataset == null || "1".equals(dataset.getDelFlag())) {
            throw new IllegalArgumentException("数据集不存在或已删除，datasetId=" + request.getDatasetId());
        }

        EsSyncTask task = new EsSyncTask();
        task.setTaskNo(generateTaskNo(request.getTaskType(), dataset.getId()));
        task.setDatasetId(dataset.getId());
        task.setTaskType(request.getTaskType());
        task.setTriggerType(StrUtil.emptyToDefault(request.getTriggerType(), "manual"));
        task.setBatchSize(request.getBatchSize());
        task.setRangeType(request.getRangeType());
        task.setRangeStart(request.getRangeStart());
        task.setRangeEnd(request.getRangeEnd());

        task.setTotalRows(0L);
        task.setSuccessRows(0L);
        task.setFailedRows(0L);
        task.setStatus("init");
        task.setErrorSummary(null);

        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        this.save(task);
        return task.getId();
    }

    private String generateTaskNo(String taskType, Long datasetId) {
        String type = StrUtil.emptyToDefault(taskType, "unknown").toUpperCase();
        // 示例：ES_FULL_1_20251203_XXXXXXXX
        return "ES_" + type + "_" + datasetId + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                "_" + IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
    }

    // ========== 重试任务 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retryTask(Long id) {
        EsSyncTask task = this.getById(id);
        if (task == null) {
            return false;
        }

        // 只允许对 failed / stopped 状态的任务重试
        if (!"failed".equalsIgnoreCase(task.getStatus())
                && !"stopped".equalsIgnoreCase(task.getStatus())) {
            log.warn("Sync task retry ignored, status not failed/stopped. id={}, status={}", id, task.getStatus());
            return false;
        }

        task.setStatus("init");
        task.setTotalRows(0L);
        task.setSuccessRows(0L);
        task.setFailedRows(0L);
        task.setErrorSummary(null);
        task.setStartedAt(null);
        task.setFinishedAt(null);
        task.setUpdateTime(LocalDateTime.now());

        return this.updateById(task);
    }
}
