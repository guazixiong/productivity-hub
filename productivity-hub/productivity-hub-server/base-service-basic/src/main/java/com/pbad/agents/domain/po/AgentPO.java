package com.pbad.agents.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 智能体实体类（PO）.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class AgentPO {
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
     * 标签列表（JSON数组字符串）
     */
    private String tags;

    /**
     * 平均延迟（毫秒）
     */
    private Integer latencyMs;

    /**
     * 负责人/团队
     */
    private String owner;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

