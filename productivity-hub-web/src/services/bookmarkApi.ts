import { request } from './http'
import http from './http'
import type {
  BookmarkTag,
  BookmarkUrl,
  BookmarkGroup,
  BookmarkTagCreateDTO,
  BookmarkTagUpdateDTO,
  BookmarkTagSortDTO,
  BookmarkUrlCreateDTO,
  BookmarkUrlUpdateDTO,
  BookmarkUrlBatchUpdateDTO,
  BookmarkImportResult,
} from '@/types/bookmark'

export const bookmarkApi = {
  // ==================== 标签管理 ====================

  /**
   * 获取标签树
   */
  getTagTree: () =>
    request<BookmarkTag[]>({
      url: '/api/bookmark/tags/tree',
      method: 'GET',
    }),

  /**
   * 获取一级标签列表
   */
  getParentTags: () =>
    request<BookmarkTag[]>({
      url: '/api/bookmark/tags/parent',
      method: 'GET',
    }),

  /**
   * 根据父标签ID获取子标签列表
   */
  getChildTags: (parentId: string) =>
    request<BookmarkTag[]>({
      url: `/api/bookmark/tags/child/${parentId}`,
      method: 'GET',
    }),

  /**
   * 创建标签
   */
  createTag: (data: BookmarkTagCreateDTO) =>
    request<BookmarkTag>({
      url: '/api/bookmark/tags',
      method: 'POST',
      data,
    }),

  /**
   * 更新标签
   */
  updateTag: (data: BookmarkTagUpdateDTO) =>
    request<BookmarkTag>({
      url: '/api/bookmark/tags',
      method: 'PUT',
      data,
    }),

  /**
   * 删除标签
   */
  deleteTag: (id: string) =>
    request<void>({
      url: `/api/bookmark/tags/${id}`,
      method: 'DELETE',
    }),

  /**
   * 更新标签排序
   */
  updateTagSort: (data: BookmarkTagSortDTO) =>
    request<void>({
      url: '/api/bookmark/tags/sort',
      method: 'PUT',
      data,
    }),

  // ==================== 网址管理 ====================

  /**
   * 获取所有网址（按标签分组）
   */
  getUrlGroups: () =>
    request<BookmarkGroup[]>({
      url: '/api/bookmark/urls/groups',
      method: 'GET',
    }),

  /**
   * 根据标签ID获取网址列表
   */
  getUrlsByTagId: (tagId: string) =>
    request<BookmarkUrl[]>({
      url: `/api/bookmark/urls/tag/${tagId}`,
      method: 'GET',
    }),

  /**
   * 搜索网址
   */
  searchUrls: (keyword: string) =>
    request<BookmarkUrl[]>({
      url: '/api/bookmark/urls/search',
      method: 'GET',
      params: { keyword },
    }),

  /**
   * 根据ID获取网址详情
   */
  getUrlById: (id: string) =>
    request<BookmarkUrl>({
      url: `/api/bookmark/urls/${id}`,
      method: 'GET',
    }),

  /**
   * 创建网址
   */
  createUrl: (data: BookmarkUrlCreateDTO) =>
    request<BookmarkUrl>({
      url: '/api/bookmark/urls',
      method: 'POST',
      data,
    }),

  /**
   * 更新网址
   */
  updateUrl: (data: BookmarkUrlUpdateDTO) =>
    request<BookmarkUrl>({
      url: '/api/bookmark/urls',
      method: 'PUT',
      data,
    }),

  /**
   * 删除网址
   */
  deleteUrl: (id: string) =>
    request<void>({
      url: `/api/bookmark/urls/${id}`,
      method: 'DELETE',
    }),

  /**
   * 批量删除网址
   */
  batchDeleteUrls: (ids: string[]) =>
    request<void>({
      url: '/api/bookmark/urls/batch',
      method: 'DELETE',
      data: ids,
    }),

  /**
   * 批量更新网址标签
   */
  batchUpdateUrlTags: (data: BookmarkUrlBatchUpdateDTO) =>
    request<void>({
      url: '/api/bookmark/urls/batch/tags',
      method: 'PUT',
      data,
    }),

  // ==================== Excel导入导出 ====================

  /**
   * 导入Excel文件
   */
  importFromExcel: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request<BookmarkImportResult>({
      url: '/api/bookmark/import',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 下载导入模板
   */
  downloadTemplate: async () => {
    const response = await http.request({
      url: '/api/bookmark/import/template',
      method: 'GET',
      responseType: 'blob',
    })
    
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'bookmark_import_template.xlsx'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  },
}

