package com.pbad.health.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 饮水记录持久化对象（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HealthWaterIntakePO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID（用于用户数据隔离）
     */
    private String userId;

    /**
     * 饮水日期
     */
    private Date intakeDate;

    /**
     * 饮水时间（精确到分钟）
     */
    private Date intakeTime;

    /**
     * 饮水类型（白开水、矿泉水、纯净水、茶水、咖啡、果汁、运动饮料、其他）
     */
    private String waterType;

    /**
     * 饮水量（单位：毫升，范围：1-5000）
     */
    private Integer volumeMl;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

