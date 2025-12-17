package com.pbad.config.domain.po;

import lombok.Data;

/**
 * 配置项实体类（PO）.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class ConfigItemPO {
    /**
     * 配置ID
     */
    private String id;

    /**
     * 所属模块（auth, sendgrid, dingtalk, agents, home）
     */
    private String module;

    /**
     * 配置键名（支持点号分隔）
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 创建时间（YYYY-MM-DD HH:mm）
     */
    private String createdAt;

    /**
     * 更新时间（YYYY-MM-DD HH:mm）
     */
    private String updatedAt;

    /**
     * 更新人
     */
    private String updatedBy;
}

