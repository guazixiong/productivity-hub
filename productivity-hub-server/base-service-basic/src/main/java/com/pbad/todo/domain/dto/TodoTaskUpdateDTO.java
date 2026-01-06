package com.pbad.todo.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 任务更新 DTO.
 */
@Data
public class TodoTaskUpdateDTO {
    private String id;
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

