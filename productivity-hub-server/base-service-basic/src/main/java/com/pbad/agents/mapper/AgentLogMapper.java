package com.pbad.agents.mapper;

import com.pbad.agents.domain.po.AgentLogPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体调用日志 Mapper 接口.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface AgentLogMapper {

    /**
     * 插入日志
     *
     * @param log 日志
     * @return 插入行数
     */
    int insert(AgentLogPO log);

    /**
     * 更新日志
     *
     * @param log 日志
     * @return 更新行数
     */
    int update(AgentLogPO log);

    /**
     * 分页查询日志（按时间倒序）
     *
     * @param offset   偏移量
     * @param pageSize 每页条数
     * @return 日志列表
     */
    List<AgentLogPO> selectPage(@Param("userId") String userId,
                                @Param("offset") int offset,
                                @Param("pageSize") int pageSize);

    /**
     * 统计日志总数
     *
     * @return 总记录数
     */
    long countAll(@Param("userId") String userId);
}

