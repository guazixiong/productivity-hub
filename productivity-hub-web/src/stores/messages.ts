import { defineStore } from 'pinia'
import { messageApi } from '@/services/api'
import type { BaseMessagePayload, MessageHistoryItem, MessageSendResult } from '@/types/messages'

interface MessageState {
  history: MessageHistoryItem[]
  loadingHistory: boolean
  sending: boolean
  latestResult: MessageSendResult | null
}

export const useMessageStore = defineStore('messages', {
  state: (): MessageState => ({
    history: [],
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
        await this.fetchHistory()
        return result
      } finally {
        this.sending = false
      }
    },
    async fetchHistory() {
      this.loadingHistory = true
      try {
        this.history = await messageApi.history()
      } finally {
        this.loadingHistory = false
      }
    },
  },
})

