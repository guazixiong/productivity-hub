/**
 * 响应式工具模块单元测试
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import {
  BREAKPOINTS,
  DeviceType,
  Orientation,
  isMobile,
  isTablet,
  isDesktop,
  getDeviceType,
  getScreenSize,
  getOrientation,
  debounce,
  throttle,
} from '../responsive'

describe('响应式工具模块', () => {
  describe('断点判断函数', () => {
    it('isMobile: 应该正确判断移动端（宽度 < 768px）', () => {
      expect(isMobile(767)).toBe(true)
      expect(isMobile(500)).toBe(true)
      expect(isMobile(768)).toBe(false)
      expect(isMobile(1024)).toBe(false)
    })

    it('isTablet: 应该正确判断平板端（768px <= 宽度 < 1024px）', () => {
      expect(isTablet(768)).toBe(true)
      expect(isTablet(900)).toBe(true)
      expect(isTablet(1023)).toBe(true)
      expect(isTablet(767)).toBe(false)
      expect(isTablet(1024)).toBe(false)
    })

    it('isDesktop: 应该正确判断PC端（宽度 >= 1024px）', () => {
      expect(isDesktop(1024)).toBe(true)
      expect(isDesktop(1920)).toBe(true)
      expect(isDesktop(1023)).toBe(false)
      expect(isDesktop(768)).toBe(false)
    })

    it('getDeviceType: 应该正确返回设备类型', () => {
      expect(getDeviceType(500)).toBe(DeviceType.MOBILE)
      expect(getDeviceType(768)).toBe(DeviceType.TABLET)
      expect(getDeviceType(1024)).toBe(DeviceType.DESKTOP)
    })
  })

  describe('屏幕尺寸计算函数', () => {
    beforeEach(() => {
      // Mock window.innerWidth 和 window.innerHeight
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 1920,
      })
      Object.defineProperty(window, 'innerHeight', {
        writable: true,
        configurable: true,
        value: 1080,
      })
    })

    afterEach(() => {
      vi.restoreAllMocks()
    })

    it('getScreenSize: 应该正确返回屏幕尺寸', () => {
      const size = getScreenSize()
      expect(size.width).toBe(1920)
      expect(size.height).toBe(1080)
    })

    it('getOrientation: 应该正确返回屏幕方向', () => {
      // 横屏
      Object.defineProperty(window, 'innerWidth', { value: 1920, writable: true, configurable: true })
      Object.defineProperty(window, 'innerHeight', { value: 1080, writable: true, configurable: true })
      expect(getOrientation()).toBe(Orientation.LANDSCAPE)

      // 竖屏
      Object.defineProperty(window, 'innerWidth', { value: 375, writable: true, configurable: true })
      Object.defineProperty(window, 'innerHeight', { value: 812, writable: true, configurable: true })
      expect(getOrientation()).toBe(Orientation.PORTRAIT)
    })
  })

  describe('防抖函数', () => {
    beforeEach(() => {
      vi.useFakeTimers()
    })

    afterEach(() => {
      vi.restoreAllMocks()
    })

    it('debounce: 应该在延迟后执行函数', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn, 300)

      debouncedFn()
      expect(fn).not.toHaveBeenCalled()

      vi.advanceTimersByTime(300)
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('debounce: 应该取消之前的调用，只执行最后一次', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn, 300)

      debouncedFn()
      vi.advanceTimersByTime(100)
      debouncedFn()
      vi.advanceTimersByTime(100)
      debouncedFn()
      vi.advanceTimersByTime(300)

      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('debounce: 应该正确传递参数', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn, 300)

      debouncedFn('arg1', 'arg2')
      vi.advanceTimersByTime(300)

      expect(fn).toHaveBeenCalledWith('arg1', 'arg2')
    })
  })

  describe('节流函数', () => {
    beforeEach(() => {
      vi.useFakeTimers()
    })

    afterEach(() => {
      vi.restoreAllMocks()
    })

    it('throttle: 应该在延迟时间内只执行一次', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn, 300)

      throttledFn()
      expect(fn).toHaveBeenCalledTimes(1)

      throttledFn()
      throttledFn()
      expect(fn).toHaveBeenCalledTimes(1)

      vi.advanceTimersByTime(300)
      expect(fn).toHaveBeenCalledTimes(2)
    })

    it('throttle: 应该正确传递参数', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn, 300)

      throttledFn('arg1', 'arg2')
      expect(fn).toHaveBeenCalledWith('arg1', 'arg2')
    })
  })
})

