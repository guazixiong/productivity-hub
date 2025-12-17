package com.pbad.config.runner;

import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.config.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时为所有用户预热全局配置缓存.
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class ConfigCacheWarmupRunner implements ApplicationRunner {

    private final UserMapper userMapper;
    private final ConfigService configService;

    @Override
    public void run(ApplicationArguments args) {
        List<UserPO> users = userMapper.selectAll();
        if (users == null || users.isEmpty()) {
            log.info("[ConfigWarmup] 没有用户需要预热，全局配置缓存跳过。");
            return;
        }

        log.info("[ConfigWarmup] 开始为 {} 个用户预热全局配置缓存。", users.size());
        for (UserPO user : users) {
            try {
                configService.getConfigList(user.getId());
            } catch (Exception ex) {
                log.warn("[ConfigWarmup] 预热用户 {} 配置缓存失败: {}", user.getId(), ex.getMessage(), ex);
            }
        }
        log.info("[ConfigWarmup] 全部用户配置缓存预热完成。");
    }
}

