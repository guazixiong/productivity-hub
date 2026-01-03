/**
 * 响应式状态管理Store单元测试
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useResponsiveStore, SidebarState, MenuState } from '../responsive'
import { DeviceType, Orientation } from '@/utils/responsive'

describe('useResponsiveStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    // 清空localStorage
    localStorage.clear()
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
    localStorage.clear()
    vi.restoreAllMocks()
  })

  it('应该正确初始化状态', () => {
    const store = useResponsiveStore()

    expect(store.deviceType).toBe(DeviceType.DESKTOP)
    expect(store.orientation).toBe(Orientation.LANDSCAPE)
    expect(store.isDesktop).toBe(true)
    expect(store.isMobile).toBe(false)
    expect(store.isTablet).toBe(false)
  })

  it('应该正确设置侧边栏状态', () => {
    const store = useResponsiveStore()

    store.setSidebarState(SidebarState.COLLAPSED)
    expect(store.sidebarState).toBe(SidebarState.COLLAPSED)
    expect(store.isSidebarCollapsed).toBe(true)

    store.setSidebarState(SidebarState.EXPANDED)
    expect(store.sidebarState).toBe(SidebarState.EXPANDED)
    expect(store.isSidebarExpanded).toBe(true)
  })

  it('应该正确切换侧边栏状态（PC端）', () => {
    Object.defineProperty(window, 'innerWidth', { value: 1920, writable: true, configurable: true })
    const store = useResponsiveStore()

    // PC端：在折叠和展开之间切换
    store.setSidebarState(SidebarState.COLLAPSED)
    store.toggleSidebar()
    expect(store.sidebarState).toBe(SidebarState.EXPANDED)

    store.toggleSidebar()
    expect(store.sidebarState).toBe(SidebarState.COLLAPSED)
  })

  it('应该正确切换侧边栏状态（移动端）', () => {
    Object.defineProperty(window, 'innerWidth', { value: 375, writable: true, configurable: true })
    const store = useResponsiveStore()

    // 移动端：在隐藏和展开之间切换
    store.setSidebarState(SidebarState.HIDDEN)
    store.toggleSidebar()
    expect(store.sidebarState).toBe(SidebarState.EXPANDED)

    store.toggleSidebar()
    expect(store.sidebarState).toBe(SidebarState.HIDDEN)
  })

  it('应该正确设置菜单状态', () => {
    const store = useResponsiveStore()

    store.setMenuState(MenuState.VISIBLE)
    expect(store.menuState).toBe(MenuState.VISIBLE)
    expect(store.isMenuVisible).toBe(true)

    store.setMenuState(MenuState.HIDDEN)
    expect(store.menuState).toBe(MenuState.HIDDEN)
    expect(store.isMenuVisible).toBe(false)
  })

  it('应该正确切换菜单状态', () => {
    const store = useResponsiveStore()

    store.setMenuState(MenuState.HIDDEN)
    store.toggleMenu()
    expect(store.menuState).toBe(MenuState.VISIBLE)

    store.toggleMenu()
    expect(store.menuState).toBe(MenuState.HIDDEN)
  })

  it('应该正确关闭侧边栏（移动端）', () => {
    Object.defineProperty(window, 'innerWidth', { value: 375, writable: true, configurable: true })
    const store = useResponsiveStore()

    store.setSidebarState(SidebarState.EXPANDED)
    store.closeSidebar()
    expect(store.sidebarState).toBe(SidebarState.HIDDEN)
  })

  it('应该正确打开侧边栏（移动端）', () => {
    Object.defineProperty(window, 'innerWidth', { value: 375, writable: true, configurable: true })
    const store = useResponsiveStore()

    store.setSidebarState(SidebarState.HIDDEN)
    store.openSidebar()
    expect(store.sidebarState).toBe(SidebarState.EXPANDED)
  })

  it('应该持久化状态到localStorage', () => {
    const store = useResponsiveStore()

    store.setSidebarState(SidebarState.COLLAPSED)
    store.setMenuState(MenuState.VISIBLE)

    expect(localStorage.getItem('phub/responsive/sidebarState')).toBe(SidebarState.COLLAPSED)
    expect(localStorage.getItem('phub/responsive/menuState')).toBe(MenuState.VISIBLE)
  })

  it('应该从localStorage恢复状态', () => {
    localStorage.setItem('phub/responsive/sidebarState', SidebarState.COLLAPSED)
    localStorage.setItem('phub/responsive/menuState', MenuState.VISIBLE)

    const store = useResponsiveStore()

    expect(store.sidebarState).toBe(SidebarState.COLLAPSED)
    expect(store.menuState).toBe(MenuState.VISIBLE)
  })
})

