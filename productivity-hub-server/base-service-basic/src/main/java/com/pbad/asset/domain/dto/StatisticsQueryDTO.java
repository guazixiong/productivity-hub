package com.pbad.asset.domain.dto;

import lombok.Data;

/**
 * 统计查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class StatisticsQueryDTO {
    /**
     * 统计周期（WEEK、MONTH、YEAR、ALL，默认WEEK）
     */
    private String period = "WEEK";

    /**
     * 开始日期（yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 结束日期（yyyy-MM-dd）
     */
    private String endDate;
}

