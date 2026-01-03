package com.pbad.codegenerator.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * 表信息VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class TableInfoVO {
    /**
     * 表名
     */
    private String name;

    /**
     * 表注释
     */
    private String comment;

    /**
     * 字段列表
     */
    private List<TableColumnVO> columns;

    /**
     * Java类名
     */
    private String javaClassName;

    /**
     * Java包名
     */
    private String javaPackage;
}

