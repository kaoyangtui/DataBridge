package com.pig4cloud.pigx.admin.sched;

import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * @author zhaoliang
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RedisScheduleTask {

    public static final Log log = LogFactory.getLog(RedisScheduleTask.class);

    private final StringRedisTemplate dupShowMasterRedisTemplate;

    @Scheduled(cron = "* 0/1 * * * ?")
    private void configureTasks() {
        log.debug("ping redis");
        dupShowMasterRedisTemplate.execute(RedisConnectionCommands::ping);
    }

}

