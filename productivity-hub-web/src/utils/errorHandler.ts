/**
 * 提供全局错误处理、错误监控、错误上报等功能
 */

/**
 * 错误类型枚举
 */
export enum ErrorType {
  /** JavaScript运行时错误 */
  RUNTIME_ERROR = 'runtime_error',
  /** Promise未捕获错误 */
  UNHANDLED_REJECTION = 'unhandled_rejection',
  /** 资源加载错误 */
  RESOURCE_ERROR = 'resource_error',
  /** 网络请求错误 */
  NETWORK_ERROR = 'network_error',
  /** Vue组件错误 */
  VUE_ERROR = 'vue_error',
  /** 自定义错误 */
  CUSTOM_ERROR = 'custom_error',
}

/**
 * 错误信息接口
 */
export interface ErrorInfo {
  /** 错误类型 */
  type: ErrorType
  /** 错误消息 */
  message: string
  /** 错误堆栈 */
  stack?: string
  /** 错误来源 */
  source?: string
  /** 错误发生时间 */
  timestamp: number
  /** 用户代理 */
  userAgent?: string
  /** 页面URL */
  url?: string
  /** 额外信息 */
  extra?: Record<string, any>
}

/**
 * 错误处理器配置选项
 */
export interface ErrorHandlerOptions {
  /** 是否启用控制台输出，默认true */
  enableConsole?: boolean
  /** 是否启用错误上报，默认false */
  enableReport?: boolean
  /** 错误上报URL */
  reportUrl?: string
  /** 错误上报函数 */
  reportFn?: (error: ErrorInfo) => void | Promise<void>
  /** 错误过滤函数，返回true表示忽略该错误 */
  filterFn?: (error: ErrorInfo) => boolean
  /** 最大错误数量，超过后不再记录，默认100 */
  maxErrors?: number
}

/**
 * 错误处理器类
 */
class ErrorHandler {
  private options: Required<ErrorHandlerOptions>
  private errors: ErrorInfo[] = []
  private isInitialized = false

  constructor(options: ErrorHandlerOptions = {}) {
    this.options = {
      enableConsole: options.enableConsole ?? true,
      enableReport: options.enableReport ?? false,
      reportUrl: options.reportUrl ?? '',
      reportFn: options.reportFn ?? (() => {}),
      filterFn: options.filterFn ?? (() => false),
      maxErrors: options.maxErrors ?? 100,
    }
  }

  /**
   * 初始化错误处理器
   */
  init() {
    if (this.isInitialized) {
      return
    }

    // 监听JavaScript运行时错误
    window.addEventListener('error', (event) => {
      this.handleError({
        type: ErrorType.RUNTIME_ERROR,
        message: event.message,
        stack: event.error?.stack,
        source: event.filename,
        timestamp: Date.now(),
        userAgent: navigator.userAgent,
        url: window.location.href,
        extra: {
          lineno: event.lineno,
          colno: event.colno,
        },
      })
    })

    // 监听Promise未捕获错误
    window.addEventListener('unhandledrejection', (event) => {
      this.handleError({
        type: ErrorType.UNHANDLED_REJECTION,
        message: event.reason?.message || String(event.reason),
        stack: event.reason?.stack,
        timestamp: Date.now(),
        userAgent: navigator.userAgent,
        url: window.location.href,
        extra: {
          reason: event.reason,
        },
      })
    })

    // 监听资源加载错误
    window.addEventListener('error', (event) => {
      if (event.target && event.target !== window) {
        const target = event.target as HTMLElement
        this.handleError({
          type: ErrorType.RESOURCE_ERROR,
          message: `Failed to load resource: ${target.tagName}`,
          source: (target as HTMLImageElement).src || (target as HTMLLinkElement).href,
          timestamp: Date.now(),
          userAgent: navigator.userAgent,
          url: window.location.href,
          extra: {
            tagName: target.tagName,
          },
        })
      }
    }, true)

    this.isInitialized = true
  }

  /**
   * 处理错误
   */
  private handleError(error: ErrorInfo) {
    // 检查是否应该忽略该错误
    if (this.options.filterFn(error)) {
      return
    }

    // 添加到错误列表
    this.errors.push(error)
    if (this.errors.length > this.options.maxErrors) {
      this.errors.shift()
    }

    // 控制台输出已禁用

    // 错误上报
    if (this.options.enableReport) {
      this.reportError(error)
    }
  }

  /**
   * 上报错误
   */
  private async reportError(error: ErrorInfo) {
    try {
      if (this.options.reportUrl) {
        // 使用fetch上报
        await fetch(this.options.reportUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(error),
        })
      } else if (this.options.reportFn) {
        // 使用自定义上报函数
        await this.options.reportFn(error)
      }
    } catch (err) {
      // 忽略上报错误
    }
  }

  /**
   * 手动记录错误
   */
  recordError(
    type: ErrorType,
    message: string,
    extra?: Record<string, any>
  ) {
    this.handleError({
      type,
      message,
      timestamp: Date.now(),
      userAgent: navigator.userAgent,
      url: window.location.href,
      extra,
    })
  }

  /**
   * 获取错误列表
   */
  getErrors(): ErrorInfo[] {
    return [...this.errors]
  }

  /**
   * 清空错误列表
   */
  clearErrors() {
    this.errors = []
  }

  /**
   * 获取错误统计
   */
  getErrorStats() {
    const stats: Record<ErrorType, number> = {
      [ErrorType.RUNTIME_ERROR]: 0,
      [ErrorType.UNHANDLED_REJECTION]: 0,
      [ErrorType.RESOURCE_ERROR]: 0,
      [ErrorType.NETWORK_ERROR]: 0,
      [ErrorType.VUE_ERROR]: 0,
      [ErrorType.CUSTOM_ERROR]: 0,
    }

    this.errors.forEach((error) => {
      stats[error.type] = (stats[error.type] || 0) + 1
    })

    return {
      total: this.errors.length,
      byType: stats,
    }
  }
}

// 创建全局错误处理器实例
export const errorHandler = new ErrorHandler({
  enableConsole: true,
  enableReport: false,
  maxErrors: 100,
})

/**
 * 初始化全局错误处理
 */
export function initErrorHandler(options?: ErrorHandlerOptions) {
  if (options) {
    Object.assign(errorHandler['options'], options)
  }
  errorHandler.init()
}

/**
 * Vue错误处理函数
 */
export function handleVueError(err: Error, instance: any, info: string) {
  errorHandler.recordError(ErrorType.VUE_ERROR, err.message, {
    componentName: instance?.$options?.name,
    errorInfo: info,
    stack: err.stack,
  })
}

