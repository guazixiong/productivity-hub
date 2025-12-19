package com.pbad.todo.domain.vo;

import lombok.Data;

/**
 * 模块视图对象.
 */
@Data
public class TodoModuleVO {
    private String id;
    private String name;
    private String description;
    private String status;
    private Integer sortOrder;

    private Long totalTasks;
    private Long completedTasks;
    private Long totalDurationMs;
}

