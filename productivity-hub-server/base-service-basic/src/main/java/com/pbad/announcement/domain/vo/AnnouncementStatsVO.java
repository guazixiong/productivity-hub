package com.pbad.announcement.domain.vo;

import lombok.Data;

/**
 * 公告统计 VO.
 */
@Data
public class AnnouncementStatsVO {

    /**
     * 公告 ID.
     */
    private String announcementId;

    /**
     * 总用户数.
     */
    private Long totalUsers;

    /**
     * 已读用户数.
     */
    private Long readUsers;

    /**
     * 阅读率（百分比）.
     */
    private Double readRate;
}

