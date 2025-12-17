package com.pbad.agents.domain.vo;

import lombok.Data;

/**
 * 智能体调用日志 VO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class AgentLogVO {
    /**
     * 日志ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 智能体ID
     */
    private String agentId;

    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 状态
     */
    private String status;

    /**
     * 耗时（毫秒）
     */
    private Integer duration;

    /**
     * 创建时间（YYYY-MM-DD HH:mm）
     */
    private String createdAt;

    /**
     * 输入内容（字符串形式）
     */
    private String input;

    /**
     * 输出内容
     */
    private String output;
}

