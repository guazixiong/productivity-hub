package com.pbad.health.domain.dto;

import lombok.Data;

/**
 * 饮水记录创建DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterIntakeCreateDTO {
    /**
     * 饮水日期（可选，格式：yyyy-MM-dd，默认当天）
     */
    private String intakeDate;

    /**
     * 饮水时间（可选，格式：HH:mm，默认当前时间）
     */
    private String intakeTime;

    /**
     * 饮水类型（必填，枚举值：白开水、矿泉水、纯净水、茶水、咖啡、果汁、运动饮料、其他）
     */
    private String waterType;

    /**
     * 饮水量（必填，单位：毫升，范围：1-5000）
     */
    private Integer volumeMl;

    /**
     * 备注信息（可选）
     */
    private String notes;
}

