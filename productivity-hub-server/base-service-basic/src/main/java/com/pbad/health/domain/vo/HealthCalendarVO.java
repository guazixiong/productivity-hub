package com.pbad.health.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 健康数据日历VO.
 * 关联需求：REQ-HEALTH-004
 * 关联接口：API-REQ-HEALTH-004-02
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HealthCalendarVO {

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 日期数据列表
     */
    private List<CalendarDay> days;

    @Data
    public static class CalendarDay {
        /**
         * 日期（yyyy-MM-dd格式）
         */
        private String date;

        /**
         * 是否有运动记录
         */
        private Boolean hasExercise;

        /**
         * 是否有饮水记录
         */
        private Boolean hasWater;

        /**
         * 是否有体重记录
         */
        private Boolean hasWeight;
    }
}

