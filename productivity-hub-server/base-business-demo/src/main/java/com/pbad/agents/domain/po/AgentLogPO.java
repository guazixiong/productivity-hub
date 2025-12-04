package com.pbad.agents.domain.po;

import lombok.Data;

/**
 * 智能体调用日志实体类（PO）.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class AgentLogPO {
    /**
     * 日志ID
     */
    private String id;

    /**
     * 智能体ID
     */
    private String agentId;

    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 状态（success, failed, running）
     */
    private String status;

    /**
     * 耗时（毫秒）
     */
    private Integer duration;

    /**
     * 输入内容（JSON）
     */
    private String inputData;

    /**
     * 输出内容
     */
    private String outputData;

    /**
     * 创建时间（YYYY-MM-DD HH:mm）
     */
    private String createdAt;
}

