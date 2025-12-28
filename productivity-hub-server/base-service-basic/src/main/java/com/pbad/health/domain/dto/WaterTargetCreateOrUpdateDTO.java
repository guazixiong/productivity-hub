package com.pbad.health.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 饮水目标创建或更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterTargetCreateOrUpdateDTO {
    /**
     * 每日目标饮水量（必填，单位：毫升，默认：2000，范围：500-10000）
     */
    private Integer dailyTargetMl;

    /**
     * 提醒时间间隔（可选，JSON数组格式，如：["09:00", "12:00", "15:00", "18:00"]）
     */
    private List<String> reminderIntervals;
}

