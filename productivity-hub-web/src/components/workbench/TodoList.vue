<template>
  <div class="todo-list">
    <el-skeleton :loading="loading" animated :rows="5">
      <template #template>
        <div v-for="i in 5" :key="i" style="height: 60px; margin-bottom: 12px;"></div>
      </template>
      <template #default>
        <div v-if="tasks.length === 0" class="empty-state">
          <el-empty description="暂无待办任务" />
        </div>
        <div v-else class="task-items">
          <div
            v-for="task in tasks"
            :key="task.id"
            class="task-item"
            :class="{ completed: task.status === 'COMPLETED' }"
          >
            <el-checkbox
              :model-value="task.status === 'COMPLETED'"
              @change="handleToggleComplete(task)"
              :disabled="task.status === 'COMPLETED'"
            />
            <div class="task-content">
              <div class="task-title">{{ task.title }}</div>
              <div class="task-info">
                <el-tag size="small" :type="getPriorityType(task.priority)">
                  {{ task.priority }}
                </el-tag>
                <span v-if="task.moduleName" class="module-name">{{ task.moduleName }}</span>
                <span v-if="task.dueDate" class="due-date">{{ formatDate(task.dueDate) }}</span>
              </div>
            </div>
            <el-button
              type="danger"
              size="small"
              text
              @click="handleDelete(task)"
            >
              删除
            </el-button>
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup lang="ts">
/**
 * 待办列表组件
 * 
 * 关联需求: REQ-009
 * 关联组件: COMP-REQ-003-01-01
 * 关联接口: API-REQ-001-04, API-REQ-001-05, API-REQ-001-06
 */

import { ElMessageBox } from 'element-plus'
import type { TodoTask } from '@/types/todo'

interface Props {
  tasks: TodoTask[]
  loading?: boolean
}

interface Emits {
  (e: 'complete', id: string): void
  (e: 'delete', id: string): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<Emits>()

const getPriorityType = (priority: string) => {
  const map: Record<string, string> = {
    P0: 'danger',
    P1: 'warning',
    P2: 'info',
    P3: '',
  }
  return map[priority] || ''
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const handleToggleComplete = (task: TodoTask) => {
  if (task.status !== 'COMPLETED') {
    emit('complete', task.id)
  }
}

const handleDelete = async (task: TodoTask) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    emit('delete', task.id)
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
.todo-list {
  .task-items {
    .task-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      border: 1px solid #E8E8E8;
      border-radius: 8px;
      margin-bottom: 12px;
      transition: all 0.3s;

      &.completed {
        opacity: 0.6;
      }

      .task-content {
        flex: 1;

        .task-title {
          font-size: 14px;
          color: #333;
          margin-bottom: 4px;
        }

        .task-info {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 12px;
          color: #999;

          .module-name,
          .due-date {
            margin-left: 4px;
          }
        }
      }

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }
  }

  .empty-state {
    padding: 40px 0;
  }
}
</style>

