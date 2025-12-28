import { request } from './http'
import http from './http'
import type {
  Image,
  ImageListQuery,
  ImageUploadResponse,
  BatchUploadResponse,
  ImageUpdateDTO,
  BatchDeleteRequest,
  BatchDeleteResponse,
  ShareImageRequest,
  ShareImageResponse,
  ImageStatistics,
} from '@/types/image'
import type { PageResult } from '@/types/common'

export const imageApi = {
  /**
   * 单张图片上传
   */
  upload: (formData: FormData) =>
    request<ImageUploadResponse>({
      url: '/api/image/upload',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),

  /**
   * 批量图片上传
   */
  batchUpload: (formData: FormData) =>
    request<BatchUploadResponse>({
      url: '/api/image/upload/batch',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),

  /**
   * 图片列表查询
   */
  list: (params?: ImageListQuery) =>
    request<PageResult<Image>>({
      url: '/api/image/list',
      method: 'GET',
      params,
    }),

  /**
   * 图片详情查询
   */
  getById: (id: string) =>
    request<Image>({
      url: `/api/image/${id}`,
      method: 'GET',
    }),

  /**
   * 更新图片信息
   */
  update: (id: string, data: ImageUpdateDTO) =>
    request<Image>({
      url: `/api/image/${id}`,
      method: 'PUT',
      data,
    }),

  /**
   * 删除图片
   */
  delete: (id: string) =>
    request<void>({
      url: `/api/image/${id}`,
      method: 'DELETE',
    }),

  /**
   * 批量删除图片
   */
  batchDelete: (data: BatchDeleteRequest) =>
    request<BatchDeleteResponse>({
      url: '/api/image/batch',
      method: 'DELETE',
      data,
    }),

  /**
   * 恢复图片
   */
  restore: (id: string) =>
    request<Image>({
      url: `/api/image/${id}/restore`,
      method: 'POST',
    }),

  /**
   * 归档图片
   */
  archive: (id: string) =>
    request<Image>({
      url: `/api/image/${id}/archive`,
      method: 'POST',
    }),

  /**
   * 生成分享链接
   */
  share: (id: string, data?: ShareImageRequest) =>
    request<ShareImageResponse>({
      url: `/api/image/${id}/share`,
      method: 'POST',
      data,
    }),

  /**
   * 撤销分享链接
   */
  revokeShare: (id: string) =>
    request<void>({
      url: `/api/image/${id}/share`,
      method: 'DELETE',
    }),

  /**
   * 图片统计查询
   */
  statistics: (params?: { startTime?: string; endTime?: string }) =>
    request<ImageStatistics>({
      url: '/api/image/statistics',
      method: 'GET',
      params,
    }),
}

