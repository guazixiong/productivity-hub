package com.pbad.generator.mapper;

import com.pbad.generator.domain.po.IdGeneratorInfoPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * id生成信息mapper接口.
 *
 * @author: pangdi
 * @date: 2023/9/8 11:28
 * @version: 1.0
 */
@Mapper
public interface IdGeneratorMapper {

    /**
     * 通过模块key获取id生成信息.
     *
     * @param idGeneratorInfoPO 查询参数
     * @return id生成信息
     */
    List<IdGeneratorInfoPO> selectByModuleKey(IdGeneratorInfoPO idGeneratorInfoPO);
}
