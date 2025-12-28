package com.pbad.health.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 饮水目标进度视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterTargetProgressVO {
    /**
     * 目标饮水量（毫升）
     */
    @JsonProperty("dailyTargetMl")
    private Integer targetMl;

    /**
     * 已饮水量（毫升）
     */
    @JsonProperty("totalIntakeMl")
    private Integer consumedMl;

    /**
     * 剩余需饮水量（毫升）
     */
    @JsonProperty("remainingMl")
    private Integer remainingMl;

    /**
     * 完成进度百分比（0-100）
     */
    @JsonProperty("progressPercent")
    private Double progressPercentage;

    /**
     * 是否达标（true-已达标，false-未达标）
     */
    @JsonProperty("isAchieved")
    private Boolean isAchieved;

    /**
     * 查询日期（yyyy-MM-dd）
     */
    @JsonProperty("date")
    private String queryDate;

    /**
     * 饮水次数
     */
    @JsonProperty("intakeCount")
    private Integer intakeCount;
}

