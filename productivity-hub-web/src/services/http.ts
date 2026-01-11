import axios, { AxiosHeaders } from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElLoading, ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'
import {
  requestCache,
  requestCancelManager,
  retryRequest,
  type OptimizedRequestConfig,
  type RetryConfig,
} from '@/utils/requestOptimizer'

interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

const REQUEST_TIMEOUT = 30_000

let activeRequests = 0
let loadingInstance: ReturnType<typeof ElLoading.service> | null = null

type RequestConfig = OptimizedRequestConfig & {
  /**
   * 是否显示全局 Loading（默认关闭，需要按需开启）
   */
  showLoading?: boolean
  /**
   * 是否静默处理错误（默认 false，设置为 true 时不显示错误消息）
   * 适用于非关键请求，如统计、埋点等
   */
  silent?: boolean
}

const startGlobalLoading = () => {
  activeRequests += 1
  if (activeRequests === 1) {
    loadingInstance = ElLoading.service({
      fullscreen: true,
      lock: true,
      text: '加载中…',
    })
  }
}

const stopGlobalLoading = () => {
  activeRequests = Math.max(0, activeRequests - 1)
  if (activeRequests === 0 && loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}

/**
 * 检测是否在 Android WebView 环境中
 * 通过检查当前协议是否为 file:// 来判断
 */
const isAndroidWebView = (): boolean => {
  if (typeof window === 'undefined') return false
  return window.location.protocol === 'file:'
}

/**
 * 获取 API 基础 URL
 * - 在 Android WebView 中（file:// 协议），使用环境变量 VITE_API_BASE_URL
 * - 在生产环境中，使用环境变量 VITE_API_BASE_URL（因为生产环境没有 Vite 代理）
 * - 在开发环境的浏览器中，使用相对路径 '/'（通过 Vite 代理）
 */
const getApiBaseURL = (): string => {
  // 检查是否为生产环境
  const isProduction = import.meta.env.MODE === 'production'
  
  if (isAndroidWebView() || isProduction) {
    // 在 Android WebView 或生产环境中，必须使用完整的 API 地址
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
    if (!apiBaseUrl) {
      // 未配置 API 地址，使用默认值
      // 如果未配置，尝试使用默认值（需要用户根据实际情况修改）
      console.warn('未配置 VITE_API_BASE_URL，使用默认值 http://127.0.0.1:9881')
      return 'http://127.0.0.1:9881'
    }
    return apiBaseUrl
  }
  // 在开发环境的浏览器中，使用相对路径，通过 Vite 代理转发
  return '/'
}

const http: AxiosInstance = axios.create({
  baseURL: getApiBaseURL(),
  timeout: REQUEST_TIMEOUT,
})

http.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  const requestConfig = config as RequestConfig
  
  // 处理请求取消
  if (requestConfig.requestId) {
    const signal = requestCancelManager.createCancelToken(requestConfig.requestId)
    requestConfig.signal = signal
  }
  
  if (authStore.isAuthenticated) {
    if (!requestConfig.headers) {
      requestConfig.headers = new AxiosHeaders()
    }
    if (typeof requestConfig.headers.set === 'function') {
      requestConfig.headers.set('Authorization', `Bearer ${authStore.token}`)
    } else {
      ;(requestConfig.headers as Record<string, string>).Authorization = `Bearer ${authStore.token}`
    }
  }
  // 确保所有请求都有统一的超时时间与加载态
  if (!requestConfig.timeout) {
    requestConfig.timeout = REQUEST_TIMEOUT
  }
  if (requestConfig.showLoading) {
    startGlobalLoading()
  }
  return config
})

// 用于防止重复弹出登录提示
let isHandlingAuthError = false

http.interceptors.response.use(
  (response: AxiosResponse<ApiEnvelope<unknown>>) => {
    const config = response.config as RequestConfig
    
    // 处理请求取消清理
    if (config.requestId) {
      requestCancelManager.remove(config.requestId)
    }
    
    // 处理请求缓存
    if (config.cache && config.method?.toLowerCase() === 'get') {
      const payload = response.data
      if (payload?.code === 200) {
        requestCache.set(config, payload.data, config.cacheTTL)
      }
    }
    
    if (config.showLoading) {
      stopGlobalLoading()
    }
    return response
  },
  async (error) => {
    const config = error.config as RequestConfig | undefined
    if (config?.showLoading) {
      stopGlobalLoading()
    }

    // 处理401未授权错误（token无效或过期）
    if (error.response?.status === 401) {
      // 防止重复弹出提示
      if (!isHandlingAuthError) {
        isHandlingAuthError = true
        const authStore = useAuthStore()
        
        // 如果当前不在登录页面，才显示弹窗并跳转
        if (router.currentRoute.value.name !== 'Login') {
          try {
            await ElMessageBox.alert(
              '您的登录已过期，请重新登录',
              '登录已过期',
              {
                confirmButtonText: '前往登录',
                type: 'warning',
                showClose: false,
                closeOnClickModal: false,
                closeOnPressEscape: false,
              }
            )
          } catch {
            // 用户关闭弹窗时也会触发，这里忽略
          }
          
          // 清除认证信息
          authStore.logout()
          
          // 跳转到登录页面，并保存当前路径以便登录后返回
          const currentPath = router.currentRoute.value.fullPath
          router.push({
            name: 'Login',
            query: { redirect: currentPath },
          })
        } else {
          // 如果已经在登录页面，只清除认证信息
          authStore.logout()
        }
        
        // 重置标志，允许下次401时再次处理
        setTimeout(() => {
          isHandlingAuthError = false
        }, 1000)
      }
      
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }

    // 处理400错误（请求错误），显示弹窗提示
    if (error.response?.status === 400) {
      const errorMessage = error.response?.data?.message ?? '请求参数错误'
      // 全局统一弹窗提示，不受 silent 选项影响
      await ElMessageBox.alert(errorMessage, '错误提示', {
        type: 'error',
        confirmButtonText: '确定',
      })
      return Promise.reject(new Error(errorMessage))
    }

    const isTimeout =
      error.code === 'ECONNABORTED' ||
      (typeof error.message === 'string' && error.message.toLowerCase().includes('timeout'))
    const message = isTimeout
      ? '请求超时，请重试'
      : error.response?.data?.message ?? error.message ?? '网络错误'
    // 只有在非静默模式下才显示错误消息
    if (isTimeout && !config?.silent) {
      ElMessage.error(message)
    }
    return Promise.reject(new Error(message))
  }
)

export const request = async <T = unknown>(config: RequestConfig): Promise<T> => {
  // 处理缓存响应（仅 GET 请求）
  if (config.cache && (!config.method || config.method.toLowerCase() === 'get')) {
    const cachedData = requestCache.get<T>(config)
    if (cachedData !== null) {
      return cachedData
    }
  }

  // 请求函数
  const requestFn = async (): Promise<T> => {
    const response: AxiosResponse<ApiEnvelope<T>> = await http.request<ApiEnvelope<T>>(config)
    const payload = response.data
    if (payload?.code === 200) {
      return payload.data
    }
    
    // 处理 code 400 错误，显示弹窗提示
    if (payload?.code === 400) {
      const errorMessage = payload?.message ?? '请求失败'
      // 全局统一弹窗提示，不受 silent 选项影响
      await ElMessageBox.alert(errorMessage, '错误提示', {
        type: 'error',
        confirmButtonText: '确定',
      })
    }
    
    // 记录详细的错误信息，便于调试
    const errorMessage = payload?.message ?? '请求失败'
    const errorCode = payload?.code ?? 'unknown'
    console.error(`API 请求失败: ${config.url}, code: ${errorCode}, message: ${errorMessage}`, payload)
    throw new Error(errorMessage)
  }

  // 处理请求重试
  if (config.retry) {
    const retryConfig: RetryConfig = typeof config.retry === 'boolean' ? {} : config.retry
    return retryRequest(requestFn, retryConfig)
  }

  return requestFn()
}

export default http

