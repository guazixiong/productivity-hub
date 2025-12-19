package com.pbad.notifications.domain.vo;

import lombok.Data;

/**
 * 通知消息 VO（返回给前端）.
 */
@Data
public class NotificationVO {

    private String id;

    private String title;

    private String content;

    private String link;

    private Object extra;

    private Boolean read;

    private String createdAt;
}


