package com.pbad.health.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 饮水目标视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterTargetVO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 每日目标饮水量（毫升）
     */
    private Integer dailyTargetMl;

    /**
     * 是否启用提醒
     */
    private Boolean reminderEnabled;

    /**
     * 提醒时间间隔（JSON数组）
     */
    private List<String> reminderIntervals;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}

