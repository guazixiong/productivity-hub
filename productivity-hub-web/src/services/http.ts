import axios, { AxiosHeaders } from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElLoading, ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

const REQUEST_TIMEOUT = 30_000

let activeRequests = 0
let loadingInstance: ReturnType<typeof ElLoading.service> | null = null

type RequestConfig = AxiosRequestConfig & {
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

const http: AxiosInstance = axios.create({
  baseURL: '/',
  timeout: REQUEST_TIMEOUT,
})

http.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  const requestConfig = config as RequestConfig
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
    if ((response.config as RequestConfig).showLoading) {
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

export const request = async <T = unknown>(config: RequestConfig) => {
  const response: AxiosResponse<ApiEnvelope<T>> = await http.request<ApiEnvelope<T>>(config)
  const payload = response.data
  if (payload?.code === 0) {
    return payload.data
  }
  throw new Error(payload?.message ?? '请求失败')
}

export default http

