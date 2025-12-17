package com.pbad.config.domain.vo;

import lombok.Data;

/**
 * 配置项 VO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class ConfigItemVO {
    /**
     * 配置ID
     */
    private String id;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 配置键名
     */
    private String key;

    /**
     * 配置值
     */
    private String value;

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

