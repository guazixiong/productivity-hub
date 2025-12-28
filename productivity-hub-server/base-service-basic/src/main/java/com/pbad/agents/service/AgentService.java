package com.pbad.agents.service;

import com.pbad.agents.domain.dto.AgentInvokeDTO;
import com.pbad.agents.domain.vo.AgentInvokeResponseVO;
import com.pbad.agents.domain.vo.AgentLogVO;
import com.pbad.agents.domain.vo.AgentSummaryVO;
import common.core.domain.PageResult;

import java.util.List;

/**
 * 智能体服务接口.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface AgentService {

    /**
     * 获取智能体列表
     *
     * @return 智能体列表
     */
    List<AgentSummaryVO> getAgentList();

    /**
     * 调用智能体
     *
     * @param invokeDTO 调用请求
     * @return 调用响应
     */
    AgentInvokeResponseVO invokeAgent(AgentInvokeDTO invokeDTO, String userId);

    /**
     * 获取调用历史（分页，按时间倒序）
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<AgentLogVO> getAgentLogs(int pageNum, int pageSize, String userId);
}

