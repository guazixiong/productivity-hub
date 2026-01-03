<script setup lang="ts">
/**
 * Todo页面组件
 */
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useDevice } from '@/composables/useDevice'
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
  UploadFilled,
  MoreFilled,
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { todoApi } from '@/services/todoApi'
import type { TodoImportResult, TodoModule, TodoStats, TodoTask, TodoImportItem } from '@/types/todo'
import TodoDashboard from './TodoDashboard.vue'
import * as XLSX from 'xlsx'

const router = useRouter()

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const loading = ref(false)
const modules = ref<TodoModule[]>([])
const tasks = ref<TodoTask[]>([])
const stats = ref<TodoStats | null>(null)

const selectedModuleId = ref<string>('')
const statusFilter = ref<string>('')
const modulesCollapsed = ref(false)

// 批量删除相关
const selectedTasks = ref<TodoTask[]>([])
const batchDeleteLoading = ref(false)

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

const importDialogVisible = ref(false)
const importText = ref('')
const importLoading = ref(false)
const importResult = ref<TodoImportResult | null>(null)
const selectedFile = ref<File | null>(null)
const uploadRef = ref()
const currentStep = ref(0) // 0: 上传文件, 1: 预览, 2: 结果
const previewData = ref<TodoImportItem[]>([])
const previewTableColumns = [
  { prop: 'rowIndex', label: '行号', width: 80, fixed: 'left' },
  { prop: 'moduleName', label: '模块', width: 120 },
  { prop: 'title', label: '标题', minWidth: 200 },
  { prop: 'priority', label: '优先级', width: 100 },
  { prop: 'dueDate', label: '截止日期', width: 150 },
  { prop: 'tags', label: '标签', width: 150 },
  { prop: 'description', label: '描述', minWidth: 200 },
]
const previewErrors = ref<Array<{ row: number; message: string }>>([])

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

const handleModuleCommand = async (command: string, module: TodoModule) => {
  switch (command) {
    case 'edit':
      openModuleDialog(module)
      break
    case 'delete':
      await confirmDeleteModule(module)
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
  await loadModules()
}

const confirmDeleteTask = async (task: TodoTask) => {
  await ElMessageBox.confirm(`确定删除任务「${task.title}」吗？`, '提示', { type: 'warning' })
  await todoApi.deleteTask(task.id)
  ElMessage.success('已删除')
  await loadTasks()
  await loadStats()
  await loadModules()
}

const handleSelectionChange = (selection: TodoTask[]) => {
  selectedTasks.value = selection
}

const handleBatchDelete = async () => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请先选择要删除的任务')
    return
  }

  // 检查是否有进行中的任务
  const inProgressTasks = selectedTasks.value.filter(t => t.status === 'IN_PROGRESS')
  if (inProgressTasks.length > 0) {
    ElMessage.warning('进行中的任务不能删除，请先暂停或完成')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedTasks.value.length} 个任务吗？`,
      '批量删除确认',
      { type: 'warning' }
    )
    
    batchDeleteLoading.value = true
    const ids = selectedTasks.value.map(t => t.id)
    await todoApi.batchDeleteTasks(ids)
    ElMessage.success(`已删除 ${selectedTasks.value.length} 个任务`)
    selectedTasks.value = []
    await loadTasks()
    await loadStats()
    await loadModules()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error((error as Error).message || '批量删除失败，请稍后再试')
    }
  } finally {
    batchDeleteLoading.value = false
  }
}

const handleStart = async (task: TodoTask) => {
  await todoApi.startTask(task.id)
  await loadTasks()
  await loadStats()
  await loadModules()
}

const handlePause = async (task: TodoTask) => {
  await todoApi.pauseTask(task.id)
  await loadTasks()
  await loadStats()
  await loadModules()
}

const handleComplete = async (task: TodoTask) => {
  await todoApi.completeTask(task.id)
  await loadTasks()
  await loadStats()
  await loadModules()
}

const handleInterrupt = async (task: TodoTask) => {
  await todoApi.interruptTask(task.id, { reason: 'manual' })
  await loadTasks()
  await loadStats()
  await loadModules()
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

const openImportDialog = () => {
  importDialogVisible.value = true
  importText.value = ''
  importResult.value = null
  selectedFile.value = null
  currentStep.value = 0
  uploadRef.value?.clearFiles()
}

const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
}

const handleDownloadTemplate = async () => {
  try {
    await todoApi.downloadTemplate()
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

const parseExcelFile = async (file: File): Promise<TodoImportItem[]> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target?.result as ArrayBuffer)
        const workbook = XLSX.read(data, { type: 'array' })
        const sheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[sheetName]
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1, defval: '' }) as any[][]

        if (jsonData.length < 2) {
          throw new Error('Excel文件至少需要表头和数据行')
        }

        // 解析表头，支持中英文列名
        const headerRow = jsonData[0]
        const colMap: Record<string, number> = {}
        const headerMap: Record<string, string[]> = {
          moduleName: ['模块', '模块名称', 'module', 'moduleName'],
          title: ['标题', '任务标题', 'title', 'taskTitle'],
          priority: ['优先级', 'priority'],
          dueDate: ['截止日期', '到期日期', 'dueDate', 'due'],
          tags: ['标签', 'tag', 'tags'],
          description: ['描述', '说明', 'description', 'desc'],
        }

        headerRow.forEach((cell: any, index: number) => {
          const cellValue = String(cell || '').trim()
          Object.keys(headerMap).forEach((key) => {
            if (headerMap[key].some((h) => cellValue.includes(h) || h.includes(cellValue))) {
              colMap[key] = index
            }
          })
        })

        // 如果没有找到表头，使用固定列顺序（兼容旧格式）
        if (Object.keys(colMap).length === 0) {
          colMap.moduleName = 0
          colMap.title = 1
          colMap.priority = 2
          colMap.dueDate = 3
          colMap.tags = 4
          colMap.description = 5
        }

        // 验证必需字段
        if (colMap.moduleName === undefined || colMap.title === undefined) {
          throw new Error('Excel文件缺少必需字段：模块和标题')
        }

        // 从第二行开始解析数据
        const items: TodoImportItem[] = []
        const errors: string[] = []
        for (let i = 1; i < jsonData.length; i++) {
          const row = jsonData[i]
          if (!row || row.length === 0) continue

          // 检查是否为空行
          const isEmptyRow = row.every((cell: any) => !String(cell || '').trim())
          if (isEmptyRow) continue

          const moduleName = String(row[colMap.moduleName] || '').trim()
          const title = String(row[colMap.title] || '').trim()
          const priority = colMap.priority !== undefined ? String(row[colMap.priority] || '').trim() : ''
          const dueDate = colMap.dueDate !== undefined ? String(row[colMap.dueDate] || '').trim() : ''
          const tagsStr = colMap.tags !== undefined ? String(row[colMap.tags] || '').trim() : ''
          const description = colMap.description !== undefined ? String(row[colMap.description] || '').trim() : ''

          // 验证必需字段
          if (!moduleName) {
            errors.push(`第 ${i + 1} 行：模块名称不能为空`)
            continue
          }
          if (!title) {
            errors.push(`第 ${i + 1} 行：任务标题不能为空`)
            continue
          }

          // 验证优先级
          let validPriority: TodoTask['priority'] | undefined
          if (priority) {
            if (['P0', 'P1', 'P2', 'P3'].includes(priority)) {
              validPriority = priority as TodoTask['priority']
            } else {
              errors.push(`第 ${i + 1} 行：优先级 "${priority}" 无效，应为 P0/P1/P2/P3`)
            }
          }

          // 验证日期格式（支持多种格式）
          let validDueDate: string | undefined
          if (dueDate) {
            // 支持多种日期格式：YYYY-MM-DD, YYYY/MM/DD, YYYY-MM-DD HH:mm:ss, YYYY/MM/DD HH:mm:ss
            const dateRegex = /^\d{4}[-\/]\d{2}[-\/]\d{2}(\s+\d{2}:\d{2}(:\d{2})?)?$/
            if (dateRegex.test(dueDate)) {
              // 统一转换为 YYYY-MM-DD HH:mm:ss 格式
              validDueDate = dueDate.replace(/\//g, '-')
              // 如果没有时间部分，添加默认时间 00:00:00
              if (!validDueDate.includes(' ')) {
                validDueDate = validDueDate + ' 00:00:00'
              } else if (validDueDate.split(' ')[1].split(':').length === 2) {
                validDueDate = validDueDate + ':00'
              }
            } else {
              errors.push(`第 ${i + 1} 行：日期格式 "${dueDate}" 无效，应为 YYYY-MM-DD 或 YYYY-MM-DD HH:mm:ss`)
            }
          }

          const tags = tagsStr ? tagsStr.split('|').map((t) => t.trim()).filter(Boolean) : []

          items.push({
            moduleName,
            title,
            priority: validPriority,
            dueDate: validDueDate,
            tags: tags.length > 0 ? tags : undefined,
            description: description || undefined,
            rowIndex: i + 1, // 添加行号用于显示
          } as TodoImportItem & { rowIndex: number })
        }

        if (items.length === 0) {
          throw new Error('Excel文件中没有有效数据')
        }

        // 保存验证错误
        previewErrors.value = errors.map((err) => {
          const match = err.match(/第 (\d+) 行/)
          return {
            row: match ? parseInt(match[1]) : 0,
            message: err,
          }
        })

        if (errors.length > 0) {
          // 如果有验证错误，先显示警告，但允许继续导入
          ElMessage.warning(`发现 ${errors.length} 条数据验证问题，将在预览中显示`)
        }

        resolve(items)
      } catch (error) {
        reject(error)
      }
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsArrayBuffer(file)
  })
}

const handlePreview = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  
  importLoading.value = true
  previewErrors.value = []
  try {
    const items = await parseExcelFile(selectedFile.value)
    previewData.value = items
    currentStep.value = 1
  } catch (error) {
    ElMessage.error((error as Error).message || '文件解析失败')
  } finally {
    importLoading.value = false
  }
}

const handleImport = async () => {
  // 防止重复提交
  if (importLoading.value) {
    return
  }
  
  if (previewData.value.length === 0) {
    ElMessage.warning('没有可导入的数据')
    return
  }

  importLoading.value = true
  try {
    const res = await todoApi.importTasks({ items: previewData.value })
    importResult.value = res
    currentStep.value = 2
    ElMessage.success(`导入完成，成功 ${res.success} 条，失败 ${res.failed} 条`)
    await refreshAll()
  } catch (error) {
    ElMessage.error((error as Error).message || '导入失败，请检查格式或稍后再试')
  } finally {
    importLoading.value = false
  }
}

const handleImportClose = () => {
  importDialogVisible.value = false
  importText.value = ''
  importResult.value = null
  selectedFile.value = null
  previewData.value = []
  previewErrors.value = []
  currentStep.value = 0
  uploadRef.value?.clearFiles()
}

const getRowErrors = (rowIndex: number) => {
  return previewErrors.value.filter((err) => err.row === rowIndex).map((err) => err.message)
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
              <div class="module-list">
                <!-- 全部模块卡片 -->
                <el-card
                  :class="['module-card-item', { active: selectedModuleId === '' }]"
                  shadow="hover"
                  @click="selectedModuleId = ''"
                >
                  <div class="module-card-content">
                    <div class="module-card-header">
                      <div class="module-card-title">全部</div>
                    </div>
                    <div class="module-card-meta">
                      <span>任务 {{ stats?.totalTasks ?? 0 }}</span>
                    </div>
                  </div>
                </el-card>
                <!-- 具体模块卡片 -->
                <el-card
                  v-for="item in modules"
                  :key="item.id"
                  :class="['module-card-item', { active: selectedModuleId === item.id }]"
                  shadow="hover"
                  @click="selectedModuleId = item.id"
                >
                  <div class="module-card-content">
                    <div class="module-card-header">
                      <div class="module-card-title">
                        {{ item.name }}
                        <el-tag size="small" class="ml-4">{{ item.status || 'ENABLED' }}</el-tag>
                      </div>
                      <el-dropdown trigger="click" @command="(cmd) => handleModuleCommand(cmd, item)" @click.stop>
                        <el-button text size="small" class="module-dropdown-btn">
                          <el-icon><MoreFilled /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="edit">
                              <el-icon class="dropdown-icon"><Edit /></el-icon>
                              <span>编辑</span>
                            </el-dropdown-item>
                            <el-dropdown-item command="delete" divided>
                              <el-icon class="dropdown-icon"><Delete /></el-icon>
                              <span style="color: var(--el-color-danger)">删除</span>
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                    <div class="module-card-meta">
                      <span>任务 {{ item.totalTasks ?? 0 }}</span>
                      <span>完成 {{ item.completedTasks ?? 0 }}</span>
                    </div>
                  </div>
                </el-card>
              </div>
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
              <el-button @click="openImportDialog()">
                批量导入
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
            <div class="table-actions">
              <el-button 
                v-if="selectedTasks.length > 0"
                type="danger" 
                size="small"
                :loading="batchDeleteLoading"
                @click="handleBatchDelete"
              >
                <el-icon><Delete /></el-icon>
                批量删除 ({{ selectedTasks.length }})
              </el-button>
            </div>
          </div>
          <el-table 
            :data="tasks" 
            v-loading="loading" 
            class="task-table"
            stripe
            :row-class-name="getRowClassName"
            border
            size="default"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" align="center" />
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

    <el-dialog v-model="importDialogVisible" title="批量导入任务" width="720px" @close="handleImportClose">
      <div class="import-content">
        <el-steps :active="currentStep" finish-status="success" align-center>
          <el-step title="上传文件" />
          <el-step title="预览数据" />
          <el-step title="导入结果" />
        </el-steps>

        <div class="step-content">
          <!-- 步骤1：上传文件 -->
          <div v-if="currentStep === 0" class="upload-step">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="1"
              accept=".xlsx,.xls"
              drag
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将Excel文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  只能上传 .xlsx 或 .xls 格式的Excel文件
                </div>
              </template>
            </el-upload>

            <div class="upload-actions">
              <el-button @click="handleDownloadTemplate">下载模板</el-button>
              <el-button
                type="primary"
                :disabled="!selectedFile"
                @click="handlePreview"
              >
                下一步：预览数据
              </el-button>
            </div>
          </div>

          <!-- 步骤2：预览数据 -->
          <div v-if="currentStep === 1" class="preview-step">
            <div class="preview-info">
              <el-alert
                :title="`已解析 ${previewData.length} 条数据，请确认后导入`"
                type="info"
                :closable="false"
                show-icon
              />
            </div>

            <div class="preview-table-wrapper">
              <el-alert
                v-if="previewErrors.length > 0"
                :title="`发现 ${previewErrors.length} 条数据验证问题`"
                type="warning"
                :closable="false"
                show-icon
                class="mb-12"
              >
                <ul class="preview-error-list">
                  <li v-for="(err, idx) in previewErrors.slice(0, 5)" :key="idx">{{ err.message }}</li>
                  <li v-if="previewErrors.length > 5">... 还有 {{ previewErrors.length - 5 }} 条错误</li>
                </ul>
              </el-alert>
              <el-table 
                :data="previewData" 
                border 
                stripe 
                max-height="400" 
                style="width: 100%"
                :row-class-name="({ row }: { row: any }) => getRowErrors(row.rowIndex).length > 0 ? 'row-with-error' : ''"
              >
                <el-table-column
                  v-for="col in previewTableColumns"
                  :key="col.prop"
                  :prop="col.prop"
                  :label="col.label"
                  :width="col.width"
                  :min-width="col.minWidth"
                  :fixed="col.fixed"
                  show-overflow-tooltip
                >
                  <template #default="{ row }">
                    <span v-if="col.prop === 'tags'">
                      {{ row.tags && row.tags.length > 0 ? row.tags.join(' | ') : '-' }}
                    </span>
                    <span v-else>{{ row[col.prop] || '-' }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="preview-actions">
              <el-button @click="currentStep = 0">上一步</el-button>
              <el-button type="primary" @click="handleImport" :loading="importLoading" v-button-lock>
                确认导入 ({{ previewData.length }} 条)
              </el-button>
            </div>
          </div>

          <!-- 步骤3：导入结果 -->
          <div v-if="currentStep === 2" class="result-step">
            <el-result
              :icon="importResult && importResult.failed === 0 ? 'success' : 'warning'"
              :title="importResult && importResult.failed === 0 ? '导入成功' : '导入完成'"
              :sub-title="importResult ? `成功：${importResult.success}，失败：${importResult.failed}，新建模块：${importResult.createdModules}` : ''"
            >
              <template #extra>
                <div v-if="importResult && importResult.errors && importResult.errors.length > 0" class="error-list">
                  <el-alert
                    title="错误信息"
                    type="error"
                    :closable="false"
                    show-icon
                  >
                    <ul class="error-items">
                      <li v-for="(error, index) in importResult.errors" :key="index">
                        {{ error }}
                      </li>
                    </ul>
                  </el-alert>
                </div>
                <el-button type="primary" @click="handleImportClose">完成</el-button>
              </template>
            </el-result>
          </div>
        </div>
      </div>
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

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .todo-page {
    gap: 10px; /* 移动端间距缩放 */
  }

  .todo-layout {
    flex-direction: column; /* 移动端垂直排列 */
    gap: 12px; /* 移动端间距缩放 */
  }

  .todo-left-col {
    /* 移动端隐藏左侧模块列表（可通过顶部菜单访问） */
    display: none;
  }

  .todo-right-col {
    width: 100%;
    flex: 1;
  }
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

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .summary {
    gap: 10px; /* 移动端间距缩放 */
  }

  .summary-item {
    padding: 10px 12px; /* 移动端间距缩放 */
    border-radius: 6px;
    flex: 1 1 auto; /* 移动端允许自动调整 */
    min-width: 0;
  }

  .summary-item .label {
    font-size: 11px; /* 移动端字体缩放 */
  }

  .summary-item .value {
    font-size: 16px; /* 移动端字体缩放 */
    margin-top: 3px; /* 移动端间距缩放 */
  }

  .actions {
    margin-left: 0; /* 移动端取消左边距 */
    margin-top: 8px; /* 移动端添加上边距 */
    width: 100%; /* 移动端全宽 */
    flex-wrap: wrap; /* 移动端允许换行 */
  }
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

.module-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 4px 0;
}

.module-card-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

.module-card-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
}

.module-card-item.active {
  border-color: #409eff;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08), rgba(139, 92, 246, 0.05));
}

.module-card-item :deep(.el-card__body) {
  padding: 12px;
}

.module-card-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.module-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.module-card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  flex: 1;
  min-width: 0;
}

.module-card-meta {
  display: flex;
  gap: 12px;
  color: #909399;
  font-size: 12px;
}

.module-dropdown-btn {
  flex-shrink: 0;
  padding: 4px;
  color: #909399;
}

.module-dropdown-btn:hover {
  color: #409eff;
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

.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
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

/* 移动端适配 - REQ-001-02, REQ-001-05 */
@media (max-width: 768px) {
  .task-table {
    /* 移动端表格横向滚动 - REQ-001-05 */
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .task-table :deep(.el-table) {
    min-width: 800px; /* 确保表格最小宽度，触发横向滚动 */
  }

  .task-table :deep(.el-table__body-wrapper) {
    max-height: calc(100vh - 300px); /* 移动端调整高度 */
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .task-table :deep(.el-table__header-wrapper) {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .table-header {
    margin-bottom: 10px; /* 移动端间距缩放 */
    flex-direction: column; /* 移动端垂直排列 */
    align-items: flex-start; /* 移动端左对齐 */
    gap: 8px; /* 移动端间距缩放 */
  }

  .table-title {
    font-size: 14px; /* 移动端字体缩放 */
  }

  .table-actions {
    width: 100%; /* 移动端全宽 */
    flex-wrap: wrap; /* 移动端允许换行 */
    gap: 6px; /* 移动端间距缩放 */
  }
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

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .summary-card {
    margin-bottom: 10px; /* 移动端间距缩放 */
  }

  .task-list-card {
    margin-top: 10px; /* 移动端间距缩放 */
  }

  .active-banner {
    margin-top: 10px; /* 移动端间距缩放 */
    padding: 10px; /* 移动端间距缩放 */
    border-radius: 6px;
    flex-direction: column; /* 移动端垂直排列 */
    align-items: flex-start; /* 移动端左对齐 */
    gap: 8px; /* 移动端间距缩放 */
  }

  .active-banner .value {
    font-size: 14px; /* 移动端字体缩放 */
  }

  .active-banner .muted {
    font-size: 11px; /* 移动端字体缩放 */
  }

  .banner-actions {
    width: 100%; /* 移动端全宽 */
    flex-wrap: wrap; /* 移动端允许换行 */
    gap: 6px; /* 移动端间距缩放 */
  }

  .task-table :deep(.el-table__header th) {
    padding: 10px 0; /* 移动端间距缩放 */
    font-size: 12px; /* 移动端字体缩放 */
  }

  .task-table :deep(.el-table__row td) {
    padding: 10px 0; /* 移动端间距缩放 */
    font-size: 12px; /* 移动端字体缩放 */
  }

  .task-title {
    font-size: 13px; /* 移动端字体缩放 */
  }

  .due-date-cell {
    font-size: 11px; /* 移动端字体缩放 */
  }

  .duration-text {
    font-size: 11px; /* 移动端字体缩放 */
  }

  .pagination-wrapper {
    margin-top: 12px; /* 移动端间距缩放 */
    padding-top: 12px; /* 移动端间距缩放 */
    justify-content: center; /* 移动端居中 */
  }

  /* 移动端禁用hover效果 - REQ-001-03 */
  .task-table :deep(.el-table__row:hover) {
    background-color: transparent;
  }

  .task-table :deep(.row-in-progress:hover) {
    background-color: rgba(103, 194, 58, 0.08);
  }

  .task-table :deep(.row-completed:hover) {
    background-color: rgba(144, 147, 153, 0.05);
  }
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

.import-content {
  padding: 20px 0;
}

.step-content {
  margin-top: 30px;
  min-height: 300px;
}

.upload-step {
  text-align: center;
}

.upload-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.preview-step {
  padding: 0 20px;
}

.preview-info {
  margin-bottom: 20px;
}

.preview-table-wrapper {
  margin-top: 20px;
  margin-bottom: 20px;
}

.mb-12 {
  margin-bottom: 12px;
}

.preview-error-list {
  margin: 8px 0 0 0;
  padding-left: 20px;
  text-align: left;
  font-size: 12px;
}

.preview-error-list li {
  margin-bottom: 4px;
}

.preview-table-wrapper :deep(.row-with-error) {
  background-color: rgba(245, 108, 108, 0.1);
}

.preview-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.result-step {
  padding: 20px;
}

.error-list {
  margin-top: 20px;
  max-width: 600px;
}

.error-items {
  margin: 10px 0 0 0;
  padding-left: 20px;
  text-align: left;
}

.error-items li {
  margin-bottom: 5px;
  font-size: 12px;
}
</style>

