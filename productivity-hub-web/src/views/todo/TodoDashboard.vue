<script setup lang="ts">
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import { Bell, ArrowDownBold } from '@element-plus/icons-vue'
import { todoApi } from '@/services/todoApi'
import type { TodoModule, TodoTask, TodoStats } from '@/types/todo'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'

use([CanvasRenderer, PieChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const props = defineProps<{
  modules: TodoModule[]
  tasks: TodoTask[]
  // 后端聚合的大屏统计数据（优先用于图表展示）
  stats?: TodoStats | null
  // 当前在主页面选中的模块，用于大屏左侧优先展示
  currentModuleId?: string
}>()

const emit = defineEmits<{
  refresh: []
}>()

const authStore = useAuthStore()
const notificationStore = useNotificationStore()

// 按模块分组任务
const tasksByModule = computed(() => {
  const map = new Map<string, TodoTask[]>()
  props.tasks.forEach((task) => {
    const moduleId = task.moduleId || 'unknown'
    if (!map.has(moduleId)) {
      map.set(moduleId, [])
    }
    map.get(moduleId)!.push(task)
  })
  return map
})

// 当前选中的模块 ID（支持外部初始值 + 内部切换）
const selectedModuleId = ref<string>('')

// 根据外部传入的 currentModuleId 和模块列表初始化 / 同步内部选中模块
watch(
  () => [props.currentModuleId, props.modules],
  ([externalId, modules]) => {
    if (externalId && modules.some((m) => m.id === externalId)) {
      selectedModuleId.value = externalId
      return
    }
    // 如果当前还没有选中模块，默认选第一个
    if (!selectedModuleId.value && modules.length > 0) {
      selectedModuleId.value = modules[0].id
    }
  },
  { immediate: true, deep: true },
)

// 当前高亮模块 ID（统一通过 selectedModuleId 获取）
const currentModuleId = computed(() => selectedModuleId.value)

const currentModule = computed(() =>
  props.modules.find((m) => m.id === currentModuleId.value) ?? null,
)

// 当前模块任务列表（作为分页数据源，可根据需要限制最大条数）
const currentModuleTasks = computed(() => {
  const list =
    (currentModuleId.value && tasksByModule.value.get(currentModuleId.value)) || props.tasks
  return (list || []).slice(0, 50)
})

// 当前模块的「待办」任务列表（不展示已完成任务）
const currentModuleTodoTasks = computed(() =>
  currentModuleTasks.value.filter((task) => task.status !== 'COMPLETED'),
)

// 列表分页（容器内分页，保证大屏整体不出现滚动）
const pageSize = ref(10)
const currentPage = ref(1)

const paginatedCurrentModuleTodoTasks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return currentModuleTodoTasks.value.slice(start, end)
})

// 模块或任务变化时，重置到第 1 页，避免页码越界
watch(
  () => [selectedModuleId.value, currentModuleTodoTasks.value.length],
  () => {
    currentPage.value = 1
  },
)

// 临时存储最新获取的执行中任务（用于状态变更后的立即更新）
const latestActiveTask = ref<TodoTask | null>(null)

// 当前执行中的任务（最近一条执行中的任务，用于倒计时）
const currentExecutingTask = computed(() => {
  // 优先使用临时存储的最新任务
  if (latestActiveTask.value) {
    return latestActiveTask.value
  }
  // 否则从props.tasks中查找
  const inProgress = props.tasks.filter((t) => t.status === 'IN_PROGRESS')
  if (inProgress.length === 0) return null
  // 按 lastEventAt 或 activeStartAt 排序，获取最近的一条
  return inProgress
    .slice()
    .sort((a, b) => {
      const aTime = a.activeStartAt ? new Date(a.activeStartAt).getTime() : 0
      const bTime = b.activeStartAt ? new Date(b.activeStartAt).getTime() : 0
      return bTime - aTime // 降序，最新的在前
    })[0]
})

interface CountdownState {
  years: string
  days: string
  hours: string
  minutes: string
  seconds: string
  hasYear: boolean
  isOver: boolean
}

const countdown = ref<CountdownState>({
  years: '00',
  days: '00',
  hours: '00',
  minutes: '00',
  seconds: '00',
  hasYear: false,
  isOver: false,
})

let countdownTimer: number | null = null

const updateCountdown = () => {
  const task = currentExecutingTask.value
  // 如果没有执行中的任务，或者任务没有截止日期，则不显示倒计时
  if (!task || !task.dueDate) {
    countdown.value = {
      years: '00',
      days: '00',
      hours: '00',
      minutes: '00',
      seconds: '00',
      hasYear: false,
      isOver: false,
    }
    return
  }

  const now = Date.now()
  const end = new Date(task.dueDate).getTime()
  let diff = end - now

  if (Number.isNaN(end)) {
    countdown.value = {
      years: '00',
      days: '00',
      hours: '00',
      minutes: '00',
      seconds: '00',
      hasYear: false,
      isOver: false,
    }
    return
  }

  if (diff <= 0) {
    diff = 0
  }

  const YEAR_MS = 365 * 24 * 60 * 60 * 1000
  const DAY_MS = 24 * 60 * 60 * 1000

  const years = Math.floor(diff / YEAR_MS)
  let remainder = diff % YEAR_MS

  const days = Math.floor(remainder / DAY_MS)
  remainder = remainder % DAY_MS

  const hours = Math.floor(remainder / 3600000)
  const minutes = Math.floor((remainder % 3600000) / 60000)
  const seconds = Math.floor((remainder % 60000) / 1000)

  countdown.value = {
    years: String(years).padStart(2, '0'),
    days: String(days).padStart(2, '0'),
    hours: String(hours).padStart(2, '0'),
    minutes: String(minutes).padStart(2, '0'),
    seconds: String(seconds).padStart(2, '0'),
    hasYear: years > 0,
    isOver: diff === 0,
  }
}

// 监听props.tasks变化，当父组件刷新数据后，清空临时存储，使用最新的props数据
watch(
  () => props.tasks,
  () => {
    // 父组件刷新数据后，清空临时存储，使用最新的props数据
    latestActiveTask.value = null
    updateCountdown()
  },
  { deep: true },
)

watch(
  () => currentExecutingTask.value?.id,
  () => {
    updateCountdown()
  },
)

onMounted(() => {
  // 初始化时获取最新的执行中任务
  refreshActiveTask()
  updateCountdown()
  countdownTimer = window.setInterval(updateCountdown, 1000)
})

onBeforeUnmount(() => {
  if (countdownTimer !== null) {
    window.clearInterval(countdownTimer)
  }
})

const headerStats = computed(() => {
  const total = props.tasks.length
  let inProgress = 0
  let completed = 0
  let interrupted = 0

  props.tasks.forEach((task) => {
    if (task.status === 'IN_PROGRESS') inProgress += 1
    if (task.status === 'COMPLETED') completed += 1
    if (task.status === 'INTERRUPTED') interrupted += 1
  })

  return {
    total,
    inProgress,
    completed,
    interrupted,
  }
})

// 状态颜色映射
const statusColors: Record<string, string> = {
  PENDING: '#909399', // 灰色 - 待开始
  IN_PROGRESS: '#67C23A', // 绿色 - 进行中
  COMPLETED: '#409EFF', // 蓝色 - 已完成
  INTERRUPTED: '#F56C6C', // 红色 - 中断
  PAUSED: '#E6A23C', // 橙色 - 暂停
}

const statusLabels: Record<string, string> = {
  PENDING: '待开始',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  INTERRUPTED: '中断',
  PAUSED: '暂停',
}

// 优先级颜色
const priorityColors: Record<string, string> = {
  P0: '#F56C6C',
  P1: '#E6A23C',
  P2: '#409EFF',
  P3: '#67C23A',
}

// 饼图配置 - 按状态分布
const pieChartOption = computed(() => {
  // 后端已提供图表统计，前端仅负责渲染
  const { stats } = props
  const statusData = stats
    ? [
        {
          name: '待开始',
          value: Math.max(
            0,
            stats.totalTasks - stats.completedTasks - stats.inProgressTasks - stats.interruptedTasks,
          ),
        },
        { name: '进行中', value: stats.inProgressTasks },
        { name: '已完成', value: stats.completedTasks },
        { name: '中断', value: stats.interruptedTasks },
      ].filter((item) => item.value > 0)
    : []

  return {
    title: {
      text: '任务状态分布',
      left: 'center',
      textStyle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#fff',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      textStyle: {
        fontSize: 14,
      },
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: {
        fontSize: 14,
        color: '#fff',
      },
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#1a1a1a',
          borderWidth: 2,
        },
        label: {
          show: true,
          fontSize: 14,
          color: '#fff',
          fontWeight: 'bold',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
          },
        },
        data: statusData.map((item, index) => ({
          ...item,
          itemStyle: {
            color: [
              statusColors.PENDING,
              statusColors.IN_PROGRESS,
              statusColors.COMPLETED,
              statusColors.INTERRUPTED,
              statusColors.PAUSED,
            ][index],
          },
        })),
      },
    ],
  }
})

// 折线图配置 - 按天统计完成任务（不区分模块）
const dailyLineChartOption = computed(() => {
  // 后端已提供按天统计序列
  const timeline = props.stats?.timeline || []
  const dates = timeline.map((item) => item.date)
  const values = timeline.map((item) => item.completedTasks)

  return {
    title: {
      text: '按天完成任务趋势',
      left: 'center',
      textStyle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#fff',
      },
    },
    tooltip: {
      trigger: 'axis',
      textStyle: {
        fontSize: 14,
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        fontSize: 12,
        color: '#fff',
      },
      axisLine: {
        lineStyle: {
          color: '#fff',
        },
      },
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 12,
        color: '#fff',
      },
      axisLine: {
        lineStyle: {
          color: '#fff',
        },
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.1)',
        },
      },
    },
    series: [
      {
        name: '已完成任务数',
        type: 'line',
        data: values,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          color: '#67C23A',
          width: 3,
        },
        itemStyle: {
          color: '#67C23A',
          borderColor: '#ecfccb',
          borderWidth: 2,
        },
        areaStyle: {
          color: 'rgba(103, 194, 58, 0.25)',
        },
      },
    ],
  }
})

// 悬停显示的任务详情
const hoveredTask = ref<TodoTask | null>(null)
const hoverPosition = ref({ x: 0, y: 0 })

const showTaskDetail = (task: TodoTask, event: MouseEvent) => {
  hoveredTask.value = task
  hoverPosition.value = { x: event.clientX, y: event.clientY }
}

const hideTaskDetail = () => {
  hoveredTask.value = null
}

// 格式化日期
const formatDueDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 格式化时长
const formatDuration = (ms: number) => {
  if (!ms) return '0分'
  const totalSeconds = Math.floor(ms / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const parts = []
  if (hours) parts.push(`${hours}时`)
  if (minutes) parts.push(`${minutes}分`)
  if (!hours && !minutes) parts.push('0分')
  return parts.join('')
}

// 任务操作：开始 / 暂停 / 完成（操作完成后通知父组件刷新数据）
const handleStart = async (task: TodoTask) => {
  try {
    const updatedTask = await todoApi.startTask(task.id)
    ElMessage.success('任务已开始')
    // 立即获取最新的执行中任务，更新倒计时
    await refreshActiveTask()
    emit('refresh')
  } catch (error) {
    ElMessage.error('开始任务失败，请稍后重试')
  }
}

const handlePause = async (task: TodoTask) => {
  try {
    await todoApi.pauseTask(task.id)
    ElMessage.success('任务已暂停')
    // 立即获取最新的执行中任务，更新倒计时
    await refreshActiveTask()
    emit('refresh')
  } catch (error) {
    ElMessage.error('暂停任务失败，请稍后重试')
  }
}

const handleComplete = async (task: TodoTask) => {
  try {
    await todoApi.completeTask(task.id)
    ElMessage.success('任务已完成')
    // 立即获取最新的执行中任务，更新倒计时
    await refreshActiveTask()
    emit('refresh')
  } catch (error) {
    ElMessage.error('完成任务失败，请稍后重试')
  }
}

// 刷新当前执行中的任务，用于更新倒计时
const refreshActiveTask = async () => {
  try {
    const activeTask = await todoApi.getActiveTask()
    // 立即更新临时存储的任务，触发倒计时更新
    latestActiveTask.value = activeTask
    // 触发倒计时立即更新
    updateCountdown()
  } catch (error) {
    // 如果获取失败，清空临时存储，使用props.tasks中的数据
    latestActiveTask.value = null
  }
}
</script>

<template>
  <div class="dashboard-container">
    <!-- 顶部总体统计 + 右侧用户 / 通知 -->
    <div class="dashboard-header">
      <div class="header-stats">
        <div class="stat-card">
          <div class="stat-label">总任务</div>
          <div class="stat-value">{{ headerStats.total }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">进行中</div>
          <div class="stat-value">{{ headerStats.inProgress }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ headerStats.completed }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">中断</div>
          <div class="stat-value">{{ headerStats.interrupted }}</div>
        </div>
      </div>

      <!-- 中间：当前执行中任务截止倒计时翻页时钟（仅时钟，无文本） -->
      <div class="header-countdown">
        <div v-if="currentExecutingTask" class="flip-clock">
          <div v-if="countdown.hasYear" class="flip-unit flip-unit--year">
            <div class="flip-number" :class="{ 'is-over': countdown.isOver }">
              {{ countdown.years }}
            </div>
            <div class="flip-label">年</div>
          </div>
          <div v-if="countdown.hasYear" class="flip-separator">:</div>
          <div class="flip-unit">
            <div class="flip-number" :class="{ 'is-over': countdown.isOver }">
              {{ countdown.days }}
            </div>
            <div class="flip-label">天</div>
          </div>
          <div class="flip-separator">:</div>
          <div class="flip-unit">
            <div class="flip-number" :class="{ 'is-over': countdown.isOver }">
              {{ countdown.hours }}
            </div>
            <div class="flip-label">时</div>
          </div>
          <div class="flip-separator">:</div>
          <div class="flip-unit">
            <div class="flip-number" :class="{ 'is-over': countdown.isOver }">
              {{ countdown.minutes }}
            </div>
            <div class="flip-label">分</div>
          </div>
          <div class="flip-separator">:</div>
          <div class="flip-unit">
            <div class="flip-number" :class="{ 'is-over': countdown.isOver }">
              {{ countdown.seconds }}
            </div>
            <div class="flip-label">秒</div>
          </div>
        </div>
        <div v-else class="flip-clock flip-clock--empty">
          <div class="flip-unit">
            <div class="flip-number">--</div>
            <div class="flip-label">天</div>
          </div>
          <div class="flip-separator">:</div>
          <div class="flip-unit">
            <div class="flip-number">--</div>
            <div class="flip-label">时</div>
          </div>
          <div class="flip-separator">:</div>
          <div class="flip-unit">
            <div class="flip-number">--</div>
            <div class="flip-label">分</div>
          </div>
          <div class="flip-separator">:</div>
          <div class="flip-unit">
            <div class="flip-number">--</div>
            <div class="flip-label">秒</div>
          </div>
        </div>
      </div>

      <!-- 右侧：大屏内嵌的用户信息和通知铃铛 -->
      <div class="header-right">
        <el-badge
          :value="notificationStore.unreadCount"
          :hidden="notificationStore.unreadCount === 0"
          class="header-notification-badge"
        >
          <el-button
            circle
            class="header-notification-button"
            :icon="Bell"
          />
        </el-badge>
        <div class="header-user">
          <el-avatar :size="40" class="header-user-avatar">
            {{ authStore.user?.name?.charAt(0) ?? 'G' }}
          </el-avatar>
          <div class="header-user-info">
            <div class="header-user-name">
              {{ authStore.user?.name ?? '访客' }}
            </div>
            <div class="header-user-role">
              {{ authStore.user?.roles?.join(', ') || '未设置角色' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体区域：左侧当前模块任务，右侧图表 -->
    <div class="dashboard-main">
      <!-- 左侧：当前模块任务列表（优先展示） -->
      <div class="current-module-panel">
        <div class="current-module-header">
          <div class="current-module-title-block">
            <div class="current-module-name">
              {{ currentModule?.name || '未选择模块' }}
            </div>
            <div class="current-module-select-wrap" v-if="modules.length">
              <el-select
                v-model="selectedModuleId"
                placeholder="选择模块"
                size="large"
                class="current-module-select"
                popper-class="current-module-select-popper"
                :suffix-icon="ArrowDownBold"
              >
                <el-option
                  v-for="module in modules"
                  :key="module.id"
                  :label="`${module.name}（${tasksByModule.get(module.id)?.length ?? 0} 条）`"
                  :value="module.id"
                />
              </el-select>
            </div>
          </div>
        </div>

        <div class="current-module-tasks">
          <div
            v-for="task in paginatedCurrentModuleTodoTasks"
            :key="task.id"
            class="current-task-item"
            @mouseenter="showTaskDetail(task, $event)"
            @mouseleave="hideTaskDetail"
          >
            <div
              class="current-task-status-dot"
              :style="{ backgroundColor: statusColors[task.status] }"
            />
            <div class="current-task-main">
              <div class="current-task-title-row">
                <span class="current-task-title">{{ task.title }}</span>
                <span
                  class="current-task-priority"
                  :style="{ color: priorityColors[task.priority] }"
                >
                  {{ task.priority }}
                </span>
              </div>
              <div class="current-task-meta-row">
                <span class="meta-item">
                  状态：{{ statusLabels[task.status] }}
                </span>
                <span v-if="task.dueDate" class="meta-item">
                  截止：{{ formatDueDate(task.dueDate) }}
                </span>
                <span class="meta-item">
                  耗时：{{ formatDuration(task.durationMs) }}
                </span>
              </div>
            </div>
            <div class="current-task-actions">
              <el-button
                v-if="task.status !== 'IN_PROGRESS'"
                size="small"
                type="primary"
                plain
                @click="handleStart(task)"
              >
                开始
              </el-button>
              <el-button
                v-else
                size="small"
                type="warning"
                plain
                @click="handlePause(task)"
              >
                暂停
              </el-button>
              <el-button
                v-if="task.status !== 'COMPLETED'"
                size="small"
                type="success"
                plain
                @click="handleComplete(task)"
              >
                已完成
              </el-button>
            </div>
          </div>
          <div v-if="currentModuleTodoTasks.length === 0" class="current-module-empty">
            当前模块暂无任务
          </div>
          <div v-else class="current-module-footer">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="currentModuleTodoTasks.length"
              background
              layout="prev, pager, next"
              class="task-pagination"
            />
          </div>
        </div>
      </div>

      <!-- 右侧：图表区域 -->
      <div class="charts-panel">
        <div class="chart-card">
          <v-chart :option="dailyLineChartOption" class="chart" />
        </div>
        <div class="chart-card">
          <v-chart :option="pieChartOption" class="chart" />
        </div>
      </div>
    </div>

    <!-- 任务详情悬浮提示 -->
    <div
      v-if="hoveredTask"
      class="task-tooltip"
      :style="{ left: `${hoverPosition.x + 10}px`, top: `${hoverPosition.y + 10}px` }"
    >
      <div class="tooltip-header">
        <div class="tooltip-title">{{ hoveredTask.title }}</div>
        <div
          class="tooltip-status"
          :style="{ backgroundColor: statusColors[hoveredTask.status] }"
        >
          {{ statusLabels[hoveredTask.status] }}
        </div>
      </div>
      <div class="tooltip-content">
        <div class="tooltip-row">
          <span class="tooltip-label">模块:</span>
          <span class="tooltip-value">{{ hoveredTask.moduleName || '-' }}</span>
        </div>
        <div class="tooltip-row">
          <span class="tooltip-label">优先级:</span>
          <span
            class="tooltip-value"
            :style="{ color: priorityColors[hoveredTask.priority] }"
          >
            {{ hoveredTask.priority }}
          </span>
        </div>
        <div v-if="hoveredTask.dueDate" class="tooltip-row">
          <span class="tooltip-label">截止日期:</span>
          <span class="tooltip-value">{{ formatDueDate(hoveredTask.dueDate) }}</span>
        </div>
        <div class="tooltip-row">
          <span class="tooltip-label">耗时:</span>
          <span class="tooltip-value">{{ formatDuration(hoveredTask.durationMs) }}</span>
        </div>
        <div v-if="hoveredTask.description" class="tooltip-row">
          <span class="tooltip-label">描述:</span>
          <span class="tooltip-value">{{ hoveredTask.description }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  padding: 16px 20px;
  overflow: hidden;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.dashboard-header {
  flex-shrink: 0;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-stats {
  display: flex;
  gap: 20px;
}

.header-countdown {
  flex: 1;
  margin: 0 24px;
  padding: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
}

.flip-clock {
  display: flex;
  align-items: center;
  gap: 16px;
}

.flip-unit {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.flip-number {
  min-width: 60px;
  padding: 8px 12px;
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.9); /* 与大屏主色接近的深色 */
  border: 1px solid rgba(30, 64, 175, 0.6);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.6);
  text-shadow: 0 0 6px rgba(15, 23, 42, 0.9);
}

.flip-number.is-over {
  background: rgba(15, 23, 42, 0.9);
  border-color: rgba(248, 113, 113, 0.8);
  text-shadow: 0 0 6px rgba(248, 113, 113, 0.8);
}

.flip-label {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(209, 213, 219, 0.9);
}

.flip-separator {
  font-size: 24px;
  font-weight: 700;
  color: rgba(249, 250, 251, 0.9);
  padding-bottom: 8px;
}

.flip-clock--empty .flip-number {
  opacity: 0.4;
}

.stat-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px 32px;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 150px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-notification-badge {
  display: flex;
  align-items: center;
}

.header-notification-button {
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  background: rgba(0, 0, 0, 0.2);
  color: #fff;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-user-avatar {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  color: #fff;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.45);
}

.header-user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.header-user-name {
  font-size: 14px;
  font-weight: 600;
}

.header-user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.dashboard-main {
  flex: 1;
  min-height: 0;
  display: flex;
  gap: 16px;
}

.current-module-panel {
  flex: 1.4;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  padding: 16px 16px 10px;
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.current-module-header {
  flex-shrink: 0;
  margin-bottom: 10px;
}

.current-module-title-block {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.current-module-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.current-module-select-wrap {
  min-width: 220px;
}

.current-module-select {
  width: 100%;
}

.current-module-select :deep(.el-select__wrapper) {
  background-color: rgba(0, 0, 0, 0.32) !important;
  border: none;
  border-radius: 999px;
  padding: 4px 14px;
  box-shadow: none !important;
  transition: all 0.2s ease;
}

.current-module-select :deep(.el-select__selection) {
  color: rgba(255, 255, 255, 0.92);
  font-size: 13px;
}

.current-module-select :deep(.el-select__placeholder) {
  color: rgba(255, 255, 255, 0.7);
}

.current-module-select :deep(.el-select__suffix) {
  color: rgba(255, 255, 255, 0.85);
}

.current-module-select :deep(.el-select__wrapper.is-focused),
.current-module-select :deep(.el-select__wrapper:hover) {
  border: 1px solid rgba(64, 158, 255, 0.8);
  background-color: rgba(0, 0, 0, 0.5) !important;
}

.current-module-badges {
  display: flex;
  gap: 8px;
  font-size: 12px;
}

.badge {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.current-module-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

:deep(.current-module-select-popper) {
  border-radius: 10px;
  overflow: hidden;
}

:deep(.current-module-select-popper .el-select-dropdown) {
  background-color: rgba(0, 0, 0, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

:deep(.current-module-select-popper .el-select-dropdown__item) {
  font-size: 13px;
  color: #fff;
  background-color: transparent;
}

:deep(.current-module-select-popper .el-select-dropdown__item.hover),
:deep(.current-module-select-popper .el-select-dropdown__item:hover) {
  background-color: rgba(255, 255, 255, 0.06);
}

:deep(.current-module-select-popper .el-select-dropdown__item.is-selected) {
  background-color: rgba(64, 158, 255, 0.22);
  color: #fff;
}

.module-switcher {
  margin: 6px 0 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.module-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  transition: all 0.15s ease;
}

.module-pill:hover {
  background: rgba(0, 0, 0, 0.32);
  border-color: rgba(255, 255, 255, 0.32);
  transform: translateY(-0.5px);
}

.module-pill.active {
  background: rgba(64, 158, 255, 0.22);
  border-color: #409eff;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.35);
}

.module-pill-name {
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.module-pill-count {
  padding: 1px 6px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.35);
  font-size: 11px;
}

.current-module-tasks {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow: hidden;
}

.current-task-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.14);
  transition: background 0.2s, transform 0.15s;
  font-size: 14px;
}

.current-task-item:hover {
  background: rgba(0, 0, 0, 0.24);
  transform: translateY(-1px);
}

.current-task-status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 4px;
  box-shadow: 0 0 6px currentColor;
}

.current-task-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.current-task-title-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.current-task-title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.current-task-priority {
  font-size: 12px;
  font-weight: 600;
}

.current-task-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.meta-item {
  white-space: nowrap;
}

.current-module-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.current-module-tip {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
}

.current-module-footer {
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.task-pagination {
  --el-pagination-bg-color: rgba(0, 0, 0, 0.3);
  --el-pagination-hover-color: #409eff;
  --el-pagination-text-color: rgba(255, 255, 255, 0.8);

  padding: 0;
}

.task-pagination :deep(.el-pager li) {
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
}

.task-pagination :deep(.el-pager li.is-active) {
  background-color: #409eff;
  color: #fff;
}

.charts-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-card {
  flex: 1;
  min-height: 0;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chart {
  width: 100%;
  height: 100%;
}

.task-tooltip {
  position: fixed;
  background: rgba(0, 0, 0, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 16px;
  z-index: 10000;
  min-width: 300px;
  max-width: 400px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  pointer-events: none;
}

.tooltip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tooltip-title {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  flex: 1;
}

.tooltip-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: #fff;
}

.tooltip-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tooltip-row {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.tooltip-label {
  color: rgba(255, 255, 255, 0.7);
  min-width: 80px;
}

.tooltip-value {
  color: #fff;
  flex: 1;
  word-break: break-word;
}

/* 简单响应：较窄宽度时，图表和列表上下排列 */
@media (max-width: 1440px) {
  .dashboard-main {
    flex-direction: column;
  }

  .current-module-panel {
    flex: 1.2;
  }

  .charts-panel {
    flex: 1;
  }
}
</style>

<!-- 全局样式，确保下拉浮层在挂载到 body 时也能应用到深色大屏主题 -->
<style>
.current-module-select-popper {
  border-radius: 10px;
  overflow: hidden;
  z-index: 4000;
}

.current-module-select-popper .el-select-dropdown {
  background-color: rgba(0, 0, 0, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.16);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.6);
}

.current-module-select-popper .el-select-dropdown__item {
  font-size: 13px;
  color: #fff;
  background-color: transparent;
}

.current-module-select-popper .el-select-dropdown__item.hover,
.current-module-select-popper .el-select-dropdown__item:hover {
  background-color: rgba(255, 255, 255, 0.06);
}

.current-module-select-popper .el-select-dropdown__item.is-selected {
  background-color: rgba(64, 158, 255, 0.22);
  color: #fff;
}
</style>
