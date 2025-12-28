<template>
  <div class="unified-workbench-view">
    <!-- 移动端风格的标签页 -->
    <el-tabs
      v-model="activeTab"
      class="mobile-tabs"
      @tab-change="handleTabChange"
    >
      <!-- 快捷记录标签页 -->
      <el-tab-pane label="快捷记录" name="quick-record">
        <div class="tab-content">
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
        </div>
      </el-tab-pane>

      <!-- 健康统计标签页 -->
      <el-tab-pane label="健康统计" name="health-stats">
        <div class="tab-content">
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
        </div>
      </el-tab-pane>

      <!-- 常用工具标签页 -->
      <el-tab-pane label="常用工具" name="common-tools">
        <div class="tab-content">
          <div class="section">
            <div class="section-title">🛠️ 常用工具</div>
            <ToolsGrid :tools="tools" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

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
 * 统一工作台页面（合并快捷记录与统计和工作台）
 * 使用标签页区分两个功能模块
 */

import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
import ToolsGrid from '@/components/workbench/ToolsGrid.vue'

const route = useRoute()
const router = useRouter()

// 标签页管理
const activeTab = ref<'quick-record' | 'health-stats' | 'common-tools'>('quick-record')

// 根据路由参数设置活动标签
watch(
  () => route.path,
  (path) => {
    if (path === '/quick-record') {
      activeTab.value = 'quick-record'
    } else if (path === '/health-stats') {
      activeTab.value = 'health-stats'
    } else if (path === '/common-tools') {
      activeTab.value = 'common-tools'
    }
  },
  { immediate: true }
)

const handleTabChange = (tabName: string) => {
  const routeMap: Record<string, string> = {
    'quick-record': '/quick-record',
    'health-stats': '/health-stats',
    'common-tools': '/common-tools'
  }
  
  const targetPath = routeMap[tabName]
  if (targetPath && route.path !== targetPath) {
    router.push(targetPath)
  }
}

// 快捷记录相关状态
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


const tools = [
  { id: 'blueprint', name: 'AI蓝图', icon: '📊', route: '/tools/blueprint' },
  { id: 'cursor', name: 'Cursor', icon: '🖱️', route: '/tools/cursor-inventory' },
  { id: 'calculator', name: '计算器', icon: '🔢', route: '/tools' },
]

// 快捷记录相关方法
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
  // 根据当前路由加载对应数据
  if (activeTab.value === 'quick-record') {
    loadOverviewData()
  } else if (activeTab.value === 'health-stats') {
    loadStatisticsData()
  }
  // common-tools 不需要加载数据
})

// 监听标签页切换，按需加载数据
watch(activeTab, (newTab) => {
  if (newTab === 'quick-record' && !overviewData.todoStats.total) {
    loadOverviewData()
  } else if (newTab === 'health-stats' && !weightTrendData.value) {
    loadStatisticsData()
  }
  // common-tools 不需要加载数据
})
</script>

<style scoped lang="scss">
.unified-workbench-view {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;

  // 移动端风格的标签页（桌面端在顶部）
  :deep(.mobile-tabs) {
    .el-tabs__header {
      margin: 0;
      background: #ffffff;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      position: sticky;
      top: 0;
      z-index: 100;
      padding: 0;
      border-bottom: 1px solid #e4e7ed;
    }

    .el-tabs__nav-wrap {
      &::after {
        display: none;
      }
    }

    .el-tabs__nav {
      width: 100%;
      display: flex;
      justify-content: space-around;
      background: #ffffff;
    }

    .el-tabs__item {
      flex: 1;
      text-align: center;
      padding: 16px 8px;
      font-size: 15px;
      font-weight: 500;
      color: #606266;
      border: none;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      cursor: pointer;
      -webkit-tap-highlight-color: transparent;
      min-width: 0; // 允许标签页缩小以适应4个标签

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 0;
        height: 3px;
        background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
        border-radius: 2px 2px 0 0;
        transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }

      &.is-active {
        color: #667eea;
        font-weight: 600;
        background: rgba(102, 126, 234, 0.05);

        &::after {
          width: 60%;
        }
      }

      &:hover {
        color: #667eea;
        background: rgba(102, 126, 234, 0.03);
      }

      &:active {
        background: rgba(102, 126, 234, 0.1);
      }
    }

    .el-tabs__active-bar {
      display: none;
    }

    .el-tabs__content {
      padding: 0;
    }

    .el-tab-pane {
      padding: 0;
    }
  }

  .tab-content {
    padding: 16px;
    max-width: 100%;
    margin: 0 auto;
    background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
    min-height: calc(100vh - 120px);

    .section-title {
      font-size: 18px;
      font-weight: 700;
      color: #1a1a1a;
      margin: 24px 0 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      letter-spacing: 0.5px;
      
      &::before {
        content: '';
        width: 4px;
        height: 18px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 2px;
      }
    }

    .section {
      margin-bottom: 32px;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
        margin-bottom: 16px;
      }
    }

    .add-task-button {
      margin-top: 16px;
      width: 100%;
      height: 48px;
      font-size: 16px;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    }

    .statistics-section {
      margin-top: 0;
      padding-top: 0;

      .section-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;
        padding-bottom: 12px;
        flex-wrap: wrap;
        gap: 12px;

        .section-title {
          margin: 0;
          font-size: 18px;
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
          padding: 20px;
          background: linear-gradient(to bottom right, #ffffff 0%, #f8f9fa 100%);
        }
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
  margin-bottom: 24px;

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
    padding: 20px;
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
    gap: 16px;
    padding: 16px 0;
  }

  .record-button {
    border-radius: 16px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: none;
    position: relative;
    overflow: hidden;
    min-height: 120px;

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
        font-size: 32px;
      }

      .button-text {
        font-weight: 600;
        letter-spacing: 0.5px;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
        font-size: 14px;
      }
    }

    &:hover .button-icon {
      transform: scale(1.1) rotate(5deg);
    }
  }
}

// 移动端优化 - 手机风格（标签页在底部）
@media (max-width: 768px) {
  .unified-workbench-view {
    background: #f5f7fa;
    padding-bottom: 60px; // 为底部标签栏留出空间

    :deep(.mobile-tabs) {
      .el-tabs__header {
        box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
        border-top: 1px solid #e4e7ed;
        border-bottom: none;
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        top: auto;
        padding: 0;
        background: #ffffff;
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        z-index: 1000;
      }

      .el-tabs__nav {
        background: transparent;
      }

      .el-tabs__item {
        padding: 10px 4px;
        font-size: 12px;
        min-height: 56px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
        gap: 4px;
        min-width: 0; // 允许标签页缩小以适应4个标签

        &::after {
          top: 0;
          bottom: auto;
          height: 3px;
          border-radius: 0 0 2px 2px;
        }

        &.is-active {
          color: #667eea;
          background: rgba(102, 126, 234, 0.08);

          &::after {
            width: 40px;
            height: 3px;
          }
        }
      }
    }

    .tab-content {
      padding-bottom: 0; // 桌面端不需要底部空间
    }

    .tab-content {
      padding: 12px 16px;
      padding-bottom: 80px; // 为底部标签栏留出空间

      .section-title {
        font-size: 16px;
        margin: 20px 0 12px;
      }

      .section {
        margin-bottom: 24px;

        .section-title {
          font-size: 16px;
          margin-bottom: 12px;
        }
      }

      .add-task-button {
        height: 48px;
        font-size: 16px;
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        font-weight: 600;
        -webkit-tap-highlight-color: transparent;
      }

      .statistics-section {
        margin-top: 24px;
        padding-top: 20px;

        .section-header {
          margin-bottom: 16px;
          padding-bottom: 10px;
          flex-direction: column;
          align-items: flex-start;

          .section-title {
            font-size: 16px;
            width: 100%;
          }
        }

        .statistics-card {
          border-radius: 16px;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

          :deep(.el-card__body) {
            padding: 16px;
          }
        }
      }
    }

    :deep(.today-overview-card) {
      border-radius: 12px;
      margin-bottom: 20px;

      .el-card__header {
        padding: 12px 16px;

        .title {
          font-size: 16px;
        }
      }

      .el-card__body {
        padding: 16px;
      }
    }

    :deep(.quick-record-buttons) {
      .buttons-grid {
        gap: 12px;
        padding: 12px 0;
        grid-template-columns: repeat(2, 1fr);
      }

      .record-button {
        border-radius: 16px;
        min-height: 120px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        -webkit-tap-highlight-color: transparent;

        &:active {
          transform: scale(0.95);
        }

        .button-content {
          .button-icon {
            font-size: 36px;
          }

          .button-text {
            font-size: 14px;
            font-weight: 600;
          }
        }
      }
    }

    // 工作台卡片样式优化
    :deep(.el-card) {
      border-radius: 16px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      border: none;
      margin-bottom: 16px;
    }

    // 待办列表移动端优化
    :deep(.todo-list) {
      .todo-item {
        border-radius: 12px;
        margin-bottom: 12px;
        padding: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        -webkit-tap-highlight-color: transparent;
      }
    }

    // 工具网格移动端优化
    :deep(.tools-grid) {
      .tool-item {
        border-radius: 16px;
        padding: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        -webkit-tap-highlight-color: transparent;

        &:active {
          transform: scale(0.95);
        }
      }
    }
  }
}

// 大屏幕优化
@media (min-width: 1200px) {
  .unified-workbench-view {
    .tab-content {
      padding: 24px;
      max-width: 1400px;
    }
  }
}
</style>

