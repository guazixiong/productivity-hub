export interface TodoModule {
  id: string
  name: string
  description?: string
  status: string
  sortOrder: number
  totalTasks: number
  completedTasks: number
  totalDurationMs: number
}

export interface TodoTask {
  id: string
  moduleId: string
  moduleName?: string
  title: string
  description?: string
  priority: 'P0' | 'P1' | 'P2' | 'P3'
  tags: string[]
  status: 'PENDING' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED' | 'INTERRUPTED'
  startedAt?: string
  endedAt?: string
  activeStartAt?: string
  pauseStartedAt?: string
  durationMs: number
  pausedDurationMs: number
  dueDate?: string
  createdAt?: string
  updatedAt?: string
}

export interface TodoEvent {
  id: string
  todoId: string
  eventType: string
  occurredAt: string
  payload?: string
}

export interface TodoModuleStat {
  moduleId: string
  moduleName?: string
  totalTasks: number
  completedTasks: number
  durationMs: number
}

export interface TodoDailyStat {
  date: string
  completedTasks: number
  durationMs: number
}

export interface TodoStats {
  totalTasks: number
  completedTasks: number
  inProgressTasks: number
  interruptedTasks: number
  totalDurationMs: number
  moduleStats: TodoModuleStat[]
  timeline: TodoDailyStat[]
}

export interface TodoImportItem {
  moduleName: string
  title: string
  description?: string
  priority?: TodoTask['priority']
  tags?: string[]
  dueDate?: string
}

export interface TodoImportResult {
  total: number
  success: number
  failed: number
  createdModules: number
  errors: string[]
}

export interface TodoModuleCreateDTO {
  name: string
  description?: string
  sortOrder?: number
  status?: string
}

export interface TodoModuleUpdateDTO extends TodoModuleCreateDTO {
  id: string
}

export interface TodoTaskCreateDTO {
  title: string
  description?: string
  moduleId: string
  priority?: TodoTask['priority']
  tags?: string[]
  dueDate?: string
}

export interface TodoTaskUpdateDTO extends TodoTaskCreateDTO {
  id: string
}

export interface TodoTaskInterruptDTO {
  reason?: string
}

