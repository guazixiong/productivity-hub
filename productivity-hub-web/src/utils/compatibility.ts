/**
 * 浏览器兼容性工具
 * 提供 Polyfill 和兼容性检测功能
 */

/**
 * 检测浏览器是否支持某个特性
 */
export function isFeatureSupported(feature: string): boolean {
  if (typeof window === 'undefined') {
    return false
  }

  const features: Record<string, () => boolean> = {
    // ES6+ 特性
    Promise: () => typeof Promise !== 'undefined',
    fetch: () => typeof fetch !== 'undefined',
    ObjectAssign: () => typeof Object.assign !== 'undefined',
    ArrayFrom: () => typeof Array.from !== 'undefined',
    ArrayIncludes: () => Array.prototype.includes !== undefined,
    StringIncludes: () => String.prototype.includes !== undefined,
    StringStartsWith: () => String.prototype.startsWith !== undefined,
    StringEndsWith: () => String.prototype.endsWith !== undefined,
    // Web API
    IntersectionObserver: () => typeof IntersectionObserver !== 'undefined',
    ResizeObserver: () => typeof ResizeObserver !== 'undefined',
    MutationObserver: () => typeof MutationObserver !== 'undefined',
    // 存储 API
    localStorage: () => {
      try {
        const test = '__localStorage_test__'
        localStorage.setItem(test, test)
        localStorage.removeItem(test)
        return true
      } catch {
        return false
      }
    },
    sessionStorage: () => {
      try {
        const test = '__sessionStorage_test__'
        sessionStorage.setItem(test, test)
        sessionStorage.removeItem(test)
        return true
      } catch {
        return false
      }
    },
    // 其他
    requestAnimationFrame: () => typeof requestAnimationFrame !== 'undefined',
    cancelAnimationFrame: () => typeof cancelAnimationFrame !== 'undefined',
  }

  const checkFn = features[feature]
  return checkFn ? checkFn() : false
}

/**
 * 检测浏览器信息
 */
export interface BrowserInfo {
  name: string
  version: string
  isMobile: boolean
  isIOS: boolean
  isAndroid: boolean
  isChrome: boolean
  isFirefox: boolean
  isSafari: boolean
  isEdge: boolean
}

export function getBrowserInfo(): BrowserInfo {
  if (typeof window === 'undefined' || typeof navigator === 'undefined') {
    return {
      name: 'unknown',
      version: '0',
      isMobile: false,
      isIOS: false,
      isAndroid: false,
      isChrome: false,
      isFirefox: false,
      isSafari: false,
      isEdge: false,
    }
  }

  const ua = navigator.userAgent.toLowerCase()
  const isMobile = /mobile|android|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(ua)
  const isIOS = /iphone|ipad|ipod/i.test(ua)
  const isAndroid = /android/i.test(ua)

  let name = 'unknown'
  let version = '0'

  // Chrome
  if (/chrome/i.test(ua) && !/edge|edg/i.test(ua)) {
    name = 'chrome'
    const match = ua.match(/chrome\/(\d+)/)
    version = match ? match[1] : '0'
  }
  // Edge
  else if (/edge|edg/i.test(ua)) {
    name = 'edge'
    const match = ua.match(/edge?\/(\d+)/)
    version = match ? match[1] : '0'
  }
  // Firefox
  else if (/firefox/i.test(ua)) {
    name = 'firefox'
    const match = ua.match(/firefox\/(\d+)/)
    version = match ? match[1] : '0'
  }
  // Safari
  else if (/safari/i.test(ua) && !/chrome/i.test(ua)) {
    name = 'safari'
    const match = ua.match(/version\/(\d+)/)
    version = match ? match[1] : '0'
  }

  return {
    name,
    version,
    isMobile,
    isIOS,
    isAndroid,
    isChrome: name === 'chrome',
    isFirefox: name === 'firefox',
    isSafari: name === 'safari',
    isEdge: name === 'edge',
  }
}

/**
 * 检测是否需要 Polyfill
 */
export function needsPolyfill(): string[] {
  const missingFeatures: string[] = []

  const requiredFeatures = [
    'Promise',
    'fetch',
    'ObjectAssign',
    'ArrayFrom',
    'ArrayIncludes',
    'StringIncludes',
    'localStorage',
    'sessionStorage',
  ]

  requiredFeatures.forEach((feature) => {
    if (!isFeatureSupported(feature)) {
      missingFeatures.push(feature)
    }
  })

  return missingFeatures
}

/**
 * 安全地动态导入 polyfill
 */
async function safeImportPolyfill(modulePath: string, name: string): Promise<void> {
  try {
    await import(/* @vite-ignore */ modulePath)
    console.log(`[Polyfill] ${name} loaded`)
  } catch (error) {
    console.warn(`[Polyfill] Failed to load ${name}:`, error)
  }
}

/**
 * 加载 Polyfill（如果需要）
 */
export async function loadPolyfills(): Promise<void> {
  if (typeof window === 'undefined') {
    return
  }

  const missing = needsPolyfill()
  if (missing.length === 0) {
    return
  }

  // 动态加载 polyfill
  const polyfills: Promise<void>[] = []

  if (missing.includes('Promise')) {
    polyfills.push(safeImportPolyfill('core-js/stable/promise', 'Promise'))
  }

  if (missing.includes('fetch')) {
    polyfills.push(safeImportPolyfill('whatwg-fetch', 'fetch'))
  }

  if (missing.includes('ObjectAssign')) {
    polyfills.push(safeImportPolyfill('core-js/stable/object/assign', 'Object.assign'))
  }

  if (missing.includes('ArrayFrom')) {
    polyfills.push(safeImportPolyfill('core-js/stable/array/from', 'Array.from'))
  }

  if (missing.includes('ArrayIncludes')) {
    polyfills.push(safeImportPolyfill('core-js/stable/array/includes', 'Array.includes'))
  }

  if (missing.includes('StringIncludes')) {
    polyfills.push(safeImportPolyfill('core-js/stable/string/includes', 'String.includes'))
  }

  await Promise.all(polyfills)
}

/**
 * 检测并提示不兼容的浏览器
 */
export function checkBrowserCompatibility(): boolean {
  const browser = getBrowserInfo()
  const minVersions: Record<string, number> = {
    chrome: 80,
    firefox: 75,
    safari: 13,
    edge: 80,
  }

  const minVersion = minVersions[browser.name]
  if (minVersion && parseInt(browser.version) < minVersion) {
    console.warn(
      `[兼容性警告] ${browser.name} 版本 ${browser.version} 可能不完全支持，建议升级到 ${minVersion}+`
    )
    return false
  }

  return true
}

