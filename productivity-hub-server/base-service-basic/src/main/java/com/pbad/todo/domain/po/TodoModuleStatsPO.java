package com.pbad.todo.domain.po;

import lombok.Data;

/**
 * 模块聚合统计 PO.
 */
@Data
public class TodoModuleStatsPO {
    private String moduleId;
    private String moduleName;
    private Long totalTasks;
    private Long completedTasks;
    private Long durationMs;
}

