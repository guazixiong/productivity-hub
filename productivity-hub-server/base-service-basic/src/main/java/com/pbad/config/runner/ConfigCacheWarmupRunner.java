package com.pbad.config.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 配置缓存预热Runner.
 * 
 * 注意：根据需求，用户级缓存不再在启动时预热，而是在用户登录后按需加载。
 * 此Runner保留用于未来可能的全局/系统级配置缓存预热。
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class ConfigCacheWarmupRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        log.info("[ConfigWarmup] 配置缓存预热已禁用，用户缓存将在登录后按需加载。");
        // 用户级缓存不再在启动时预热，改为登录后加载
        // 如需预热全局/系统级配置，可在此处添加相关逻辑
    }
}

