import axios, { AxiosHeaders } from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

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

http.interceptors.request.use((config: RequestConfig) => {
  const authStore = useAuthStore()
  if (authStore.isAuthenticated) {
    if (!config.headers) {
      config.headers = new AxiosHeaders()
    }
    if (typeof config.headers.set === 'function') {
      config.headers.set('Authorization', `Bearer ${authStore.token}`)
    } else {
      ;(config.headers as Record<string, string>).Authorization = `Bearer ${authStore.token}`
    }
  }
  // 确保所有请求都有统一的超时时间与加载态
  if (!config.timeout) {
    config.timeout = REQUEST_TIMEOUT
  }
  if (config.showLoading) {
    startGlobalLoading()
  }
  return config
})

http.interceptors.response.use(
  (response: AxiosResponse<ApiEnvelope<unknown>>) => {
    if ((response.config as RequestConfig).showLoading) {
      stopGlobalLoading()
    }
    return response
  },
  (error) => {
    const config = error.config as RequestConfig | undefined
    if (config?.showLoading) {
      stopGlobalLoading()
    }
    const isTimeout =
      error.code === 'ECONNABORTED' ||
      (typeof error.message === 'string' && error.message.toLowerCase().includes('timeout'))
    const message = isTimeout
      ? '请求超时，请重试'
      : error.response?.data?.message ?? error.message ?? '网络错误'
    if (isTimeout) {
      // 触发显式提示，避免静默失败
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

