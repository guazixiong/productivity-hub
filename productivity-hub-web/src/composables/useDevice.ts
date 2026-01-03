/**
 * 设备检测Composable
 * 
 * 提供设备类型检测、屏幕方向检测、窗口resize事件监听等功能
 */

import { ref, computed, onMounted, onBeforeUnmount, type Ref } from 'vue'
import {
  DeviceType,
  Orientation,
  getDeviceType,
  getScreenSize,
  getOrientation,
  debounce,
} from '@/utils/responsive'

/**
 * 设备检测Composable返回值类型
 */
export interface UseDeviceReturn {
  /** 设备类型（响应式） */
  deviceType: Ref<DeviceType>
  /** 屏幕方向（响应式） */
  orientation: Ref<Orientation>
  /** 屏幕宽度（响应式） */
  width: Ref<number>
  /** 屏幕高度（响应式） */
  height: Ref<number>
  /** 是否为移动端（计算属性） */
  isMobile: Ref<boolean>
  /** 是否为平板端（计算属性） */
  isTablet: Ref<boolean>
  /** 是否为PC端（计算属性） */
  isDesktop: Ref<boolean>
  /** 是否为竖屏（计算属性） */
  isPortrait: Ref<boolean>
  /** 是否为横屏（计算属性） */
  isLandscape: Ref<boolean>
  /** 更新设备信息 */
  updateDeviceInfo: () => void
}

/**
 * 设备检测Composable
 * 
 * @param debounceDelay resize事件防抖延迟时间（毫秒），默认300ms
 * @returns 设备检测相关的响应式状态和方法
 */
export function useDevice(debounceDelay: number = 300): UseDeviceReturn {
  // 设备类型状态
  const deviceType = ref<DeviceType>(DeviceType.DESKTOP)
  
  // 屏幕方向状态
  const orientation = ref<Orientation>(Orientation.LANDSCAPE)
  
  // 屏幕尺寸状态
  const width = ref<number>(0)
  const height = ref<number>(0)

  /**
   * 更新设备信息
   */
  const updateDeviceInfo = () => {
    const size = getScreenSize()
    width.value = size.width
    height.value = size.height
    deviceType.value = getDeviceType(size.width)
    orientation.value = getOrientation()
  }

  // 防抖的resize处理函数
  const handleResize = debounce(() => {
    updateDeviceInfo()
  }, debounceDelay)

  // orientationchange事件处理函数
  const handleOrientationChange = () => {
    // orientationchange事件后，需要延迟一下再获取尺寸，因为浏览器需要时间调整
    setTimeout(() => {
      updateDeviceInfo()
    }, 100)
  }

  // 计算属性：是否为移动端
  const isMobile = computed(() => deviceType.value === DeviceType.MOBILE)

  // 计算属性：是否为平板端
  const isTablet = computed(() => deviceType.value === DeviceType.TABLET)

  // 计算属性：是否为PC端
  const isDesktop = computed(() => deviceType.value === DeviceType.DESKTOP)

  // 计算属性：是否为竖屏
  const isPortrait = computed(() => orientation.value === Orientation.PORTRAIT)

  // 计算属性：是否为横屏
  const isLandscape = computed(() => orientation.value === Orientation.LANDSCAPE)

  // 组件挂载时初始化设备信息并添加事件监听
  onMounted(() => {
    updateDeviceInfo()
    window.addEventListener('resize', handleResize)
    window.addEventListener('orientationchange', handleOrientationChange)
  })

  // 组件卸载前移除事件监听
  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
    window.removeEventListener('orientationchange', handleOrientationChange)
  })

  return {
    deviceType,
    orientation,
    width,
    height,
    isMobile,
    isTablet,
    isDesktop,
    isPortrait,
    isLandscape,
    updateDeviceInfo,
  }
}

