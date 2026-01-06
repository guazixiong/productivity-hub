package com.pbad.config.domain.dto;

import lombok.Data;

/**
 * 更新配置项请求 DTO.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class ConfigUpdateDTO {
    /**
     * 配置ID
     */
    private String id;

    /**
     * 配置值
     */
    private String value;

    /**
     * 配置描述（可选）
     */
    private String description;
}

