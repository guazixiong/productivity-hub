package com.pbad.todo.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 任务创建 DTO.
 */
@Data
public class TodoTaskCreateDTO {
    private String title;
    private String description;
    private String moduleId;
    private String priority;
    private List<String> tags;
    /**
     * 截止日期，格式：yyyy-MM-dd
     */
    private String dueDate;
}

