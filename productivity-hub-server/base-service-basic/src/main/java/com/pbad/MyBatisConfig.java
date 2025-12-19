package com.pbad;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Mapper 扫描配置
 */
@Configuration
@MapperScan({
        "com.pbad.auth.mapper",
        "com.pbad.bookmark.mapper",
        "com.pbad.codegenerator.mapper",
        "com.pbad.config.mapper",
        "com.pbad.tools.mapper",
        "com.pbad.messages.mapper",
        "com.pbad.agents.mapper",
        "com.pbad.todo.mapper",
        "com.pbad.notifications.mapper"
})
public class MyBatisConfig {
}


