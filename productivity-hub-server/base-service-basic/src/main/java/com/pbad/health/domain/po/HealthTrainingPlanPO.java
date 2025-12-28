package com.pbad.health.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 训练计划持久化对象（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HealthTrainingPlanPO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 计划类型（减脂、增肌、塑形、耐力提升、康复训练、其他）
     */
    private String planType;

    /**
     * 目标持续天数
     */
    private Integer targetDurationDays;

    /**
     * 每日目标卡路里
     */
    private Integer targetCaloriesPerDay;

    /**
     * 计划描述
     */
    private String description;

    /**
     * 状态（ACTIVE-进行中、COMPLETED-已完成、PAUSED-已暂停、CANCELLED-已取消）
     */
    private String status;

    /**
     * 开始日期
     */
    private Date startDate;

    /**
     * 结束日期
     */
    private Date endDate;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

