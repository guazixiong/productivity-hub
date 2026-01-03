package com.pbad.asset.runner;

import com.pbad.asset.domain.po.AssetCategoryPO;
import com.pbad.asset.mapper.AssetCategoryMapper;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 资产分类缓存预热Runner.
 * 在项目启动时将资产分类数据加载到Redis缓存中.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Component
@Order(30)
@RequiredArgsConstructor
public class AssetCategoryCacheWarmupRunner implements ApplicationRunner {

    private static final String CACHE_KEY = "asset:category:list";

    private final AssetCategoryMapper categoryMapper;
    private final RedisUtil redisUtil;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("[AssetCategoryWarmup] 开始加载资产分类数据到Redis缓存...");
            List<AssetCategoryPO> categories = categoryMapper.selectAll();
            if (categories != null && !categories.isEmpty()) {
                // 将分类数据存入Redis缓存，不设置过期时间（永久缓存，通过更新操作来刷新）
                redisUtil.defaultSetKeyNoExpiration(CACHE_KEY, categories);
                log.info("[AssetCategoryWarmup] 资产分类数据已加载到Redis缓存，共{}条记录", categories.size());
            } else {
                log.warn("[AssetCategoryWarmup] 未找到资产分类数据，跳过缓存预热");
            }
        } catch (Exception e) {
            log.error("[AssetCategoryWarmup] 资产分类缓存预热失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}

