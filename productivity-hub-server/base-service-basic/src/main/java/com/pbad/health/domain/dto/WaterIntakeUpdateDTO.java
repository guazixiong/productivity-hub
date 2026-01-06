package com.pbad.health.domain.dto;

import lombok.Data;

/**
 * 饮水记录更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterIntakeUpdateDTO {
    /**
     * 饮水日期（可选，格式：yyyy-MM-dd）
     */
    private String intakeDate;

    /**
     * 饮水时间（可选，格式：HH:mm）
     */
    private String intakeTime;

    /**
     * 饮水类型（可选，枚举值：白开水、矿泉水、纯净水、茶水、咖啡、果汁、运动饮料、其他）
     */
    private String waterType;

    /**
     * 饮水量（可选，单位：毫升，范围：1-5000）
     */
    private Integer volumeMl;

    /**
     * 备注信息（可选）
     */
    private String notes;
}

