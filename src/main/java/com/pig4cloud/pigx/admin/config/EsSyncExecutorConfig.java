package com.pig4cloud.pigx.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class EsSyncExecutorConfig {

    @Bean("esSyncExecutor")
    public Executor esSyncExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("es-sync-");
        ex.initialize();
        return ex;
    }
}
