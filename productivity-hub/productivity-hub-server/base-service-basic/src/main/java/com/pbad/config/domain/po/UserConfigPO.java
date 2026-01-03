package com.pbad.config.domain.po;

import lombok.Data;

/**
 * 用户级配置项实体.
 */
@Data
public class UserConfigPO {
    /**
     * 配置ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 配置键名
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

