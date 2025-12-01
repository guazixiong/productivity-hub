package com.pbad.generator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 发号器启动类.
 *
 * @author: pangdi
 * @date: 2023/9/8 14:00
 * @version: 1.0
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan(value = "com.pbad.generator.mapper")
public class GeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
    }
}
