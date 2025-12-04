package com.pig4cloud.pigx.admin.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pig4cloud.pigx.admin.service.EtlInvoker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 基于 canal-adapter 1.1.8 的 ETL 调用实现：
 * - POST /etl/es8/{mapping}.yml
 * - body: params=1;start;end
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlInvokerImpl implements EtlInvoker {

    /**
     * canal-adapter 的基础地址
     * 例如: http://14.103.193.249:8081
     */
    @Value("${sync.canal-adapter.base-url}")
    private String canalAdapterBaseUrl;

    @Override
    public String invokeEtl(String mappingName, Map<String, String> params) {
        if (StrUtil.isBlank(mappingName)) {
            throw new IllegalArgumentException("mappingName 不能为空");
        }

        // 1) 组装 URL: /etl/es8/{mapping}.yml
        StringBuilder url = new StringBuilder();
        url.append(canalAdapterBaseUrl);
        if (!canalAdapterBaseUrl.endsWith("/")) {
            url.append("/");
        }
        url.append("etl/es8/").append(mappingName).append(".yml");

        // 2) 组装 params 字符串
        // 约定：
        //  - 全量：params=1
        //  - 区间：params=1;{start};{end}
        String paramsStr = buildParamsString(params);

        String body = "params=" + paramsStr;
        String finalUrl = url.toString();

        log.info("调用 canal-adapter ETL 开始, url={}, body={}", finalUrl, body);

        try {
            // 对应你的 curl: curl -X POST ... -d "params=1;xxx;yyy"
            String resp = HttpUtil.post(finalUrl, body);
            log.info("调用 canal-adapter ETL 完成, url={}, resp={}", finalUrl, resp);
            return resp;
        } catch (Exception e) {
            log.error("调用 canal-adapter ETL 失败, url={}", finalUrl, e);
            throw new RuntimeException("调用 canal-adapter ETL 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构造 canal-adapter 1.1.8 需要的 params 字符串
     *
     * @param params 约定:
     *               - 全量: null 或空 Map -> "1"
     *               - 增量/区间: 期待有 start/end 两个 key -> "1;startVal;endVal"
     */
    private String buildParamsString(Map<String, String> params) {
        // 线程数/模式的占位符，按你现在的用法固定为 1
        String first = "1";

        if (MapUtil.isEmpty(params)) {
            // 全量：只传 "1"
            return first;
        }

        String start = params.get("start");
        String end = params.get("end");

        if (StrUtil.isNotBlank(start) && StrUtil.isNotBlank(end)) {
            // 区间增量/补数
            return StrUtil.format("{};{};{}", first, start, end);
        }

        // 如果以后你有更复杂的参数，可以在这里扩展
        // 默认兜底：把 Map 的 value 按顺序拼接
        StringBuilder sb = new StringBuilder(first);
        params.values().forEach(v -> {
            sb.append(";");
            sb.append(StrUtil.nullToEmpty(v));
        });
        return sb.toString();
    }
}
