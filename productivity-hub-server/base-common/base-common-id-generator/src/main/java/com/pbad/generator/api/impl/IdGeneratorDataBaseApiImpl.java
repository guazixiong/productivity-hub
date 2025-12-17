package com.pbad.generator.api.impl;

import com.pbad.generator.api.IdGeneratorDataBaseApi;
import com.pbad.generator.constants.IdGeneratorConstants;
import com.pbad.generator.domain.SnowflakeIdWorker;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于入参的分布式id生成api接口实现类.
 *
 * @author: pangdi
 * @date: 2023/9/8 11:16
 * @version: 1.0
 */
@Service
public class IdGeneratorDataBaseApiImpl implements IdGeneratorDataBaseApi {

    private static final ConcurrentMap<String, SnowflakeIdWorker> ID_WORKER_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取唯一id.
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     * @return 唯一id
     */
    @Override
    public String generatorId(long workerId, long datacenterId) {
        final long resolvedWorkerId =
                workerId < 0 ? IdGeneratorConstants.DEFAULT_WORKER_ID : workerId;
        final long resolvedDatacenterId =
                datacenterId < 0 ? IdGeneratorConstants.DEFAULT_DATACENTER_ID : datacenterId;

        String cacheKey = resolvedWorkerId + ":" + resolvedDatacenterId;
        SnowflakeIdWorker idWorker = ID_WORKER_CACHE.computeIfAbsent(
                cacheKey,
                key -> new SnowflakeIdWorker(resolvedWorkerId, resolvedDatacenterId)
        );
        return String.valueOf(idWorker.nextId());
    }
}
