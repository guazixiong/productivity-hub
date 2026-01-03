import { request } from './http'
import type {
  StatisticsQueryParams,
  StatisticsOverview,
  StatisticsTrend,
  AssetDistribution,
  CategoryStatistics,
  AssetStatistics,
  AssetCardStatistics,
  WishlistCardStatistics,
} from '@/types/statistics'

/**
 * 统计分析 API
 */
export const statisticsApi = {
  /**
   * 获取统计概览
   */
  getOverview: (params?: StatisticsQueryParams) =>
    request<StatisticsOverview>({
      url: '/api/statistics/overview',
      method: 'GET',
      params,
    }),

  /**
   * 获取日均趋势图表数据
   */
  getDailyAverageTrend: (params?: StatisticsQueryParams) =>
    request<StatisticsTrend>({
      url: '/api/statistics/daily-average-trend',
      method: 'GET',
      params,
    }),

  /**
   * 获取资产总额趋势图表数据
   */
  getTotalValueTrend: (params?: StatisticsQueryParams) =>
    request<StatisticsTrend>({
      url: '/api/statistics/total-value-trend',
      method: 'GET',
      params,
    }),

  /**
   * 获取资产分布图表数据
   */
  getAssetDistribution: (params?: StatisticsQueryParams) =>
    request<AssetDistribution[]>({
      url: '/api/statistics/asset-distribution',
      method: 'GET',
      params,
    }),

  /**
   * 获取分类统计图表数据
   */
  getCategoryStatistics: (params?: StatisticsQueryParams) =>
    request<CategoryStatistics[]>({
      url: '/api/statistics/category-statistics',
      method: 'GET',
      params,
    }),

  /**
   * 获取资产统计信息（基础统计）
   */
  getAssetStatistics: () =>
    request<AssetStatistics>({
      url: '/api/asset/statistics',
      method: 'GET',
    }),

  /**
   * 获取资产卡片统计信息
   */
  getAssetCardStatistics: (params?: StatisticsQueryParams) =>
    request<AssetCardStatistics>({
      url: '/api/statistics/asset-card',
      method: 'GET',
      params,
    }),

  /**
   * 获取心愿单卡片统计信息
   */
  getWishlistCardStatistics: () =>
    request<WishlistCardStatistics>({
      url: '/api/statistics/wishlist-card',
      method: 'GET',
    }),

}

