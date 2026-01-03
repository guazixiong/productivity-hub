package com.pbad.health.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 饮水目标配置持久化对象（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HealthWaterTargetPO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID（唯一索引，每个用户只能有一条记录）
     */
    private String userId;

    /**
     * 每日目标饮水量（单位：毫升，默认：2000，范围：500-10000）
     */
    private Integer dailyTargetMl;

    /**
     * 是否启用提醒（0-否，1-是，默认：0）
     */
    private Integer reminderEnabled;

    /**
     * 提醒时间间隔（JSON数组格式，如：["09:00", "12:00", "15:00", "18:00"]）
     */
    private String reminderIntervals;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

