package com.pbad.generator.api.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.generator.domain.SnowflakeIdWorker;
import org.springframework.stereotype.Service;

/**
 * 分布式id生成api接口实现类.
 *
 * @author: pangdi
 * @date: 2023/9/8 11:16
 * @version: 1.0
 */
@Service
public class IdGeneratorApiImpl implements IdGeneratorApi {

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
