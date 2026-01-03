package com.pbad.asset.statistics.factory;

/**
 * 统计图表类型枚举.
 *
 * <p>与 REQ-003 中的统计图表一一对应：</p>
 * <ul>
 *     <li>日均趋势图表（折线图）</li>
 *     <li>资产总额趋势图表（折线图）</li>
 *     <li>资产分布图表（饼图）</li>
 *     <li>分类统计图表（柱状图）</li>
 * </ul>
 */
public enum ChartType {

    /**
     * 日均趋势图表
     */
    DAILY_AVERAGE_TREND,

    /**
     * 资产总额趋势图表
     */
    TOTAL_VALUE_TREND,

    /**
     * 资产分布图表
     */
    ASSET_DISTRIBUTION,

    /**
     * 分类统计图表
     */
    CATEGORY_STATISTICS
}


