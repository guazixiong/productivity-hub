import { request } from './http'
import http from './http'
import type { PageResult } from '@/types/common'
import type {
  ExerciseRecord,
  ExerciseRecordQuery,
  ExerciseRecordDTO,
  TrainingPlan,
  TrainingPlanDTO,
  ExerciseStatistics,
  ExerciseTrend,
  WaterIntake,
  WaterIntakeQuery,
  WaterIntakeDTO,
  WaterTarget,
  WaterTargetDTO,
  WaterProgress,
  WaterStatistics,
  WaterTrend,
  WeightRecord,
  WeightRecordQuery,
  WeightRecordDTO,
  UserBodyInfo,
  UserBodyInfoDTO,
  WeightStatistics,
  WeightTrend,
  HealthOverview,
  HealthCalendar,
} from '@/types/health'

export const healthApi = {
  // ==================== 运动记录 ====================
  
  /**
   * 查询运动记录列表
   */
  listExerciseRecords: (params?: ExerciseRecordQuery) =>
    request<PageResult<ExerciseRecord>>({
      url: '/api/health/exercise/records',
      method: 'GET',
      params,
    }),
  
  /**
   * 查询运动记录详情
   */
  getExerciseRecord: (id: string) =>
    request<ExerciseRecord>({
      url: `/api/health/exercise/records/${id}`,
      method: 'GET',
    }),
  
  /**
   * 新增运动记录
   */
  createExerciseRecord: (data: ExerciseRecordDTO) =>
    request<ExerciseRecord>({
      url: '/api/health/exercise/records',
      method: 'POST',
      data,
    }),
  
  /**
   * 更新运动记录
   */
  updateExerciseRecord: (id: string, data: Partial<ExerciseRecordDTO>) =>
    request<ExerciseRecord>({
      url: `/api/health/exercise/records/${id}`,
      method: 'PUT',
      data,
    }),
  
  /**
   * 删除运动记录
   */
  deleteExerciseRecord: (id: string) =>
    request<void>({
      url: `/api/health/exercise/records/${id}`,
      method: 'DELETE',
    }),
  
  /**
   * 批量删除运动记录
   */
  batchDeleteExerciseRecords: (ids: string[]) =>
    request<void>({
      url: '/api/health/exercise/records/batch',
      method: 'DELETE',
      data: { ids },
    }),
  
  /**
   * 导出运动记录（Excel格式）
   */
  exportExerciseRecordsExcel: async (params?: ExerciseRecordQuery) => {
    const response = await http.request<Blob>({
      url: '/api/health/exercise/records/export/excel',
      method: 'GET',
      params,
      responseType: 'blob',
    })
    return response.data
  },
  
  /**
   * 导出运动记录（CSV格式）
   */
  exportExerciseRecordsCsv: async (params?: ExerciseRecordQuery) => {
    const response = await http.request<Blob>({
      url: '/api/health/exercise/records/export/csv',
      method: 'GET',
      params,
      responseType: 'blob',
    })
    return response.data
  },

  /**
   * 导入运动记录（Excel格式）
   */
  importExerciseRecords: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await http.request<{
      total: number
      success: number
      failed: number
      skipped: number
      errors: string[]
    }>({
      url: '/api/health/exercise/records/import',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * 下载运动记录导入模板
   */
  downloadExerciseRecordTemplate: async () => {
    const response = await http.request<Blob>({
      url: '/api/health/exercise/records/import/template',
      method: 'GET',
      responseType: 'blob',
    })
    const url = window.URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.href = url
    link.download = `运动记录导入模板.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  },
  
  // ==================== 训练计划 ====================
  
  /**
   * 查询训练计划列表
   */
  listTrainingPlans: (params?: { status?: string; planType?: string }) =>
    request<TrainingPlan[]>({
      url: '/api/health/exercise/plans',
      method: 'GET',
      params,
    }),
  
  /**
   * 查询训练计划详情
   */
  getTrainingPlan: (id: string) =>
    request<TrainingPlan>({
      url: `/api/health/exercise/plans/${id}`,
      method: 'GET',
    }),
  
  /**
   * 创建训练计划
   */
  createTrainingPlan: (data: TrainingPlanDTO) =>
    request<TrainingPlan>({
      url: '/api/health/exercise/plans',
      method: 'POST',
      data,
    }),
  
  /**
   * 更新训练计划
   */
  updateTrainingPlan: (id: string, data: Partial<TrainingPlanDTO>) =>
    request<TrainingPlan>({
      url: `/api/health/exercise/plans/${id}`,
      method: 'PUT',
      data,
    }),
  
  /**
   * 暂停训练计划
   */
  pauseTrainingPlan: (id: string) =>
    request<TrainingPlan>({
      url: `/api/health/exercise/plans/${id}/pause`,
      method: 'POST',
    }),
  
  /**
   * 恢复训练计划
   */
  resumeTrainingPlan: (id: string) =>
    request<TrainingPlan>({
      url: `/api/health/exercise/plans/${id}/resume`,
      method: 'POST',
    }),
  
  /**
   * 完成训练计划
   */
  completeTrainingPlan: (id: string) =>
    request<TrainingPlan>({
      url: `/api/health/exercise/plans/${id}/complete`,
      method: 'POST',
    }),
  
  /**
   * 删除训练计划
   */
  deleteTrainingPlan: (id: string, force?: boolean) =>
    request<void>({
      url: `/api/health/exercise/plans/${id}`,
      method: 'DELETE',
      params: { force },
    }),
  
  // ==================== 运动统计 ====================
  
  /**
   * 查询运动统计数据
   */
  getExerciseStatistics: (params?: {
    period?: 'today' | 'week' | 'month' | 'all' | 'custom'
    startDate?: string
    endDate?: string
    groupBy?: 'type' | 'plan'
  }) =>
    request<ExerciseStatistics>({
      url: '/api/health/exercise/records/statistics',
      method: 'GET',
      params,
    }),
  
  /**
   * 查询运动趋势数据
   */
  getExerciseTrend: (params?: {
    type?: 'duration' | 'count' | 'calories'
    days?: number
  }) =>
    request<ExerciseTrend>({
      url: '/api/health/exercise/records/trend',
      method: 'GET',
      params,
    }),
  
  // ==================== 饮水记录 ====================
  
  /**
   * 查询饮水记录列表
   */
  listWaterIntakes: (params?: WaterIntakeQuery) =>
    request<PageResult<WaterIntake>>({
      url: '/api/health/water/records',
      method: 'GET',
      params,
    }),
  
  /**
   * 查询饮水记录详情
   */
  getWaterIntake: (id: string) =>
    request<WaterIntake>({
      url: `/api/health/water/records/${id}`,
      method: 'GET',
    }),
  
  /**
   * 新增饮水记录
   */
  createWaterIntake: (data: WaterIntakeDTO) =>
    request<WaterIntake>({
      url: '/api/health/water/records',
      method: 'POST',
      data,
    }),
  
  /**
   * 更新饮水记录
   */
  updateWaterIntake: (id: string, data: Partial<WaterIntakeDTO>) =>
    request<WaterIntake>({
      url: `/api/health/water/records/${id}`,
      method: 'PUT',
      data,
    }),
  
  /**
   * 删除饮水记录
   */
  deleteWaterIntake: (id: string) =>
    request<void>({
      url: `/api/health/water/records/${id}`,
      method: 'DELETE',
    }),
  
  /**
   * 批量删除饮水记录
   */
  batchDeleteWaterIntakes: (ids: string[]) =>
    request<void>({
      url: '/api/health/water/records/batch',
      method: 'DELETE',
      data: { ids },
    }),
  
  /**
   * 导出饮水记录（Excel格式）
   */
  exportWaterIntakesExcel: async (params?: WaterIntakeQuery) => {
    const response = await http.request<Blob>({
      url: '/api/health/water/records/export/excel',
      method: 'GET',
      params,
      responseType: 'blob',
    })
    return response.data
  },
  
  /**
   * 导出饮水记录（CSV格式）
   */
  exportWaterIntakesCsv: async (params?: WaterIntakeQuery) => {
    const response = await http.request<Blob>({
      url: '/api/health/water/records/export/csv',
      method: 'GET',
      params,
      responseType: 'blob',
    })
    return response.data
  },
  
  // ==================== 饮水目标 ====================
  
  /**
   * 查询饮水目标
   */
  getWaterTarget: () =>
    request<WaterTarget | null>({
      url: '/api/health/water/target',
      method: 'GET',
    }),
  
  /**
   * 设置/更新饮水目标
   */
  setWaterTarget: (data: WaterTargetDTO) =>
    request<WaterTarget>({
      url: '/api/health/water/target',
      method: 'POST',
      data,
    }),
  
  /**
   * 查询当日饮水进度
   */
  getWaterProgress: (date?: string) =>
    request<WaterProgress>({
      url: '/api/health/water/progress',
      method: 'GET',
      params: { date },
    }),
  
  // ==================== 饮水统计 ====================
  
  /**
   * 查询饮水统计数据
   */
  getWaterStatistics: (params?: {
    period?: 'today' | 'week' | 'month' | 'all' | 'custom'
    startDate?: string
    endDate?: string
  }) =>
    request<WaterStatistics>({
      url: '/api/health/water/statistics',
      method: 'GET',
      params,
    }),
  
  /**
   * 查询饮水趋势数据
   */
  getWaterTrend: (params?: { days?: number }) =>
    request<WaterTrend>({
      url: '/api/health/water/trend',
      method: 'GET',
      params,
    }),
  
  // ==================== 体重记录 ====================
  
  /**
   * 查询体重记录列表
   */
  listWeightRecords: (params?: WeightRecordQuery) =>
    request<PageResult<WeightRecord>>({
      url: '/api/health/weight/records',
      method: 'GET',
      params,
    }),
  
  /**
   * 查询体重记录详情
   */
  getWeightRecord: (id: string) =>
    request<WeightRecord>({
      url: `/api/health/weight/records/${id}`,
      method: 'GET',
    }),
  
  /**
   * 新增体重记录
   */
  createWeightRecord: (data: WeightRecordDTO) =>
    request<WeightRecord>({
      url: '/api/health/weight/records',
      method: 'POST',
      data,
    }),
  
  /**
   * 更新体重记录
   */
  updateWeightRecord: (id: string, data: Partial<WeightRecordDTO>) =>
    request<WeightRecord>({
      url: `/api/health/weight/records/${id}`,
      method: 'PUT',
      data,
    }),
  
  /**
   * 删除体重记录
   */
  deleteWeightRecord: (id: string) =>
    request<void>({
      url: `/api/health/weight/records/${id}`,
      method: 'DELETE',
    }),
  
  /**
   * 批量删除体重记录
   */
  batchDeleteWeightRecords: (ids: string[]) =>
    request<void>({
      url: '/api/health/weight/records/batch',
      method: 'DELETE',
      data: { ids },
    }),
  
  /**
   * 导出体重记录（Excel格式）
   */
  exportWeightRecordsExcel: async (params?: WeightRecordQuery) => {
    const response = await http.request<Blob>({
      url: '/api/health/weight/records/export/excel',
      method: 'GET',
      params,
      responseType: 'blob',
    })
    return response.data
  },
  
  /**
   * 导出体重记录（CSV格式）
   */
  exportWeightRecordsCsv: async (params?: WeightRecordQuery) => {
    const response = await http.request<Blob>({
      url: '/api/health/weight/records/export/csv',
      method: 'GET',
      params,
      responseType: 'blob',
    })
    return response.data
  },

  /**
   * 导入体重记录（Excel格式）
   */
  importWeightRecords: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await http.request<{
      total: number
      success: number
      failed: number
      skipped: number
      errors: string[]
    }>({
      url: '/api/health/weight/records/import',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * 下载体重记录导入模板
   */
  downloadWeightRecordTemplate: async () => {
    const response = await http.request<Blob>({
      url: '/api/health/weight/records/import/template',
      method: 'GET',
      responseType: 'blob',
    })
    const url = window.URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.href = url
    link.download = `体重记录导入模板.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  },
  
  // ==================== 身体信息 ====================
  
  /**
   * 查询用户身体信息
   */
  getBodyInfo: () =>
    request<UserBodyInfo | null>({
      url: '/api/health/weight/body-info',
      method: 'GET',
    }),
  
  /**
   * 设置/更新用户身体信息
   */
  setBodyInfo: (data: UserBodyInfoDTO) =>
    request<UserBodyInfo>({
      url: '/api/health/weight/body-info',
      method: 'POST',
      data,
    }),
  
  // ==================== 体重统计 ====================
  
  /**
   * 查询体重统计数据
   */
  getWeightStatistics: (params?: {
    period?: 'week' | 'month' | 'quarter'
  }) =>
    request<WeightStatistics>({
      url: '/api/health/weight/statistics',
      method: 'GET',
      params,
    }),
  
  /**
   * 查询体重趋势数据
   */
  getWeightTrend: (params?: { days?: number }) =>
    request<WeightTrend>({
      url: '/api/health/weight/trend',
      method: 'GET',
      params,
    }),
  
  // ==================== 综合统计 ====================
  
  /**
   * 查询健康数据概览
   */
  getHealthOverview: (params?: {
    period?: 'today' | 'week' | 'month'
  }) =>
    request<HealthOverview>({
      url: '/api/health/statistics/overview',
      method: 'GET',
      params,
    }),
  
  /**
   * 查询健康数据日历
   */
  getHealthCalendar: (params?: {
    year?: number
    month?: number
  }) =>
    request<HealthCalendar>({
      url: '/api/health/statistics/calendar',
      method: 'GET',
      params,
    }),
}

