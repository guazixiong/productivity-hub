/**
 * 图片分类枚举
 */
export type ImageCategory = 'avatar' | 'bookmark' | 'todo' | 'health' | 'article' | 'other'

/**
 * 图片状态枚举
 */
export type ImageStatus = 'ACTIVE' | 'DELETED' | 'ARCHIVED'

/**
 * 图片信息视图对象
 */
export interface Image {
  id: string
  userId: string
  originalFilename: string
  storedFilename: string
  filePath: string
  fileUrl: string
  thumbnailPath?: string
  thumbnailUrl?: string
  fileSize: number
  fileType: string
  fileExtension: string
  width?: number
  height?: number
  category: ImageCategory
  businessModule?: string
  businessId?: string
  description?: string
  shareToken?: string
  shareExpiresAt?: string
  accessCount: number
  status: ImageStatus
  createdAt: string
  updatedAt: string
}

/**
 * 图片上传响应
 */
export interface ImageUploadResponse {
  id: string
  originalFilename: string
  storedFilename: string
  fileUrl: string
  thumbnailUrl?: string
  fileSize: number
  width?: number
  height?: number
  category: ImageCategory
  description?: string
}

/**
 * 批量上传成功项
 */
export interface BatchUploadSuccessItem {
  id: string
  originalFilename: string
  fileUrl: string
  thumbnailUrl?: string
}

/**
 * 批量上传失败项
 */
export interface BatchUploadFailItem {
  originalFilename: string
  errorCode: number
  errorMessage: string
}

/**
 * 批量上传响应
 */
export interface BatchUploadResponse {
  successCount: number
  failCount: number
  successList: BatchUploadSuccessItem[]
  failList: BatchUploadFailItem[]
}

/**
 * 图片列表查询参数
 */
export interface ImageListQuery {
  pageNum?: number
  pageSize?: number
  category?: ImageCategory
  businessModule?: string
  businessId?: string
  keyword?: string
  startTime?: string
  endTime?: string
  status?: ImageStatus
  sortBy?: 'createTime' | 'fileSize' | 'accessCount'
  sortOrder?: 'asc' | 'desc'
}

/**
 * 图片更新DTO
 */
export interface ImageUpdateDTO {
  description?: string
  businessModule?: string
  businessId?: string
}

/**
 * 批量删除请求
 */
export interface BatchDeleteRequest {
  ids: string[]
}

/**
 * 批量删除响应
 */
export interface BatchDeleteResponse {
  successCount: number
  failCount: number
  failList: Array<{
    id: string
    errorCode: number
    errorMessage: string
  }>
}

/**
 * 生成分享链接请求
 */
export interface ShareImageRequest {
  expiresAt?: string // yyyy-MM-dd HH:mm:ss
}

/**
 * 生成分享链接响应
 */
export interface ShareImageResponse {
  id: string
  shareToken: string
  shareUrl: string
  expiresAt?: string
}

/**
 * 图片统计信息
 */
export interface ImageStatistics {
  totalCount: number
  totalSize: number
  averageSize: number
  maxFileSize: number
  minFileSize: number
  categoryStats: Array<{
    category: ImageCategory
    count: number
    totalSize: number
  }>
  accessStats: {
    totalAccessCount: number
    averageAccessCount: number
    maxAccessCount: number
  }
  hotImages: Array<{
    id: string
    originalFilename: string
    fileUrl: string
    thumbnailUrl?: string
    accessCount: number
  }>
  uploadTrend: Array<{
    date: string
    count: number
  }>
  accessTrend: Array<{
    date: string
    count: number
  }>
}

/**
 * 图片上传表单数据
 */
export interface ImageUploadForm {
  file?: File
  files?: File[]
  category: ImageCategory
  businessModule?: string
  businessId?: string
  description?: string
}

