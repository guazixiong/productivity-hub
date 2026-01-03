package com.pbad.todo.domain.dto;

import lombok.Data;

/**
 * 模块创建 DTO.
 */
@Data
public class TodoModuleCreateDTO {
    private String name;
    private String description;
    private Integer sortOrder;
    private String status;
}

