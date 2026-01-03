/**
 * 布局适配Composable
 * 
 * 使用策略模式实现不同设备类型的布局适配
 */

import { computed, type Ref } from 'vue'
import { DeviceType } from '@/utils/responsive'
import { useResponsiveStore, SidebarState, MenuState } from '@/stores/responsive'

/**
 * 布局策略接口
 */
export interface LayoutStrategy {
  /** 应用侧边栏布局 */
  applySidebar(): SidebarState
  /** 应用菜单布局 */
  applyMenu(): MenuState
  /** 应用内容布局 */
  applyContent(): { width: string; marginLeft: string }
}

/**
 * 移动端布局策略
 */
class MobileLayoutStrategy implements LayoutStrategy {
  applySidebar(): SidebarState {
    return SidebarState.HIDDEN
  }

  applyMenu(): MenuState {
    return MenuState.VISIBLE
  }

  applyContent(): { width: string; marginLeft: string } {
    return {
      width: '100%',
      marginLeft: '0',
    }
  }
}

/**
 * 平板端布局策略
 */
class TabletLayoutStrategy implements LayoutStrategy {
  applySidebar(): SidebarState {
    return SidebarState.COLLAPSED
  }

  applyMenu(): MenuState {
    return MenuState.HIDDEN
  }

  applyContent(): { width: string; marginLeft: string } {
    return {
      width: 'calc(100% - 64px)',
      marginLeft: '64px',
    }
  }
}

/**
 * PC端布局策略
 */
class DesktopLayoutStrategy implements LayoutStrategy {
  private sidebarState: SidebarState

  constructor(sidebarState: SidebarState) {
    this.sidebarState = sidebarState
  }

  applySidebar(): SidebarState {
    return this.sidebarState
  }

  applyMenu(): MenuState {
    return MenuState.HIDDEN
  }

  applyContent(): { width: string; marginLeft: string } {
    if (this.sidebarState === SidebarState.COLLAPSED) {
      return {
        width: 'calc(100% - 64px)',
        marginLeft: '64px',
      }
    }
    return {
      width: 'calc(100% - 228px)',
      marginLeft: '228px',
    }
  }
}

/**
 * 布局上下文
 */
class LayoutContext {
  private strategy: LayoutStrategy

  constructor(strategy: LayoutStrategy) {
    this.strategy = strategy
  }

  setStrategy(strategy: LayoutStrategy) {
    this.strategy = strategy
  }

  getSidebarState(): SidebarState {
    return this.strategy.applySidebar()
  }

  getMenuState(): MenuState {
    return this.strategy.applyMenu()
  }

  getContentLayout(): { width: string; marginLeft: string } {
    return this.strategy.applyContent()
  }
}

/**
 * 布局适配Composable返回值类型
 */
export interface UseLayoutReturn {
  /** 侧边栏状态（计算属性） */
  sidebarState: Ref<SidebarState>
  /** 菜单状态（计算属性） */
  menuState: Ref<MenuState>
  /** 内容布局（计算属性） */
  contentLayout: Ref<{ width: string; marginLeft: string }>
  /** 侧边栏宽度（计算属性） */
  sidebarWidth: Ref<string>
  /** 是否显示菜单按钮（计算属性） */
  showMenuButton: Ref<boolean>
}

/**
 * 布局适配Composable
 * 
 * @returns 布局适配相关的响应式状态
 */
export function useLayout(): UseLayoutReturn {
  const responsiveStore = useResponsiveStore()

  // 创建布局上下文
  const layoutContext = computed(() => {
    let strategy: LayoutStrategy

    switch (responsiveStore.deviceType) {
      case DeviceType.MOBILE:
        strategy = new MobileLayoutStrategy()
        break
      case DeviceType.TABLET:
        strategy = new TabletLayoutStrategy()
        break
      case DeviceType.DESKTOP:
        strategy = new DesktopLayoutStrategy(responsiveStore.sidebarState)
        break
      default:
        strategy = new DesktopLayoutStrategy(SidebarState.EXPANDED)
    }

    return new LayoutContext(strategy)
  })

  // 侧边栏状态（根据设备类型和用户操作决定）
  const sidebarState = computed(() => {
    const context = layoutContext.value
    const suggestedState = context.getSidebarState()

    // 移动端：使用策略建议的状态，但允许用户手动控制
    if (responsiveStore.isMobile) {
      return responsiveStore.sidebarState
    }

    // 平板端和PC端：使用策略建议的状态，但允许用户手动控制
    return responsiveStore.sidebarState
  })

  // 菜单状态
  const menuState = computed(() => {
    const context = layoutContext.value
    const suggestedState = context.getMenuState()

    // 移动端：使用策略建议的状态
    if (responsiveStore.isMobile) {
      return suggestedState
    }

    // 其他端：菜单始终隐藏
    return MenuState.HIDDEN
  })

  // 内容布局
  const contentLayout = computed(() => {
    const context = layoutContext.value
    return context.getContentLayout()
  })

  // 侧边栏宽度
  const sidebarWidth = computed(() => {
    if (sidebarState.value === SidebarState.HIDDEN) {
      return '0'
    }
    if (sidebarState.value === SidebarState.COLLAPSED) {
      return '64px'
    }
    return '228px'
  })

  // 是否显示菜单按钮
  const showMenuButton = computed(() => {
    return menuState.value === MenuState.VISIBLE
  })

  return {
    sidebarState,
    menuState,
    contentLayout,
    sidebarWidth,
    showMenuButton,
  }
}

