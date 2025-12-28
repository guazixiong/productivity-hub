export interface Announcement {
  id: string
  title: string
  content?: string
  richContent?: string
  link?: string
  type: 'NORMAL' | 'URGENT' | 'INFO' | 'WARNING'
  priority: number
  status: 'DRAFT' | 'PUBLISHED' | 'WITHDRAWN'
  pushStrategy: 'IMMEDIATE' | 'LOGIN' | 'SCHEDULED'
  requireConfirm: number // 0-不需要确认，1-需要确认
  effectiveTime?: string
  expireTime?: string
  scheduledTime?: string
  createdAt: string
  updatedAt: string
  read?: boolean
}

export interface AnnouncementCreateDTO {
  title: string
  content?: string
  richContent?: string
  link?: string
  type?: 'NORMAL' | 'URGENT' | 'INFO' | 'WARNING'
  priority?: number
  pushStrategy?: 'IMMEDIATE' | 'LOGIN' | 'SCHEDULED'
  requireConfirm?: number
  effectiveTime?: string
  expireTime?: string
  scheduledTime?: string
}

export interface AnnouncementUpdateDTO {
  title?: string
  content?: string
  richContent?: string
  link?: string
  type?: 'NORMAL' | 'URGENT' | 'INFO' | 'WARNING'
  priority?: number
  status?: 'DRAFT' | 'PUBLISHED' | 'WITHDRAWN'
  pushStrategy?: 'IMMEDIATE' | 'LOGIN' | 'SCHEDULED'
  requireConfirm?: number
  effectiveTime?: string
  expireTime?: string
  scheduledTime?: string
}

export interface AnnouncementStats {
  announcementId: string
  totalUsers: number
  readUsers: number
  readRate: number
}

