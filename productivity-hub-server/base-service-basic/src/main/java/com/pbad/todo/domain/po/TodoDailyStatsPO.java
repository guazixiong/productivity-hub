package com.pbad.todo.domain.po;

import lombok.Data;

/**
 * 按日聚合统计 PO.
 */
@Data
public class TodoDailyStatsPO {
    private String date;
    private Long completedTasks;
    private Long durationMs;
}

