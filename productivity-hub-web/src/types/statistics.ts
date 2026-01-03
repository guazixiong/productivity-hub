/**
 * 统计分析相关类型定义
 */

/**
 * 统计查询参数
 */
export interface StatisticsQueryParams {
  period?: 'MONTH' | 'YEAR' | 'CUSTOM' | 'ALL'
  startDate?: string
  endDate?: string
}

/**
 * 统计概览视图对象
 */
export interface StatisticsOverview {
  purchaseAmount: number
  purchaseCount: number
  totalDailyAverage: number
}

/**
 * 统计趋势视图对象
 */
export interface StatisticsTrend {
  dates: string[]
  values: number[]
}

/**
 * 资产分布视图对象
 */
export interface AssetDistribution {
  categoryName: string
  value: number
  percentage: number
}

/**
 * 分类统计视图对象
 */
export interface CategoryStatistics {
  categoryName: string
  count: number
  totalValue: number
}

/**
 * 资产统计信息（基础统计）
 */
export interface AssetStatistics {
  totalAssets: number
  inServiceAssets: number
  retiredAssets: number
  totalValue: number
}

/**
 * 资产卡片统计信息
 */
export interface AssetCardStatistics {
  totalPrice: number // 资产价格（总购入金额）
  totalCount: number // 资产总数
  totalAdditionalFees: number // 总资产附加费用
  totalValue: number // 资产总价值(总购入金额 + 附加费用)
  dailyAverageTotal: number // 日均总额 = SUM((每件资产价值 + 额外费用) / 使用天数)
}

/**
 * 心愿单卡片统计信息
 */
export interface WishlistCardStatistics {
  totalCount: number // 心愿总数(不限时间)
  totalValue: number // 心愿总价值(不限时间)
  addedCount: number // 心愿追加个数
  achievedCount: number // 心愿实现个数
}

/**
 * ECharts图表配置
 */
export interface ChartOption {
  option: Record<string, any>
}

