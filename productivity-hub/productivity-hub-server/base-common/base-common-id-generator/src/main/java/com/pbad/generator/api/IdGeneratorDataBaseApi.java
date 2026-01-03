package com.pbad.generator.api;

/**
 * 数据库配置分布式id生成api接口.
 *
 * @author: pbad
 * @date: 2023/9/8 11:15
 * @version: 1.0
 */
public interface IdGeneratorDataBaseApi {

    /**
     * 获取唯一id.
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     * @return 唯一id
     */
    String generatorId(long workerId, long datacenterId);
}
