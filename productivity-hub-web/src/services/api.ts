import { request } from './http'
import type { LoginPayload, AuthResponse } from '@/types/auth'
import type { ConfigItem, ConfigUpdatePayload } from '@/types/config'
import type { BaseMessagePayload, MessageHistoryItem, MessageSendResult } from '@/types/messages'
import type { AgentSummary, AgentInvocationPayload, AgentInvocationResult, AgentLogEntry } from '@/types/agents'
import type { ToolStat } from '@/types/tools'
import type { PageResult } from '@/types/common'

export const authApi = {
  login: (payload: LoginPayload) =>
    request<AuthResponse>({
      url: '/api/auth/login',
      method: 'POST',
      data: payload,
    }),
  resetPassword: () =>
    request<{ success: boolean; message: string }>({
      url: '/api/auth/reset-password',
      method: 'POST',
    }),
}

export const configApi = {
  list: () =>
    request<ConfigItem[]>({
      url: '/api/config',
      method: 'GET',
    }),
  update: (payload: ConfigUpdatePayload) =>
    request<ConfigItem>({
      url: '/api/config',
      method: 'PUT',
      data: payload,
    }),
}

export const messageApi = {
  send: (payload: BaseMessagePayload) =>
    request<MessageSendResult>({
      url: '/api/messages/send',
      method: 'POST',
      data: payload,
    }),
  history: (params?: { page?: number; pageSize?: number }) =>
    request<PageResult<MessageHistoryItem>>({
      url: '/api/messages/history',
      method: 'GET',
      params,
    }),
}

export const agentApi = {
  list: () =>
    request<AgentSummary[]>({
      url: '/api/agents',
      method: 'GET',
    }),
  invoke: (payload: AgentInvocationPayload) =>
    request<AgentInvocationResult>({
      url: '/api/agents/invoke',
      method: 'POST',
      data: payload,
    }),
  logs: (params?: { page?: number; pageSize?: number }) =>
    request<PageResult<AgentLogEntry>>({
      url: '/api/agents/logs',
      method: 'GET',
      params,
    }),
}

export const toolApi = {
  stats: () =>
    request<ToolStat[]>({
      url: '/api/tools/stats',
      method: 'GET',
    }),
  track: (toolId: string) =>
    request<ToolStat[]>({
      url: '/api/tools/track',
      method: 'POST',
      data: { toolId },
    }),
}

