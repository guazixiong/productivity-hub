/**
 * 响应式状态管理Store
 * 
 * 管理响应式布局相关的状态，包括设备类型、屏幕方向、侧边栏状态、菜单状态等
 */

import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { DeviceType, Orientation, getDeviceType, getOrientation } from '@/utils/responsive'
import { useDevice } from '@/composables/useDevice'

/**
 * 侧边栏状态枚举
 */
export enum SidebarState {
  /** 展开 */
  EXPANDED = 'expanded',
  /** 折叠 */
  COLLAPSED = 'collapsed',
  /** 隐藏 */
  HIDDEN = 'hidden',
}

/**
 * 菜单状态枚举
 */
export enum MenuState {
  /** 可见 */
  VISIBLE = 'visible',
  /** 隐藏 */
  HIDDEN = 'hidden',
}

/**
 * 响应式Store状态接口
 */
interface ResponsiveState {
  /** 设备类型 */
  deviceType: DeviceType
  /** 屏幕方向 */
  orientation: Orientation
  /** 侧边栏状态 */
  sidebarState: SidebarState
  /** 菜单状态 */
  menuState: MenuState
}

/**
 * localStorage键名常量
 */
const STORAGE_KEYS = {
  SIDEBAR_STATE: 'phub/responsive/sidebarState',
  MENU_STATE: 'phub/responsive/menuState',
} as const

/**
 * 响应式状态管理Store
 */
export const useResponsiveStore = defineStore('responsive', () => {
  // 使用useDevice composable获取设备信息
  const device = useDevice()

  // 设备类型状态（从useDevice同步）
  const deviceType = ref<DeviceType>(device.deviceType.value)
  
  // 屏幕方向状态（从useDevice同步）
  const orientation = ref<Orientation>(device.orientation.value)

  // 侧边栏状态（支持持久化）
  const sidebarState = ref<SidebarState>(() => {
    // 从localStorage恢复状态
    const saved = localStorage.getItem(STORAGE_KEYS.SIDEBAR_STATE)
    if (saved && Object.values(SidebarState).includes(saved as SidebarState)) {
      return saved as SidebarState
    }
    // 默认状态：移动端隐藏，平板端折叠，PC端展开
    if (device.isMobile.value) {
      return SidebarState.HIDDEN
    }
    if (device.isTablet.value) {
      return SidebarState.COLLAPSED
    }
    return SidebarState.EXPANDED
  })

  // 菜单状态（支持持久化）
  const menuState = ref<MenuState>(() => {
    // 从localStorage恢复状态
    const saved = localStorage.getItem(STORAGE_KEYS.MENU_STATE)
    if (saved && Object.values(MenuState).includes(saved as MenuState)) {
      return saved as MenuState
    }
    // 默认状态：移动端显示，其他端隐藏
    return device.isMobile.value ? MenuState.VISIBLE : MenuState.HIDDEN
  })

  // 同步设备信息
  watch(
    () => device.deviceType.value,
    (newType) => {
      deviceType.value = newType
      // 设备类型变化时，自动调整侧边栏和菜单状态
      updateStateByDeviceType(newType)
    },
    { immediate: true }
  )

  watch(
    () => device.orientation.value,
    (newOrientation) => {
      orientation.value = newOrientation
    },
    { immediate: true }
  )

  /**
   * 根据设备类型更新状态
   */
  const updateStateByDeviceType = (type: DeviceType) => {
    switch (type) {
      case DeviceType.MOBILE:
        // 移动端：侧边栏隐藏，菜单显示
        sidebarState.value = SidebarState.HIDDEN
        menuState.value = MenuState.VISIBLE
        break
      case DeviceType.TABLET:
        // 平板端：侧边栏折叠，菜单隐藏
        sidebarState.value = SidebarState.COLLAPSED
        menuState.value = MenuState.HIDDEN
        break
      case DeviceType.DESKTOP:
        // PC端：侧边栏展开，菜单隐藏
        sidebarState.value = SidebarState.EXPANDED
        menuState.value = MenuState.HIDDEN
        break
    }
    // 持久化状态
    persistState()
  }

  /**
   * 持久化状态到localStorage
   */
  const persistState = () => {
    try {
      localStorage.setItem(STORAGE_KEYS.SIDEBAR_STATE, sidebarState.value)
      localStorage.setItem(STORAGE_KEYS.MENU_STATE, menuState.value)
    } catch (error) {
      // 忽略持久化错误
    }
  }

  /**
   * 设置侧边栏状态
   */
  const setSidebarState = (state: SidebarState) => {
    sidebarState.value = state
    persistState()
  }

  /**
   * 切换侧边栏状态
   */
  const toggleSidebar = () => {
    if (deviceType.value === DeviceType.MOBILE) {
      // 移动端：在隐藏和展开之间切换
      sidebarState.value =
        sidebarState.value === SidebarState.HIDDEN
          ? SidebarState.EXPANDED
          : SidebarState.HIDDEN
    } else {
      // 平板端和PC端：在折叠和展开之间切换
      sidebarState.value =
        sidebarState.value === SidebarState.COLLAPSED
          ? SidebarState.EXPANDED
          : SidebarState.COLLAPSED
    }
    persistState()
  }

  /**
   * 设置菜单状态
   */
  const setMenuState = (state: MenuState) => {
    menuState.value = state
    persistState()
  }

  /**
   * 切换菜单状态
   */
  const toggleMenu = () => {
    menuState.value =
      menuState.value === MenuState.VISIBLE ? MenuState.HIDDEN : MenuState.VISIBLE
    persistState()
  }

  /**
   * 关闭侧边栏（移动端专用）
   */
  const closeSidebar = () => {
    if (deviceType.value === DeviceType.MOBILE) {
      sidebarState.value = SidebarState.HIDDEN
      persistState()
    }
  }

  /**
   * 打开侧边栏（移动端专用）
   */
  const openSidebar = () => {
    if (deviceType.value === DeviceType.MOBILE) {
      sidebarState.value = SidebarState.EXPANDED
      persistState()
    }
  }

  // 计算属性：侧边栏是否展开
  const isSidebarExpanded = computed(() => sidebarState.value === SidebarState.EXPANDED)

  // 计算属性：侧边栏是否折叠
  const isSidebarCollapsed = computed(() => sidebarState.value === SidebarState.COLLAPSED)

  // 计算属性：侧边栏是否隐藏
  const isSidebarHidden = computed(() => sidebarState.value === SidebarState.HIDDEN)

  // 计算属性：菜单是否可见
  const isMenuVisible = computed(() => menuState.value === MenuState.VISIBLE)

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

  return {
    // 状态
    deviceType,
    orientation,
    sidebarState,
    menuState,
    // 计算属性
    isSidebarExpanded,
    isSidebarCollapsed,
    isSidebarHidden,
    isMenuVisible,
    isMobile,
    isTablet,
    isDesktop,
    isPortrait,
    isLandscape,
    // 方法
    setSidebarState,
    toggleSidebar,
    setMenuState,
    toggleMenu,
    closeSidebar,
    openSidebar,
    persistState,
  }
})

