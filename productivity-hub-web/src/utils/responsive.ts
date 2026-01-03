/**
 * 响应式工具模块
 * 
 * 提供响应式布局相关的工具函数，包括：
 * - 断点判断函数（isMobile、isTablet、isDesktop）
 * - 屏幕尺寸计算函数
 * - 防抖/节流工具函数
 */

/**
 * 响应式断点常量
 */
export const BREAKPOINTS = {
  /** 移动端断点（< 768px） */
  MOBILE: 768,
  /** 平板端断点（768px - 1024px） */
  TABLET: 1024,
} as const

/**
 * 设备类型枚举
 */
export enum DeviceType {
  /** 移动端 */
  MOBILE = 'mobile',
  /** 平板端 */
  TABLET = 'tablet',
  /** PC端 */
  DESKTOP = 'desktop',
}

/**
 * 屏幕方向枚举
 */
export enum Orientation {
  /** 竖屏 */
  PORTRAIT = 'portrait',
  /** 横屏 */
  LANDSCAPE = 'landscape',
}

/**
 * 判断是否为移动端
 * @param width 屏幕宽度，默认为 window.innerWidth
 * @returns 是否为移动端
 */
export function isMobile(width?: number): boolean {
  const w = width ?? (typeof window !== 'undefined' ? window.innerWidth : 0)
  return w < BREAKPOINTS.MOBILE
}

/**
 * 判断是否为平板端
 * @param width 屏幕宽度，默认为 window.innerWidth
 * @returns 是否为平板端
 */
export function isTablet(width?: number): boolean {
  const w = width ?? (typeof window !== 'undefined' ? window.innerWidth : 0)
  return w >= BREAKPOINTS.MOBILE && w < BREAKPOINTS.TABLET
}

/**
 * 判断是否为PC端
 * @param width 屏幕宽度，默认为 window.innerWidth
 * @returns 是否为PC端
 */
export function isDesktop(width?: number): boolean {
  const w = width ?? (typeof window !== 'undefined' ? window.innerWidth : 0)
  return w >= BREAKPOINTS.TABLET
}

/**
 * 获取设备类型
 * @param width 屏幕宽度，默认为 window.innerWidth
 * @returns 设备类型
 */
export function getDeviceType(width?: number): DeviceType {
  if (isMobile(width)) {
    return DeviceType.MOBILE
  }
  if (isTablet(width)) {
    return DeviceType.TABLET
  }
  return DeviceType.DESKTOP
}

/**
 * 获取屏幕尺寸信息
 * @returns 屏幕尺寸信息对象
 */
export function getScreenSize(): { width: number; height: number } {
  if (typeof window === 'undefined') {
    return { width: 0, height: 0 }
  }
  return {
    width: window.innerWidth,
    height: window.innerHeight,
  }
}

/**
 * 获取屏幕方向
 * @returns 屏幕方向
 */
export function getOrientation(): Orientation {
  if (typeof window === 'undefined') {
    return Orientation.PORTRAIT
  }
  return window.innerWidth > window.innerHeight
    ? Orientation.LANDSCAPE
    : Orientation.PORTRAIT
}

/**
 * 防抖函数类型
 */
export type DebounceFunction<T extends (...args: any[]) => any> = (
  ...args: Parameters<T>
) => void

/**
 * 防抖函数
 * 
 * 在指定时间内，如果函数被多次调用，只执行最后一次调用
 * 
 * @param fn 要防抖的函数
 * @param delay 延迟时间（毫秒），默认 300ms
 * @returns 防抖后的函数
 */
export function debounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): DebounceFunction<T> {
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
 * 节流函数类型
 */
export type ThrottleFunction<T extends (...args: any[]) => any> = (
  ...args: Parameters<T>
) => void

/**
 * 节流函数
 * 
 * 在指定时间内，函数最多执行一次
 * 
 * @param fn 要节流的函数
 * @param delay 延迟时间（毫秒），默认 300ms
 * @returns 节流后的函数
 */
export function throttle<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): ThrottleFunction<T> {
  let lastCallTime = 0
  let timeoutId: ReturnType<typeof setTimeout> | null = null

  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()
    const timeSinceLastCall = now - lastCallTime

    if (timeSinceLastCall >= delay) {
      // 如果距离上次调用已经超过延迟时间，立即执行
      lastCallTime = now
      fn.apply(this, args)
    } else {
      // 否则，设置定时器，在剩余时间后执行
      if (timeoutId !== null) {
        clearTimeout(timeoutId)
      }
      timeoutId = setTimeout(() => {
        lastCallTime = Date.now()
        fn.apply(this, args)
        timeoutId = null
      }, delay - timeSinceLastCall)
    }
  }
}

