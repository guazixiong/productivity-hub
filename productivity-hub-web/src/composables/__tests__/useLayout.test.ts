/**
 * 布局适配Composable单元测试
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { mount } from '@vue/test-utils'
import { defineComponent } from 'vue'
import { useLayout } from '../useLayout'
import { useResponsiveStore, SidebarState, MenuState } from '@/stores/responsive'
import { DeviceType } from '@/utils/responsive'

describe('useLayout', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
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

  it('应该正确应用移动端布局策略', () => {
    Object.defineProperty(window, 'innerWidth', { value: 375, writable: true, configurable: true })

    const TestComponent = defineComponent({
      setup() {
        const layout = useLayout()
        return { layout }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const layout = (wrapper.vm as any).layout

    expect(layout.showMenuButton.value).toBe(true)
    expect(layout.sidebarWidth.value).toBe('0')
    expect(layout.contentLayout.value.width).toBe('100%')
    expect(layout.contentLayout.value.marginLeft).toBe('0')
  })

  it('应该正确应用平板端布局策略', () => {
    Object.defineProperty(window, 'innerWidth', { value: 768, writable: true, configurable: true })

    const TestComponent = defineComponent({
      setup() {
        const layout = useLayout()
        return { layout }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const layout = (wrapper.vm as any).layout

    expect(layout.showMenuButton.value).toBe(false)
    expect(layout.sidebarWidth.value).toBe('64px')
    expect(layout.contentLayout.value.width).toBe('calc(100% - 64px)')
    expect(layout.contentLayout.value.marginLeft).toBe('64px')
  })

  it('应该正确应用PC端布局策略（展开）', () => {
    Object.defineProperty(window, 'innerWidth', { value: 1920, writable: true, configurable: true })

    const store = useResponsiveStore()
    store.setSidebarState(SidebarState.EXPANDED)

    const TestComponent = defineComponent({
      setup() {
        const layout = useLayout()
        return { layout }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const layout = (wrapper.vm as any).layout

    expect(layout.showMenuButton.value).toBe(false)
    expect(layout.sidebarWidth.value).toBe('228px')
    expect(layout.contentLayout.value.width).toBe('calc(100% - 228px)')
    expect(layout.contentLayout.value.marginLeft).toBe('228px')
  })

  it('应该正确应用PC端布局策略（折叠）', () => {
    Object.defineProperty(window, 'innerWidth', { value: 1920, writable: true, configurable: true })

    const store = useResponsiveStore()
    store.setSidebarState(SidebarState.COLLAPSED)

    const TestComponent = defineComponent({
      setup() {
        const layout = useLayout()
        return { layout }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const layout = (wrapper.vm as any).layout

    expect(layout.showMenuButton.value).toBe(false)
    expect(layout.sidebarWidth.value).toBe('64px')
    expect(layout.contentLayout.value.width).toBe('calc(100% - 64px)')
    expect(layout.contentLayout.value.marginLeft).toBe('64px')
  })

  it('应该响应设备类型变化', async () => {
    Object.defineProperty(window, 'innerWidth', { value: 1920, writable: true, configurable: true })

    const TestComponent = defineComponent({
      setup() {
        const layout = useLayout()
        return { layout }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const layout = (wrapper.vm as any).layout

    // 初始为PC端
    expect(layout.showMenuButton.value).toBe(false)

    // 切换到移动端
    Object.defineProperty(window, 'innerWidth', { value: 375, writable: true, configurable: true })
    window.dispatchEvent(new Event('resize'))
    
    // 等待防抖延迟
    await new Promise(resolve => setTimeout(resolve, 400))
    await wrapper.vm.$nextTick()

    expect(layout.showMenuButton.value).toBe(true)
  })
})

