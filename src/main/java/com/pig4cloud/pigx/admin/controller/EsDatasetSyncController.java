package com.pig4cloud.pigx.admin.controller;

import com.pig4cloud.pigx.admin.api.dto.es.EsSyncResultVO;
import com.pig4cloud.pigx.admin.service.EsSyncService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/es/sync")
@RequiredArgsConstructor
@Tag(name = "ES 通用查询 - 同步任务")
public class EsDatasetSyncController {

    private final EsSyncService esSyncService;

    @PostMapping("/{datasetId}/full")
    @Operation(summary = "全量同步：SQL -> ES（Bulk）")
    public R<EsSyncResultVO> full(@PathVariable Long datasetId,
                                  @RequestParam(defaultValue = "false") boolean recreateIndex) {
        return R.ok(esSyncService.fullSync(datasetId, recreateIndex));
    }
}
