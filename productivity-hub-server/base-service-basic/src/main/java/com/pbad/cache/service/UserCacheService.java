package com.pbad.cache.service;

/**
 * 用户缓存服务接口.
 * 统一管理用户级缓存模块的加载、清理等操作.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface UserCacheService {

    /**
     * 加载指定用户的所有缓存模块.
     * 该方法会异步执行，不会阻塞调用线程.
     *
     * @param userId 用户ID
     */
    void loadUserCache(String userId);

    /**
     * 加载指定用户的指定缓存模块.
     *
     * @param userId 用户ID
     * @param cacheModule 缓存模块名称
     */
    void loadUserCacheModule(String userId, String cacheModule);

    /**
     * 清理指定用户的所有缓存.
     *
     * @param userId 用户ID
     */
    void clearUserCache(String userId);

    /**
     * 清理指定用户的指定缓存模块.
     *
     * @param userId 用户ID
     * @param cacheModule 缓存模块名称
     */
    void clearUserCacheModule(String userId, String cacheModule);

    /**
     * 检查指定用户的缓存是否已加载.
     *
     * @param userId 用户ID
     * @return true表示已加载，false表示未加载
     */
    boolean isUserCacheLoaded(String userId);
}

