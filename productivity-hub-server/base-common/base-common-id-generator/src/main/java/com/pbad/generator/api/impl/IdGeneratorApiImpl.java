package com.pbad.generator.api.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.generator.constants.IdGeneratorConstants;
import com.pbad.generator.domain.SnowflakeIdWorker;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 分布式id生成api接口实现类（基于 Snowflake 算法）.
 *
 * @author: pbad
 * @date: 2023/9/8 11:16
 * @version: 1.0
 */
@Service
public class IdGeneratorApiImpl implements IdGeneratorApi {

    /**
     * 默认的ID生成器实例（单例）
     */
    private static final SnowflakeIdWorker DEFAULT_ID_WORKER =
            new SnowflakeIdWorker(IdGeneratorConstants.DEFAULT_WORKER_ID, IdGeneratorConstants.DEFAULT_DATACENTER_ID);

    /**
     * 缓存不同workerId和datacenterId组合的ID生成器实例
     */
    private static final ConcurrentMap<String, SnowflakeIdWorker> ID_WORKER_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取唯一id（使用默认的workerId和datacenterId）.
     *
     * @return 唯一id
     */
    @Override
    public String generateId() {
        return String.valueOf(DEFAULT_ID_WORKER.nextId());
    }

    /**
     * 获取唯一id.
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     * @return 唯一id
     */
    @Override
    public String generatorId(long workerId, long datacenterId) {
        // 如果使用默认值，直接使用默认实例
        if (workerId == IdGeneratorConstants.DEFAULT_WORKER_ID && datacenterId == IdGeneratorConstants.DEFAULT_DATACENTER_ID) {
            return String.valueOf(DEFAULT_ID_WORKER.nextId());
        }

        // 为不同的workerId和datacenterId组合创建或获取对应的实例
        String key = workerId + ":" + datacenterId;
        SnowflakeIdWorker idWorker = ID_WORKER_CACHE.computeIfAbsent(key, 
            k -> new SnowflakeIdWorker(workerId, datacenterId));
        return String.valueOf(idWorker.nextId());
    }
}

