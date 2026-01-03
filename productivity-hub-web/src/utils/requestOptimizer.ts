/**
 * 请求优化工具
 * 提供请求缓存、请求取消、请求重试等功能
 */

import type { AxiosRequestConfig } from 'axios'

/**
 * 请求缓存项
 */
interface CacheItem<T> {
  data: T
  timestamp: number
  expireTime: number
}

/**
 * 请求缓存管理器
 */
class RequestCache {
  private cache = new Map<string, CacheItem<any>>()

  /**
   * 生成缓存键
   */
  private getCacheKey(config: AxiosRequestConfig): string {
    const { method, url, params, data } = config
    return `${method}:${url}:${JSON.stringify(params)}:${JSON.stringify(data)}`
  }

  /**
   * 获取缓存
   */
  get<T>(config: AxiosRequestConfig): T | null {
    const key = this.getCacheKey(config)
    const item = this.cache.get(key)
    if (!item) return null

    // 检查是否过期
    if (Date.now() > item.expireTime) {
      this.cache.delete(key)
      return null
    }

    return item.data as T
  }

  /**
   * 设置缓存
   */
  set<T>(config: AxiosRequestConfig, data: T, ttl: number = 5 * 60 * 1000): void {
    const key = this.getCacheKey(config)
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      expireTime: Date.now() + ttl,
    })
  }

  /**
   * 清除缓存
   */
  clear(): void {
    this.cache.clear()
  }

  /**
   * 删除指定缓存
   */
  delete(config: AxiosRequestConfig): void {
    const key = this.getCacheKey(config)
    this.cache.delete(key)
  }

  /**
   * 清理过期缓存
   */
  cleanExpired(): void {
    const now = Date.now()
    for (const [key, item] of this.cache.entries()) {
      if (now > item.expireTime) {
        this.cache.delete(key)
      }
    }
  }
}

/**
 * 请求取消管理器
 */
class RequestCancelManager {
  private cancelTokens = new Map<string, AbortController>()

  /**
   * 创建取消令牌
   */
  createCancelToken(requestId: string): AbortSignal {
    // 如果已存在，先取消之前的请求
    this.cancel(requestId)

    const controller = new AbortController()
    this.cancelTokens.set(requestId, controller)
    return controller.signal
  }

  /**
   * 取消请求
   */
  cancel(requestId: string): void {
    const controller = this.cancelTokens.get(requestId)
    if (controller) {
      controller.abort()
      this.cancelTokens.delete(requestId)
    }
  }

  /**
   * 取消所有请求
   */
  cancelAll(): void {
    for (const controller of this.cancelTokens.values()) {
      controller.abort()
    }
    this.cancelTokens.clear()
  }

  /**
   * 移除取消令牌（请求完成后调用）
   */
  remove(requestId: string): void {
    this.cancelTokens.delete(requestId)
  }
}

/**
 * 请求重试配置
 */
export interface RetryConfig {
  /** 重试次数，默认 3 次 */
  retries?: number
  /** 重试延迟（毫秒），默认 1000ms */
  retryDelay?: number
  /** 是否启用指数退避，默认 true */
  exponentialBackoff?: boolean
  /** 重试条件函数，返回 true 时重试 */
  retryCondition?: (error: any) => boolean
}

/**
 * 请求重试工具
 */
export async function retryRequest<T>(
  requestFn: () => Promise<T>,
  config: RetryConfig = {}
): Promise<T> {
  const {
    retries = 3,
    retryDelay = 1000,
    exponentialBackoff = true,
    retryCondition = (error) => {
      // 默认重试条件：网络错误或 5xx 错误
      return (
        !error.response ||
        (error.response.status >= 500 && error.response.status < 600)
      )
    },
  } = config

  let lastError: any

  for (let i = 0; i <= retries; i++) {
    try {
      return await requestFn()
    } catch (error: any) {
      lastError = error

      // 检查是否应该重试
      if (i < retries && retryCondition(error)) {
        const delay = exponentialBackoff ? retryDelay * Math.pow(2, i) : retryDelay
        await new Promise((resolve) => setTimeout(resolve, delay))
        continue
      }

      // 不应该重试或已达到最大重试次数
      throw error
    }
  }

  throw lastError
}

// 创建全局实例
export const requestCache = new RequestCache()
export const requestCancelManager = new RequestCancelManager()

// 定期清理过期缓存
if (typeof window !== 'undefined') {
  setInterval(() => {
    requestCache.cleanExpired()
  }, 60 * 1000) // 每分钟清理一次
}

/**
 * 请求配置扩展
 */
export interface OptimizedRequestConfig extends AxiosRequestConfig {
  /** 是否启用缓存，默认 false */
  cache?: boolean
  /** 缓存时间（毫秒），默认 5 分钟 */
  cacheTTL?: number
  /** 请求 ID，用于取消请求 */
  requestId?: string
  /** 是否启用重试，默认 false */
  retry?: boolean | RetryConfig
}

