import { request } from './http'
import type { Announcement, AnnouncementCreateDTO, AnnouncementUpdateDTO, AnnouncementStats } from '@/types/announcement'
import type { PageResult } from '@/types/common'

export const announcementApi = {
  // 用户端API
  getUnread: () =>
    request<Announcement[]>({
      url: '/api/announcements/unread',
      method: 'GET',
    }),
  
  markRead: (id: string) =>
    request<void>({
      url: `/api/announcements/${id}/read`,
      method: 'POST',
    }),
  
  getById: (id: string) =>
    request<Announcement>({
      url: `/api/announcements/${id}`,
      method: 'GET',
    }),

  // 管理员API
  admin: {
    create: (data: AnnouncementCreateDTO) =>
      request<Announcement>({
        url: '/api/admin/announcements',
        method: 'POST',
        data,
      }),
    
    update: (id: string, data: AnnouncementUpdateDTO) =>
      request<Announcement>({
        url: `/api/admin/announcements/${id}`,
        method: 'PUT',
        data,
      }),
    
    delete: (id: string) =>
      request<void>({
        url: `/api/admin/announcements/${id}`,
        method: 'DELETE',
      }),
    
    publish: (id: string) =>
      request<Announcement>({
        url: `/api/admin/announcements/${id}/publish`,
        method: 'POST',
      }),
    
    withdraw: (id: string) =>
      request<Announcement>({
        url: `/api/admin/announcements/${id}/withdraw`,
        method: 'POST',
      }),
    
    getById: (id: string) =>
      request<Announcement>({
        url: `/api/admin/announcements/${id}`,
        method: 'GET',
      }),
    
    page: (params?: { page?: number; pageSize?: number; status?: string }) =>
      request<PageResult<Announcement>>({
        url: '/api/admin/announcements',
        method: 'GET',
        params,
      }),
    
    getStats: (id: string) =>
      request<AnnouncementStats>({
        url: `/api/admin/announcements/${id}/stats`,
        method: 'GET',
      }),
  },
}

