import axios, { AxiosHeaders } from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/auth'

interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

const http: AxiosInstance = axios.create({
  baseURL: '/',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
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
  return config
})

http.interceptors.response.use(undefined, (error) => {
  const message = error.response?.data?.message ?? error.message ?? '网络错误'
  return Promise.reject(message)
})

export const request = async <T = unknown>(config: AxiosRequestConfig) => {
  const response: AxiosResponse<ApiEnvelope<T>> = await http.request<ApiEnvelope<T>>(config)
  const payload = response.data
  if (payload?.code === 0) {
    return payload.data
  }
  throw new Error(payload?.message ?? '请求失败')
}

export default http

