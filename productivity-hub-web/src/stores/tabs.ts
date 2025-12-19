import { defineStore } from 'pinia'
import type { RouteLocationNormalized } from 'vue-router'

export interface TabItem {
  path: string
  name: string
  title: string
  query?: Record<string, any>
  params?: Record<string, any>
  meta?: Record<string, any>
  refreshKey?: number
}

interface TabsState {
  tabs: TabItem[]
  activeTab: string | null
}

export const useTabsStore = defineStore('tabs', {
  state: (): TabsState => ({
    tabs: [],
    activeTab: null,
  }),

  getters: {
    // 获取当前激活的标签页
    currentTab: (state): TabItem | null => {
      return state.tabs.find(tab => tab.path === state.activeTab) || null
    },
    // 检查标签页是否存在
    hasTab: (state) => (path: string) => {
      return state.tabs.some(tab => tab.path === path)
    },
  },

  actions: {
    // 添加标签页
    addTab(route: RouteLocationNormalized) {
      // 排除登录页和404页
      if (route.meta?.public || route.name === 'NotFound') {
        return
      }

      const path = route.fullPath
      
      // 如果标签页已存在，直接激活
      if (this.hasTab(path)) {
        this.activeTab = path
        return
      }

      // 创建新标签页
      const tab: TabItem = {
        path,
        name: route.name as string,
        title: (route.meta?.title as string) || route.name as string || '未命名',
        query: { ...route.query },
        params: { ...route.params },
        meta: { ...route.meta },
        refreshKey: 0,
      }

      this.tabs.push(tab)
      this.activeTab = path
    },

    // 移除标签页
    removeTab(path: string) {
      const index = this.tabs.findIndex(tab => tab.path === path)
      if (index === -1) return

      this.tabs.splice(index, 1)

      // 如果移除的是当前激活的标签页，需要切换到其他标签页
      if (this.activeTab === path) {
        if (this.tabs.length > 0) {
          // 优先切换到右侧的标签页，如果没有则切换到左侧
          const nextTab = this.tabs[index] || this.tabs[index - 1]
          this.activeTab = nextTab?.path || null
        } else {
          this.activeTab = null
        }
      }
    },

    // 关闭其他标签页
    closeOtherTabs(currentPath: string) {
      this.tabs = this.tabs.filter(tab => tab.path === currentPath)
      this.activeTab = currentPath
    },

    // 关闭所有标签页
    closeAllTabs() {
      this.tabs = []
      this.activeTab = null
    },

    // 设置激活的标签页
    setActiveTab(path: string) {
      if (this.hasTab(path)) {
        this.activeTab = path
      }
    },

    // 刷新指定或当前标签页
    refreshTab(path?: string) {
      const targetPath = path || this.activeTab
      if (!targetPath) return
      
      const tab = this.tabs.find(tab => tab.path === targetPath)
      if (tab) {
        // 增加该标签页的 refreshKey，触发组件重新渲染
        tab.refreshKey = (tab.refreshKey || 0) + 1
        // 如果刷新的是非当前激活标签，先切换过去
        if (targetPath !== this.activeTab) {
          this.activeTab = targetPath
        }
      }
    },
    
    // 获取指定标签页的 refreshKey
    getRefreshKey(path: string): number {
      const tab = this.tabs.find(tab => tab.path === path)
      return tab?.refreshKey || 0
    },

    // 更新标签页信息（当路由变化时）
    updateTab(route: RouteLocationNormalized) {
      const path = route.fullPath
      const tab = this.tabs.find(tab => tab.path === path)
      
      if (tab) {
        tab.title = (route.meta?.title as string) || route.name as string || '未命名'
        tab.query = { ...route.query }
        tab.params = { ...route.params }
        tab.meta = { ...route.meta }
      }
    },

    // 清除所有标签页（用于登出等场景）
    clearTabs() {
      this.tabs = []
      this.activeTab = null
    },
  },
})

