package com.pbad.health.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 健康数据概览VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HealthOverviewVO {

    /**
     * 运动概览
     */
    private ExerciseOverview exercise;

    /**
     * 饮水概览
     */
    private WaterOverview water;

    /**
     * 体重概览
     */
    private WeightOverview weight;

    /**
     * 数据完成度
     */
    private DataCompleteness dataCompleteness;

    @Data
    public static class ExerciseOverview {
        private Integer todayDuration;
        private Integer todayCount;
        private Integer todayCalories;
        private Integer weekDuration;
        private Integer weekCount;
        private Integer weekCalories;
        private Integer monthDuration;
        private Integer monthCount;
        private Integer monthCalories;
    }

    @Data
    public static class WaterOverview {
        private Integer todayIntakeMl;
        private Integer todayTargetMl;
        private Double todayProgress;
        private Boolean todayIsAchieved;
        private Integer weekIntakeMl;
        private Integer weekAchievementDays;
        private Integer monthIntakeMl;
        private Integer monthAchievementDays;
    }

    @Data
    public static class WeightOverview {
        private BigDecimal latestWeight;
        private BigDecimal targetWeight;
        private BigDecimal gapFromTarget;
        private BigDecimal weekChange;
        private BigDecimal monthChange;
    }

    @Data
    public static class DataCompleteness {
        private Integer exerciseDays;
        private Integer waterDays;
        private Integer weightDays;
        private Integer totalDays;
        private Double completenessPercent;
    }
}

