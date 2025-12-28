package com.pbad.tools.service;

import com.pbad.tools.domain.dto.ToolTrackDTO;
import com.pbad.tools.domain.vo.ToolStatVO;

import java.util.List;

/**
 * 工具服务接口.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface ToolService {

    /**
     * 获取工具使用统计
     *
     * @return 工具统计列表（按点击次数降序）
     */
    List<ToolStatVO> getToolStats();

    /**
     * 记录工具使用
     *
     * @param trackDTO 记录请求
     * @return 更新后的完整统计列表（按点击次数降序）
     */
    List<ToolStatVO> trackToolUsage(ToolTrackDTO trackDTO);
}

