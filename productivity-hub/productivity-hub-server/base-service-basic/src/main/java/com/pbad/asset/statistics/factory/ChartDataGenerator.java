package com.pbad.asset.statistics.factory;

import com.pbad.asset.domain.dto.StatisticsQueryDTO;

/**
 * 图表数据生成器接口.
 *
 * <p>通过工厂模式为不同类型的统计图表生成对应的数据结构。</p>
 *
 * @param <T> 图表数据类型（如 StatisticsTrendVO、List<CategoryStatisticsVO> 等）
 */
public interface ChartDataGenerator<T> {

    /**
     * 支持的图表类型.
     *
     * @return 图表类型
     */
    ChartType getType();

    /**
     * 生成图表数据.
     *
     * @param userId 当前用户ID
     * @param query  统计查询条件
     * @return 图表数据
     */
    T generate(String userId, StatisticsQueryDTO query);
}


