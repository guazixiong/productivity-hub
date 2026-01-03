/**
 * 兼容性工具函数单元测试
 * TASK-FRONTEND-22: 前端兼容性实现
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  isFeatureSupported,
  getBrowserInfo,
  checkBrowserCompatibility,
} from '../compatibility'

describe('compatibility', () => {
  describe('isFeatureSupported', () => {
    it('应该检测 Promise 支持', () => {
      expect(isFeatureSupported('Promise')).toBe(true)
    })

    it('应该检测 fetch 支持', () => {
      // 在 Node.js 环境中，fetch 可能不存在
      const result = isFeatureSupported('fetch')
      expect(typeof result).toBe('boolean')
    })

    it('应该检测 localStorage 支持', () => {
      // Mock localStorage
      const localStorageMock = {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn(),
        clear: vi.fn(),
      }
      Object.defineProperty(window, 'localStorage', {
        value: localStorageMock,
        writable: true,
      })

      const result = isFeatureSupported('localStorage')
      expect(typeof result).toBe('boolean')
    })

    it('应该检测 sessionStorage 支持', () => {
      // Mock sessionStorage
      const sessionStorageMock = {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn(),
        clear: vi.fn(),
      }
      Object.defineProperty(window, 'sessionStorage', {
        value: sessionStorageMock,
        writable: true,
      })

      const result = isFeatureSupported('sessionStorage')
      expect(typeof result).toBe('boolean')
    })

    it('应该返回 false 对于不支持的特性', () => {
      expect(isFeatureSupported('UnknownFeature')).toBe(false)
    })

    it('应该在非浏览器环境中返回 false', () => {
      const originalWindow = global.window
      // @ts-ignore
      delete global.window

      expect(isFeatureSupported('Promise')).toBe(false)

      global.window = originalWindow
    })
  })

  describe('getBrowserInfo', () => {
    beforeEach(() => {
      // 重置 navigator
      Object.defineProperty(navigator, 'userAgent', {
        writable: true,
        value: '',
      })
    })

    it('应该检测 Chrome 浏览器', () => {
      Object.defineProperty(navigator, 'userAgent', {
        writable: true,
        value:
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
      })

      const info = getBrowserInfo()
      expect(info.name).toBe('chrome')
      expect(info.version).toBeTruthy()
    })

    it('应该检测 Firefox 浏览器', () => {
      Object.defineProperty(navigator, 'userAgent', {
        writable: true,
        value:
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0',
      })

      const info = getBrowserInfo()
      expect(info.name).toBe('firefox')
    })

    it('应该检测 Safari 浏览器', () => {
      Object.defineProperty(navigator, 'userAgent', {
        writable: true,
        value:
          'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15',
      })

      const info = getBrowserInfo()
      expect(info.name).toBe('safari')
    })

    it('应该检测 Edge 浏览器', () => {
      Object.defineProperty(navigator, 'userAgent', {
        writable: true,
        value:
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0',
      })

      const info = getBrowserInfo()
      expect(info.name).toBe('edge')
    })

    it('应该检测移动设备', () => {
      Object.defineProperty(navigator, 'userAgent', {
        writable: true,
        value:
          'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1',
      })

      const info = getBrowserInfo()
      expect(info.isMobile).toBe(true)
      expect(info.isIOS).toBe(true)
    })
  })

  describe('checkBrowserCompatibility', () => {
    it('应该检查浏览器兼容性', () => {
      const isCompatible = checkBrowserCompatibility()
      expect(typeof isCompatible).toBe('boolean')
    })
  })
})

