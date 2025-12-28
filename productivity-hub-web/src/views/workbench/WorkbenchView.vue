<template>
  <div class="workbench-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">工作台</span>
        </div>
      </template>

      <!-- 待办列表 -->
      <div class="section">
        <div class="section-title">✅ 待办模块 ({{ tasks.length }})</div>
        <TodoList
          :tasks="tasks"
          :loading="loading"
          @complete="handleComplete"
          @delete="handleDelete"
        />
        <el-button
          type="primary"
          class="add-task-button"
          @click="handleAddTask"
        >
          <el-icon><Plus /></el-icon>
          <span>新增待办</span>
        </el-button>
      </div>

      <!-- 常用工具 -->
      <div class="section">
        <div class="section-title">🛠️ 常用工具</div>
        <ToolsGrid :tools="tools" />
      </div>
    </el-card>

    <!-- 新增待办弹窗 -->
    <TodoRecordDialog
      v-model:visible="dialogVisible"
      @success="handleTaskSuccess"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 工作台页面
 * 
 * 关联需求: REQ-009
 * 关联页面: PAGE-REQ-003-01
 * 关联接口: API-REQ-001-04, API-REQ-001-05, API-REQ-001-06, API-REQ-001-02
 */

import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { todoApi } from '@/services/todoApi'
import type { TodoTask } from '@/types/todo'
import TodoList from '@/components/workbench/TodoList.vue'
import ToolsGrid from '@/components/workbench/ToolsGrid.vue'
import TodoRecordDialog from '@/components/quick-record/TodoRecordDialog.vue'

const loading = ref(false)
const tasks = ref<TodoTask[]>([])
const dialogVisible = ref(false)

const tools = [
  { id: 'blueprint', name: 'AI蓝图', icon: '📊', route: '/tools/blueprint' },
  { id: 'cursor', name: 'Cursor', icon: '🖱️', route: '/tools/cursor-inventory' },
  { id: 'calculator', name: '计算器', icon: '🔢', route: '/tools' },
]

const loadTasks = async () => {
  try {
    loading.value = true
    tasks.value = await todoApi.listTasks({ status: 'PENDING' })
  } catch (error: any) {
    ElMessage.error(error.message || '加载待办列表失败')
  } finally {
    loading.value = false
  }
}

const handleComplete = async (id: string) => {
  try {
    await todoApi.completeTask(id)
    ElMessage.success('任务已完成')
    loadTasks()
  } catch (error: any) {
    ElMessage.error(error.message || '完成任务失败')
  }
}

const handleDelete = async (id: string) => {
  try {
    await todoApi.deleteTask(id)
    ElMessage.success('任务已删除')
    loadTasks()
  } catch (error: any) {
    ElMessage.error(error.message || '删除任务失败')
  }
}

const handleAddTask = () => {
  dialogVisible.value = true
}

const handleTaskSuccess = () => {
  loadTasks()
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped lang="scss">
.workbench-view {
  padding: 16px;

  .card-header {
    .title {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .section {
    margin-bottom: 32px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 16px;
    }
  }

  .add-task-button {
    margin-top: 16px;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .workbench-view {
    padding: 12px;
  }
}
</style>

