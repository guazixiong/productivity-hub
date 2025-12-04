package com.pbad.tools.mapper;

import com.pbad.tools.domain.po.ToolStatPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工具统计 Mapper 接口.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface ToolStatMapper {

    /**
     * 查询所有工具统计（按点击次数降序）
     *
     * @return 工具统计列表
     */
    List<ToolStatPO> selectAllOrderByClicks();

    /**
     * 根据工具ID查询
     *
     * @param toolId 工具ID
     * @return 工具统计
     */
    ToolStatPO selectById(@Param("toolId") String toolId);

    /**
     * 插入工具统计
     *
     * @param toolStat 工具统计
     * @return 插入行数
     */
    int insert(ToolStatPO toolStat);

    /**
     * 更新点击次数
     *
     * @param toolId 工具ID
     * @return 更新行数
     */
    int incrementClicks(@Param("toolId") String toolId);
}

