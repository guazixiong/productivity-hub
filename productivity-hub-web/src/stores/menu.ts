import { defineStore } from 'pinia'
import { aclAuthApi } from '@/services/api'
import type { AclMenuTreeVO } from '@/types/acl'

const MENU_CACHE_KEY = 'phub/userMenus'

interface MenuState {
  menus: AclMenuTreeVO[]
  loading: boolean
  lastFetchTime: number | null
}

export const useMenuStore = defineStore('menu', {
  state: (): MenuState => ({
    menus: [],
    loading: false,
    lastFetchTime: null,
  }),
  getters: {
    hasMenus: (state) => state.menus.length > 0,
  },
  actions: {
    /**
     * 从缓存中恢复菜单
     */
    hydrateFromCache() {
      try {
        const cached = localStorage.getItem(MENU_CACHE_KEY)
        if (cached) {
          const data = JSON.parse(cached)
          this.menus = data.menus || []
          this.lastFetchTime = data.lastFetchTime || null
        }
      } catch (error) {
        console.error('恢复菜单缓存失败:', error)
        this.menus = []
        this.lastFetchTime = null
      }
    },

    /**
     * 获取当前用户的菜单（登录后拉取）
     */
    async fetchMenus() {
      this.loading = true
      try {
        const menus = await aclAuthApi.getCurrentUserMenus()
        this.menus = menus
        this.lastFetchTime = Date.now()
        // 保存到缓存
        this.saveToCache()
      } catch (error) {
        console.error('获取菜单失败:', error)
        // 如果获取失败，尝试从缓存恢复
        this.hydrateFromCache()
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 刷新菜单（按钮刷新）
     */
    async refreshMenus() {
      return this.fetchMenus()
    },

    /**
     * 保存菜单到缓存
     */
    saveToCache() {
      try {
        const data = {
          menus: this.menus,
          lastFetchTime: this.lastFetchTime,
        }
        localStorage.setItem(MENU_CACHE_KEY, JSON.stringify(data))
      } catch (error) {
        console.error('保存菜单缓存失败:', error)
      }
    },

    /**
     * 清除菜单缓存
     */
    clearCache() {
      this.menus = []
      this.lastFetchTime = null
      localStorage.removeItem(MENU_CACHE_KEY)
    },
  },
})

