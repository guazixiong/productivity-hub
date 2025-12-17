package com.pbad.codegenerator.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 代码生成请求DTO.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class GenerateCodeRequestDTO {
    /**
     * 公司模板ID
     */
    private String companyTemplateId;

    /**
     * 表信息
     */
    private TableInfoDTO tableInfo;

    /**
     * 字段映射列表
     */
    private List<FieldMappingDTO> fieldMappings;

    /**
     * 选中的模板ID列表
     */
    private List<String> selectedTemplateIds;
}

