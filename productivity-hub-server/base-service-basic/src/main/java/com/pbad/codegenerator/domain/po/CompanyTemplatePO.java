package com.pbad.codegenerator.domain.po;

import lombok.Data;

/**
 * 公司代码模板实体类（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class CompanyTemplatePO {
    /**
     * 模板ID
     */
    private String id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 基础包名
     */
    private String basePackage;

    /**
     * 作者
     */
    private String author;

    /**
     * 模板内容（JSON格式，包含文件模板列表）
     */
    private String templatesJson;

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

