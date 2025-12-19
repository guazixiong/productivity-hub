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

  // 统计
  overview: (params?: { from?: string; to?: string }) =>
    request<TodoStats>({
      url: '/api/todo/stats/overview',
      method: 'GET',
      params,
    }),
}

