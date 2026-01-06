package com.pbad.health.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户身体信息创建或更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class UserBodyInfoCreateOrUpdateDTO {
    /**
     * 身高（可选，单位：厘米，范围：100.00-250.00）
     */
    private BigDecimal heightCm;

    /**
     * 出生日期（可选，格式：yyyy-MM-dd）
     */
    private String birthDate;

    /**
     * 性别（可选，枚举值：MALE/FEMALE/OTHER）
     */
    private String gender;

    /**
     * 目标体重（可选，单位：公斤，范围：20.00-300.00）
     */
    private BigDecimal targetWeightKg;

    /**
     * Resend邮箱地址（可选，邮箱格式）
     */
    private String resendEmail;
}

