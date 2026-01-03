package com.pbad.health.domain.dto;

import lombok.Data;

/**
 * 训练计划查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class TrainingPlanQueryDTO {
    /**
     * 计划状态（可选，ACTIVE/COMPLETED/PAUSED/CANCELLED）
     */
    private String status;

    /**
     * 计划类型（可选）
     */
    private String planType;
}

