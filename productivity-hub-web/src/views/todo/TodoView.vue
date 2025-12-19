<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  VideoPlay,
  VideoPause,
  CircleCheck,
  Edit,
  Warning,
  Delete,
  ArrowUp,
  CaretTop,
  CaretBottom,
  CaretLeft,
  CaretRight,
  View,
  Plus,
  FullScreen,
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { todoApi } from '@/services/todoApi'
import type { TodoModule, TodoStats, TodoTask } from '@/types/todo'
import TodoDashboard from './TodoDashboard.vue'

const router = useRouter()

const loading = ref(false)
const modules = ref<TodoModule[]>([])
const tasks = ref<TodoTask[]>([])
const stats = ref<TodoStats | null>(null)

const selectedModuleId = ref<string>('')
const statusFilter = ref<string>('')
const modulesCollapsed = ref(false)

// 分页相关
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const usePagination = ref(true) // 是否使用分页

// 动态计算列宽：折叠时左侧变窄，右侧扩展
const leftColSpan = computed(() => modulesCollapsed.value ? 2 : 6)
const rightColSpan = computed(() => modulesCollapsed.value ? 22 : 18)

const taskDialogVisible = ref(false)
const editingTaskId = ref<string | null>(null)
const taskForm = reactive({
  title: '',
  moduleId: '',
  priority: 'P2' as TodoTask['priority'],
  description: '',
  tags: '' as string,
  dueDate: '' as string,
})

const taskDetailDrawerVisible = ref(false)
const viewingTask = ref<TodoTask | null>(null)

const moduleDialogVisible = ref(false)
const editingModuleId = ref<string | null>(null)
const moduleForm = reactive({
  name: '',
  description: '',
  sortOrder: 0,
})

// 大屏模式（改为跳转独立路由）
const dashboardVisible = ref(false) // 保留字段避免破坏现有逻辑/模板，实际不再使用弹窗
const allTasksForDashboard = ref<TodoTask[]>([])
const loadingDashboard = ref(false)
const showDashboardContent = ref(false)

const openDashboardPage = () => {
  // 将当前选择的模块通过 query 传给大屏，方便大屏优先展示该模块
  router.push({
    name: 'TodoDashboardPage',
    query: selectedModuleId.value ? { moduleId: selectedModuleId.value } : {},
  })
}

const activeTask = computed(() => tasks.value.find((t) => t.status === 'IN_PROGRESS') || null)

const statusTagType: Record<TodoTask['status'], string> = {
  PENDING: 'info',
  IN_PROGRESS: 'success',
  PAUSED: 'warning',
  COMPLETED: 'primary',
  INTERRUPTED: 'danger',
}

const priorityTagType: Record<TodoTask['priority'], string> = {
  P0: 'danger',
  P1: 'warning',
  P2: 'info',
  P3: 'success',
}

const formatDuration = (ms: number) => {
  if (!ms) return '0分'
  const totalSeconds = Math.floor(ms / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  const parts = []
  if (hours) parts.push(`${hours}时`)
  if (minutes) parts.push(`${minutes}分`)
  if (!hours && !minutes) parts.push(`${seconds}秒`)
  return parts.join('')
}

const formatDueDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  // 24小时制，HH:mm:ss
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const getDueDateClass = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const dueDate = new Date(date)
  dueDate.setHours(0, 0, 0, 0)
  const diffDays = Math.floor((dueDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  
  if (diffDays < 0) return 'due-date-overdue'
  if (diffDays === 0) return 'due-date-today'
  if (diffDays <= 3) return 'due-date-urgent'
  return 'due-date-normal'
}

const getRowClassName = ({ row }: { row: TodoTask }) => {
  if (row.status === 'IN_PROGRESS') return 'row-in-progress'
  if (row.status === 'COMPLETED') return 'row-completed'
  return ''
}

const sortByDueDate = (a: TodoTask, b: TodoTask) => {
  if (!a.dueDate && !b.dueDate) return 0
  if (!a.dueDate) return 1
  if (!b.dueDate) return -1
  return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime()
}

const sortByPriority = (a: TodoTask, b: TodoTask) => {
  const priorityOrder: Record<TodoTask['priority'], number> = {
    P0: 0,
    P1: 1,
    P2: 2,
    P3: 3,
  }
  return priorityOrder[a.priority] - priorityOrder[b.priority]
}

const openTaskDetail = (task: TodoTask) => {
  viewingTask.value = task
  taskDetailDrawerVisible.value = true
}

const handleActionCommand = async (command: string, task: TodoTask) => {
  switch (command) {
    case 'view':
      openTaskDetail(task)
      break
    case 'start':
    case 'resume':
      await handleStart(task)
      break
    case 'pause':
      await handlePause(task)
      break
    case 'complete':
      await handleComplete(task)
      break
    case 'edit':
      openTaskDialog(task)
      break
    case 'interrupt':
      await handleInterrupt(task)
      break
    case 'delete':
      await confirmDeleteTask(task)
      break
  }
}

const loadModules = async () => {
  const data = await todoApi.listModules()
  modules.value = data || []
  if (!selectedModuleId.value && modules.value.length > 0) {
    selectedModuleId.value = modules.value[0].id
  }
}

const loadTasks = async () => {
  loading.value = true
  try {
    if (usePagination.value) {
      const data = await todoApi.listTasksWithPage({
        moduleId: selectedModuleId.value || undefined,
        status: statusFilter.value || undefined,
        pageNum: pageNum.value,
        pageSize: pageSize.value,
      })
      tasks.value = data?.items || []
      total.value = data?.total || 0
    } else {
      const data = await todoApi.listTasks({
        moduleId: selectedModuleId.value || undefined,
        status: statusFilter.value || undefined,
      })
      tasks.value = data || []
      total.value = data?.length || 0
    }
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  const data = await todoApi.overview()
  stats.value = data || null
}

const refreshAll = async () => {
  await Promise.all([loadModules(), loadTasks(), loadStats()])
}

onMounted(() => {
  refreshAll()
})

watch([selectedModuleId, statusFilter], () => {
  pageNum.value = 1 // 切换筛选条件时重置到第一页
  loadTasks()
})

watch([pageNum, pageSize], () => {
  if (usePagination.value) {
    loadTasks()
  }
})

const openTaskDialog = (task?: TodoTask) => {
  editingTaskId.value = task?.id || null
  taskDialogVisible.value = true
  taskForm.title = task?.title || ''
  taskForm.moduleId = task?.moduleId || selectedModuleId.value || ''
  taskForm.priority = task?.priority || 'P2'
  taskForm.description = task?.description || ''
  taskForm.tags = (task?.tags || []).join(', ')
  taskForm.dueDate = task?.dueDate || ''
}

const submitTask = async () => {
  if (!taskForm.title) {
    ElMessage.warning('请输入任务标题')
    return
  }
  const payload = {
    title: taskForm.title,
    moduleId: taskForm.moduleId,
    priority: taskForm.priority,
    description: taskForm.description,
    tags: taskForm.tags
      ? taskForm.tags
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean)
      : [],
    dueDate: taskForm.dueDate || undefined,
  }
  if (editingTaskId.value) {
    await todoApi.updateTask(editingTaskId.value, { id: editingTaskId.value, ...payload })
    ElMessage.success('任务已更新')
  } else {
    await todoApi.createTask(payload)
    ElMessage.success('任务已创建')
  }
  taskDialogVisible.value = false
  await loadTasks()
  await loadStats()
}

const confirmDeleteTask = async (task: TodoTask) => {
  await ElMessageBox.confirm(`确定删除任务「${task.title}」吗？`, '提示', { type: 'warning' })
  await todoApi.deleteTask(task.id)
  ElMessage.success('已删除')
  await loadTasks()
  await loadStats()
}

const handleStart = async (task: TodoTask) => {
  await todoApi.startTask(task.id)
  await loadTasks()
  await loadStats()
}

const handlePause = async (task: TodoTask) => {
  await todoApi.pauseTask(task.id)
  await loadTasks()
  await loadStats()
}

const handleComplete = async (task: TodoTask) => {
  await todoApi.completeTask(task.id)
  await loadTasks()
  await loadStats()
}

const handleInterrupt = async (task: TodoTask) => {
  await todoApi.interruptTask(task.id, { reason: 'manual' })
  await loadTasks()
  await loadStats()
}

const openModuleDialog = (module?: TodoModule) => {
  editingModuleId.value = module?.id || null
  moduleDialogVisible.value = true
  moduleForm.name = module?.name || ''
  moduleForm.description = module?.description || ''
  moduleForm.sortOrder = module?.sortOrder || 0
}

const submitModule = async () => {
  if (!moduleForm.name) {
    ElMessage.warning('请输入模块名称')
    return
  }
  if (editingModuleId.value) {
    await todoApi.updateModule(editingModuleId.value, { id: editingModuleId.value, ...moduleForm })
    ElMessage.success('模块已更新')
  } else {
    await todoApi.createModule(moduleForm)
    ElMessage.success('模块已创建')
  }
  moduleDialogVisible.value = false
  await loadModules()
}

const confirmDeleteModule = async (module: TodoModule) => {
  await ElMessageBox.confirm(`删除模块「${module.name}」前需要清空关联任务，继续吗？`, '提示', {
    type: 'warning',
  })
  await todoApi.deleteModule(module.id)
  ElMessage.success('模块已删除')
  if (selectedModuleId.value === module.id) {
    selectedModuleId.value = ''
  }
  await loadModules()
  await loadTasks()
}

// 打开大屏模式（改为新窗口打开独立大屏路由）
const openDashboard = () => {
  const url = router.resolve({
    name: 'TodoDashboardPage',
    query: selectedModuleId.value ? { moduleId: selectedModuleId.value } : {},
  }).href
  window.open(url, '_blank')
}
</script>

<template>
  <div class="todo-page">
    <div class="todo-layout">
      <div :class="['todo-left-col', { collapsed: modulesCollapsed }]">
        <el-card :class="['module-card', { collapsed: modulesCollapsed }]" shadow="hover">
          <!-- 折叠状态：竖条样式 -->
          <div v-if="modulesCollapsed" class="module-collapsed-bar">
            <div class="module-vertical-text">模块</div>
            <el-button
              text
              size="small"
              class="collapse-btn"
              @click="modulesCollapsed = !modulesCollapsed"
            >
              <el-icon>
                <CaretRight />
              </el-icon>
            </el-button>
          </div>
          <!-- 展开状态：正常显示 -->
          <div v-else>
            <div class="card-header">
              <div class="card-header-left">
                <span>模块</span>
                <el-button
                  text
                  size="small"
                  class="collapse-btn"
                  @click="modulesCollapsed = !modulesCollapsed"
                >
                  <el-icon>
                    <CaretLeft />
                  </el-icon>
                </el-button>
              </div>
              <el-button type="primary" text @click="openModuleDialog()">新建模块</el-button>
            </div>
            <el-scrollbar class="module-scroll">
              <el-menu :default-active="selectedModuleId" class="module-menu">
                <el-menu-item index="" @click="selectedModuleId = ''">
                  <div class="module-item">
                    <div class="module-title">全部</div>
                    <div class="module-meta">
                      <span>任务 {{ stats?.totalTasks ?? 0 }}</span>
                    </div>
                  </div>
                </el-menu-item>
                <el-menu-item
                  v-for="item in modules"
                  :key="item.id"
                  :index="item.id"
                  @click="selectedModuleId = item.id"
                >
                  <div class="module-item">
                    <div class="module-title">
                      {{ item.name }}
                      <el-tag size="small" class="ml-4">{{ item.status || 'ENABLED' }}</el-tag>
                    </div>
                    <div class="module-meta">
                      <span>任务 {{ item.totalTasks ?? 0 }}</span>
                      <span>完成 {{ item.completedTasks ?? 0 }}</span>
                    </div>
                    <div class="module-actions">
                      <el-button text size="small" @click.stop="openModuleDialog(item)">编辑</el-button>
                      <el-button text size="small" type="danger" @click.stop="confirmDeleteModule(item)">删除</el-button>
                    </div>
                  </div>
                </el-menu-item>
              </el-menu>
            </el-scrollbar>
          </div>
        </el-card>
      </div>
      <div :class="['todo-right-col', { expanded: modulesCollapsed }]">
        <el-card class="summary-card" shadow="hover">
          <div class="summary">
            <div class="summary-item">
              <div class="label">总任务</div>
              <div class="value">{{ stats?.totalTasks ?? 0 }}</div>
            </div>
            <div class="summary-item">
              <div class="label">进行中</div>
              <div class="value">{{ stats?.inProgressTasks ?? 0 }}</div>
            </div>
            <div class="summary-item">
              <div class="label">已完成</div>
              <div class="value">{{ stats?.completedTasks ?? 0 }}</div>
            </div>
            <div class="summary-item">
              <div class="label">中断</div>
              <div class="value">{{ stats?.interruptedTasks ?? 0 }}</div>
            </div>
            <div class="summary-item">
              <div class="label">总耗时</div>
              <div class="value">{{ formatDuration(stats?.totalDurationMs || 0) }}</div>
            </div>
            <div class="actions">
              <el-button type="primary" @click="openTaskDialog()">
                <el-icon><Plus /></el-icon>
                新增任务
              </el-button>
              <el-button type="success" @click="openDashboard()">
                <el-icon><FullScreen /></el-icon>
                大屏模式
              </el-button>
              <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px">
                <el-option label="待开始" value="PENDING" />
                <el-option label="进行中" value="IN_PROGRESS" />
                <el-option label="暂停" value="PAUSED" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="中断" value="INTERRUPTED" />
              </el-select>
            </div>
          </div>
          <div v-if="activeTask" class="active-banner">
            <div>
              <div class="label">当前进行中</div>
              <div class="value">{{ activeTask.title }}</div>
              <div class="muted">模块：{{ activeTask.moduleName || '-' }} · 优先级：{{ activeTask.priority }}</div>
            </div>
            <div class="banner-actions">
              <el-button size="small" @click="handlePause(activeTask)">暂停</el-button>
              <el-button size="small" type="success" @click="handleComplete(activeTask)">完成</el-button>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="mt-12 task-list-card">
          <div class="table-header">
            <div class="table-title">任务列表</div>
          </div>
          <el-table 
            :data="tasks" 
            v-loading="loading" 
            class="task-table"
            stripe
            :row-class-name="getRowClassName"
            border
            size="default"
          >
            <el-table-column prop="title" label="任务标题" min-width="250" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="task-title-cell">
                  <span class="task-title">{{ row.title }}</span>
                  <el-tag v-if="row.tags && row.tags.length > 0" size="small" effect="plain" class="ml-8">
                    {{ row.tags.length }}个标签
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="moduleName" label="所属模块" width="160" show-overflow-tooltip />
            <el-table-column prop="priority" label="优先级" width="80" sortable="custom" :sort-method="sortByPriority" align="center">
              <template #default="{ row }">
                <el-tag :type="priorityTagType[row.priority]" size="small" effect="dark">
                  {{ row.priority }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagType[row.status]" size="small">
                  {{
                    {
                      PENDING: '待开始',
                      IN_PROGRESS: '进行中',
                      PAUSED: '暂停',
                      COMPLETED: '已完成',
                      INTERRUPTED: '中断',
                    }[row.status]
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="截止日期" width="180" sortable="custom" :sort-method="sortByDueDate">
              <template #default="{ row }">
                <div class="due-date-cell">
                  <span v-if="row.dueDate" :class="getDueDateClass(row.dueDate)">
                    {{ formatDueDate(row.dueDate) }}
                  </span>
                  <span v-else class="no-due-date">-</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="耗时" width="110" align="center">
              <template #default="{ row }">
                <span class="duration-text">{{ formatDuration(row.durationMs) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-dropdown trigger="click" @command="(cmd) => handleActionCommand(cmd, row)">
                  <el-button type="primary" size="small">
                    <span>操作</span>
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="view">
                        <el-icon class="dropdown-icon"><View /></el-icon>
                        <span>详情</span>
                      </el-dropdown-item>
                      <template v-if="row.status !== 'IN_PROGRESS'">
                        <el-dropdown-item :command="row.status === 'PAUSED' ? 'resume' : 'start'">
                          <el-icon class="dropdown-icon"><VideoPlay /></el-icon>
                          <span>{{ row.status === 'PAUSED' ? '恢复' : '开始' }}</span>
                        </el-dropdown-item>
                      </template>
                      <template v-if="row.status === 'IN_PROGRESS'">
                        <el-dropdown-item command="pause">
                          <el-icon class="dropdown-icon"><VideoPause /></el-icon>
                          <span>暂停</span>
                        </el-dropdown-item>
                      </template>
                      <template v-if="row.status === 'IN_PROGRESS' || row.status === 'PAUSED'">
                        <el-dropdown-item command="complete">
                          <el-icon class="dropdown-icon"><CircleCheck /></el-icon>
                          <span>完成</span>
                        </el-dropdown-item>
                      </template>
                      <el-dropdown-item command="edit" divided>
                        <el-icon class="dropdown-icon"><Edit /></el-icon>
                        <span>编辑</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="interrupt">
                        <el-icon class="dropdown-icon"><Warning /></el-icon>
                        <span>中断</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>
                        <el-icon class="dropdown-icon"><Delete /></el-icon>
                        <span style="color: var(--el-color-danger)">删除</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="usePagination" class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadTasks"
              @current-change="loadTasks"
            />
          </div>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="taskDialogVisible" :title="editingTaskId ? '编辑任务' : '新建任务'" width="520px">
      <el-form label-width="90px" @submit.prevent>
        <el-form-item label="标题" required>
          <el-input 
            v-model="taskForm.title" 
            placeholder="任务标题" 
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="模块" required>
          <el-select v-model="taskForm.moduleId" placeholder="选择模块" style="width: 100%">
            <el-option v-for="m in modules" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="taskForm.priority" style="width: 100%">
            <el-option label="P0" value="P0" />
            <el-option label="P1" value="P1" />
            <el-option label="P2" value="P2" />
            <el-option label="P3" value="P3" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="taskForm.dueDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择截止日期和时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input 
            v-model="taskForm.tags" 
            placeholder="多个标签以逗号分隔" 
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="taskForm.description" 
            type="textarea" 
            rows="3" 
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="taskDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTask" v-button-lock>保存</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog v-model="moduleDialogVisible" :title="editingModuleId ? '编辑模块' : '新建模块'" width="480px">
      <el-form label-width="90px">
        <el-form-item label="名称" required>
          <el-input 
            v-model="moduleForm.name" 
            placeholder="模块名称" 
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="moduleForm.description" 
            type="textarea" 
            rows="3" 
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="moduleForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="moduleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitModule" v-button-lock>保存</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-drawer v-model="taskDetailDrawerVisible" title="任务详情" size="50%" direction="rtl">
      <template v-if="viewingTask">
        <el-descriptions :column="2" border class="task-detail-descriptions">
          <el-descriptions-item label="任务标题" :span="2">
            <div style="font-weight: 600; font-size: 16px;">{{ viewingTask.title }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="所属模块">
            <el-tag type="primary" size="large">{{ viewingTask.moduleName || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="priorityTagType[viewingTask.priority]" size="large" effect="dark">
              {{ viewingTask.priority }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType[viewingTask.status]" size="large">
              {{
                {
                  PENDING: '待开始',
                  IN_PROGRESS: '进行中',
                  PAUSED: '暂停',
                  COMPLETED: '已完成',
                  INTERRUPTED: '中断',
                }[viewingTask.status]
              }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="截止日期">
            <span v-if="viewingTask.dueDate" :class="getDueDateClass(viewingTask.dueDate)">
              {{ formatDueDate(viewingTask.dueDate) }}
            </span>
            <span v-else class="no-due-date">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">
            <span class="duration-text">{{ formatDuration(viewingTask.durationMs) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">
            <div v-if="viewingTask.tags && viewingTask.tags.length > 0" style="display: flex; flex-wrap: wrap; gap: 8px;">
              <el-tag v-for="tag in viewingTask.tags" :key="tag" size="small" effect="plain">
                {{ tag }}
              </el-tag>
            </div>
            <span v-else class="no-due-date">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            <div v-if="viewingTask.description" style="white-space: pre-wrap; word-break: break-word;">
              {{ viewingTask.description }}
            </div>
            <span v-else class="no-due-date">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            <span v-if="viewingTask.startedAt">{{ formatDueDate(viewingTask.startedAt) }}</span>
            <span v-else class="no-due-date">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            <span v-if="viewingTask.endedAt">{{ formatDueDate(viewingTask.endedAt) }}</span>
            <span v-else class="no-due-date">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            <span v-if="viewingTask.createdAt">{{ formatDueDate(viewingTask.createdAt) }}</span>
            <span v-else class="no-due-date">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            <span v-if="viewingTask.updatedAt">{{ formatDueDate(viewingTask.updatedAt) }}</span>
            <span v-else class="no-due-date">-</span>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- 大屏模式改为独立路由页面，此处不再使用对话框 -->
  </div>
</template>

<style scoped>
.todo-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
  box-sizing: border-box;
}

.todo-layout {
  display: flex;
  gap: 16px;
  transition: all 0.3s ease;
  align-items: stretch;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
}

.todo-left-col {
  flex: 0 0 25%;
  max-width: 25%;
  min-width: 0;
  transition: all 0.3s ease;
  display: flex;
  align-items: flex-start;
  box-sizing: border-box;
}

.todo-left-col.collapsed {
  flex: 0 0 60px;
  max-width: 60px;
}

.todo-right-col {
  flex: 1;
  min-width: 0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
}

.todo-right-col.expanded {
  flex: 1;
}

.todo-right-col .summary-card,
.todo-right-col .task-list-card {
  flex-shrink: 0;
}

.summary {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  width: 100%;
  box-sizing: border-box;
}

.summary-item {
  padding: 12px 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  flex: 0 0 auto;
}

.summary-item .label {
  font-size: 12px;
  color: #909399;
}

.summary-item .value {
  font-size: 20px;
  font-weight: 600;
  margin-top: 4px;
}

.actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.module-card {
  width: 100%;
  max-width: 100%;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.module-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.module-card.collapsed {
  width: 60px;
  min-width: 60px;
  padding: 0;
}

.module-card.collapsed :deep(.el-card__body) {
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.module-collapsed-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 12px 0;
  gap: 16px;
}

.module-vertical-text {
  writing-mode: vertical-rl;
  text-orientation: upright;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 8px;
}

.module-card .card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.collapse-btn {
  padding: 2px 4px;
  color: #909399;
  transition: color 0.2s, transform 0.2s;
}

.collapse-btn:hover {
  color: #409eff;
}

.module-scroll {
  flex: 1;
  min-height: 0;
  transition: all 0.3s ease;
}

.module-menu {
  border: none;
}

.module-menu :deep(.el-menu-item) {
  height: auto;
  line-height: 1.4;
  padding-top: 8px;
  padding-bottom: 8px;
  border-radius: 8px;
  margin-bottom: 4px;
}

.module-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08), rgba(139, 92, 246, 0.05));
}

.module-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.module-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
}

.module-meta {
  display: flex;
  gap: 12px;
  color: #909399;
  font-size: 12px;
}

.module-actions {
  display: flex;
  gap: 8px;
}

.summary-card {
  margin-bottom: 12px;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.summary-card :deep(.el-card__body) {
  width: 100%;
  box-sizing: border-box;
}

.active-banner {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.active-banner .value {
  font-weight: 600;
  font-size: 16px;
}

.active-banner .muted {
  color: #909399;
  font-size: 12px;
}

.banner-actions {
  display: flex;
  gap: 8px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
}

.mt-12 {
  margin-top: 12px;
}

.ml-4 {
  margin-left: 4px;
}

.ml-8 {
  margin-left: 8px;
}

.module-scroll {
  max-height: calc(100vh - 220px);
}

.task-list-card {
  margin-top: 12px;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.task-list-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.task-table {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.task-table :deep(.el-table) {
  width: 100%;
}

.task-table :deep(.el-table__body-wrapper) {
  max-height: calc(100vh - 400px);
  overflow-y: auto;
  overflow-x: auto;
}

.task-table :deep(.el-table__header-wrapper) {
  overflow-x: hidden;
}

.task-table :deep(.el-table__header) {
  background-color: #f5f7fa;
}

.task-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #303133;
  font-weight: 600;
  padding: 12px 0;
}

.task-table :deep(.el-table__row) {
  transition: background-color 0.2s;
}

.task-table :deep(.el-table__row td) {
  padding: 14px 0;
}

.task-table :deep(.row-in-progress) {
  background-color: rgba(103, 194, 58, 0.08);
}

.task-table :deep(.row-completed) {
  background-color: rgba(144, 147, 153, 0.05);
}

.task-table :deep(.el-table__row:hover) {
  background-color: rgba(64, 158, 255, 0.08);
}

.task-table :deep(.el-table__border) {
  border: 1px solid #ebeef5;
}

.task-table :deep(.el-table__border th) {
  border-right: 1px solid #ebeef5;
}

.task-table :deep(.el-table__border td) {
  border-right: 1px solid #ebeef5;
}

.task-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-title {
  font-weight: 500;
  color: #303133;
}

.due-date-cell {
  font-size: 13px;
  font-family: 'Courier New', monospace;
}

.due-date-overdue {
  color: var(--el-color-danger);
  font-weight: 600;
}

.due-date-today {
  color: var(--el-color-warning);
  font-weight: 600;
}

.due-date-urgent {
  color: var(--el-color-warning);
}

.due-date-normal {
  color: #606266;
}

.no-due-date {
  color: #c0c4cc;
}

.duration-text {
  color: #909399;
  font-size: 13px;
}

.dropdown-icon {
  margin-right: 8px;
  vertical-align: middle;
}

.dropdown-icon {
  margin-right: 8px;
  vertical-align: middle;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.dashboard-dialog :deep(.el-dialog__body) {
  padding: 0;
  background: #1a1a2e;
}

.dashboard-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.dashboard-title {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}

.dashboard-wrapper {
  width: 100%;
  height: 100%;
}
</style>

