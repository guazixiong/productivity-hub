import type { DataImportResult, ExportFormat } from '@/types/dataManagement'
import http from './http'

/**
 * 数据管理API
 */
export const dataManagementApi = {
  /**
   * 导入数据
   */
  importData: async (file: File, dataType: 'asset' | 'wishlist' = 'asset', incremental: boolean = false) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('dataType', dataType)
    formData.append('incremental', String(incremental))

    const response = await http.post<{
      code: number
      message: string
      data: DataImportResult
    }>('/api/data/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    const payload = response.data
    if (payload.code !== 0) {
      throw new Error(payload.message || '导入失败')
    }

    return payload
  },

  /**
   * 导出数据
   */
  exportData: async (dataType: 'asset' | 'wishlist' = 'asset', format: ExportFormat = 'excel') => {
    const response = await http.get('/api/data/export', {
      params: { dataType, format },
      responseType: 'blob',
    })

    // 创建下载链接
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    // 根据格式和数据类型设置文件名
    const extension = format === 'excel' ? 'xlsx' : 'csv'
    const dataTypeName = dataType === 'asset' ? '资产数据' : '心愿单数据'
    const filename = `${dataTypeName}_${new Date().toISOString().split('T')[0]}.${extension}`
    link.download = filename

    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  },
}

