/**
 * 性能优化工具
 * 
 * 提供性能监控、性能优化等功能
 */

/**
 * 性能指标接口
 */
export interface PerformanceMetrics {
  /** 首次内容绘制时间（FCP） */
  fcp?: number
  /** 最大内容绘制时间（LCP） */
  lcp?: number
  /** 首次输入延迟（FID） */
  fid?: number
  /** 累积布局偏移（CLS） */
  cls?: number
  /** 首屏加载时间 */
  loadTime?: number
  /** DOM内容加载时间 */
  domContentLoaded?: number
}

/**
 * 性能监控类
 */
class PerformanceMonitor {
  private metrics: PerformanceMetrics = {}
  private observers: PerformanceObserver[] = []

  /**
   * 初始化性能监控
   */
  init() {
    if (typeof window === 'undefined' || !window.performance) {
      console.warn('Performance API is not supported')
      return
    }

    // 监控LCP（最大内容绘制）
    this.observeLCP()

    // 监控FID（首次输入延迟）
    this.observeFID()

    // 监控CLS（累积布局偏移）
    this.observeCLS()

    // 监控FCP（首次内容绘制）
    this.observeFCP()

    // 监控页面加载时间
    this.observeLoadTime()
  }

  /**
   * 监控LCP（最大内容绘制）
   */
  private observeLCP() {
    if (!('PerformanceObserver' in window)) return

    try {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries()
        const lastEntry = entries[entries.length - 1] as PerformancePaintTiming
        this.metrics.lcp = lastEntry.renderTime || lastEntry.loadTime
      })

      observer.observe({ entryTypes: ['largest-contentful-paint'] })
      this.observers.push(observer)
    } catch (error) {
      console.warn('Failed to observe LCP:', error)
    }
  }

  /**
   * 监控FID（首次输入延迟）
   */
  private observeFID() {
    if (!('PerformanceObserver' in window)) return

    try {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries()
        entries.forEach((entry) => {
          if (entry.entryType === 'first-input') {
            const fidEntry = entry as PerformanceEventTiming
            this.metrics.fid = fidEntry.processingStart - fidEntry.startTime
          }
        })
      })

      observer.observe({ entryTypes: ['first-input'] })
      this.observers.push(observer)
    } catch (error) {
      console.warn('Failed to observe FID:', error)
    }
  }

  /**
   * 监控CLS（累积布局偏移）
   */
  private observeCLS() {
    if (!('PerformanceObserver' in window)) return

    try {
      let clsValue = 0
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries()
        entries.forEach((entry: any) => {
          if (!entry.hadRecentInput) {
            clsValue += entry.value
          }
        })
        this.metrics.cls = clsValue
      })

      observer.observe({ entryTypes: ['layout-shift'] })
      this.observers.push(observer)
    } catch (error) {
      console.warn('Failed to observe CLS:', error)
    }
  }

  /**
   * 监控FCP（首次内容绘制）
   */
  private observeFCP() {
    if (!('PerformanceObserver' in window)) return

    try {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries()
        entries.forEach((entry) => {
          if (entry.name === 'first-contentful-paint') {
            this.metrics.fcp = entry.startTime
          }
        })
      })

      observer.observe({ entryTypes: ['paint'] })
      this.observers.push(observer)
    } catch (error) {
      console.warn('Failed to observe FCP:', error)
    }
  }

  /**
   * 监控页面加载时间
   */
  private observeLoadTime() {
    if (document.readyState === 'complete') {
      this.calculateLoadTime()
    } else {
      window.addEventListener('load', () => {
        this.calculateLoadTime()
      })
    }
  }

  /**
   * 计算页面加载时间
   */
  private calculateLoadTime() {
    const timing = performance.timing
    if (timing) {
      this.metrics.loadTime = timing.loadEventEnd - timing.navigationStart
      this.metrics.domContentLoaded =
        timing.domContentLoadedEventEnd - timing.navigationStart
    }
  }

  /**
   * 获取性能指标
   */
  getMetrics(): PerformanceMetrics {
    return { ...this.metrics }
  }

  /**
   * 清理监控器
   */
  disconnect() {
    this.observers.forEach((observer) => observer.disconnect())
    this.observers = []
  }
}

// 创建全局性能监控实例
export const performanceMonitor = new PerformanceMonitor()

/**
 * 初始化性能监控
 */
export function initPerformanceMonitor() {
  performanceMonitor.init()
}

/**
 * 图片懒加载
 */
export function lazyLoadImage(img: HTMLImageElement, src: string) {
  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            img.src = src
            observer.unobserve(img)
          }
        })
      },
      { rootMargin: '50px' }
    )
    observer.observe(img)
  } else {
    // 降级方案：直接加载
    img.src = src
  }
}

/**
 * 防抖函数（性能优化版本）
 */
export function debounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout> | null = null

  return function (this: any, ...args: Parameters<T>) {
    if (timeoutId !== null) {
      clearTimeout(timeoutId)
    }
    timeoutId = setTimeout(() => {
      fn.apply(this, args)
      timeoutId = null
    }, delay)
  }
}

/**
 * 节流函数（性能优化版本）
 */
export function throttle<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): (...args: Parameters<T>) => void {
  let lastCallTime = 0

  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()
    if (now - lastCallTime >= delay) {
      lastCallTime = now
      fn.apply(this, args)
    }
  }
}

