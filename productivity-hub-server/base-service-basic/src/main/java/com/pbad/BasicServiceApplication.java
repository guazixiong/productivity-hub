package com.pbad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 基础服务启动类
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.pbad",   // 统一扫描业务与第三方 API Bean
                "common"      // 扫描通用组件
        },
        exclude = {DataSourceAutoConfiguration.class}
)
@EnableCaching
@EnableAsync
@EnableScheduling
public class BasicServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicServiceApplication.class, args);
    }
}

