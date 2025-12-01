package com.pbad.generator.api.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.pbad.generator.api.IdGeneratorDataBaseApi;
import com.pbad.generator.domain.SnowflakeIdWorker;
import com.pbad.generator.domain.po.IdGeneratorInfoPO;
import com.pbad.generator.enums.StatusEnum;
import com.pbad.generator.exception.IdExceptionMsgEnum;
import com.pbad.generator.mapper.IdGeneratorMapper;
import common.util.judge.JudgeParameterUtil;
import common.util.RedisUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据库配置分布式id生成api接口实现类.
 *
 * @author: pangdi
 * @date: 2023/9/8 11:16
 * @version: 1.0
 */
@Service
public class IdGeneratorDataBaseApiImpl implements IdGeneratorDataBaseApi {

    /**
     * 默认工作ID
     */
    private static final long DEFAULT_WORKER_ID = 0;
    /**
     * 默认数据中心ID
     */
    private static final long DEFAULT_DATA_CENTER_ID = 0;

    @Resource
    private IdGeneratorMapper idGeneratorMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 获取唯一id.
     *
     * @param moduleKey 模块唯一标识
     * @return 唯一id
     */
    @Override
    public String generatorId(String moduleKey) {
        JudgeParameterUtil.checkNotNull(moduleKey,
                IdExceptionMsgEnum.MODULE_KEY_IS_NULL.getErrorCode(),
                IdExceptionMsgEnum.MODULE_KEY_IS_NULL.getErrorMessage());
        if (Boolean.TRUE.equals(redisUtil.hasKey(moduleKey))) {
            // 缓存中读取
            IdGeneratorInfoPO idGeneratorInfo = JSONUtil.toBean((String) redisUtil.getValue(moduleKey), IdGeneratorInfoPO.class);
            SnowflakeIdWorker idWorker = new SnowflakeIdWorker(idGeneratorInfo.getWorkerId(), idGeneratorInfo.getDatacenterId());
            return String.valueOf(idWorker.nextId());
        }
        IdGeneratorInfoPO queryIdGeneratorInfo = new IdGeneratorInfoPO()
                .setModuleKey(moduleKey)
                .setStatus(StatusEnum.NORMAL.getCode());
        List<IdGeneratorInfoPO> generatorInfoList = idGeneratorMapper.selectByModuleKey(queryIdGeneratorInfo);
        IdGeneratorInfoPO generatorInfo = generatorInfoList.get(0);
        long workerId = CollUtil.isEmpty(generatorInfoList) ? DEFAULT_WORKER_ID : generatorInfo.getWorkerId();
        long datacenterId = CollUtil.isEmpty(generatorInfoList) ? DEFAULT_DATA_CENTER_ID : generatorInfo.getDatacenterId();
        redisUtil.defaultSetKeyNoExpiration(moduleKey,JSONUtil.toJsonStr(generatorInfo));
        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(workerId, datacenterId);
        return String.valueOf(idWorker.nextId());
    }
}
