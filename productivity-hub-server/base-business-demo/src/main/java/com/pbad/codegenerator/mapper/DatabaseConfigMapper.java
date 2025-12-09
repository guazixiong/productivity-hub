package com.pbad.codegenerator.mapper;

import com.pbad.codegenerator.domain.po.DatabaseConfigPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库配置 Mapper 接口.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface DatabaseConfigMapper {

    /**
     * 查询所有配置
     *
     * @return 配置列表
     */
    List<DatabaseConfigPO> selectAll();

    /**
     * 根据ID查询配置
     *
     * @param id 配置ID
     * @return 配置
     */
    DatabaseConfigPO selectById(@Param("id") String id);

    /**
     * 插入配置
     *
     * @param config 配置
     * @return 插入行数
     */
    int insert(DatabaseConfigPO config);

    /**
     * 更新配置
     *
     * @param config 配置
     * @return 更新行数
     */
    int update(DatabaseConfigPO config);

    /**
     * 删除配置
     *
     * @param id 配置ID
     * @return 删除行数
     */
    int deleteById(@Param("id") String id);
}

