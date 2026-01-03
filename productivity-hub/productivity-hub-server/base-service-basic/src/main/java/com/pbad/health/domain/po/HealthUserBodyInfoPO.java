package com.pbad.health.domain.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户身体信息持久化对象（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HealthUserBodyInfoPO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID（唯一索引，每个用户只能有一条记录）
     */
    private String userId;

    /**
     * 身高（单位：厘米，范围：100.00-250.00）
     */
    private BigDecimal heightCm;

    /**
     * 出生日期
     */
    private Date birthDate;

    /**
     * 性别（MALE/FEMALE/OTHER）
     */
    private String gender;

    /**
     * 目标体重（单位：公斤，范围：20.00-300.00）
     */
    private BigDecimal targetWeightKg;

    /**
     * Resend邮箱地址（用于邮件推送）
     */
    private String resendEmail;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

