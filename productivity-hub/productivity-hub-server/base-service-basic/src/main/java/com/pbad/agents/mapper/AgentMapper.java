package com.pbad.agents.mapper;

import com.pbad.agents.domain.po.AgentPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体 Mapper 接口.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface AgentMapper {

    /**
     * 查询所有智能体
     *
     * @return 智能体列表
     */
    List<AgentPO> selectAll();

    /**
     * 根据ID查询智能体
     *
     * @param id 智能体ID
     * @return 智能体
     */
    AgentPO selectById(@Param("id") String id);
}

