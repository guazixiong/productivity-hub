package com.pbad.todo.domain.dto;

import lombok.Data;

/**
 * 模块更新 DTO.
 */
@Data
public class TodoModuleUpdateDTO {
    private String id;
    private String name;
    private String description;
    private Integer sortOrder;
    private String status;
}

