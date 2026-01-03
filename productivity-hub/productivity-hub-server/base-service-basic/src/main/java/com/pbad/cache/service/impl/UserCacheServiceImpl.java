package com.pbad.cache.service.impl;

import com.pbad.bookmark.service.BookmarkUrlService;
import com.pbad.cache.service.UserCacheService;
import com.pbad.config.service.ConfigService;
import com.pbad.home.service.HomeService;
import common.util.RedisUtil;
import common.web.context.RequestUser;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用户缓存服务实现类.
 * 统一管理用户级缓存模块的加载、清理等操作.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheServiceImpl implements UserCacheService {

    private final ConfigService configService;
    private final BookmarkUrlService bookmarkUrlService;
    private final HomeService homeService;
    private final RedisUtil redisUtil;

    /**
     * 缓存模块名称常量
     */
    public static final String CACHE_MODULE_CONFIG = "config";
    public static final String CACHE_MODULE_BOOKMARK = "bookmark";
    public static final String CACHE_MODULE_HOME = "home";

    /**
     * 用户缓存加载锁，防止并发重复加载
     */
    private final Map<String, ReentrantLock> userCacheLocks = new ConcurrentHashMap<>();

    /**
     * 已加载缓存的用户集合（用于快速判断）
     */
    private final Set<String> loadedUsers = ConcurrentHashMap.newKeySet();

    /**
     * 缓存模块的加载器映射
     */
    private final Map<String, CacheLoader> cacheLoaders = createCacheLoaders();

    /**
     * 初始化缓存加载器映射，兼容 JDK8 环境。
     */
    private Map<String, CacheLoader> createCacheLoaders() {
        Map<String, CacheLoader> loaders = new ConcurrentHashMap<>();

        loaders.put(CACHE_MODULE_CONFIG, userId -> {
            try {
                // 配置服务不需要用户上下文，直接调用
                configService.getConfigList(userId);
                log.debug("[UserCache] 用户 {} 的配置缓存加载完成", userId);
            } catch (Exception e) {
                log.error("[UserCache] 用户 {} 的配置缓存加载失败: {}", userId, e.getMessage(), e);
                throw e;
            }
        });

        loaders.put(CACHE_MODULE_BOOKMARK, userId -> {
            try {
                // 书签服务需要用户上下文，需要临时设置
                RequestUserContext.set(RequestUser.builder().userId(userId).username("system").build());
                try {
                    bookmarkUrlService.getUrlGroups();
                    log.debug("[UserCache] 用户 {} 的书签缓存加载完成", userId);
                } finally {
                    // 确保清理上下文
                    RequestUserContext.clear();
                }
            } catch (Exception e) {
                log.error("[UserCache] 用户 {} 的书签缓存加载失败: {}", userId, e.getMessage(), e);
                throw e;
            }
        });

        loaders.put(CACHE_MODULE_HOME, userId -> {
            try {
                // 首页服务（天气和每日一签）预加载
                // 使用默认位置（郑州）进行预加载，用户首次访问天气接口时会根据实际位置更新
                Double defaultLatitude = 34.7466;
                Double defaultLongitude = 113.6254;
                String defaultCityName = "郑州";
                homeService.preloadUserCache(userId, defaultLatitude, defaultLongitude, defaultCityName, null);
                log.debug("[UserCache] 用户 {} 的首页缓存（天气和每日一签）加载完成", userId);
            } catch (Exception e) {
                log.error("[UserCache] 用户 {} 的首页缓存加载失败: {}", userId, e.getMessage(), e);
                throw e;
            }
        });

        return loaders;
    }

    /**
     * 缓存加载器接口
     */
    @FunctionalInterface
    private interface CacheLoader {
        void load(String userId) throws Exception;
    }

    @Override
    @Async
    public void loadUserCache(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[UserCache] 用户ID为空，跳过缓存加载");
            return;
        }

        // 异步方法在新线程中执行，需要设置用户上下文
        RequestUserContext.set(RequestUser.builder().userId(userId).username("system").build());
        try {
            // 获取用户专属的锁
            ReentrantLock lock = userCacheLocks.computeIfAbsent(userId, k -> new ReentrantLock());
            
            // 如果已经加载过，直接返回
            if (loadedUsers.contains(userId)) {
                log.debug("[UserCache] 用户 {} 的缓存已加载，跳过", userId);
                return;
            }

            lock.lock();
            try {
                // 双重检查，防止并发
                if (loadedUsers.contains(userId)) {
                    log.debug("[UserCache] 用户 {} 的缓存已加载（双重检查），跳过", userId);
                    return;
                }

                log.info("[UserCache] 开始加载用户 {} 的缓存", userId);
                long startTime = System.currentTimeMillis();

                // 按顺序加载各个缓存模块
                for (Map.Entry<String, CacheLoader> entry : cacheLoaders.entrySet()) {
                    String module = entry.getKey();
                    CacheLoader loader = entry.getValue();
                    try {
                        loader.load(userId);
                    } catch (Exception e) {
                        log.error("[UserCache] 用户 {} 的 {} 模块缓存加载失败，继续加载其他模块", userId, module, e);
                        // 继续加载其他模块，不中断整个流程
                    }
                }

                long duration = System.currentTimeMillis() - startTime;
                loadedUsers.add(userId);
                log.info("[UserCache] 用户 {} 的缓存加载完成，耗时 {} ms", userId, duration);
            } finally {
                lock.unlock();
                // 清理锁（保留一段时间后清理，避免频繁创建）
                // 这里简化处理，直接保留锁对象
            }
        } finally {
            // 确保清理用户上下文
            RequestUserContext.clear();
        }
    }

    @Override
    public void loadUserCacheModule(String userId, String cacheModule) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[UserCache] 用户ID为空，跳过缓存模块加载");
            return;
        }
        if (cacheModule == null || cacheModule.isEmpty()) {
            log.warn("[UserCache] 缓存模块名称为空，跳过加载");
            return;
        }

        CacheLoader loader = cacheLoaders.get(cacheModule);
        if (loader == null) {
            log.warn("[UserCache] 未知的缓存模块: {}", cacheModule);
            return;
        }

        // 设置用户上下文（某些服务需要）
        RequestUserContext.set(RequestUser.builder().userId(userId).username("system").build());
        try {
            loader.load(userId);
            log.info("[UserCache] 用户 {} 的 {} 模块缓存加载完成", userId, cacheModule);
        } catch (Exception e) {
            log.error("[UserCache] 用户 {} 的 {} 模块缓存加载失败: {}", userId, cacheModule, e.getMessage(), e);
            throw new RuntimeException("缓存模块加载失败: " + cacheModule, e);
        } finally {
            // 确保清理上下文
            RequestUserContext.clear();
        }
    }

    @Override
    public void clearUserCache(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[UserCache] 用户ID为空，跳过缓存清理");
            return;
        }

        log.info("[UserCache] 开始清理用户 {} 的所有缓存", userId);

        // 清理各个缓存模块
        clearUserCacheModule(userId, CACHE_MODULE_CONFIG);
        clearUserCacheModule(userId, CACHE_MODULE_BOOKMARK);
        clearUserCacheModule(userId, CACHE_MODULE_HOME);

        // 从已加载集合中移除
        loadedUsers.remove(userId);
        
        // 清理锁
        userCacheLocks.remove(userId);

        log.info("[UserCache] 用户 {} 的所有缓存清理完成", userId);
    }

    @Override
    public void clearUserCacheModule(String userId, String cacheModule) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[UserCache] 用户ID为空，跳过缓存模块清理");
            return;
        }
        if (cacheModule == null || cacheModule.isEmpty()) {
            log.warn("[UserCache] 缓存模块名称为空，跳过清理");
            return;
        }

        try {
            switch (cacheModule) {
                case CACHE_MODULE_CONFIG:
                    String configKey = String.format("phub:config:list:%s", userId);
                    redisUtil.delete(configKey);
                    log.debug("[UserCache] 用户 {} 的配置缓存已清理", userId);
                    break;
                case CACHE_MODULE_BOOKMARK:
                    String urlKey = String.format("phub:bookmark:urls:%s", userId);
                    String groupKey = String.format("phub:bookmark:groups:%s", userId);
                    redisUtil.delete(urlKey);
                    redisUtil.delete(groupKey);
                    log.debug("[UserCache] 用户 {} 的书签缓存已清理", userId);
                    break;
                case CACHE_MODULE_HOME:
                    // 清理首页缓存（天气和每日一签）
                    // 使用通配符删除所有相关的缓存key
                    String weatherPattern = String.format("phub:home:weather:%s:*", userId);
                    String dailyQuotePattern = String.format("phub:home:dailyQuote:%s:*", userId);
                    Set<String> weatherKeys = redisUtil.keys(weatherPattern);
                    Set<String> dailyQuoteKeys = redisUtil.keys(dailyQuotePattern);
                    if (weatherKeys != null) {
                        for (String key : weatherKeys) {
                            redisUtil.delete(key);
                        }
                    }
                    if (dailyQuoteKeys != null) {
                        for (String key : dailyQuoteKeys) {
                            redisUtil.delete(key);
                        }
                    }
                    log.debug("[UserCache] 用户 {} 的首页缓存（天气和每日一签）已清理", userId);
                    break;
                default:
                    log.warn("[UserCache] 未知的缓存模块: {}", cacheModule);
            }
        } catch (Exception e) {
            log.error("[UserCache] 用户 {} 的 {} 模块缓存清理失败: {}", userId, cacheModule, e.getMessage(), e);
        }
    }

    @Override
    public boolean isUserCacheLoaded(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        return loadedUsers.contains(userId);
    }
}

