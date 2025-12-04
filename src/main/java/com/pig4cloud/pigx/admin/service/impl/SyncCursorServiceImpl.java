package com.pig4cloud.pigx.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.admin.entity.SyncCursor;
import com.pig4cloud.pigx.admin.mapper.SyncCursorMapper;
import com.pig4cloud.pigx.admin.service.SyncCursorService;
import org.springframework.stereotype.Service;
/**
 * 同步游标
 *
 * @author pigx
 * @date 2025-12-03 22:26:55
 */
@Service
public class SyncCursorServiceImpl extends ServiceImpl<SyncCursorMapper, SyncCursor> implements SyncCursorService {
}