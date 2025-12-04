package com.pig4cloud.pigx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 同步游标
 *
 * @author pigx
 * @date 2025-12-03 22:26:55
 */
@Data
@TableName("t_sync_cursor")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "同步游标")
public class SyncCursor extends Model<SyncCursor> {


	/**
	* 主键
	*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description="主键")
    private Long id;

	/**
	* 关联 t_sync_dataset.id
	*/
    @Schema(description="关联 t_sync_dataset.id")
    private Long datasetId;

	/**
	* 上次增量值, 如 last_id 或 last_time
	*/
    @Schema(description="上次增量值, 如 last_id 或 last_time")
    private String lastIncVal;

	/**
	* 上次同步时间
	*/
    @Schema(description="上次同步时间")
    private LocalDateTime lastSyncAt;
}