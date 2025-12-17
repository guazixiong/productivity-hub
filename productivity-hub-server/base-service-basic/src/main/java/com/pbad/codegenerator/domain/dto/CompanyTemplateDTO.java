package com.pbad.codegenerator.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 公司模板DTO.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class CompanyTemplateDTO {
    /**
     * 模板ID（更新时使用）
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
    private List<FileTemplateDTO> templates;
}

