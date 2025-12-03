package com.pig4cloud.databridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DataBridge 应用启动类。
 *
 * <p>基于 pigx 框架的 MySQL → ES 同步与通用查询工具的入口。</p>
 */
@SpringBootApplication
public class DataBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataBridgeApplication.class, args);
    }
}
