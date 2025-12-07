export type MessageChannel = 'dingtalk' | 'resend' | 'sendgrid'

export interface BaseMessagePayload {
  channel: MessageChannel
  data: Record<string, unknown>
}

export interface MessageHistoryItem {
  id: string
  channel: MessageChannel
  request: Record<string, unknown>
  status: 'success' | 'failed'
  response: string
  createdAt: string
}

export interface MessageSendResult {
  requestId: string
  status: 'success' | 'queued' | 'failed'
  detail: string
}

export type { PageResult } from './common'
