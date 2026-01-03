<template>
  <div class="stat-analysis-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">资产统计分析</span>
          <div class="header-actions">
            <el-select v-model="queryParams.period" @change="handlePeriodChange" style="width: 150px">
              <el-option label="近一月" value="MONTH" />
              <el-option label="近一年" value="YEAR" />
              <el-option label="自定义时间" value="CUSTOM" />
              <el-option label="全部" value="ALL" />
            </el-select>
            <el-date-picker
              v-if="queryParams.period === 'CUSTOM'"
              v-model="customDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px; margin-left: 10px"
              @change="handleCustomDateChange"
            />
            <el-button type="primary" @click="loadAllData">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 资产统计分析卡片 -->
      <el-row :gutter="20" class="stat-cards-row">
        <!-- 资产卡片 -->
        <el-col :xs="24" :sm="24" :md="12" class="card-col">
          <el-card shadow="hover" class="main-stat-card asset-card">
            <template #header>
              <div class="card-title">
                <el-icon class="title-icon"><Box /></el-icon>
                <span>资产统计</span>
              </div>
            </template>
            <div class="stat-card-content">
              <div class="stat-item">
                <div class="stat-label">
                  资产价格
                  <el-tooltip placement="top" effect="dark">
                    <template #content>
                      <div class="tooltip-content">
                        <div class="tooltip-text">统计所有非退役资产的购入价格总和（已退役资产不计入）</div>
                      </div>
                    </template>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="stat-value">{{ formatCurrency(assetCardStats.totalPrice) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">资产总数</div>
                <div class="stat-value">{{ assetCardStats.totalCount }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">
                  总资产附加费用
                  <el-tooltip placement="top" effect="dark">
                    <template #content>
                      <div class="tooltip-content">
                        <div class="tooltip-text">所有资产（包括已退役）的附加费用总和</div>
                      </div>
                    </template>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="stat-value">{{ formatCurrency(assetCardStats.totalAdditionalFees) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">
                  资产总价值
                  <el-tooltip placement="top" effect="dark">
                    <template #content>
                      <div class="tooltip-content">
                        <div class="tooltip-text">资产总价值 = 资产价格（非退役）+ 总资产附加费用</div>
                      </div>
                    </template>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="stat-value highlight-value">{{ formatCurrency(assetCardStats.totalValue) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">
                  日均总额
                  <el-tooltip placement="top" effect="dark">
                    <template #content>
                      <div class="tooltip-content">
                        <div class="tooltip-title">计算公式</div>
                        <div class="tooltip-text">日均总额 = SUM((每件资产价值 + 额外费用) / 使用天数)</div>
                        <div class="tooltip-divider"></div>
                        <div class="tooltip-title">说明</div>
                        <div class="tooltip-item">1. 每件资产价值 = 资产购入价格 + 该资产的所有附加费用</div>
                        <div class="tooltip-item">2. 使用天数 = 根据资产的贬值策略计算（按使用日期或按使用次数）</div>
                        <div class="tooltip-item">3. 日均总额表示：所有资产的日均价格总和</div>
                      </div>
                    </template>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="stat-value highlight-daily">{{ formatCurrency(assetCardStats.dailyAverageTotal) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 心愿单卡片 -->
        <el-col :xs="24" :sm="24" :md="12" class="card-col">
          <el-card shadow="hover" class="main-stat-card wishlist-card">
            <template #header>
              <div class="card-title">
                <el-icon class="title-icon"><Star /></el-icon>
                <span>心愿单统计</span>
              </div>
            </template>
            <div class="stat-card-content">
              <div class="stat-item">
                <div class="stat-label">
                  心愿总数
                  <el-tooltip placement="top" effect="dark">
                    <template #content>
                      <div class="tooltip-content">
                        <div class="tooltip-text">不限时间，所有心愿单的总数</div>
                      </div>
                    </template>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="stat-value">{{ wishlistCardStats.totalCount }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">
                  心愿总价值
                  <el-tooltip placement="top" effect="dark">
                    <template #content>
                      <div class="tooltip-content">
                        <div class="tooltip-text">不限时间，所有心愿单的价格总和</div>
                      </div>
                    </template>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="stat-value">{{ formatCurrency(wishlistCardStats.totalValue) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">
                  心愿追加个数
                  <el-tooltip placement="top" effect="dark">
                    <template #content>
                      <div class="tooltip-content">
                        <div class="tooltip-text">统计所选时间范围内新增的心愿单数量</div>
                      </div>
                    </template>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="stat-value">{{ wishlistCardStats.addedCount }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">
                  心愿实现个数
                  <el-tooltip placement="top" effect="dark">
                    <template #content>
                      <div class="tooltip-content">
                        <div class="tooltip-text">统计所选时间范围内已实现的心愿单数量</div>
                      </div>
                    </template>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="stat-value highlight-achieved">{{ wishlistCardStats.achievedCount }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="chart-row">
        <!-- 日均趋势图 -->
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="section-title">日均趋势</span>
            </template>
            <div ref="dailyAverageChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 资产总额趋势图 -->
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="section-title">资产总额趋势</span>
            </template>
            <div ref="totalValueChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 资产分布饼图 -->
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="section-title">资产分布</span>
            </template>
            <div ref="distributionChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 分类统计柱状图 -->
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="section-title">分类统计</span>
            </template>
            <div ref="categoryChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, Box, Star } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/services/statisticsApi'
import { wishlistApi } from '@/services/wishlistApi'
import { assetApi } from '@/services/assetApi'
import type {
  StatisticsQueryParams,
  StatisticsOverview,
  AssetStatistics,
  StatisticsTrend,
  AssetDistribution,
  CategoryStatistics,
  AssetCardStatistics,
  WishlistCardStatistics,
} from '@/types/statistics'
import { formatCurrency } from '@/utils/format'
import dayjs from 'dayjs'

const queryParams = reactive<StatisticsQueryParams>({
  period: 'MONTH',
})

const customDateRange = ref<[string, string] | null>(null)

const loading = ref(false)
const overview = ref<StatisticsOverview>({
  purchaseAmount: 0,
  purchaseCount: 0,
  totalDailyAverage: 0,
})

const assetStatistics = ref<AssetStatistics>({
  totalAssets: 0,
  inServiceAssets: 0,
  retiredAssets: 0,
  totalValue: 0,
})

const assetCardStats = ref<AssetCardStatistics>({
  totalPrice: 0,
  totalCount: 0,
  totalAdditionalFees: 0,
  totalValue: 0,
  dailyAverageTotal: 0,
})

const wishlistCardStats = ref<WishlistCardStatistics>({
  totalCount: 0,
  totalValue: 0,
  addedCount: 0,
  achievedCount: 0,
})

// 图表引用
const dailyAverageChartRef = ref<HTMLDivElement>()
const totalValueChartRef = ref<HTMLDivElement>()
const distributionChartRef = ref<HTMLDivElement>()
const categoryChartRef = ref<HTMLDivElement>()

// 图表实例
const dailyAverageChart = ref<echarts.ECharts | null>(null)
const totalValueChart = ref<echarts.ECharts | null>(null)
const distributionChart = ref<echarts.ECharts | null>(null)
const categoryChart = ref<echarts.ECharts | null>(null)

// 计算时间范围
const getDateRange = () => {
  const now = dayjs()
  let startDate: string | undefined
  let endDate: string | undefined

  if (queryParams.period === 'MONTH') {
    // 近一月：当前月的第一天到当前日期
    startDate = now.startOf('month').format('YYYY-MM-DD')
    endDate = now.format('YYYY-MM-DD')
  } else if (queryParams.period === 'YEAR') {
    // 近一年：当前年的第一天到当前日期
    startDate = now.startOf('year').format('YYYY-MM-DD')
    endDate = now.format('YYYY-MM-DD')
  } else if (queryParams.period === 'CUSTOM') {
    // 自定义时间
    if (customDateRange.value) {
      startDate = customDateRange.value[0]
      endDate = customDateRange.value[1]
    }
  }
  // ALL 时不设置日期范围

  return { startDate, endDate }
}

// 处理自定义日期变化
const handleCustomDateChange = () => {
  if (customDateRange.value) {
    queryParams.startDate = customDateRange.value[0]
    queryParams.endDate = customDateRange.value[1]
  } else {
    queryParams.startDate = undefined
    queryParams.endDate = undefined
  }
  loadAllData()
}

// 加载统计概览
const loadOverview = async () => {
  try {
    const dateRange = getDateRange()
    const params = {
      ...queryParams,
      ...dateRange,
    }
    console.log('调用统计概览接口，参数:', params)
    const res = await statisticsApi.getOverview(params)
    console.log('统计概览接口响应:', res)
    if (res) {
      overview.value = res
      console.log('统计概览数据已更新:', overview.value)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载统计概览失败')
    console.error('加载统计概览失败:', error)
  }
}

// 前端计算资产卡片统计数据
const calculateAssetCardStats = async () => {
  try {
    // 获取所有资产（分页获取全部）
    let allAssets: any[] = []
    let pageNum = 1
    const pageSize = 100
    let hasMore = true
    
    while (hasMore) {
      const res = await assetApi.getAssetPage({
        pageNum,
        pageSize,
      })
      if (res?.list && res.list.length > 0) {
        allAssets = allAssets.concat(res.list)
        hasMore = res.list.length === pageSize
        pageNum++
      } else {
        hasMore = false
      }
    }
    
    // 计算统计数据
    let totalPrice = 0 // 资产价格之和（过滤已退役资产）
    let totalAdditionalFees = 0 // 总资产附加费用之和（所有资产）
    let dailyAverageTotal = 0 // 日均总额
    
    allAssets.forEach((asset) => {
      // 累加所有资产的附加费用
      const additionalFees = asset.additionalFeesTotal || 0
      totalAdditionalFees += additionalFees
      
      // 只统计非退役资产的价格
      if (asset.status !== 'RETIRED' && asset.price) {
        totalPrice += asset.price
      }
      
      // 累加日均价格
      if (asset.dailyAveragePrice) {
        dailyAverageTotal += asset.dailyAveragePrice
      }
    })
    
    // 资产总价格 = 资产价格之和（非退役）+ 资产附加费用之和
    const totalValue = totalPrice + totalAdditionalFees
    
    assetCardStats.value = {
      totalPrice,
      totalCount: allAssets.length,
      totalAdditionalFees,
      totalValue,
      dailyAverageTotal,
    }
  } catch (error: any) {
    console.error('计算资产卡片统计数据失败:', error)
    ElMessage.error('加载资产统计失败')
  }
}

// 加载资产卡片统计信息
const loadAssetCardStatistics = async () => {
  try {
    const dateRange = getDateRange()
    const params = {
      ...queryParams,
      ...dateRange,
    }
    console.log('调用资产卡片统计接口，参数:', params)
    const res = await statisticsApi.getAssetCardStatistics(params)
    console.log('资产卡片统计接口响应:', res)
    if (res) {
      assetCardStats.value = res
      console.log('资产卡片统计数据已更新:', assetCardStats.value)
    } else {
      // 如果后端接口不存在，使用前端计算
      await calculateAssetCardStats()
    }
  } catch (error: any) {
    console.warn('加载资产卡片统计失败，使用前端计算:', error)
    // 如果接口不存在，使用前端计算
    await calculateAssetCardStats()
  }
}

// 加载资产统计信息
const loadAssetStatistics = async () => {
  try {
    console.log('调用资产统计接口')
    const res = await statisticsApi.getAssetStatistics()
    console.log('资产统计接口响应:', res)
    if (res) {
      assetStatistics.value = res
      console.log('资产统计数据已更新:', assetStatistics.value)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载资产统计失败')
    console.error('加载资产统计失败:', error)
  }
}

// 加载心愿单卡片统计信息
const loadWishlistCardStatistics = async () => {
  try {
    console.log('调用心愿单卡片统计接口')
    const res = await statisticsApi.getWishlistCardStatistics()
    console.log('心愿单卡片统计接口响应:', res)
    if (res) {
      wishlistCardStats.value = res
      console.log('心愿单卡片统计数据已更新:', wishlistCardStats.value)
    } else {
      // 如果后端接口不存在，使用前端计算
      await calculateWishlistStats()
    }
  } catch (error: any) {
    console.warn('加载心愿单卡片统计失败，使用前端计算:', error)
    // 如果接口不存在，使用前端计算
    await calculateWishlistStats()
  }
}

// 前端计算心愿单统计数据
const calculateWishlistStats = async () => {
  try {
    // 获取所有心愿单（不限时间）
    const allWishlists = await wishlistApi.getWishlistList()
    
    // 计算总数据
    const totalCount = allWishlists.length
    const totalValue = allWishlists.reduce((sum, item) => sum + (item.price || 0), 0)
    
    // 计算时间范围内的数据
    const dateRange = getDateRange()
    let addedCount = 0
    let achievedCount = 0
    
    if (dateRange.startDate && dateRange.endDate) {
      const start = dayjs(dateRange.startDate).startOf('day')
      const end = dayjs(dateRange.endDate).endOf('day')
      
      allWishlists.forEach((item) => {
        // 追加个数：创建时间在范围内（包含边界）
        if (item.createdAt) {
          const createdAt = dayjs(item.createdAt)
          if ((createdAt.isAfter(start) || createdAt.isSame(start)) && 
              (createdAt.isBefore(end) || createdAt.isSame(end))) {
            addedCount++
          }
        }
        
        // 实现个数：实现时间在范围内（包含边界）
        if (item.achieved && item.achievedAt) {
          const achievedAt = dayjs(item.achievedAt)
          if ((achievedAt.isAfter(start) || achievedAt.isSame(start)) && 
              (achievedAt.isBefore(end) || achievedAt.isSame(end))) {
            achievedCount++
          }
        }
      })
    } else {
      // 全部时间：追加个数是所有心愿单数量，实现个数是已实现的心愿单数量
      addedCount = totalCount
      achievedCount = allWishlists.filter(item => item.achieved).length
    }
    
    wishlistCardStats.value = {
      totalCount,
      totalValue,
      addedCount,
      achievedCount,
    }
  } catch (error: any) {
    console.error('计算心愿单统计数据失败:', error)
    ElMessage.error('加载心愿单统计失败')
  }
}

// 加载资产日均趋势图表
const loadDailyAverageTrend = async () => {
  try {
    if (!dailyAverageChartRef.value) {
      return
    }

    await nextTick()

    // 检查容器尺寸
    const rect = dailyAverageChartRef.value.getBoundingClientRect()
    if (rect.width === 0 || rect.height === 0) {
      setTimeout(() => loadDailyAverageTrend(), 100)
      return
    }

    // 创建或重用实例
    if (!dailyAverageChart.value) {
      dailyAverageChart.value = echarts.init(dailyAverageChartRef.value)
    }

    // 获取数据
    const dateRange = getDateRange()
    const params = {
      ...queryParams,
      ...dateRange,
    }
    const res = await statisticsApi.getDailyAverageTrend(params)
    
    // 处理数据
    const dates = res?.dates || []
    const values = res?.values || []
    const hasData = dates.length > 0 && values.length > 0 && dates.length === values.length

    // 如果没有数据，不渲染图表
    if (!hasData) {
      if (dailyAverageChart.value) {
        dailyAverageChart.value.clear()
      }
      return
    }

    // 确保数据格式正确
    const chartDates = dates
    const chartValues = values.map(v => Number(v) || 0)

    // 构建配置 - 使用对象格式
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: chartDates,
        axisLabel: {
          rotate: 45,
        },
      },
      yAxis: {
        type: 'value',
        name: '日均价格',
      },
      series: [
        {
          name: '日均价格',
          type: 'line',
          data: chartValues,
          smooth: true,
          areaStyle: {
            opacity: 0.3,
          },
          itemStyle: {
            color: '#409EFF',
          },
          lineStyle: {
            width: 2,
          },
        },
      ],
    }

    // 设置配置
    if (dailyAverageChart.value) {
      // 使用 replaceMerge 完全替换配置，避免状态不一致
      dailyAverageChart.value.setOption(option, {
        replaceMerge: ['series', 'xAxis', 'yAxis', 'legend', 'tooltip']
      })
      // 等待 DOM 更新和图表内部状态完全初始化后再 resize
      await nextTick()
      setTimeout(() => {
        if (dailyAverageChart.value) {
          try {
            dailyAverageChart.value.resize()
          } catch (error) {
            console.warn('日均趋势图 resize 失败:', error)
          }
        }
      }, 200)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载日均趋势失败')
    console.error('加载日均趋势失败:', error)
  }
}

// 加载资产总额趋势图表
const loadTotalValueTrend = async () => {
  try {
    if (!totalValueChartRef.value) {
      return
    }

    await nextTick()

    // 检查容器尺寸
    const rect = totalValueChartRef.value.getBoundingClientRect()
    if (rect.width === 0 || rect.height === 0) {
      setTimeout(() => loadTotalValueTrend(), 100)
      return
    }

    // 创建或重用实例
    if (!totalValueChart.value) {
      totalValueChart.value = echarts.init(totalValueChartRef.value)
    }

    // 获取数据
    const dateRange = getDateRange()
    const params = {
      ...queryParams,
      ...dateRange,
    }
    const res = await statisticsApi.getTotalValueTrend(params)
    
    // 处理数据
    const dates = res?.dates || []
    const values = res?.values || []
    const hasData = dates.length > 0 && values.length > 0 && dates.length === values.length

    // 如果没有数据，不渲染图表
    if (!hasData) {
      if (totalValueChart.value) {
        totalValueChart.value.clear()
      }
      return
    }

    // 确保数据格式正确
    const chartDates = dates
    const chartValues = values.map(v => Number(v) || 0)

    // 构建配置 - 使用对象格式
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: chartDates,
        axisLabel: {
          rotate: 45,
        },
      },
      yAxis: {
        type: 'value',
        name: '资产总额',
      },
      series: [
        {
          name: '资产总额',
          type: 'line',
          data: chartValues,
          smooth: true,
          areaStyle: {
            opacity: 0.3,
          },
          itemStyle: {
            color: '#67C23A',
          },
          lineStyle: {
            width: 2,
          },
        },
      ],
    }

    // 设置配置
    if (totalValueChart.value) {
      // 使用 replaceMerge 完全替换配置，避免状态不一致
      totalValueChart.value.setOption(option, {
        replaceMerge: ['series', 'xAxis', 'yAxis', 'legend', 'tooltip']
      })
      // 等待 DOM 更新和图表内部状态完全初始化后再 resize
      await nextTick()
      setTimeout(() => {
        if (totalValueChart.value) {
          try {
            totalValueChart.value.resize()
          } catch (error) {
            console.warn('资产总额趋势图 resize 失败:', error)
          }
        }
      }, 200)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载资产总额趋势失败')
    console.error('加载资产总额趋势失败:', error)
  }
}

// 加载资产分布图表
const loadAssetDistribution = async () => {
  try {
    if (!distributionChartRef.value) {
      return
    }

    await nextTick()

    // 检查容器尺寸
    const rect = distributionChartRef.value.getBoundingClientRect()
    if (rect.width === 0 || rect.height === 0) {
      setTimeout(() => loadAssetDistribution(), 100)
      return
    }

    // 创建或重用实例
    if (!distributionChart.value) {
      distributionChart.value = echarts.init(distributionChartRef.value)
    }

    // 获取数据
    const dateRange = getDateRange()
    const params = {
      ...queryParams,
      ...dateRange,
    }
    const res = await statisticsApi.getAssetDistribution(params)
    
    // 处理数据
    const hasData = res && res.length > 0

    // 如果没有数据，不渲染图表
    if (!hasData) {
      if (distributionChart.value) {
        distributionChart.value.clear()
      }
      return
    }

    const chartData = res.map((item) => ({
      value: Number(item.value) || 0,
      name: item.categoryName || '未分类',
    }))

    // 构建配置 - 使用对象格式
    const option = {
      tooltip: {
        trigger: 'item',
      },
      legend: {
        orient: 'vertical',
        left: 'left',
      },
      series: [
        {
          name: '资产分布',
          type: 'pie',
          radius: '50%',
          data: chartData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
        },
      ],
    }

    // 设置配置
    if (distributionChart.value) {
      // 使用 replaceMerge 完全替换配置，避免状态不一致
      distributionChart.value.setOption(option, {
        replaceMerge: ['series', 'xAxis', 'yAxis', 'legend', 'tooltip']
      })
      // 等待 DOM 更新和图表内部状态完全初始化后再 resize
      await nextTick()
      setTimeout(() => {
        if (distributionChart.value) {
          try {
            distributionChart.value.resize()
          } catch (error) {
            console.warn('资产分布图 resize 失败:', error)
          }
        }
      }, 200)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载资产分布失败')
    console.error('加载资产分布失败:', error)
  }
}

// 加载资产分类统计图表
const loadCategoryStatistics = async () => {
  try {
    if (!categoryChartRef.value) {
      return
    }

    await nextTick()

    // 检查容器尺寸
    const rect = categoryChartRef.value.getBoundingClientRect()
    if (rect.width === 0 || rect.height === 0) {
      setTimeout(() => loadCategoryStatistics(), 100)
      return
    }

    // 创建或重用实例
    if (!categoryChart.value) {
      categoryChart.value = echarts.init(categoryChartRef.value)
    }

    // 获取数据
    const dateRange = getDateRange()
    const params = {
      ...queryParams,
      ...dateRange,
    }
    const res = await statisticsApi.getCategoryStatistics(params)
    
    // 处理数据
    const hasData = res && res.length > 0

    // 如果没有数据，不渲染图表
    if (!hasData) {
      if (categoryChart.value) {
        categoryChart.value.clear()
      }
      return
    }

    const categories = res.map((item) => item.categoryName || '未分类')
    const countData = res.map((item) => Number(item.count) || 0)
    const valueData = res.map((item) => Number(item.totalValue) || 0)

    // 构建配置 - 使用对象格式的 xAxis，数组格式的 yAxis（因为有两个Y轴）
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      legend: {
        data: ['数量', '总价值'],
        top: 10,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '15%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: categories,
        axisLabel: {
          rotate: -45,
          interval: 0,
        },
      },
      yAxis: [
        {
          type: 'value',
          name: '数量',
          position: 'left',
        },
        {
          type: 'value',
          name: '总价值',
          position: 'right',
        },
      ],
      series: [
        {
          name: '数量',
          type: 'bar',
          data: countData,
          itemStyle: {
            color: '#409EFF',
          },
        },
        {
          name: '总价值',
          type: 'bar',
          yAxisIndex: 1,
          data: valueData,
          itemStyle: {
            color: '#67C23A',
          },
        },
      ],
    }

    // 设置配置
    if (categoryChart.value) {
      // 使用 replaceMerge 完全替换配置，避免状态不一致
      categoryChart.value.setOption(option, {
        replaceMerge: ['series', 'xAxis', 'yAxis', 'legend', 'tooltip', 'angleAxis', 'radiusAxis']
      })
      // 等待 DOM 更新和图表内部状态完全初始化后再 resize
      await nextTick()
      setTimeout(() => {
        if (categoryChart.value) {
          try {
            categoryChart.value.resize()
          } catch (error) {
            console.warn('分类统计图 resize 失败:', error)
          }
        }
      }, 200)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载分类统计失败')
    console.error('加载分类统计失败:', error)
  }
}

// 加载所有数据
const loadAllData = async () => {
  loading.value = true
  try {
    console.log('开始加载统计数据，查询参数:', queryParams)
    
    // 更新日期范围
    const dateRange = getDateRange()
    queryParams.startDate = dateRange.startDate
    queryParams.endDate = dateRange.endDate
    
    await Promise.all([
      loadOverview(),
      loadAssetCardStatistics(),
      loadWishlistCardStatistics(),
      loadDailyAverageTrend(),
      loadTotalValueTrend(),
      loadAssetDistribution(),
      loadCategoryStatistics(),
    ])
    console.log('统计数据加载完成')
  } catch (error: any) {
    console.error('加载统计数据时发生错误:', error)
    ElMessage.error('加载统计数据失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

// 处理周期变化
const handlePeriodChange = () => {
  // 如果不是自定义时间，清空日期范围
  if (queryParams.period !== 'CUSTOM') {
    customDateRange.value = null
  }
  loadAllData()
}

// 窗口大小变化时调整图表
const handleResize = () => {
  try {
    dailyAverageChart.value?.resize()
    totalValueChart.value?.resize()
    distributionChart.value?.resize()
    categoryChart.value?.resize()
  } catch (error) {
    // 忽略 resize 错误，避免影响用户体验
    console.warn('图表 resize 失败:', error)
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  // 确保 DOM 完全渲染后再加载数据
  nextTick(() => {
    setTimeout(() => {
      loadAllData()
    }, 100)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  try {
    dailyAverageChart.value?.dispose()
    totalValueChart.value?.dispose()
    distributionChart.value?.dispose()
    categoryChart.value?.dispose()
  } catch (error) {
    // 忽略 dispose 错误
    console.warn('图表 dispose 失败:', error)
  }
})
</script>

<style scoped lang="scss">
.stat-analysis-view {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 18px;
      font-weight: bold;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .stat-cards-row {
    margin-bottom: 20px;

    &:last-of-type {
      margin-bottom: 0;
    }
  }

  .card-col {
    margin-bottom: 20px;
  }

  .main-stat-card {
    height: 100%;
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
    }

    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 2px solid #ebeef5;
      background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
    }

    :deep(.el-card__body) {
      padding: 24px;
    }
  }

  .asset-card {
    :deep(.el-card__header) {
      background: linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%);
    }
  }

  .wishlist-card {
    :deep(.el-card__header) {
      background: linear-gradient(135deg, #fff3e0 0%, #ffffff 100%);
    }
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;

    .title-icon {
      font-size: 20px;
      color: #409EFF;
    }
  }

  .wishlist-card .card-title .title-icon {
    color: #ff9800;
  }

  .stat-card-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .stat-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .stat-label {
    font-size: 14px;
    color: #606266;
    display: flex;
    align-items: center;
    gap: 6px;
    flex: 1;

    .info-icon {
      font-size: 14px;
      color: #909399;
      cursor: help;
      transition: color 0.3s;

      &:hover {
        color: #409EFF;
      }
    }
  }

  .stat-value {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    text-align: right;
    min-width: 120px;

    &.highlight-value {
      color: #67C23A;
      font-size: 20px;
    }

    &.highlight-daily {
      color: #409EFF;
      font-size: 20px;
    }

    &.highlight-achieved {
      color: #ff9800;
      font-size: 20px;
    }
  }

  .section-title {
    font-size: 16px;
    font-weight: bold;
  }

  .chart-row {
    margin-top: 20px;
  }

  .chart-col {
    margin-bottom: 20px;

    &:nth-last-child(-n + 2) {
      margin-bottom: 0;
    }
  }

  .chart-card {
    height: 100%;

    :deep(.el-card__header) {
      padding: 15px 20px;
      border-bottom: 1px solid #ebeef5;
    }

    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .chart-container {
    width: 100%;
    height: 400px;
  }
}
</style>

<!-- Tooltip 内容样式（全局样式，因为 tooltip 渲染在 body 中） -->
<style lang="scss">
.tooltip-content {
  max-width: 300px;
  line-height: 1.6;

  .tooltip-title {
    font-weight: bold;
    font-size: 14px;
    color: #fff;
    margin-bottom: 8px;
  }

  .tooltip-text {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 12px;
    word-break: break-word;
  }

  .tooltip-divider {
    height: 1px;
    background: rgba(255, 255, 255, 0.2);
    margin: 12px 0;
  }

  .tooltip-item {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 6px;
    padding-left: 8px;
    position: relative;

    &:last-child {
      margin-bottom: 0;
    }

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 8px;
      width: 4px;
      height: 4px;
      background: rgba(255, 255, 255, 0.6);
      border-radius: 50%;
    }
  }
}
</style>

