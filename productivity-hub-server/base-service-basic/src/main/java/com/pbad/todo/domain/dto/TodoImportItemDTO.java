package com.pbad.todo.domain.dto;

import lombok.Data;

/**
 * 单条待办导入记录.
 */
@Data
public class TodoImportItemDTO {
    /**
     * 目标模块名称，若不存在会自动创建。
     */
    private String moduleName;

    /**
     * 任务标题（必填）。
     */
    private String title;

    /**
     * 任务描述（可选）。
     */
    private String description;

    /**
     * 优先级（P0-P3，可选）。
     */
    private String priority;

    /**
     * 标签列表。
     */
    private java.util.List<String> tags;

    /**
     * 截止日期（YYYY-MM-DD，可选）。
     */
    private String dueDate;
}

