package com.pbad.codegenerator.domain.vo;

import lombok.Data;

/**
 * 数据库配置VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class DatabaseConfigVO {
    /**
     * 配置ID
     */
    private String id;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 数据库类型
     */
    private String type;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 数据库名
     */
    private String database;

    /**
     * Schema
     */
    private String schema;

    /**
     * 用户名
     */
    private String username;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;
}

