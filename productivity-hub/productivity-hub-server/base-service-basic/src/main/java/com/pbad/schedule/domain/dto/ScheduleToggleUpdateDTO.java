package com.pbad.schedule.domain.dto;

import lombok.Data;

/**
 * 定时任务开关更新 DTO.
 */
@Data
public class ScheduleToggleUpdateDTO {

    /**
     * 任务 ID
     */
    private String id;

    /**
     * 是否启用
     */
    private Boolean enabled;
}


