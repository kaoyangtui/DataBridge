package com.pig4cloud.pigx.admin.controller;

import com.pig4cloud.pigx.admin.api.dto.es.EsSyncResultVO;
import com.pig4cloud.pigx.admin.service.EsSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/es/sync")
public class EsSyncController {

    private final EsSyncService esSyncService;

    /**
     * 全量同步（可选重建索引）
     * POST /admin/es/sync/full/{datasetId}?recreateIndex=true
     */
    @PostMapping("/full/{datasetId}")
    public EsSyncResultVO fullSync(@PathVariable Long datasetId,
                                   @RequestParam(defaultValue = "false") boolean recreateIndex) {
        return esSyncService.fullSync(datasetId, recreateIndex);
    }
}
