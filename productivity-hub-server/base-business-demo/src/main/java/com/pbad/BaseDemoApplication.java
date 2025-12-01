package com.pbad;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 启动类
 *
 * @author: pangdi
 * @date: 2023/8/29 16:30
 * @version: 1.0
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan(value = "com.pbad.customer.mapper")
// 开启缓存
@EnableCaching
public class BaseDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseDemoApplication.class, args);
    }
}
