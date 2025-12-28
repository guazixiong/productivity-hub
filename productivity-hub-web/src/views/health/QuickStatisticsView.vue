<template>
  <div class="quick-statistics-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">健康统计</span>
          <TimeRangeSelector
            :value="timeRange"
            @change="handleTimeRangeChange"
          />
        </div>
      </template>

      <!-- 体重趋势图 -->
      <WeightTrendChart
        :data="weightTrendData"
        :loading="loading"
      />

      <!-- 运动数据卡片 -->
      <ExerciseStatsCard
        :data="exerciseStats"
        :loading="loading"
      />

      <!-- 饮水达标卡片 -->
      <WaterStatsCard
        :data="waterStats"
        :target="waterTarget"
        :today-intake="todayWaterIntake"
        :loading="loading"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 健康中心页面
 * 
 * 关联需求: REQ-007, REQ-008
 * 关联页面: PAGE-REQ-002-01
 * 关联接口: API-REQ-002-07, API-REQ-002-08, API-REQ-002-09, API-REQ-002-10
 */

import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { healthApi } from '@/services/healthApi'
import type { WeightTrend, ExerciseStatistics, WaterStatistics, WaterTarget } from '@/types/health'
import TimeRangeSelector from '@/components/health/TimeRangeSelector.vue'
import WeightTrendChart from '@/components/health/WeightTrendChart.vue'
import ExerciseStatsCard from '@/components/health/ExerciseStatsCard.vue'
import WaterStatsCard from '@/components/health/WaterStatsCard.vue'

const loading = ref(false)
const timeRange = ref<'today' | 'week' | 'month' | 'custom'>('week')
const customDateRange = ref<{ startDate?: string; endDate?: string }>({})

const weightTrendData = ref<WeightTrend | null>(null)
const exerciseStats = ref<ExerciseStatistics | null>(null)
const waterStats = ref<WaterStatistics | null>(null)
const waterTarget = ref<number>(2000)
const todayWaterIntake = ref<number>(0)

const calculateDays = (range: string) => {
  switch (range) {
    case 'today':
      return 1
    case 'week':
      return 7
    case 'month':
      return 30
    default:
      return 7
  }
}

const loadData = async () => {
  try {
    loading.value = true

    const days = calculateDays(timeRange.value)

    // 加载体重趋势
    weightTrendData.value = await healthApi.getWeightTrend({ days })

    // 加载运动统计
    exerciseStats.value = await healthApi.getExerciseStatistics({
      period: timeRange.value === 'custom' ? 'custom' : timeRange.value,
      startDate: customDateRange.value.startDate,
      endDate: customDateRange.value.endDate,
    })

    // 加载饮水统计
    waterStats.value = await healthApi.getWaterStatistics({
      period: timeRange.value === 'custom' ? 'custom' : timeRange.value,
      startDate: customDateRange.value.startDate,
      endDate: customDateRange.value.endDate,
    })

    // 加载饮水目标
    const target = await healthApi.getWaterTarget()
    waterTarget.value = target?.dailyTargetMl || 2000

    // 加载今日饮水进度
    const todayProgress = await healthApi.getWaterProgress()
    todayWaterIntake.value = todayProgress.totalIntakeMl || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleTimeRangeChange = (
  value: string,
  startDate?: string,
  endDate?: string
) => {
  timeRange.value = value as any
  if (value === 'custom') {
    customDateRange.value = { startDate, endDate }
  } else {
    customDateRange.value = {}
  }
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.quick-statistics-view {
  padding: 16px;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .title {
      font-size: 18px;
      font-weight: 600;
    }
  }
}

@media (max-width: 768px) {
  .quick-statistics-view {
    padding: 12px;
  }
}
</style>

