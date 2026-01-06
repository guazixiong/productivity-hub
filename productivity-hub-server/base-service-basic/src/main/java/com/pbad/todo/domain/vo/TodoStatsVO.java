package com.pbad.todo.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计视图对象.
 */
@Data
public class TodoStatsVO {
    private Long totalTasks;
    private Long completedTasks;
    private Long inProgressTasks;
    private Long interruptedTasks;
    private Long totalDurationMs;

    private List<ModuleStat> moduleStats;
    private List<DailyStat> timeline;

    @Data
    public static class ModuleStat {
        private String moduleId;
        private String moduleName;
        private Long totalTasks;
        private Long completedTasks;
        private Long durationMs;
    }

    @Data
    public static class DailyStat {
        private LocalDate date;
        private Long completedTasks;
        private Long durationMs;
    }
}

