package com.pbad.todo.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 行为日志 VO.
 */
@Data
public class TodoEventVO {
    private String id;
    private String todoId;
    private String eventType;
    private Date occurredAt;
    private String payload;
}

