package com.pig4cloud.pigx;

import com.pig4cloud.pigx.common.security.annotation.EnablePigxResourceServer;
import com.pig4cloud.pigx.common.swagger.annotation.EnableOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lengleng 单体版本启动器，只需要运行此模块则整个系统启动
 */
@EnableOpenApi(value = "admin", isMicro = false)
@EnablePigxResourceServer
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
