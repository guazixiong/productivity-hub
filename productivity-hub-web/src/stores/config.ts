import { defineStore } from 'pinia'
import { configApi } from '@/services/api'
import type { ConfigItem, ConfigUpdatePayload, ConfigCreateOrUpdatePayload } from '@/types/config'

interface ConfigState {
  configs: ConfigItem[]
  loading: boolean
  lastFetched: number
}

const CACHE_TTL = 5 * 60 * 1000
const MODULE_DESCRIPTION_KEY = '_module.description'

export const useConfigStore = defineStore('config', {
  state: (): ConfigState => ({
    configs: [],
    loading: false,
    lastFetched: 0,
  }),
  getters: {
    isStale: (state) => Date.now() - state.lastFetched > CACHE_TTL,
    // 从配置项中获取模块描述
    getModuleDescription: (state) => (module: string): string => {
      const configItem = state.configs.find(
        (item) => item.module === module && item.key === MODULE_DESCRIPTION_KEY
      )
      return configItem?.value || ''
    },
  },
  actions: {
    async fetchConfigs(force = false) {
      if (!force && this.configs.length && !this.isStale) return
      this.loading = true
      try {
        this.configs = await configApi.list()
        this.lastFetched = Date.now()
      } catch (error) {
        // 配置加载失败时，使用空数组，不影响应用运行
        this.configs = []
        this.lastFetched = Date.now()
      } finally {
        this.loading = false
      }
    },
    /**
     * 清空本地缓存（用于登出/切换账号时防止拿到旧用户的配置 ID）
     */
    reset() {
      this.configs = []
      this.lastFetched = 0
      this.loading = false
    },
    async updateConfig(payload: ConfigUpdatePayload) {
      const updated = await configApi.update(payload)
      this.configs = this.configs.map((item) => (item.id === updated.id ? updated : item))
    },
    // 保存模块描述到配置项
    async updateModuleDescription(module: string, description: string) {
      const payload: ConfigCreateOrUpdatePayload = {
        module,
        key: MODULE_DESCRIPTION_KEY,
        value: description,
        description: '模块描述',
      }
      const updated = await configApi.createOrUpdate(payload)
      // 更新本地缓存
      const index = this.configs.findIndex((item) => item.id === updated.id)
      if (index >= 0) {
        this.configs[index] = updated
      } else {
        this.configs.push(updated)
      }
    },
  },
})

