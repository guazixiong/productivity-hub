import { defineStore } from 'pinia'
import { messageApi } from '@/services/api'
import type { BaseMessagePayload, MessageHistoryItem, MessageSendResult } from '@/types/messages'

interface MessageState {
  history: MessageHistoryItem[]
  historyPagination: {
    pageNum: number
    pageSize: number
    total: number
  }
  loadingHistory: boolean
  sending: boolean
  latestResult: MessageSendResult | null
}

export const useMessageStore = defineStore('messages', {
  state: (): MessageState => ({
    history: [],
    historyPagination: {
      pageNum: 1,
      pageSize: 10,
      total: 0,
    },
    loadingHistory: false,
    sending: false,
    latestResult: null,
  }),
  actions: {
    async sendMessage(payload: BaseMessagePayload) {
      this.sending = true
      try {
        const result = await messageApi.send(payload)
        this.latestResult = result
        return result
      } finally {
        await this.fetchHistory({ page: 1 })
        this.sending = false
      }
    },
    async fetchHistory(options?: { page?: number; pageSize?: number }) {
      this.loadingHistory = true
       const targetPage = options?.page ?? this.historyPagination.pageNum ?? 1
       const targetSize = options?.pageSize ?? this.historyPagination.pageSize ?? 10
      try {
        const result = await messageApi.history({ page: targetPage, pageSize: targetSize })
        this.history = result.items
        this.historyPagination = {
          pageNum: result.pageNum,
          pageSize: result.pageSize,
          total: result.total,
        }
      } finally {
        this.loadingHistory = false
      }
    },
  },
})

