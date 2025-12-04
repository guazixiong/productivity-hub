package com.pbad.agents.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 智能体摘要 VO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class AgentSummaryVO {
    /**
     * 智能体ID
     */
    private String id;

    /**
     * 智能体名称
     */
    private String name;

    /**
     * 智能体描述
     */
    private String description;

    /**
     * 版本号
     */
    private String version;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 平均延迟（毫秒）
     */
    private Integer latencyMs;

    /**
     * 负责人/团队
     */
    private String owner;
}

