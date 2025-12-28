<template>
  <div class="quick-record-view">
    <TodayOverviewCard
      :todo-stats="overviewData.todoStats"
      :water-data="overviewData.waterData"
      :exercise-data="overviewData.exerciseData"
      :loading="loading"
    />

    <div class="section-title">⚡ 快捷记录</div>

    <QuickRecordButtons
      @todo-click="handleTodoClick"
      @exercise-click="handleExerciseClick"
      @water-click="handleWaterClick"
      @weight-click="handleWeightClick"
    />

    <!-- 健康统计部分 -->
    <div class="statistics-section">
      <div class="section-header">
        <div class="section-title">📊 健康统计</div>
        <TimeRangeSelector
          :value="timeRange"
          @change="handleTimeRangeChange"
        />
      </div>

      <el-card class="statistics-card">
        <!-- 体重趋势图 -->
        <WeightTrendChart
          :data="weightTrendData"
          :loading="statisticsLoading"
        />

        <!-- 运动数据卡片 -->
        <ExerciseStatsCard
          :data="exerciseStats"
          :loading="statisticsLoading"
        />

        <!-- 饮水达标卡片 -->
        <WaterStatsCard
          :data="waterStats"
          :target="waterTarget"
          :today-intake="todayWaterIntake"
          :loading="statisticsLoading"
        />
      </el-card>
    </div>

    <!-- 弹窗组件 -->
    <TodoRecordDialog
      v-model:visible="dialogs.todo"
      @success="handleRecordSuccess"
    />
    <ExerciseRecordDialog
      v-model:visible="dialogs.exercise"
      @success="handleRecordSuccess"
    />
    <WaterRecordDialog
      v-model:visible="dialogs.water"
      @success="handleRecordSuccess"
    />
    <WeightRecordDialog
      v-model:visible="dialogs.weight"
      @success="handleRecordSuccess"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 首页-快捷记录页面（融合快捷统计）
 * 
 * 关联需求: REQ-001, REQ-002, REQ-003, REQ-004, REQ-005, REQ-006, REQ-007, REQ-008
 * 关联页面: PAGE-REQ-001-01, PAGE-REQ-002-01
 * 关联接口: API-REQ-001-03, API-REQ-002-01, API-REQ-002-07, API-REQ-002-08, API-REQ-002-09, API-REQ-002-10
 */

import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { todoApi } from '@/services/todoApi'
import { healthApi } from '@/services/healthApi'
import type { WeightTrend, ExerciseStatistics, WaterStatistics } from '@/types/health'
import TodayOverviewCard from '@/components/quick-record/TodayOverviewCard.vue'
import QuickRecordButtons from '@/components/quick-record/QuickRecordButtons.vue'
import TodoRecordDialog from '@/components/quick-record/TodoRecordDialog.vue'
import ExerciseRecordDialog from '@/components/quick-record/ExerciseRecordDialog.vue'
import WaterRecordDialog from '@/components/quick-record/WaterRecordDialog.vue'
import WeightRecordDialog from '@/components/quick-record/WeightRecordDialog.vue'
import TimeRangeSelector from '@/components/health/TimeRangeSelector.vue'
import WeightTrendChart from '@/components/health/WeightTrendChart.vue'
import ExerciseStatsCard from '@/components/health/ExerciseStatsCard.vue'
import WaterStatsCard from '@/components/health/WaterStatsCard.vue'

const loading = ref(false)
const statisticsLoading = ref(false)
const timeRange = ref<'today' | 'week' | 'month' | 'custom'>('week')
const customDateRange = ref<{ startDate?: string; endDate?: string }>({})

const overviewData = reactive({
  todoStats: {
    completed: 0,
    total: 0,
  },
  waterData: {
    intake: 0,
    target: 2000,
  },
  exerciseData: {
    duration: 0,
  },
})

const weightTrendData = ref<WeightTrend | null>(null)
const exerciseStats = ref<ExerciseStatistics | null>(null)
const waterStats = ref<WaterStatistics | null>(null)
const waterTarget = ref<number>(2000)
const todayWaterIntake = ref<number>(0)

const dialogs = reactive({
  todo: false,
  exercise: false,
  water: false,
  weight: false,
})

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

const loadOverviewData = async () => {
  try {
    loading.value = true

    // 加载待办统计
    const todoStats = await todoApi.overview()
    overviewData.todoStats = {
      completed: todoStats.completedTasks || 0,
      total: todoStats.totalTasks || 0,
    }

    // 加载健康数据概览
    const healthOverview = await healthApi.getHealthOverview({ period: 'today' })
    overviewData.waterData = {
      intake: healthOverview.water?.todayIntakeMl || 0,
      target: healthOverview.water?.todayTargetMl || 2000,
    }
    overviewData.exerciseData = {
      duration: healthOverview.exercise?.todayDuration || 0,
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadStatisticsData = async () => {
  try {
    statisticsLoading.value = true

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
    ElMessage.error(error.message || '加载统计数据失败')
  } finally {
    statisticsLoading.value = false
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
  loadStatisticsData()
}

const handleRecordSuccess = () => {
  loadOverviewData()
  // 如果当前选择的是今日统计，也刷新统计数据
  if (timeRange.value === 'today') {
    loadStatisticsData()
  }
}

const handleTodoClick = () => {
  dialogs.todo = true
}

const handleExerciseClick = () => {
  dialogs.exercise = true
}

const handleWaterClick = () => {
  dialogs.water = true
}

const handleWeightClick = () => {
  dialogs.weight = true
}

onMounted(() => {
  loadOverviewData()
  loadStatisticsData()
})
</script>

<style scoped lang="scss">
.quick-record-view {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
  min-height: calc(100vh - 60px);

  .section-title {
    font-size: 20px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 32px 0 20px;
    display: flex;
    align-items: center;
    gap: 8px;
    letter-spacing: 0.5px;
    
    &::before {
      content: '';
      width: 4px;
      height: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 2px;
    }
  }

  .statistics-section {
    margin-top: 40px;
    padding-top: 32px;
    border-top: 1px solid #e4e7ed;

    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 2px solid #f0f2f5;

      .section-title {
        margin: 0;
        font-size: 22px;
        color: #2c3e50;
      }
    }

    .statistics-card {
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      border: none;
      overflow: hidden;
      background: #ffffff;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
      }

      :deep(.el-card__body) {
        padding: 28px;
        background: linear-gradient(to bottom right, #ffffff 0%, #f8f9fa 100%);
      }
    }
  }
}

// 美化今日概览卡片
:deep(.today-overview-card) {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  &:hover {
    box-shadow: 0 8px 30px rgba(102, 126, 234, 0.3);
    transform: translateY(-4px);
  }

  .el-card__header {
    background: rgba(255, 255, 255, 0.1);
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    padding: 16px 20px;

    .title {
      color: #ffffff;
      font-weight: 700;
      font-size: 18px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }

  .el-card__body {
    background: rgba(255, 255, 255, 0.95);
    padding: 24px;
  }

  .overview-item {
    padding: 12px 0;
    border-bottom: 1px solid #f0f2f5;
    transition: all 0.2s ease;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #f8f9fa;
      padding-left: 8px;
      border-radius: 8px;
    }

    .label {
      color: #606266;
      font-weight: 500;
    }

    .value {
      color: #303133;
      font-weight: 600;
      font-size: 15px;
    }
  }
}

// 美化快捷记录按钮
:deep(.quick-record-buttons) {
  .buttons-grid {
    gap: 20px;
    padding: 20px 0;
  }

  .record-button {
    border-radius: 16px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: none;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
      transition: left 0.5s;
    }

    &:hover {
      transform: translateY(-6px) scale(1.02);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);

      &::before {
        left: 100%;
      }
    }

    &:active {
      transform: translateY(-2px) scale(0.98);
    }

    .button-content {
      position: relative;
      z-index: 1;

      .button-icon {
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
        transition: transform 0.3s ease;
      }

      .button-text {
        font-weight: 600;
        letter-spacing: 0.5px;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
      }
    }

    &:hover .button-icon {
      transform: scale(1.1) rotate(5deg);
    }
  }
}

@media (max-width: 768px) {
  .quick-record-view {
    padding: 16px;
    background: #ffffff;

    .section-title {
      font-size: 18px;
      margin: 24px 0 16px;
    }

    .statistics-section {
      margin-top: 32px;
      padding-top: 24px;

      .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
        margin-bottom: 20px;
        padding-bottom: 12px;

        .section-title {
          font-size: 20px;
        }
      }

      .statistics-card {
        border-radius: 12px;

        :deep(.el-card__body) {
          padding: 20px;
        }
      }
    }
  }

  :deep(.today-overview-card) {
    border-radius: 12px;

    .el-card__header {
      padding: 12px 16px;

      .title {
        font-size: 16px;
      }
    }

    .el-card__body {
      padding: 20px;
    }
  }

  :deep(.quick-record-buttons) {
    .buttons-grid {
      gap: 16px;
      padding: 16px 0;
    }

    .record-button {
      border-radius: 12px;
      min-height: 140px;

      .button-content {
        .button-icon {
          font-size: 36px;
        }

        .button-text {
          font-size: 14px;
        }
      }
    }
  }
}

// 大屏幕优化
@media (min-width: 1400px) {
  .quick-record-view {
    padding: 32px;
  }
}
</style>

