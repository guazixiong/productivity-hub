package com.pbad.config.domain.dto;

import lombok.Data;

/**
 * 创建或更新配置项请求 DTO（通过 module 和 key）.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class ConfigCreateOrUpdateDTO {
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
     * 配置描述（可选）
     */
    private String description;
}

