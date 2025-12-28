package com.pbad.codegenerator.domain.dto;

import lombok.Data;

/**
 * 解析表结构请求DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ParseTableRequestDTO {
    /**
     * 数据库配置ID
     */
    private String databaseConfigId;

    /**
     * 表名（可选，不传则解析所有表）
     */
    private String tableName;
}

