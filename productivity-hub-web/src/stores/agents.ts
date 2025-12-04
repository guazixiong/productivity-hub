import { defineStore } from 'pinia'
import { agentApi } from '@/services/api'
import type { AgentSummary, AgentInvocationPayload, AgentInvocationResult, AgentLogEntry } from '@/types/agents'

interface AgentState {
  agents: AgentSummary[]
  logs: AgentLogEntry[]
  logsPagination: {
    pageNum: number
    pageSize: number
    total: number
  }
  loadingAgents: boolean
  loadingLogs: boolean
  invoking: boolean
  lastInvocation: AgentInvocationResult | null
  selectedAgentId: string | null
  lastFetchedAgents: number
}

const AGENTS_CACHE_TTL = 5 * 60 * 1000 // 5分钟缓存

export const useAgentStore = defineStore('agents', {
  state: (): AgentState => ({
    agents: [],
    logs: [],
    logsPagination: {
      pageNum: 1,
      pageSize: 10,
      total: 0,
    },
    loadingAgents: false,
    loadingLogs: false,
    invoking: false,
    lastInvocation: null,
    selectedAgentId: null,
    lastFetchedAgents: 0,
  }),
  getters: {
    selectedAgent(state) {
      return state.agents.find((agent) => agent.id === state.selectedAgentId) ?? null
    },
  },
  actions: {
    async fetchAgents(force = false) {
      // 检查缓存，避免频繁请求
      const now = Date.now()
      if (!force && this.agents.length > 0 && (now - this.lastFetchedAgents) < AGENTS_CACHE_TTL) {
        return
      }
      this.loadingAgents = true
      try {
        this.agents = await agentApi.list()
        this.lastFetchedAgents = now
        if (!this.selectedAgentId && this.agents.length) {
          this.selectedAgentId = this.agents[0]?.id ?? null
        }
      } finally {
        this.loadingAgents = false
      }
    },
    async fetchLogs(page?: number, pageSize?: number) {
      this.loadingLogs = true
      const targetPage = page ?? this.logsPagination.pageNum ?? 1
      const targetSize = pageSize ?? this.logsPagination.pageSize ?? 10
      try {
        const pageResult = await agentApi.logs({ page: targetPage, pageSize: targetSize })
        this.logs = pageResult.items ?? []
        this.logsPagination = {
          pageNum: pageResult.pageNum ?? targetPage,
          pageSize: pageResult.pageSize ?? targetSize,
          total: pageResult.total ?? 0,
        }
      } finally {
        this.loadingLogs = false
      }
    },
    selectAgent(id: string) {
      this.selectedAgentId = id
    },
    async invokeAgent(payload: Omit<AgentInvocationPayload, 'agentId'> & { agentId?: string }) {
      if (!payload.agentId && !this.selectedAgentId) {
        throw new Error('请选择智能体')
      }
      this.invoking = true
      try {
        const result = await agentApi.invoke({
          agentId: payload.agentId ?? (this.selectedAgentId as string),
          input: payload.input,
          mode: payload.mode,
          context: payload.context,
        })
        this.lastInvocation = result
        await this.fetchLogs(1, this.logsPagination.pageSize)
        return result
      } finally {
        this.invoking = false
      }
    },
  },
})

