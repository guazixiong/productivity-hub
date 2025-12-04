package com.pbad.agents.domain.vo;

import lombok.Data;

/**
 * 调用智能体响应 VO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class AgentInvokeResponseVO {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 状态（success, running, failed）
     */
    private String status;

    /**
     * 输出内容
     */
    private String output;

    /**
     * 开始时间（ISO 8601 格式）
     */
    private String startedAt;

    /**
     * 结束时间（ISO 8601 格式，可选）
     */
    private String finishedAt;
}

