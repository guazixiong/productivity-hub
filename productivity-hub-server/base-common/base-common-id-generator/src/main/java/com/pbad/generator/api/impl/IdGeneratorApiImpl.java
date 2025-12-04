package com.pbad.generator.api.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.generator.domain.SnowflakeIdWorker;
import org.springframework.stereotype.Service;

/**
 * 分布式id生成api接口实现类（基于 Snowflake 算法）.
 *
 * @author: pangdi
 * @date: 2023/9/8 11:16
 * @version: 1.0
 */
@Service
public class IdGeneratorApiImpl implements IdGeneratorApi {

    /**
     * 默认工作ID
     */
    private static final long DEFAULT_WORKER_ID = 0;
    /**
     * 默认数据中心ID
     */
    private static final long DEFAULT_DATA_CENTER_ID = 0;

    /**
     * 获取唯一id（使用默认的workerId和datacenterId）.
     *
     * @return 唯一id
     */
    @Override
    public String generateId() {
        return generatorId(DEFAULT_WORKER_ID, DEFAULT_DATA_CENTER_ID);
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
        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(workerId, datacenterId);
        return String.valueOf(idWorker.nextId());
    }
}

