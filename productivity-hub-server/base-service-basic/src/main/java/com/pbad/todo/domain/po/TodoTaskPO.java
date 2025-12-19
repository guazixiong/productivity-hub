package com.pbad.todo.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 任务持久化对象.
 */
@Data
public class TodoTaskPO {
    private String id;
    private String userId;
    private String moduleId;
    private String title;
    private String description;
    private String priority;
    private String tagsJson;
    private String status;
    /**
     * 截止日期（仅日期，不含时间）
     */
    private Date dueDate;
    private Date startedAt;
    private Date endedAt;
    private Date activeStartAt;
    private Date pauseStartedAt;
    private Long durationMs;
    private Long pausedDurationMs;
    private Date lastEventAt;
    private Date createdAt;
    private Date updatedAt;
}

