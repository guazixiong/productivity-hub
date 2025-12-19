package com.pbad.notifications.domain.po;

import lombok.Data;

/**
 * 通知消息实体（持久化对象）.
 */
@Data
public class NotificationPO {

    /**
     * 主键 ID.
     */
    private String id;

    /**
     * 用户 ID（接收者）.
     */
    private String userId;

    /**
     * 通知标题.
     */
    private String title;

    /**
     * 通知内容（简要文案）.
     */
    private String content;

    /**
     * 点击后前端跳转路径.
     */
    private String link;

    /**
     * 额外数据，JSON 字符串.
     */
    private String extraData;

    /**
     * 是否已读：0=未读，1=已读.
     */
    private Integer readFlag;

    /**
     * 创建时间.
     */
    private String createdAt;

    /**
     * 修改时间.
     */
    private String updatedAt;
}


