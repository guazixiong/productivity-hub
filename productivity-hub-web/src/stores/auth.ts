import { defineStore } from 'pinia'
import type { LoginPayload, UserInfo, AuthResponse, UserProfileUpdatePayload } from '@/types/auth'
import { authApi, userApi } from '@/services/api'
import { useConfigStore } from '@/stores/config'
import { useMenuStore } from '@/stores/menu'

const TOKEN_KEY = 'phub/token'
const REFRESH_KEY = 'phub/refreshToken'
const USER_KEY = 'phub/user'

interface AuthState {
  token: string
  refreshToken: string
  user: UserInfo | null
  loading: boolean
  isHydrated: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: '',
    refreshToken: '',
    user: null,
    loading: false,
    isHydrated: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    async login(payload: LoginPayload) {
      this.loading = true
      try {
        const response = await authApi.login(payload)
        this.applyAuthResponse(response)
        // 登录成功后拉取菜单
        const menuStore = useMenuStore()
        await menuStore.fetchMenus()
      } finally {
        this.loading = false
      }
    },
    async resetPassword() {
      this.loading = true
      try {
        await authApi.resetPassword()
      } finally {
        this.loading = false
      }
    },
    logout() {
      this.token = ''
      this.refreshToken = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(REFRESH_KEY)
      localStorage.removeItem(USER_KEY)
      // 切换账号时清空配置缓存，避免沿用上一位用户的配置 ID
      const configStore = useConfigStore()
      configStore.reset()
      // 清除菜单缓存
      const menuStore = useMenuStore()
      menuStore.clearCache()
    },
    hydrateFromCache() {
      if (this.isHydrated) return
      this.token = localStorage.getItem(TOKEN_KEY) ?? ''
      this.refreshToken = localStorage.getItem(REFRESH_KEY) ?? ''
      const cachedUser = localStorage.getItem(USER_KEY)
      this.user = cachedUser ? (JSON.parse(cachedUser) as UserInfo) : null
      this.isHydrated = true
    },
    applyAuthResponse(response: AuthResponse) {
      this.token = response.token
      this.refreshToken = response.refreshToken
      this.user = response.user
      this.isHydrated = true
      localStorage.setItem(TOKEN_KEY, response.token)
      localStorage.setItem(REFRESH_KEY, response.refreshToken)
      localStorage.setItem(USER_KEY, JSON.stringify(response.user))
    },
    async updateUserInfo(payload: UserProfileUpdatePayload) {
      const updatedUser = await userApi.updateProfile(payload)
      this.user = updatedUser
      localStorage.setItem(USER_KEY, JSON.stringify(updatedUser))
      return updatedUser
    },
    async refreshUserInfo() {
      try {
        const userInfo = await userApi.getProfile()
        this.user = userInfo
        localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
        return userInfo
      } catch (error) {
        console.error('刷新用户信息失败:', error)
        throw error
      }
    },
  },
})

