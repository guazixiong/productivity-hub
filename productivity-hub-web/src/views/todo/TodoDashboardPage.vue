<script setup lang="ts">
import { onMounted, ref } from 'vue'
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
  }
}

onMounted(() => {
  void loadData()
})
</script>

<template>
  <div class="todo-dashboard-page">
    <div class="todo-dashboard-inner" v-loading="loading">
      <TodoDashboard
        :modules="modules"
        :tasks="tasks"
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
  width: 100%;
  height: 100%;
}
</style>


