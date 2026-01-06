package com.pbad.asset.service;

import com.pbad.asset.domain.dto.StatisticsQueryDTO;
import com.pbad.asset.domain.vo.AssetCardStatisticsVO;
import com.pbad.asset.domain.vo.AssetDistributionVO;
import com.pbad.asset.domain.vo.AssetStatisticsVO;
import com.pbad.asset.domain.vo.CategoryStatisticsVO;
import com.pbad.asset.domain.vo.StatisticsOverviewVO;
import com.pbad.asset.domain.vo.StatisticsTrendVO;

import java.util.List;

/**
 * 资产统计服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetStatisticsService {

    /**
     * 获取资产统计信息
     *
     * @return 统计信息
     */
    AssetStatisticsVO getStatistics();

    /**
     * 获取统计概览
     *
     * @param query 查询条件
     * @return 统计概览
     */
    StatisticsOverviewVO getOverview(StatisticsQueryDTO query);

    /**
     * 获取日均趋势图表数据
     *
     * @param query 查询条件
     * @return 趋势数据
     */
    StatisticsTrendVO getDailyAverageTrend(StatisticsQueryDTO query);

    /**
     * 获取资产总额趋势图表数据
     *
     * @param query 查询条件
     * @return 趋势数据
     */
    StatisticsTrendVO getTotalValueTrend(StatisticsQueryDTO query);

    /**
     * 获取资产分布图表数据
     *
     * @param query 查询条件
     * @return 分布数据
     */
    List<AssetDistributionVO> getAssetDistribution(StatisticsQueryDTO query);

    /**
     * 获取分类统计图表数据
     *
     * @param query 查询条件
     * @return 分类统计数据
     */
    List<CategoryStatisticsVO> getCategoryStatistics(StatisticsQueryDTO query);

    /**
     * 获取资产卡片统计信息
     *
     * @param query 查询条件
     * @return 资产卡片统计数据
     */
    AssetCardStatisticsVO getAssetCardStatistics(StatisticsQueryDTO query);

    /**
     * 清理统计相关缓存.
     *
     * 该方法用于配合资产事件观察者，在资产发生变更时使统计结果失效，
     * 目前实现为占位方法，后续可接入实际缓存组件。
     */
    default void clearCache() {
        // 默认空实现，避免现有调用方受影响
    }
}

