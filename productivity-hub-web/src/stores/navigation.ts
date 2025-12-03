import { defineStore } from 'pinia'

interface NavigationState {
  // 记录每个路由的来源页面
  // key: 当前路由路径, value: 来源路由路径
  history: Map<string, string>
}

export const useNavigationStore = defineStore('navigation', {
  state: (): NavigationState => ({
    history: new Map(),
  }),
  actions: {
    // 记录导航历史
    recordNavigation(to: string, from: string) {
      // 如果来源页面存在且不是当前页面，则记录
      if (from && from !== to) {
        this.history.set(to, from)
      }
    },
    // 获取来源页面
    getPreviousRoute(currentRoute: string): string | null {
      return this.history.get(currentRoute) || null
    },
    // 清除某个路由的历史记录
    clearHistory(route: string) {
      this.history.delete(route)
    },
    // 清除所有历史记录
    clearAllHistory() {
      this.history.clear()
    },
  },
})

