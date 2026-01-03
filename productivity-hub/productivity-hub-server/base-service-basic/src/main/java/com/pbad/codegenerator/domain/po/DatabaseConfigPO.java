package com.pbad.codegenerator.domain.po;

import lombok.Data;

/**
 * 数据库配置实体类（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class DatabaseConfigPO {
    /**
     * 配置ID
     */
    private String id;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 数据库类型（mysql, postgresql, oracle, sqlserver, sqlite）
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
     * Schema（PostgreSQL等使用）
     */
    private String schema;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;
}

