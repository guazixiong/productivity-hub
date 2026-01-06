package com.pbad.announcement.domain.dto;

import lombok.Data;

/**
 * 创建公告 DTO.
 */
@Data
public class AnnouncementCreateDTO {

    /**
     * 公告标题.
     */
    private String title;

    /**
     * 公告内容（纯文本）.
     */
    private String content;

    /**
     * 公告富文本内容（HTML）.
     */
    private String richContent;

    /**
     * 点击后跳转路径.
     */
    private String link;

    /**
     * 公告类型：NORMAL-普通，URGENT-紧急.
     */
    private String type;

    /**
     * 优先级（数字越大优先级越高）.
     */
    private Integer priority;

    /**
     * 推送策略：IMMEDIATE-立即推送，LOGIN-登录时推送.
     */
    private String pushStrategy;

    /**
     * 是否需要确认（0-否，1-是）.
     */
    private Integer requireConfirm;

    /**
     * 生效时间.
     */
    private String effectiveTime;

    /**
     * 失效时间.
     */
    private String expireTime;

    /**
     * 定时发布时间.
     */
    private String scheduledTime;
}

