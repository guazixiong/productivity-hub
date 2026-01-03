package com.pbad.codegenerator.domain.dto;

import lombok.Data;

/**
 * 文件模板DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class FileTemplateDTO {
    /**
     * 模板ID
     */
    private String id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板类型（controller, service, serviceImpl, mapper, mapperXml, entity, dto, vo, custom）
     */
    private String type;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 文件名模式
     */
    private String fileNamePattern;

    /**
     * 是否启用
     */
    private Boolean enabled;
}

