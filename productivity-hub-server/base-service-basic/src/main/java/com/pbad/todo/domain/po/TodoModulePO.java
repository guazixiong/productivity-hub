package com.pbad.todo.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 模块持久化对象.
 */
@Data
public class TodoModulePO {
    private String id;
    private String userId;
    private String name;
    private String description;
    private String status;
    private Integer sortOrder;
    private Date createdAt;
    private Date updatedAt;
}

