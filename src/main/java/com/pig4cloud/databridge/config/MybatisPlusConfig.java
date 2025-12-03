package com.pig4cloud.databridge.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 覆盖 pigx 默认的 MyBatis Plus 拦截器配置，避免缺少数据权限拦截器导致启动失败。
 * 如需数据权限能力，可在后续引入 DataScopeInterceptor 并补充配置。
 */
@Configuration(proxyBeanMethods = false)
public class MybatisPlusConfig {

    @Bean(name = "mybatisPlusInterceptor")
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

}
