package com.pbad.codegenerator.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * 公司模板VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class CompanyTemplateVO {
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
     * 文件模板列表
     */
    private List<FileTemplateVO> templates;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;
}

