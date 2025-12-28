import type { ImageCategory, ImageStatus } from '@/types/image'

/**
 * 获取图片URL（处理相对路径）
 */
export const getImageUrl = (url?: string): string => {
  if (!url) return ''
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 处理相对路径
  const baseURL = import.meta.env.VITE_API_BASE_URL || ''
  if (baseURL) {
    // 如果配置了 baseURL，使用它拼接完整URL
    const normalizedBaseURL = baseURL.replace(/\/$/, '')
    const normalizedUrl = url.startsWith('/') ? url : '/' + url
    return normalizedBaseURL + normalizedUrl
  }
  // 如果没有配置 baseURL，直接返回相对路径
  // 浏览器会自动使用当前域名，通过 Vite 代理访问后端
  // 确保相对路径以 / 开头
  return url.startsWith('/') ? url : '/' + url
}

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 获取分类标签类型
 */
export const getCategoryTagType = (category: ImageCategory): string => {
  const map: Record<ImageCategory, string> = {
    avatar: 'success',
    bookmark: 'primary',
    todo: 'warning',
    health: 'info',
    article: '',
    other: 'info',
  }
  return map[category] || 'info'
}

/**
 * 获取分类标签文本
 */
export const getCategoryLabel = (category: ImageCategory): string => {
  const map: Record<ImageCategory, string> = {
    avatar: '头像',
    bookmark: '书签',
    todo: '待办',
    health: '健康',
    article: '文章',
    other: '其他',
  }
  return map[category] || category
}

/**
 * 获取状态标签类型
 */
export const getStatusTagType = (status: ImageStatus): string => {
  const map: Record<ImageStatus, string> = {
    ACTIVE: 'success',
    DELETED: 'danger',
    ARCHIVED: 'warning',
  }
  return map[status] || 'info'
}

/**
 * 获取状态标签文本
 */
export const getStatusLabel = (status: ImageStatus): string => {
  const map: Record<ImageStatus, string> = {
    ACTIVE: '正常',
    DELETED: '已删除',
    ARCHIVED: '已归档',
  }
  return map[status] || status
}



