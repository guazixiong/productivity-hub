import { request } from './http'
import type {
  TodoModule,
  TodoModuleCreateDTO,
  TodoModuleUpdateDTO,
  TodoTask,
  TodoTaskCreateDTO,
  TodoTaskUpdateDTO,
  TodoTaskInterruptDTO,
  TodoEvent,
  TodoStats,
  TodoImportItem,
  TodoImportResult,
} from '@/types/todo'
import type { PageResult } from '@/types/common'

export const todoApi = {
  // 模块
  listModules: () =>
    request<TodoModule[]>({
      url: '/api/todo/modules',
      method: 'GET',
    }),
  createModule: (data: TodoModuleCreateDTO) =>
    request<TodoModule>({
      url: '/api/todo/modules',
      method: 'POST',
      data,
    }),
  updateModule: (id: string, data: TodoModuleUpdateDTO) =>
    request<TodoModule>({
      url: `/api/todo/modules/${id}`,
      method: 'PUT',
      data,
    }),
  deleteModule: (id: string) =>
    request<void>({
      url: `/api/todo/modules/${id}`,
      method: 'DELETE',
    }),

  // 任务
  listTasks: (params?: { moduleId?: string; status?: string }) =>
    request<TodoTask[]>({
      url: '/api/todo/tasks',
      method: 'GET',
      params,
    }),
  listTasksWithPage: (params?: { moduleId?: string; status?: string; pageNum?: number; pageSize?: number }) =>
    request<PageResult<TodoTask>>({
      url: '/api/todo/tasks',
      method: 'GET',
      params,
    }),
  getActiveTask: () =>
    request<TodoTask | null>({
      url: '/api/todo/tasks/active',
      method: 'GET',
    }),
  getTask: (id: string) =>
    request<TodoTask>({
      url: `/api/todo/tasks/${id}`,
      method: 'GET',
    }),
  createTask: (data: TodoTaskCreateDTO) =>
    request<TodoTask>({
      url: '/api/todo/tasks',
      method: 'POST',
      data,
    }),
  updateTask: (id: string, data: TodoTaskUpdateDTO) =>
    request<TodoTask>({
      url: `/api/todo/tasks/${id}`,
      method: 'PUT',
      data,
    }),
  deleteTask: (id: string) =>
    request<void>({
      url: `/api/todo/tasks/${id}`,
      method: 'DELETE',
    }),
  batchDeleteTasks: (ids: string[]) =>
    request<void>({
      url: '/api/todo/tasks/batch',
      method: 'DELETE',
      data: ids,
    }),
  startTask: (id: string) =>
    request<TodoTask>({
      url: `/api/todo/tasks/${id}/start`,
      method: 'POST',
    }),
  pauseTask: (id: string) =>
    request<TodoTask>({
      url: `/api/todo/tasks/${id}/pause`,
      method: 'POST',
    }),
  resumeTask: (id: string) =>
    request<TodoTask>({
      url: `/api/todo/tasks/${id}/resume`,
      method: 'POST',
    }),
  completeTask: (id: string) =>
    request<TodoTask>({
      url: `/api/todo/tasks/${id}/complete`,
      method: 'POST',
    }),
  interruptTask: (id: string, data?: TodoTaskInterruptDTO) =>
    request<TodoTask>({
      url: `/api/todo/tasks/${id}/interrupt`,
      method: 'POST',
      data,
    }),
  listEvents: (id: string) =>
    request<TodoEvent[]>({
      url: `/api/todo/tasks/${id}/events`,
      method: 'GET',
    }),
  importTasks: (data: { items: TodoImportItem[] }) =>
    request<TodoImportResult>({
      url: '/api/todo/tasks/import',
      method: 'POST',
      data,
    }),
  downloadTemplate: async () => {
    // 前端生成Excel模板，包含格式和示例数据
    const XLSX = await import('xlsx')
    
    // 创建表头和数据
    const data = [
      ['模块', '标题', '优先级', '截止日期', '标签', '描述'],
      ['工作', '写周报', 'P2', '2024-12-31', '汇总|输出', '每周五前完成'],
      ['个人成长', '阅读技术文章', 'P3', '2024-12-31 18:00:00', '学习|成长', '每天阅读30分钟'],
      ['', '', '', '', '', ''],
    ]
    
    const ws = XLSX.utils.aoa_to_sheet(data)
    
    // 设置列宽
    ws['!cols'] = [
      { wch: 15 }, // 模块
      { wch: 25 }, // 标题
      { wch: 10 }, // 优先级
      { wch: 20 }, // 截止日期
      { wch: 20 }, // 标签
      { wch: 30 }, // 描述
    ]
    
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '任务导入模板')
    XLSX.writeFile(wb, 'todo_import_template.xlsx')
  },

  // 统计
  overview: (params?: { from?: string; to?: string }) =>
    request<TodoStats>({
      url: '/api/todo/stats/overview',
      method: 'GET',
      params,
    }),
}

