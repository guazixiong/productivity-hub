package com.pbad.todo.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 行为事件持久化对象.
 */
@Data
public class TodoEventPO {
    private String id;
    private String userId;
    private String todoId;
    private String eventType;
    private Date occurredAt;
    private String payload;
    private Date createdAt;
}

