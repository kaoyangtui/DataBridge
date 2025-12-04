package com.pig4cloud.pigx.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pig4cloud.pigx.admin.api.dto.es.*;
import com.pig4cloud.pigx.admin.entity.EsDataset;
import com.pig4cloud.pigx.admin.enums.es.EsDatasetStatusEnum;
import com.pig4cloud.pigx.admin.service.EsDatasetService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/es/dataset")
@RequiredArgsConstructor
@Tag(name = "ES 通用查询 - 数据集管理")
public class EsDatasetController {

    private final EsDatasetService esDatasetService;

    @PostMapping("/page")
    @Operation(summary = "分页查询数据集")
    public R<IPage<DatasetPageItemVO>> page(@RequestBody DatasetPageRequest request) {
        return R.ok(esDatasetService.pageDatasets(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "数据集详情（含字段配置）")
    public R<DatasetDetailVO> detail(@PathVariable("id") Long id) {
        DatasetDetailVO vo = esDatasetService.getDatasetDetail(id);
        if (vo == null) {
            return R.failed("数据集不存在或已删除");
        }
        return R.ok(vo);
    }


    @PostMapping
    @Operation(summary = "新建数据集（可带字段配置）")
    public R<Boolean> create(@Valid @RequestBody DatasetSaveRequest request) {
        request.setId(null);
        return R.ok(esDatasetService.saveOrUpdateDataset(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑数据集（可带字段配置）")
    public R<Boolean> update(@PathVariable("id") @NotNull Long id,
                             @Valid @RequestBody DatasetSaveRequest request) {
        request.setId(id);
        return R.ok(esDatasetService.saveOrUpdateDataset(request));
    }

    @PostMapping("/sql/validate")
    @Operation(summary = "校验 SQL 并预览字段")
    public R<SqlValidateResultVO> validateSql(@RequestBody DatasetSqlValidateRequest request) {
        return R.ok(esDatasetService.validateSql(request));
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新数据集状态（启用/停用）")
    public R<Boolean> updateStatus(@PathVariable("id") Long id,
                                   @PathVariable("status") Integer status) {

        if (EsDatasetStatusEnum.of(status) == null) {
            return R.failed("非法状态值，只允许 0-停用 / 1-启用");
        }

        EsDataset entity = new EsDataset();
        entity.setId(id);
        entity.setStatus(status);
        return R.ok(esDatasetService.updateById(entity));
    }

}
