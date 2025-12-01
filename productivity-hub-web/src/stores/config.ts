import { defineStore } from 'pinia'
import { configApi } from '@/services/api'
import type { ConfigItem, ConfigUpdatePayload } from '@/types/config'

interface ConfigState {
  configs: ConfigItem[]
  loading: boolean
  lastFetched: number
}

const CACHE_TTL = 5 * 60 * 1000

export const useConfigStore = defineStore('config', {
  state: (): ConfigState => ({
    configs: [],
    loading: false,
    lastFetched: 0,
  }),
  getters: {
    isStale: (state) => Date.now() - state.lastFetched > CACHE_TTL,
  },
  actions: {
    async fetchConfigs(force = false) {
      if (!force && this.configs.length && !this.isStale) return
      this.loading = true
      try {
        this.configs = await configApi.list()
        this.lastFetched = Date.now()
      } finally {
        this.loading = false
      }
    },
    async updateConfig(payload: ConfigUpdatePayload) {
      const updated = await configApi.update(payload)
      this.configs = this.configs.map((item) => (item.id === updated.id ? updated : item))
    },
  },
})

