package com.pig4cloud.pigx.admin.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pigx.admin.api.dto.es.EsExportRequest;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryPageVO;
import com.pig4cloud.pigx.admin.api.dto.es.EsQueryRequest;
import com.pig4cloud.pigx.admin.entity.EsDataset;
import com.pig4cloud.pigx.admin.entity.EsDatasetField;
import com.pig4cloud.pigx.admin.service.EsDatasetFieldService;
import com.pig4cloud.pigx.admin.service.EsDatasetService;
import com.pig4cloud.pigx.admin.service.EsQueryService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ES 通用查询 - 运行时查询 & 导出
 */
@RestController
@RequestMapping("/es/query")
@RequiredArgsConstructor
@Tag(name = "ES 通用查询 - 运行时查询")
public class EsQueryController {

    private final EsQueryService esQueryService;
    private final EsDatasetService esDatasetService;
    private final EsDatasetFieldService esDatasetFieldService;

    // ================== 查询 ==================

    @PostMapping("/run")
    @Operation(summary = "执行通用查询")
    public R<EsQueryPageVO> run(@RequestBody EsQueryRequest request) {
        // datasetId / datasetCode 至少一个
        if (request.getDatasetId() == null &&
                StrUtil.isBlank(request.getDatasetCode())) {
            return R.failed("datasetId 和 datasetCode 至少需要一个");
        }
        return R.ok(esQueryService.query(request));
    }

    // ================== 导出 Excel ==================

    @PostMapping("/export")
    @Operation(summary = "导出查询结果为 Excel")
    public void export(@RequestBody EsExportRequest request,
                       HttpServletResponse response) throws Exception {

        // 1）确定数据集
        EsDataset dataset = null;
        if (request.getDatasetId() != null) {
            dataset = esDatasetService.getById(request.getDatasetId());
        } else if (StrUtil.isNotBlank(request.getDatasetCode())) {
            dataset = esDatasetService.getOne(
                    Wrappers.lambdaQuery(EsDataset.class)
                            .eq(EsDataset::getDatasetCode, request.getDatasetCode())
                            .eq(EsDataset::getDelFlag, "0"),
                    false
            );
        }
        if (dataset == null || "1".equals(dataset.getDelFlag())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("数据集不存在或已删除");
            return;
        }

        // 2）查字段配置：可导出字段 + 排序
        List<EsDatasetField> allFields = esDatasetFieldService.list(
                Wrappers.lambdaQuery(EsDatasetField.class)
                        .eq(EsDatasetField::getDatasetId, dataset.getId())
        );

        Set<String> reqFieldSet = request.getExportFieldCodes() == null
                ? Collections.emptySet()
                : request.getExportFieldCodes().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<EsDatasetField> exportFields = allFields.stream()
                // exportable: null 或 1 认为可导出
                .filter(f -> f.getExportable() == null || f.getExportable() == 1)
                // 如果前端传了 exportFieldCodes，则只导出勾选的
                .filter(f -> reqFieldSet.isEmpty() || reqFieldSet.contains(f.getFieldCode()))
                // 按 exportOrder 排序
                .sorted(Comparator.comparing(f -> f.getExportOrder() == null ? 100 : f.getExportOrder()))
                .collect(Collectors.toList());

        if (exportFields.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("当前数据集未配置可导出字段");
            return;
        }

        // 3）构造查询参数：从第 1 页开始，size = maxRows（做一下上限保护）
        int maxRows = request.getMaxRows() == null ? 5000 : request.getMaxRows();
        if (maxRows <= 0) {
            maxRows = 5000;
        }
        int maxLimit = 50000; // 防止一次性拉爆
        maxRows = Math.min(maxRows, maxLimit);

        request.setCurrent(1L);
        request.setSize((long) maxRows);

        EsQueryPageVO page = esQueryService.query(request);

        // 4）组装表头 & 数据
        // head: List<List<String>>，每个内层 List 代表一列的多级表头
        List<List<String>> head = exportFields.stream()
                .map(f -> Collections.singletonList(
                        Optional.ofNullable(f.getExportTitle())
                                .filter(StrUtil::isNotBlank)
                                .orElse(f.getFieldName())
                ))
                .collect(Collectors.toList());

        // data: List<List<Object>>
        List<List<Object>> data = page.getRecords().stream()
                .map(hit -> {
                    Map<String, Object> src = hit.getSource();
                    List<Object> row = new ArrayList<>(exportFields.size());
                    for (EsDatasetField f : exportFields) {
                        Object val = src.get(f.getFieldCode());
                        row.add(normalizeCellValue(val));
                    }
                    return row;
                })
                .collect(Collectors.toList());

        // 5）设置响应头，写 Excel
        String fileName = Optional.ofNullable(request.getFileName())
                .filter(StrUtil::isNotBlank)
                .orElse(dataset.getDatasetName());
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replaceAll("\\+", "%20");

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=" + fileName + ".xlsx"
        );

        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("数据")
                .doWrite(data);
    }

    /**
     * 将 ES 返回的各种类型统一转换为 EasyExcel 可写入的类型
     * - null -> ""
     * - Collection / 数组 -> "v1;v2;v3"
     * - Map / 复杂对象 -> JSON 字符串
     * - 其他 -> 原样或 toString()
     */
    private Object normalizeCellValue(Object val) {
        if (val == null) {
            return "";
        }

        // 集合：用分号拼接
        if (val instanceof Collection) {
            Collection<?> coll = (Collection<?>) val;
            if (coll.isEmpty()) {
                return "";
            }
            return coll.stream()
                    .map(v -> v == null ? "" : String.valueOf(v))
                    .collect(Collectors.joining(";"));
        }

        // 数组：同样按分号拼接
        if (val.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(val);
            if (len == 0) {
                return "";
            }
            List<String> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                Object elem = java.lang.reflect.Array.get(val, i);
                list.add(elem == null ? "" : String.valueOf(elem));
            }
            return String.join(";", list);
        }

        // Map 或其他复杂对象：转 JSON
        if (val instanceof Map) {
            return JSONUtil.toJsonStr(val);
        }

        // 其他普通类型：交给 EasyExcel 默认处理（String/Number/Date 等）
        return val;
    }
}
