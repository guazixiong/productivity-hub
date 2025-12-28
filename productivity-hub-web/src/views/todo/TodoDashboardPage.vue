<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, ref, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { todoApi } from '@/services/todoApi'
import type { TodoModule, TodoTask, TodoStats } from '@/types/todo'
import TodoDashboard from './TodoDashboard.vue'

const route = useRoute()

const loading = ref(false)
const modules = ref<TodoModule[]>([])
const tasks = ref<TodoTask[]>([])
// 大屏统计数据（由后端聚合提供）
const stats = ref<TodoStats | null>(null)

// 当前选中的模块，支持通过路由 query 预选中，例如 /todo/dashboard?moduleId=xxx
const currentModuleId = ref<string>((route.query.moduleId as string) || '')

// 响应式缩放相关
const dashboardInnerRef = ref<HTMLElement | null>(null)

// 基准尺寸（设计稿尺寸）
const BASE_WIDTH = 1920
const BASE_HEIGHT = 1080

// 计算并应用缩放
const updateScale = () => {
  if (!dashboardInnerRef.value) return

  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight

  // 计算宽高缩放比例，取较小值以保持宽高比
  const scaleX = windowWidth / BASE_WIDTH
  const scaleY = windowHeight / BASE_HEIGHT
  const newScale = Math.min(scaleX, scaleY)

  // 应用缩放
  dashboardInnerRef.value.style.transform = `scale(${newScale})`
  dashboardInnerRef.value.style.transformOrigin = 'top left'
  
  // 计算缩放后的实际尺寸
  const scaledWidth = BASE_WIDTH * newScale
  const scaledHeight = BASE_HEIGHT * newScale
  
  // 居中显示（如果屏幕比例不同）
  const offsetX = (windowWidth - scaledWidth) / 2
  const offsetY = (windowHeight - scaledHeight) / 2
  
  dashboardInnerRef.value.style.left = `${offsetX}px`
  dashboardInnerRef.value.style.top = `${offsetY}px`
}

// 窗口大小变化监听
const handleResize = () => {
  updateScale()
}

const loadData = async () => {
  loading.value = true
  try {
    const [moduleList, taskList, overview] = await Promise.all([
      todoApi.listModules(),
      // 拉取所有任务，供大屏展示和统计使用（不分页）
      todoApi.listTasks(),
      // 从后端获取已聚合的大屏统计数据
      todoApi.overview(),
    ])
    modules.value = moduleList
    tasks.value = taskList || []
    stats.value = overview
  } finally {
    loading.value = false
    // 数据加载完成后，等待DOM更新再更新缩放
    await nextTick()
    updateScale()
  }
}

onMounted(() => {
  void loadData()
  // 初始化缩放（DOM渲染后）
  nextTick(() => {
    updateScale()
  })
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

// 大屏只展示未完成任务，避免列表与统计口径混入已完成
const incompleteTasks = computed(() => tasks.value.filter((task) => task.status !== 'COMPLETED'))
</script>

<template>
  <div class="todo-dashboard-page">
    <div ref="dashboardInnerRef" class="todo-dashboard-inner" v-loading="loading">
      <TodoDashboard
        :modules="modules"
        :tasks="incompleteTasks"
        :stats="stats"
        :current-module-id="currentModuleId"
        @refresh="loadData"
      />
    </div>
  </div>
</template>

<style scoped>
.todo-dashboard-page {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  background: #000;
  overflow: hidden;
}

.todo-dashboard-inner {
  position: absolute;
  width: 1920px;
  height: 1080px;
  top: 0;
  left: 0;
  transition: transform 0.3s ease, left 0.3s ease, top 0.3s ease;
}
</style>


