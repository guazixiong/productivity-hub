<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { scheduleTaskApi } from '@/services/api'
import type { ScheduleTask } from '@/types/schedule'

const loading = ref(false)
const bulkLoading = ref(false)
const triggeringId = ref<string | null>(null)
const tasks = ref<ScheduleTask[]>([])

const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await scheduleTaskApi.listTasks()
    if ((res as any).success === false) {
      ElMessage.error((res as any).message || '获取任务列表失败')
      return
    }
    tasks.value = (res as any).data ?? res
  } catch (e) {
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

const handleToggle = async (row: ScheduleTask) => {
  try {
    const msg = await scheduleTaskApi.toggleTask({ id: row.id, enabled: row.enabled })
    ElMessage.success(msg || '已更新任务开关')
    await fetchTasks()
  } catch (e) {
    ElMessage.error((e as Error).message || '更新任务开关失败')
    // 回滚
    row.enabled = !row.enabled
  }
}

const handleTrigger = async (row: ScheduleTask) => {
  try {
    await ElMessageBox.confirm(
      `确认立即执行任务「${row.name}」吗？这可能会立刻推送消息或执行相关操作。`,
      '确认执行',
      {
        type: 'warning',
      },
    )
  } catch {
    return
  }

  triggeringId.value = row.id
  try {
    const msg = await scheduleTaskApi.triggerTask(row.id)
    ElMessage.success(msg || '任务已触发')
  } catch (e) {
    ElMessage.error((e as Error).message || '触发任务失败')
  } finally {
    triggeringId.value = null
  }
}

const handleEnableAll = async () => {
  bulkLoading.value = true
  try {
    const msg = await scheduleTaskApi.enableAll()
    ElMessage.success(msg || '已启用所有任务')
    await fetchTasks()
  } catch (e) {
    ElMessage.error((e as Error).message || '一键启用失败')
  } finally {
    bulkLoading.value = false
  }
}

const handleDisableAll = async () => {
  bulkLoading.value = true
  try {
    const msg = await scheduleTaskApi.disableAll()
    ElMessage.success(msg || '已关闭所有任务')
    await fetchTasks()
  } catch (e) {
    ElMessage.error((e as Error).message || '一键关闭失败')
  } finally {
    bulkLoading.value = false
  }
}

onMounted(() => {
  fetchTasks()
})
</script>

<template>
  <el-card class="schedule-card">
    <template #header>
      <div class="card-header">
        <div>
          <h2>定时任务管理</h2>
          <p>集中管理后端定时任务的启停与手动触发</p>
        </div>
        <div class="header-actions">
          <el-button :loading="loading" @click="fetchTasks">刷新</el-button>
          <el-button
            type="success"
            :loading="bulkLoading"
            @click="handleEnableAll"
          >
            一键启用
          </el-button>
          <el-button
            type="danger"
            :loading="bulkLoading"
            @click="handleDisableAll"
          >
            一键关闭
          </el-button>
        </div>
      </div>
    </template>

    <el-table
      :data="tasks"
      :loading="loading"
      stripe
      class="schedule-table"
      empty-text="暂无可管理的定时任务"
    >
      <el-table-column prop="name" label="任务名称" min-width="200" />
      <el-table-column prop="description" label="说明" min-width="260" />
      <el-table-column prop="cron" label="调度规则" width="220" />
      <el-table-column label="启用" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            @change="handleToggle(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :loading="triggeringId === row.id"
            @click="handleTrigger(row)"
          >
            手动执行
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.schedule-card {
  border-radius: 24px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  box-shadow:
    0 20px 60px rgba(15, 23, 42, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.95));
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

.card-header p {
  margin: 4px 0 0;
  color: #64748b;
}

.schedule-table {
  margin-top: 8px;
}
</style>


