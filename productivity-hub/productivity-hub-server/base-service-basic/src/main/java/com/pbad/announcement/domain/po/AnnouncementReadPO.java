package com.pbad.announcement.domain.po;

import lombok.Data;

/**
 * 公告阅读记录实体（持久化对象）.
 */
@Data
public class AnnouncementReadPO {

    /**
     * 主键 ID.
     */
    private String id;

    /**
     * 公告 ID.
     */
    private String announcementId;

    /**
     * 用户 ID.
     */
    private String userId;

    /**
     * 阅读时间.
     */
    private String readAt;
}

