/**
 * 数据管理类型定义
 */

/**
 * 导入错误信息
 */
export interface ImportError {
  /** 行号（从1开始） */
  row: number
  /** 错误消息 */
  message: string
}

/**
 * 数据导入结果
 */
export interface DataImportResult {
  /** 成功数量 */
  successCount: number
  /** 失败数量 */
  failCount: number
  /** 错误信息列表 */
  errors: ImportError[]
}

/**
 * 导出格式
 */
export type ExportFormat = 'excel' | 'csv'

