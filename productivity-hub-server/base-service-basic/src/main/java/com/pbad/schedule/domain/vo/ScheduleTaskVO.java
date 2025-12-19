package com.pbad.schedule.domain.vo;

import lombok.Data;

/**
 * 定时任务管理 VO.
 */
@Data
public class ScheduleTaskVO {

    /**
     * 任务唯一标识（前后端约定的 ID）
     */
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务说明
     */
    private String description;

    /**
     * Cron 表达式或触发规则说明
     */
    private String cron;

    /**
     * 是否启用
     */
    private boolean enabled;
}


