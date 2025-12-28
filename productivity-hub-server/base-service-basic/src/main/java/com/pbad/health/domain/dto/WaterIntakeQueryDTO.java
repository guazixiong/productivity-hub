package com.pbad.health.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 饮水记录查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterIntakeQueryDTO {
    /**
     * 页码（默认：1）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数（默认：20）
     */
    private Integer pageSize = 20;

    /**
     * 饮水类型筛选（可选，支持多个）
     */
    private List<String> waterTypes;

    /**
     * 开始日期（可选，格式：yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 结束日期（可选，格式：yyyy-MM-dd）
     */
    private String endDate;

    /**
     * 排序字段（可选，默认：intakeDate）
     * 可选值：intakeDate、intakeTime、volumeMl
     */
    private String sortField;

    /**
     * 排序方向（可选，默认：DESC）
     * 可选值：ASC、DESC
     */
    private String sortOrder;
}

