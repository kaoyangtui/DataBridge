package com.pig4cloud.pigx.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.api.dto.es.EsSyncResultVO;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executor;
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

    /**
     * ⭐方案1：直接注入实现类（调用 fullSyncWithBatch）
     * 如果你遇到循环依赖，再把 @Lazy 打开即可
     */
    private final @Lazy EsSyncServiceImpl esSyncServiceImpl;

    /**
     * 线程池执行器（你项目里有就保留 @Qualifier，没有就去掉）
     */
    private final @Qualifier("esSyncExecutor") Executor esSyncExecutor;

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
        task.setStartedAt(null);
        task.setFinishedAt(null);

        // extJson：如果 request 里带了 extJson / recreateIndex 等字段，用反射兼容读取
        JSONObject ext = new JSONObject();
        Object extJson = getPropertySafe(request, "extJson");
        if (extJson != null && StrUtil.isNotBlank(String.valueOf(extJson))) {
            try {
                JSONObject parsed = JSON.parseObject(String.valueOf(extJson));
                if (parsed != null) {
                    ext.putAll(parsed);
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
        Object recreateIndex = getPropertySafe(request, "recreateIndex");
        if (recreateIndex != null) {
            ext.put("recreateIndex", toBoolSafe(recreateIndex));
        }
        if (!ext.isEmpty()) {
            task.setExtJson(ext.toJSONString());
        }

        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        this.save(task);
        return task.getId();
    }

    private String generateTaskNo(String taskType, Long datasetId) {
        String type = StrUtil.emptyToDefault(taskType, "unknown").toUpperCase();
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

        if (!"failed".equalsIgnoreCase(task.getStatus())
                && !"stopped".equalsIgnoreCase(task.getStatus())) {
            log.warn("Sync task retry ignored, status not failed/stopped. id={}, status={}", id, task.getStatus());
            return false;
        }

        EsSyncTask upd = new EsSyncTask();
        upd.setId(id);
        upd.setStatus("init");
        upd.setTotalRows(0L);
        upd.setSuccessRows(0L);
        upd.setFailedRows(0L);
        upd.setErrorSummary(null);
        upd.setStartedAt(null);
        upd.setFinishedAt(null);
        upd.setUpdateTime(LocalDateTime.now());

        return this.updateById(upd);
    }

    // ========== ⭐新增：创建并启动 / 启动任务（异步）==========

    /**
     * 创建任务并立即异步启动（不要求接口必须声明该方法）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createAndStart(EsSyncTaskCreateRequest request) {
        Long taskId = this.createTask(request);
        this.startTaskAsync(taskId);
        return taskId;
    }

    /**
     * 将 init -> running，并异步执行（不要求接口必须声明该方法）
     */
    @Override
    public boolean startTaskAsync(Long id) {
        // 只允许 init 启动（用条件更新避免并发重复启动）
        boolean updated = this.update(
                Wrappers.lambdaUpdate(EsSyncTask.class)
                        .eq(EsSyncTask::getId, id)
                        .eq(EsSyncTask::getStatus, "init")
                        .set(EsSyncTask::getStatus, "running")
                        .set(EsSyncTask::getStartedAt, LocalDateTime.now())
                        .set(EsSyncTask::getUpdateTime, LocalDateTime.now())
        );
        if (!updated) {
            EsSyncTask task = this.getById(id);
            String st = task == null ? "null" : task.getStatus();
            log.warn("Sync task start ignored, status not init. id={}, status={}", id, st);
            return false;
        }

        esSyncExecutor.execute(() -> {
            this.runTask(id);
        });

        return true;
    }

    /**
     * 真正执行任务：调用 EsSyncServiceImpl.fullSyncWithBatch()
     */
    private void runTask(Long taskId) {
        EsSyncTask task = this.getById(taskId);
        if (task == null) {
            return;
        }

        try {
            boolean recreateIndex = readRecreateIndex(task.getExtJson());

            // ⭐关键：用任务 batchSize 覆盖 dataset.commitBatch
            Integer batchSize = task.getBatchSize();

            EsSyncResultVO vo = esSyncServiceImpl.fullSyncWithBatch(
                    task.getDatasetId(),
                    recreateIndex,
                    batchSize
            );

            // 写回结果
            EsSyncTask done = new EsSyncTask();
            done.setId(taskId);
            done.setStatus("success");
            done.setTotalRows(vo.getReadTotal());
            done.setSuccessRows(vo.getSuccess());
            done.setFailedRows(vo.getFail());
            done.setFinishedAt(LocalDateTime.now());
            done.setUpdateTime(LocalDateTime.now());

            // errorSummary：取第一条
            if (vo.getErrors() != null && !vo.getErrors().isEmpty()) {
                done.setErrorSummary(StrUtil.sub(vo.getErrors().get(0), 0, 2000));
            }

            // extJson：保留 recreateIndex、batchSize、errors(前50)
            JSONObject ext = safeParseJson(task.getExtJson());
            if (ext == null) {
                ext = new JSONObject();
            }
            ext.put("recreateIndex", recreateIndex);
            if (batchSize != null) {
                ext.put("batchSize", batchSize);
            }
            if (vo.getErrors() != null && !vo.getErrors().isEmpty()) {
                List<String> errs = vo.getErrors();
                if (errs.size() > 50) {
                    errs = errs.subList(0, 50);
                }
                JSONArray errArr = new JSONArray();
                errArr.addAll(errs);
                ext.put("errors", errArr);
            }
            done.setExtJson(ext.toJSONString());

            this.updateById(done);

        } catch (Exception e) {
            log.error("Sync task run failed. taskId={}", taskId, e);

            EsSyncTask fail = new EsSyncTask();
            fail.setId(taskId);
            fail.setStatus("failed");
            fail.setFinishedAt(LocalDateTime.now());
            fail.setUpdateTime(LocalDateTime.now());
            fail.setErrorSummary(StrUtil.sub(e.getMessage(), 0, 2000));

            JSONObject ext = safeParseJson(task.getExtJson());
            if (ext == null) {
                ext = new JSONObject();
            }
            ext.put("exception", StrUtil.sub(String.valueOf(e), 0, 4000));
            fail.setExtJson(ext.toJSONString());

            this.updateById(fail);
        }
    }

    private boolean readRecreateIndex(String extJson) {
        JSONObject ext = safeParseJson(extJson);
        if (ext == null) {
            return false;
        }
        Boolean b = ext.getBoolean("recreateIndex");
        return b != null && b;
    }

    private static JSONObject safeParseJson(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getPropertySafe(Object bean, String prop) {
        try {
            return BeanUtil.getProperty(bean, prop);
        } catch (Exception e) {
            return null;
        }
    }

    private static Boolean toBoolSafe(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue() != 0;
        }
        String s = String.valueOf(v).trim();
        if (StrUtil.isBlank(s)) {
            return null;
        }
        if ("1".equals(s)) {
            return true;
        }
        if ("0".equals(s)) {
            return false;
        }
        if ("true".equalsIgnoreCase(s) || "y".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)) {
            return true;
        }
        if ("false".equalsIgnoreCase(s) || "n".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)) {
            return false;
        }
        return null;
    }
}
