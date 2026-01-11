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
        "com.pbad.notifications.mapper",
        // Announcements
        "com.pbad.announcement.mapper",
        // Short links
        "com.pbad.shortlink.mapper",
        // Image management
        "com.pbad.image.mapper",
        // Health
        "com.pbad.health.mapper",
        // Asset
        "com.pbad.asset.mapper",
        // ACL
        "com.pbad.acl.mapper"
})
public class MyBatisConfig {
}


