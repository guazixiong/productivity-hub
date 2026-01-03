package com.pbad.health.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 训练计划视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class TrainingPlanVO {
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
     * 计划类型
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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /**
     * 关联的运动记录数（统计字段）
     */
    private Long exerciseRecordCount;

    /**
     * 总卡路里（统计字段，可选）
     */
    private Integer totalCalories;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}

