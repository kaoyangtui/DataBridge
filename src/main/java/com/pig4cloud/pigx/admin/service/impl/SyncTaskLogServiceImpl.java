package com.pig4cloud.pigx.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.entity.SyncTaskLog;
import com.pig4cloud.pigx.admin.mapper.SyncTaskLogMapper;
import com.pig4cloud.pigx.admin.service.SyncTaskLogService;
import org.springframework.stereotype.Service;
/**
 * 同步任务执行日志
 *
 * @author pigx
 * @date 2025-12-03 22:27:37
 */
@Service
public class SyncTaskLogServiceImpl extends ServiceImpl<SyncTaskLogMapper, SyncTaskLog> implements SyncTaskLogService {
}