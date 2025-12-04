package com.pbad.generator.api;

/**
 * 分布式id生成api接口（基于 Snowflake 算法）.
 *
 * @author: pangdi
 * @date: 2023/9/8 11:15
 * @version: 1.0
 */
public interface IdGeneratorApi {

    /**
     * 获取唯一id（使用默认的workerId和datacenterId）.
     *
     * @return 唯一id
     */
    String generateId();

    /**
     * 获取唯一id.
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     * @return 唯一id
     */
    String generatorId(long workerId, long datacenterId);
}

