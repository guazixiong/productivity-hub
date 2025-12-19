export interface NotificationItem {
  id: string
  title: string
  content: string
  link?: string
  extra?: Record<string, unknown> | null
  read: boolean
  createdAt: string
}


