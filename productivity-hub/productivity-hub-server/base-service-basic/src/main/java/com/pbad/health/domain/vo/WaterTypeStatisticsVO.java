package com.pbad.health.domain.vo;

import lombok.Data;

/**
 * 按类型统计饮水数据VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterTypeStatisticsVO {
    /**
     * 饮水类型
     */
    private String waterType;

    /**
     * 次数
     */
    private Integer count;

    /**
     * 总饮水量（毫升）
     */
    private Integer totalIntakeMl;
}

