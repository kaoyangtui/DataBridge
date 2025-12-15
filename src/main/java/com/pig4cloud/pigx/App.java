package com.pig4cloud.pigx;

import com.pig4cloud.pigx.common.security.annotation.EnablePigxResourceServer;
import com.pig4cloud.pigx.common.swagger.annotation.EnableOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

/**
 * @author lengleng 单体版本启动器，只需要运行此模块则整个系统启动
 */
@EnableOpenApi(value = "admin", isMicro = false)
@EnablePigxResourceServer
@SpringBootApplication
public class App {

    private static final String DEFAULT_PROFILE = "dev";

    public static void main(String[] args) {
        ensureDefaultProfile();
        SpringApplication.run(App.class, args);
    }

    /**
     * 在未显式指定 spring.profiles.active 时，为应用注入默认的 dev 环境，
     * 避免因缺少配置而导致启动失败。
     */
    private static void ensureDefaultProfile() {
        if (System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME) == null
                && System.getenv("SPRING_PROFILES_ACTIVE") == null) {
            System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, DEFAULT_PROFILE);
        }
    }
}
