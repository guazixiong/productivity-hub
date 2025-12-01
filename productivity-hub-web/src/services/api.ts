import { request } from './http'
import type { LoginPayload, AuthResponse } from '@/types/auth'
import type { ConfigItem, ConfigUpdatePayload } from '@/types/config'
import type { BaseMessagePayload, MessageHistoryItem, MessageSendResult } from '@/types/messages'
import type { AgentSummary, AgentInvocationPayload, AgentInvocationResult, AgentLogEntry } from '@/types/agents'

export const authApi = {
  login: (payload: LoginPayload) =>
    request<AuthResponse>({
      url: '/api/auth/login',
      method: 'POST',
      data: payload,
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
  history: () =>
    request<MessageHistoryItem[]>({
      url: '/api/messages/history',
      method: 'GET',
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
  logs: () =>
    request<AgentLogEntry[]>({
      url: '/api/agents/logs',
      method: 'GET',
    }),
}

