package com.pbad.codegenerator.domain.vo;

import lombok.Data;

/**
 * 表字段VO.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class TableColumnVO {
    /**
     * 字段名
     */
    private String name;

    /**
     * 数据类型
     */
    private String type;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 是否可空
     */
    private Boolean nullable;

    /**
     * 是否主键
     */
    private Boolean primaryKey;

    /**
     * 是否自增
     */
    private Boolean autoIncrement;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 注释
     */
    private String comment;

    /**
     * Java类型
     */
    private String javaType;

    /**
     * Java字段名
     */
    private String javaField;
}

