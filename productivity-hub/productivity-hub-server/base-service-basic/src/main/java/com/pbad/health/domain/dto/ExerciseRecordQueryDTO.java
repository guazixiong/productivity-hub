package com.pbad.health.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 运动记录查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExerciseRecordQueryDTO {
    /**
     * 页码（默认1）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数（默认20，最大100）
     */
    private Integer pageSize = 20;

    /**
     * 运动类型筛选（多选）
     */
    private List<String> exerciseType;

    /**
     * 开始日期（格式：yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 结束日期（格式：yyyy-MM-dd）
     */
    private String endDate;

    /**
     * 训练计划ID
     */
    private String trainingPlanId;

    /**
     * 排序字段（exerciseDate/durationMinutes/caloriesBurned），默认exerciseDate
     */
    private String sortField = "exerciseDate";

    /**
     * 排序方向（asc/desc），默认desc
     */
    private String sortOrder = "desc";
}

