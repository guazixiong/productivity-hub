/**
 * 设备检测Composable单元测试
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent } from 'vue'
import { useDevice, DeviceType, Orientation } from '../useDevice'

describe('useDevice', () => {
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

  it('应该正确初始化设备信息', () => {
    const TestComponent = defineComponent({
      setup() {
        const device = useDevice()
        return { device }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const device = (wrapper.vm as any).device

    expect(device.width.value).toBe(1920)
    expect(device.height.value).toBe(1080)
    expect(device.deviceType.value).toBe(DeviceType.DESKTOP)
    expect(device.orientation.value).toBe(Orientation.LANDSCAPE)
    expect(device.isDesktop.value).toBe(true)
    expect(device.isMobile.value).toBe(false)
    expect(device.isTablet.value).toBe(false)
    expect(device.isLandscape.value).toBe(true)
    expect(device.isPortrait.value).toBe(false)
  })

  it('应该正确检测移动端', () => {
    Object.defineProperty(window, 'innerWidth', { value: 375, writable: true, configurable: true })
    Object.defineProperty(window, 'innerHeight', { value: 812, writable: true, configurable: true })

    const TestComponent = defineComponent({
      setup() {
        const device = useDevice()
        return { device }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const device = (wrapper.vm as any).device

    expect(device.deviceType.value).toBe(DeviceType.MOBILE)
    expect(device.isMobile.value).toBe(true)
    expect(device.isDesktop.value).toBe(false)
    expect(device.isTablet.value).toBe(false)
  })

  it('应该正确检测平板端', () => {
    Object.defineProperty(window, 'innerWidth', { value: 768, writable: true, configurable: true })
    Object.defineProperty(window, 'innerHeight', { value: 1024, writable: true, configurable: true })

    const TestComponent = defineComponent({
      setup() {
        const device = useDevice()
        return { device }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const device = (wrapper.vm as any).device

    expect(device.deviceType.value).toBe(DeviceType.TABLET)
    expect(device.isTablet.value).toBe(true)
    expect(device.isMobile.value).toBe(false)
    expect(device.isDesktop.value).toBe(false)
  })

  it('应该正确检测竖屏', () => {
    Object.defineProperty(window, 'innerWidth', { value: 375, writable: true, configurable: true })
    Object.defineProperty(window, 'innerHeight', { value: 812, writable: true, configurable: true })

    const TestComponent = defineComponent({
      setup() {
        const device = useDevice()
        return { device }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const device = (wrapper.vm as any).device

    expect(device.orientation.value).toBe(Orientation.PORTRAIT)
    expect(device.isPortrait.value).toBe(true)
    expect(device.isLandscape.value).toBe(false)
  })

  it('应该监听resize事件', async () => {
    vi.useFakeTimers()

    const TestComponent = defineComponent({
      setup() {
        const device = useDevice(300)
        return { device }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const device = (wrapper.vm as any).device

    // 初始宽度
    expect(device.width.value).toBe(1920)

    // 模拟resize事件
    Object.defineProperty(window, 'innerWidth', { value: 800, writable: true, configurable: true })
    window.dispatchEvent(new Event('resize'))

    // 防抖延迟，应该还没有更新
    expect(device.width.value).toBe(1920)

    // 等待防抖延迟
    vi.advanceTimersByTime(300)
    await wrapper.vm.$nextTick()

    // 应该已经更新
    expect(device.width.value).toBe(800)

    vi.useRealTimers()
  })

  it('应该监听orientationchange事件', async () => {
    const TestComponent = defineComponent({
      setup() {
        const device = useDevice()
        return { device }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const device = (wrapper.vm as any).device

    // 模拟orientationchange事件
    Object.defineProperty(window, 'innerWidth', { value: 812, writable: true, configurable: true })
    Object.defineProperty(window, 'innerHeight', { value: 375, writable: true, configurable: true })

    window.dispatchEvent(new Event('orientationchange'))

    // 等待延迟
    await new Promise(resolve => setTimeout(resolve, 150))
    await wrapper.vm.$nextTick()

    expect(device.orientation.value).toBe(Orientation.LANDSCAPE)
  })

  it('应该正确更新设备信息', () => {
    const TestComponent = defineComponent({
      setup() {
        const device = useDevice()
        return { device }
      },
      template: '<div></div>',
    })

    const wrapper = mount(TestComponent)
    const device = (wrapper.vm as any).device

    // 修改窗口尺寸
    Object.defineProperty(window, 'innerWidth', { value: 500, writable: true, configurable: true })
    Object.defineProperty(window, 'innerHeight', { value: 800, writable: true, configurable: true })

    // 手动更新
    device.updateDeviceInfo()

    expect(device.width.value).toBe(500)
    expect(device.height.value).toBe(800)
    expect(device.deviceType.value).toBe(DeviceType.MOBILE)
    expect(device.orientation.value).toBe(Orientation.PORTRAIT)
  })
})

