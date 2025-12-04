package com.pbad.agents.domain.dto;

import lombok.Data;

import java.util.Map;

/**
 * 调用智能体请求 DTO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class AgentInvokeDTO {
    /**
     * 智能体ID
     */
    private String agentId;

    /**
     * 输入数据
     */
    private Map<String, Object> input;

    /**
     * 调用模式（sync, async）
     */
    private String mode;

    /**
     * 上下文（可选，JSON 字符串）
     */
    private String context;
}

