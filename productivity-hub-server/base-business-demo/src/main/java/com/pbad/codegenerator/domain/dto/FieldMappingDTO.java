package com.pbad.codegenerator.domain.dto;

import lombok.Data;

/**
 * 字段映射DTO.
 */
@Data
public class FieldMappingDTO {
    /**
     * 数据库字段名
     */
    private String dbField;

    /**
     * Java字段名
     */
    private String javaField;

    /**
     * Java类型
     */
    private String javaType;

    /**
     * 字段备注
     */
    private String comment;
}


