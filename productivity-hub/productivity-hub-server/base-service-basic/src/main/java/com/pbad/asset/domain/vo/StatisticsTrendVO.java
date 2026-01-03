package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 统计趋势视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class StatisticsTrendVO {
    /**
     * 日期列表
     */
    private List<String> dates;

    /**
     * 数值列表
     */
    private List<BigDecimal> values;
}

