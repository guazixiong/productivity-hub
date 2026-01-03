/**
 * 认证Store单元测试
 * TASK-TEST-03: 前端单元测试编写
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'
import { authApi, userApi } from '@/services/api'
import { useConfigStore } from '../config'

// Mock API
vi.mock('@/services/api', () => ({
  authApi: {
    login: vi.fn(),
    resetPassword: vi.fn(),
  },
  userApi: {
    updateProfile: vi.fn(),
  },
}))

// Mock config store
vi.mock('../config', () => ({
  useConfigStore: vi.fn(),
}))

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  afterEach(() => {
    localStorage.clear()
    vi.restoreAllMocks()
  })

  it('应该正确初始化状态', () => {
    const store = useAuthStore()

    expect(store.token).toBe('')
    expect(store.refreshToken).toBe('')
    expect(store.user).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.isHydrated).toBe(false)
    expect(store.isAuthenticated).toBe(false)
  })

  it('应该正确执行登录', async () => {
    const store = useAuthStore()
    const mockResponse = {
      token: 'test_token',
      refreshToken: 'test_refresh_token',
      user: {
        id: 'user_001',
        username: 'test_user',
        email: 'test@example.com',
      },
    }

    vi.mocked(authApi.login).mockResolvedValue(mockResponse)

    await store.login({
      username: 'test_user',
      password: 'password123',
      captcha: '1234',
      captchaId: 'captcha_001',
    })

    expect(store.token).toBe('test_token')
    expect(store.refreshToken).toBe('test_refresh_token')
    expect(store.user).toEqual(mockResponse.user)
    expect(store.isAuthenticated).toBe(true)
    expect(localStorage.getItem('phub/token')).toBe('test_token')
  })

  it('应该正确执行登出', () => {
    const store = useAuthStore()
    const mockConfigStore = {
      reset: vi.fn(),
    }
    vi.mocked(useConfigStore).mockReturnValue(mockConfigStore as any)

    // 先设置一些状态
    store.token = 'test_token'
    store.refreshToken = 'test_refresh_token'
    store.user = { id: 'user_001', username: 'test_user' } as any
    localStorage.setItem('phub/token', 'test_token')
    localStorage.setItem('phub/refreshToken', 'test_refresh_token')
    localStorage.setItem('phub/user', JSON.stringify({ id: 'user_001' }))

    store.logout()

    expect(store.token).toBe('')
    expect(store.refreshToken).toBe('')
    expect(store.user).toBeNull()
    expect(localStorage.getItem('phub/token')).toBeNull()
    expect(mockConfigStore.reset).toHaveBeenCalled()
  })

  it('应该从缓存恢复状态', () => {
    localStorage.setItem('phub/token', 'cached_token')
    localStorage.setItem('phub/refreshToken', 'cached_refresh_token')
    localStorage.setItem('phub/user', JSON.stringify({ id: 'user_001', username: 'test_user' }))

    const store = useAuthStore()
    store.hydrateFromCache()

    expect(store.token).toBe('cached_token')
    expect(store.refreshToken).toBe('cached_refresh_token')
    expect(store.user).toEqual({ id: 'user_001', username: 'test_user' })
    expect(store.isHydrated).toBe(true)
  })

  it('应该正确更新用户信息', async () => {
    const store = useAuthStore()
    const mockUpdatedUser = {
      id: 'user_001',
      username: 'updated_user',
      email: 'updated@example.com',
    }

    vi.mocked(userApi.updateProfile).mockResolvedValue(mockUpdatedUser as any)

    const result = await store.updateUserInfo({
      username: 'updated_user',
    })

    expect(store.user).toEqual(mockUpdatedUser)
    expect(result).toEqual(mockUpdatedUser)
    expect(localStorage.getItem('phub/user')).toBe(JSON.stringify(mockUpdatedUser))
  })

  it('应该正确应用认证响应', () => {
    const store = useAuthStore()
    const mockResponse = {
      token: 'new_token',
      refreshToken: 'new_refresh_token',
      user: {
        id: 'user_002',
        username: 'new_user',
      },
    }

    store.applyAuthResponse(mockResponse)

    expect(store.token).toBe('new_token')
    expect(store.refreshToken).toBe('new_refresh_token')
    expect(store.user).toEqual(mockResponse.user)
    expect(store.isHydrated).toBe(true)
    expect(localStorage.getItem('phub/token')).toBe('new_token')
  })
})

