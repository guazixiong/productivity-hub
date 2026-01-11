<script setup lang="ts">
/**
 * DashboardLayout组件
 */
import { computed, watch, onMounted, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNavigationStore } from '@/stores/navigation'
import { useTabsStore } from '@/stores/tabs'
import { useNotificationStore } from '@/stores/notifications'
import { useResponsiveStore, SidebarState } from '@/stores/responsive'
import { useMenuStore } from '@/stores/menu'
import { useLayout } from '@/composables/useLayout'
import TabsView from '@/components/TabsView.vue'
import ChatWidget from '@/components/ChatWidget.vue'
import AnnouncementDialog from '@/components/AnnouncementDialog.vue'
import NotificationDetailDialog from '@/components/NotificationDetailDialog.vue'
import { Setting, Message, Cpu, Lock, SwitchButton, ArrowDownBold, HomeFilled, Tools, ArrowLeft, Document, TrendCharts, Collection, Bell, Search, Fold, Expand, User, DataAnalysis, Loading, SuccessFilled, WarningFilled, Menu, Picture, Money, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import logoIcon from '@/assets/logo.svg'
import { announcementApi } from '@/services/announcementApi'
import type { Announcement } from '@/types/announcement'
import type { NotificationItem } from '@/stores/notifications'
import DynamicMenu from '@/components/DynamicMenu.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const navigationStore = useNavigationStore()
const tabsStore = useTabsStore()
const notificationStore = useNotificationStore()
const responsiveStore = useResponsiveStore()
const menuStore = useMenuStore()
const layout = useLayout()

const notificationVisible = ref(false)
const notificationDetailVisible = ref(false)
const currentNotification = ref<NotificationItem | null>(null)
const isCollapsed = ref(false)
const announcementDialogVisible = ref(false)
const unreadAnnouncements = ref<Announcement[]>([])
const fetchingAnnouncements = ref(false)

// 移动端遮罩层显示状态
const showOverlay = ref(false)

const activeMenu = computed(() => {
  if (route.path.startsWith('/home')) return '/home'
  if (route.path.startsWith('/ai-daily')) return '/ai-daily'
  if (route.path.startsWith('/todo')) return '/todo'
  if (route.path.startsWith('/hot-sections')) return '/hot-sections'
  if (route.path.startsWith('/quick-record')) return '/quick-record'
  if (route.path.startsWith('/health')) return route.path
  if (route.path.startsWith('/asset') || route.path.startsWith('/assets')) return route.path
  if (route.path.startsWith('/wishlist')) return route.path
  if (route.path.startsWith('/data/management')) return '/data/management'
  if (route.path.startsWith('/settings/currency')) return '/settings/currency'
  if (route.path.startsWith('/messages')) return '/messages'
  if (route.path.startsWith('/bookmark')) return '/bookmark'
  if (route.path.startsWith('/code-generator')) return '/code-generator'
  if (route.path.startsWith('/ai/')) return route.path

  // 工具箱：根据分类高亮“生活工具”或“技术工具”二级菜单
  if (route.path === '/tools') {
    const category = route.query.category as string | undefined
    if (category === 'life') return '/tools?category=life'
    if (category === 'tech') return '/tools?category=tech'
    return '/tools'
  }
  if (route.path.startsWith('/tools')) return '/tools'

  if (route.path.startsWith('/settings/schedules')) return '/settings/schedules'
  if (route.path.startsWith('/config')) return '/config'
  if (route.path.startsWith('/image')) return '/image'
  if (route.path.startsWith('/settings/announcements')) return '/settings/announcements'
  if (route.path.startsWith('/settings/users')) return '/settings/users'
  if (route.path.startsWith('/settings/menus')) return '/settings/menus'
  if (route.path.startsWith('/settings/roles')) return '/settings/roles'
  if (route.path.startsWith('/settings/user-roles')) return '/settings/user-roles'
  return route.path
})

// 是否为 Todo 任务大屏页面
const isTodoDashboardPage = computed(() => route.name === 'TodoDashboardPage')

const pageTitle = computed(() => {
  if (isTodoDashboardPage.value) {
    // 大屏内不展示“TODO任务大屏”等标题文案
    return ''
  }
  return route.meta.title ?? '控制台'
})

const defaultOpenMenus = computed(() => {
  const openeds: string[] = []

// 生活中心菜单：包含快捷记录、健康管理、资产管理、消息推送
  if (
    route.path.startsWith('/quick-record') ||
    route.path.startsWith('/health') ||
    route.path.startsWith('/asset') ||
    route.path.startsWith('/assets') ||
    route.path.startsWith('/wishlist') ||
    route.path.startsWith('/data/management') ||
    route.path.startsWith('/settings/currency') ||
    route.path.startsWith('/messages')
  ) {
    openeds.push('life-center')
    // 如果是健康管理相关路径，还需要打开健康管理子菜单
    if (route.path.startsWith('/health')) {
      openeds.push('health-management')
    }
    // 如果是资产管理相关路径，还需要打开资产管理子菜单
    if (
      route.path.startsWith('/asset') ||
      route.path.startsWith('/assets') ||
      route.path.startsWith('/wishlist') ||
      route.path.startsWith('/data/management') ||
      route.path.startsWith('/settings/currency')
    ) {
      openeds.push('asset-management')
    }
  }

  // 技术工作台菜单：包含书签收藏、低代码生成、AI助手
  if (
    route.path.startsWith('/bookmark') ||
    route.path.startsWith('/code-generator') ||
    route.path.startsWith('/ai/')
  ) {
    openeds.push('tech-workbench')
    // 如果是AI助手相关路径，还需要打开AI助手子菜单
    if (route.path.startsWith('/ai/')) {
      openeds.push('ai-assistant')
    }
  }

  // 工具箱菜单：包含生活工具和技术工具
  if (route.path.startsWith('/tools')) {
    openeds.push('tools')
  }

  // 全局配置菜单：包含定时任务、参数设置
  if (route.path.startsWith('/settings/schedules') || route.path.startsWith('/config')) {
    openeds.push('global-config')
  }

  // 系统管理菜单：包含图片管理、公告管理、用户管理、菜单管理、角色管理、用户角色管理（仅管理员）
  if (
    route.path.startsWith('/image') ||
    route.path.startsWith('/settings/announcements') ||
    route.path.startsWith('/settings/users') ||
    route.path.startsWith('/settings/menus') ||
    route.path.startsWith('/settings/roles') ||
    route.path.startsWith('/settings/user-roles')
  ) {
    openeds.push('system-management')
  }

  return openeds
})

const isAdminUser = computed(() => authStore.user?.roles?.includes('admin'))

// 切换导航栏折叠状态（PC端和平板端）
const toggleCollapse = () => {
  if (responsiveStore.isMobile) {
    // 移动端：切换侧边栏显示/隐藏
    responsiveStore.toggleSidebar()
    showOverlay.value = responsiveStore.isSidebarExpanded
  } else {
    // PC端和平板端：切换折叠/展开
  isCollapsed.value = !isCollapsed.value
    responsiveStore.setSidebarState(
      isCollapsed.value ? SidebarState.COLLAPSED : SidebarState.EXPANDED
    )
  }
}

// 移动端菜单按钮点击处理
const handleMenuButtonClick = () => {
  responsiveStore.toggleSidebar()
  showOverlay.value = responsiveStore.isSidebarExpanded
}

// 关闭侧边栏（移动端）
const closeSidebar = () => {
  responsiveStore.closeSidebar()
  showOverlay.value = false
}

// 点击遮罩层关闭侧边栏
const handleOverlayClick = () => {
  closeSidebar()
}

// 左上角 Logo 点击返回首页
const handleLogoClick = () => {
  router.push('/home')
}

// 处理菜单项点击（当 router 属性无法正常工作时使用）
const handleMenuSelect = (index: string) => {
  // 如果当前路径已经是目标路径，则不进行跳转
  if (route.path !== index) {
    router.push(index).catch((err) => {
      // 忽略重复导航错误
    })
  }
}

const handleLogout = () => {
  authStore.logout()
  tabsStore.clearTabs()
  notificationStore.reset()
  router.replace({ name: 'Login' })
}

// 刷新菜单
const handleRefreshMenus = async () => {
  try {
    await menuStore.refreshMenus()
    // 刷新成功后提示
    ElMessage.success('菜单已刷新')
  } catch (error) {
    ElMessage.error('刷新菜单失败，请稍后重试')
  }
}

const handleNotificationClick = (item: NotificationItem) => {
  currentNotification.value = item
  notificationDetailVisible.value = true
  // 不自动关闭消息列表弹窗，让用户手动关闭
}

const handleNotificationRead = (id: string) => {
  notificationStore.markRead(id)
}

// 计算属性：排序后的未读消息（按时间倒序）
const unreadNotifications = computed(() => {
  return notificationStore.notifications
    .filter(n => !n.read)
    .sort((a, b) => b.receivedAt - a.receivedAt)
})

// 计算属性：排序后的已读消息（按时间倒序）
const readNotifications = computed(() => {
  return notificationStore.notifications
    .filter(n => n.read)
    .sort((a, b) => b.receivedAt - a.receivedAt)
})

const fetchUnreadAnnouncements = async () => {
  if (!authStore.isAuthenticated || fetchingAnnouncements.value) return
  fetchingAnnouncements.value = true
  try {
    const result = await announcementApi.getUnread()
    unreadAnnouncements.value = result ?? []
    announcementDialogVisible.value = unreadAnnouncements.value.length > 0
  } catch (error) {
    // 忽略加载公告错误
  } finally {
    fetchingAnnouncements.value = false
  }
}

const handleAnnouncementRead = (id: string) => {
  unreadAnnouncements.value = unreadAnnouncements.value.filter((item) => item.id !== id)
  if (!unreadAnnouncements.value.length) {
    announcementDialogVisible.value = false
  }
}

// 计算属性：获取排序后的菜单（确保按照 orderNum 排序）
const sortedMenus = computed(() => {
  const menus = menuStore.menus || []
  // 递归排序菜单及其子菜单
  const sortMenuTree = (menuList: typeof menus): typeof menus => {
    return menuList
      .map(menu => ({
        ...menu,
        children: menu.children ? sortMenuTree(menu.children) : undefined
      }))
      .sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0))
  }
  return sortMenuTree(menus)
})

onMounted(() => {
  if (!authStore.isHydrated) {
    authStore.hydrateFromCache()
    // 从缓存恢复菜单
    const menuStore = useMenuStore()
    menuStore.hydrateFromCache()
  }
  if (authStore.isAuthenticated) {
    // 如果菜单为空，尝试加载菜单
    if (!menuStore.hasMenus) {
      menuStore.fetchMenus().catch(() => {
        // 加载失败不影响页面显示
      })
    }
    // 连接失败不影响用户正常使用，使用 try-catch 保护
    try {
      notificationStore.connect()
    } catch (error) {
      // 忽略连接错误
    }
    fetchUnreadAnnouncements()
  }
})

// 判断两个路径是否是同级页面（都属于同一个父路径）
const isSiblingRoute = (path1: string, path2: string): boolean => {
  // 获取父路径（去掉最后一个路径段）
  const getParentPath = (path: string): string => {
    const segments = path.split('/').filter(Boolean)
    if (segments.length <= 1) return ''
    segments.pop()
    return '/' + segments.join('/')
  }
  
  const parent1 = getParentPath(path1)
  const parent2 = getParentPath(path2)
  
  // 如果两个路径的父路径相同且不为空，则它们是同级页面
  return parent1 === parent2 && parent1 !== ''
}

// 返回上一级页面
const handleGoBack = () => {
  const previousRoute = navigationStore.getPreviousRoute(route.path)
  if (previousRoute) {
    // 如果来源页面与当前页面是同级页面，则返回到首页，避免来回跳转
    if (isSiblingRoute(route.path, previousRoute)) {
      router.push('/home')
    } else {
      router.push(previousRoute)
    }
  } else {
    // 如果没有记录来源页面，则返回到首页
    router.push('/home')
  }
}

// 判断是否是不显示返回按钮的页面（包括首页、一级菜单和二级菜单）
const isSubMenuPage = (path: string): boolean => {
  // 首页不显示返回按钮
  if (path === '/home' || path === '/') {
    return true
  }
  
  // 一级菜单：快捷记录
  if (path === '/quick-record') {
    return true
  }
  
  // 一级菜单：公告管理
  if (path === '/settings/announcements') {
    return true
  }
  
  // 一级菜单：定时任务管理
  if (path === '/settings/schedules') {
    return true
  }
  
  // 一级菜单：工具箱主页面
  if (path === '/tools') {
    return true
  }
  
  // 工作台下的菜单项
  if (path === '/todo') {
    return true
  }
  
  if (path === '/hot-sections') {
    return true
  }
  
  if (path === '/bookmark') {
    return true
  }
  
  if (path === '/code-generator') {
    return true
  }
  
  // 健康管理下的页面
  if (path === '/health' || path.startsWith('/health/')) {
    return true
  }
  
  // 资产管理下的页面
  if (
    path.startsWith('/asset/') ||
    path.startsWith('/assets') ||
    path.startsWith('/wishlist') ||
    path === '/data/management' ||
    path.startsWith('/data/management') ||
    path.startsWith('/settings/currency')
  ) {
    return true
  }
  
  // 图片管理
  if (path === '/image' || path.startsWith('/image')) {
    return true
  }
  
  // 消息推送
  if (path === '/messages' || path.startsWith('/messages/')) {
    return true
  }
  
  // 一级菜单：智能体调用
  if (path === '/agents') {
    return true
  }
  
  // AI工具下的页面
  if (path.startsWith('/ai/')) {
    return true
  }
  
  // 二级菜单项：设置下的页面
  if (path === '/config' || path.startsWith('/settings')) {
    return true
  }
  
  // 二级菜单项：工具箱下的子页面
  if (path.startsWith('/tools/')) {
    return true
  }
  
  return false
}

// 判断是否显示返回按钮（一级菜单页面显示，二级菜单页面不显示）
const showBackButton = computed(() => {
  // 任务大屏不展示返回按钮
  if (isTodoDashboardPage.value) return false
  return !isSubMenuPage(route.path)
})

// 监听路由变化，自动添加标签页
watch(
  () => route.fullPath,
  (newPath, oldPath) => {
    // 排除登录页和404页
    if (!route.meta?.public && route.name !== 'NotFound') {
      tabsStore.addTab(route)
    }
    // 移动端：路由跳转后自动关闭侧边栏
    if (responsiveStore.isMobile && responsiveStore.isSidebarExpanded) {
      closeSidebar()
    }
  },
  { immediate: true }
)

// 监听侧边栏状态变化，更新遮罩层显示
watch(
  () => responsiveStore.sidebarState,
  (newState) => {
    if (responsiveStore.isMobile) {
      showOverlay.value = newState === SidebarState.EXPANDED
    } else {
      showOverlay.value = false
    }
  }
)

// 监听设备类型变化，自动调整布局
watch(
  () => responsiveStore.deviceType,
  () => {
    // 设备类型变化时，重置遮罩层状态
    if (!responsiveStore.isMobile) {
      showOverlay.value = false
    }
  }
)

// 组件挂载时，如果当前路由不是登录页，添加标签页
onMounted(() => {
  if (!route.meta?.public && route.name !== 'NotFound') {
    tabsStore.addTab(route)
  }
})

watch(
  () => authStore.isAuthenticated,
  (authed) => {
    if (authed) {
      // 连接失败不影响用户正常使用，使用 try-catch 保护
      try {
        notificationStore.connect(true)
      } catch (error) {
        // 忽略连接错误
      }
      fetchUnreadAnnouncements()
    } else {
      notificationStore.reset()
      unreadAnnouncements.value = []
      announcementDialogVisible.value = false
    }
  },
  { immediate: true },
)

watch(
  () => authStore.user?.id,
  (id) => {
    if (id) {
      // 连接失败不影响用户正常使用，使用 try-catch 保护
      try {
        notificationStore.connect(true)
      } catch (error) {
        // 忽略连接错误
      }
      fetchUnreadAnnouncements()
    }
  },
)

interface QuickCommand {
  id: string
  label: string
  description?: string
  route?: string
}

const commandPaletteVisible = ref(false)
const commandQuery = ref('')

const quickCommands: QuickCommand[] = [
  { id: 'go-home', label: '首页 / 概览', description: '查看总览与快捷入口', route: '/home' },
  { id: 'go-ai-daily', label: 'AI日报', description: 'AI生成的个性化日报', route: '/ai-daily' },
  { id: 'go-todo', label: '待办事项', description: '任务管理与计时', route: '/todo' },
  { id: 'go-hot', label: '热点速览', description: '快速浏览热点与动态', route: '/hot-sections' },
  { id: 'go-messages', label: '消息推送', description: '查看与配置消息推送', route: '/messages' },
  { id: 'go-bookmark', label: '书签收藏', description: '站点收藏与导航', route: '/bookmark' },
  { id: 'go-codegen', label: '低代码生成', description: '快速搭建页面与脚本', route: '/code-generator' },
  { id: 'go-tools', label: '工具箱', description: '常用工程工具与小组件', route: '/tools' },
  { id: 'go-announcement', label: '公告管理（管理员）', description: '创建/发布/撤回公告', route: '/settings/announcements' },
]

const filteredCommands = computed(() => {
  const keyword = commandQuery.value.trim().toLowerCase()
  if (!keyword) return quickCommands
  return quickCommands.filter((cmd) => {
    const label = cmd.label.toLowerCase()
    const desc = cmd.description?.toLowerCase() ?? ''
    return label.includes(keyword) || desc.includes(keyword)
  })
})

const executeCommand = (cmd: QuickCommand) => {
  if (cmd.route) {
    router.push(cmd.route)
  }
  commandPaletteVisible.value = false
  commandQuery.value = ''
}

const handleOpenCommandPalette = () => {
  commandPaletteVisible.value = true
}

const closeCommandPalette = () => {
  commandPaletteVisible.value = false
  commandQuery.value = ''
}

const handleGlobalKeydown = (event: KeyboardEvent) => {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    commandPaletteVisible.value = true
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleGlobalKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleGlobalKeydown)
})

// 获取需要缓存的组件名称列表
// 注意：keep-alive 的 include 需要匹配组件的 name 选项
// 对于使用 <script setup> 的组件，如果没有显式设置 name，Vue 会使用文件名
// 这里我们使用路由的 name，需要确保路由的 name 与组件的 name 一致
// 如果组件没有 name，keep-alive 可能无法正确缓存，所以这里我们缓存所有标签页
const cachedViews = computed(() => {
  // 返回所有标签页的路由名称，用于 keep-alive 的 include
  // 如果路由没有 name，则尝试从路径推断组件名
  return tabsStore.tabs
    .filter(tab => tab.meta?.keepAlive !== false)
    .map(tab => {
      if (tab.name) {
        return tab.name
      }
      // 如果没有 name，尝试从路径推断（例如 /tools/json -> JsonFormatter）
      // 但这种方式不可靠，最好确保路由都有 name
      return tab.path.split('/').filter(Boolean).join('-') || 'default'
    })
    .filter(Boolean) as string[]
})

</script>

<template>
  <el-container class="layout-shell">
    <!-- 移动端遮罩层 -->
    <div
      v-if="showOverlay && responsiveStore.isMobile"
      class="sidebar-overlay"
      @click="handleOverlayClick"
    />
      <el-aside
      :width="responsiveStore.isMobile ? (responsiveStore.isSidebarExpanded ? '80%' : '0') : (isCollapsed ? '64px' : '228px')"
      class="layout-aside"
      :class="{
        'is-collapsed': isCollapsed && !responsiveStore.isMobile,
        'is-mobile': responsiveStore.isMobile,
        'is-mobile-open': responsiveStore.isMobile && responsiveStore.isSidebarExpanded,
      }"
    >
      <div class="aside-content">
        <div class="logo" @click="handleLogoClick">
          <img :src="logoIcon" alt="工作台" class="logo-icon" />
          <span v-show="!isCollapsed">工作台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :default-openeds="defaultOpenMenus"
          :collapse="isCollapsed"
          :collapse-transition="false"
          router
          unique-opened
          class="menu"
          background-color="transparent"
          text-color="var(--text-secondary)"
          active-text-color="var(--primary-color)"
          @select="handleMenuSelect"
        >
          <!-- 动态渲染菜单 -->
          <template v-if="sortedMenus.length > 0">
            <DynamicMenu :menus="sortedMenus" :is-collapsed="isCollapsed" />
            </template>
          <!-- 如果菜单为空，显示加载状态或空状态 -->
          <template v-else>
            <div v-if="menuStore.loading" class="menu-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载菜单中...</span>
            </div>
              </template>
        </el-menu>
        <div class="collapse-button-wrapper">
          <el-button
            :icon="isCollapsed ? Expand : Fold"
            circle
            size="default"
            class="collapse-button"
            @click="toggleCollapse"
          />
        </div>
      </div>
    </el-aside>
    <el-container>
      <el-header
        v-if="!isTodoDashboardPage"
        class="layout-header"
      >
        <div class="page-meta">
          <!-- 移动端菜单按钮 -->
          <el-button
            v-if="layout.showMenuButton"
            :icon="Menu"
            circle
            class="menu-button mobile-only"
            @click="handleMenuButtonClick"
          />
          <el-button
            v-if="showBackButton"
            :icon="ArrowLeft"
            circle
            class="back-button"
            @click="handleGoBack"
          />
          <h1 v-if="pageTitle">{{ pageTitle }}</h1>
        </div>
        <div class="header-center" v-if="!isTodoDashboardPage">
          <button
            class="command-k-button"
            type="button"
            @click="handleOpenCommandPalette"
          >
            <el-icon class="command-k-icon"><Search /></el-icon>
            <span class="command-k-text">搜索页面 / 执行命令</span>
            <span class="command-k-kbd-group">
              <kbd>Ctrl</kbd>
              <span class="command-k-plus">+</span>
              <kbd>K</kbd>
            </span>
          </button>
        </div>
        <div class="header-actions" v-if="!isTodoDashboardPage">
          <el-popover
            placement="bottom-end"
            width="360"
            trigger="click"
            :show-arrow="false"
            v-model:visible="notificationVisible"
            popper-class="notification-popper"
          >
            <template #reference>
              <el-badge
                :value="notificationStore.unreadCount"
                :hidden="notificationStore.unreadCount === 0"
                class="notification-badge"
              >
                <el-button
                  circle
                  class="notification-button"
                  :icon="Bell"
                />
              </el-badge>
            </template>
            <div class="notification-panel">
              <div class="notification-header">
                <div>
                  <div class="notification-title-text">消息提醒</div>
                  <div class="notification-subtitle">
                    <span v-if="notificationStore.connecting" class="status-connecting">
                      <el-icon class="status-icon"><Loading /></el-icon>
                      连接中...
                    </span>
                    <span v-else-if="!notificationStore.connected" class="status-disconnected">
                      <el-icon class="status-icon"><WarningFilled /></el-icon>
                      未连接，稍后自动重试
                    </span>
                    <span v-if="notificationStore.unreadCount"> · {{ notificationStore.unreadCount }} 未读</span>
                  </div>
                </div>
                <el-button text size="small" @click="notificationStore.markAllRead">全部已读</el-button>
              </div>
              <el-empty v-if="!notificationStore.notifications.length" description="暂无消息" />
              <div v-else class="notification-list">
                <!-- 未读消息 -->
                <div v-if="unreadNotifications.length > 0" class="notification-section">
                  <div class="section-title">未读消息</div>
                  <div
                    v-for="item in unreadNotifications"
                    :key="item.id"
                    :class="['notification-item', 'is-unread']"
                    @click="handleNotificationClick(item)"
                  >
                    <div class="notification-item__title">{{ item.title }}</div>
                    <div class="notification-item__content">{{ item.content }}</div>
                    <div class="notification-item__meta">
                      <span>{{ new Date(item.receivedAt).toLocaleString() }}</span>
                      <el-tag size="small" type="warning" effect="plain">未读</el-tag>
                    </div>
                  </div>
                </div>
                <!-- 已读消息 -->
                <div v-if="readNotifications.length > 0" class="notification-section">
                  <div class="section-title">已读消息</div>
                  <div
                    v-for="item in readNotifications"
                    :key="item.id"
                    class="notification-item"
                    @click="handleNotificationClick(item)"
                  >
                    <div class="notification-item__title">{{ item.title }}</div>
                    <div class="notification-item__content">{{ item.content }}</div>
                    <div class="notification-item__meta">
                      <span>{{ new Date(item.receivedAt).toLocaleString() }}</span>
                      <el-tag size="small" type="info" effect="plain">已读</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-popover>
          <el-button
            circle
            class="refresh-menu-button"
            :icon="Refresh"
            :loading="menuStore.loading"
            @click="handleRefreshMenus"
            title="刷新菜单"
          />
          <el-dropdown trigger="click" @command="(cmd) => cmd === 'logout' && handleLogout()">
            <div class="user-dropdown">
              <el-avatar :size="36" class="user-avatar" :src="authStore.user?.avatar">
                {{ authStore.user?.name?.charAt(0) ?? 'G' }}
              </el-avatar>
              <div class="user-info">
                <span class="user-name">{{ authStore.user?.name ?? '访客' }}</span>
                <small class="user-role">{{ authStore.user?.roles?.join(', ') ?? '--' }}</small>
              </div>
              <el-icon class="dropdown-icon"><ArrowDownBold /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="'profile'" @click="router.push({ name: 'UserProfile' })">
                  <el-icon><User /></el-icon>
                  <span>我的信息</span>
                </el-dropdown-item>
                <el-dropdown-item :command="'resetPassword'" @click="router.push({ name: 'ResetPassword' })">
                  <el-icon><Lock /></el-icon>
                  <span>重置密码</span>
                </el-dropdown-item>
                <el-dropdown-item divided :command="'logout'">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <TabsView />
      <el-main class="layout-main">
        <router-view v-slot="{ Component, route: currentRoute }">
          <keep-alive :include="cachedViews">
            <component 
              :is="Component" 
              :key="`${currentRoute.fullPath}-${tabsStore.getRefreshKey(currentRoute.fullPath)}`"
              v-if="Component"
            />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
    <!-- 全局聊天组件 -->
    <ChatWidget />
    <AnnouncementDialog
      v-if="authStore.isAuthenticated"
      v-model="announcementDialogVisible"
      :announcements="unreadAnnouncements"
      :loading="fetchingAnnouncements"
      @read="handleAnnouncementRead"
    />
    <NotificationDetailDialog
      v-model="notificationDetailVisible"
      :notification="currentNotification"
      @read="handleNotificationRead"
    />
    <el-dialog
      v-model="commandPaletteVisible"
      width="560px"
      top="10vh"
      :show-close="false"
      class="command-dialog"
      destroy-on-close
      @closed="closeCommandPalette"
    >
      <div class="command-dialog-body">
        <el-input
          v-model="commandQuery"
          autofocus
          size="large"
          placeholder="搜索页面或功能，例如：工具箱、消息推送、智能体..."
          class="command-dialog-input"
          :prefix-icon="Search"
          clearable
        />
        <div v-if="filteredCommands.length" class="command-dialog-list">
          <button
            v-for="cmd in filteredCommands"
            :key="cmd.id"
            type="button"
            class="command-dialog-item"
            @click="executeCommand(cmd)"
          >
            <div class="command-dialog-item-main">
              <span class="command-label">{{ cmd.label }}</span>
              <span v-if="cmd.description" class="command-desc">{{ cmd.description }}</span>
            </div>
            <span class="command-enter">↵</span>
          </button>
        </div>
        <div v-else class="command-dialog-empty">
          暂未找到匹配的命令，试试换个关键词。
        </div>
        <div class="command-dialog-footer">
          支持 Ctrl + K 快速唤起 · 鼠标点击执行 · Esc 关闭
        </div>
      </div>
    </el-dialog>
  </el-container>
</template>

<style scoped>
.layout-shell {
  min-height: 100vh;
  background: var(--bg-gradient);
  color: var(--text-primary);
}

.layout-aside {
  background: var(--surface-color);
  border-right: 1px solid var(--ph-border-subtle);
  display: flex;
  flex-direction: column;
  box-shadow: var(--ph-shadow-soft);
  position: relative;
  transition: width 0.3s ease;
}

.layout-aside::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 1px;
  height: 100%;
  background: linear-gradient(180deg, transparent, rgba(148, 163, 184, 0.35), transparent);
}

.aside-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  font-size: 18px;
  padding: 20px 20px 16px;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  color: #e5e7eb;
  justify-content: center;
  flex-shrink: 0;
}

.layout-aside.is-collapsed .logo {
  padding: 20px 10px 16px;
  justify-content: center;
}

.logo:hover {
  transform: translateX(2px);
  color: #e5e7eb;
}

.logo-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.collapse-button-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  margin-top: auto;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.02);
}

.layout-aside.is-collapsed .collapse-button-wrapper {
  padding: 16px 10px;
}

.collapse-button {
  width: 40px;
  height: 40px;
  border: 1.5px solid rgba(148, 163, 184, 0.3);
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
  color: rgba(226, 232, 240, 0.9);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.collapse-button:hover {
  background: rgba(37, 99, 235, 0.15);
  border-color: rgba(37, 99, 235, 0.5);
  color: rgba(129, 140, 248, 1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.collapse-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.15);
}

.menu {
  border-right: none;
  flex: 1;
  padding: 8px 10px 20px;
  background: transparent;
  overflow-y: auto;
  overflow-x: hidden;
}

.layout-aside.is-collapsed .menu {
  padding: 8px 5px 20px;
}

.menu::-webkit-scrollbar {
  width: 4px;
}

.menu::-webkit-scrollbar-track {
  background: transparent;
}

.menu::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 2px;
}

.menu::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.5);
}

.menu :deep(.el-menu-item),
.menu :deep(.el-sub-menu__title) {
  border-radius: 10px;
  margin: 6px 0;
  height: 42px;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 13px;
  transition: background-color 0.15s ease, color 0.15s ease, transform 0.1s ease-out, box-shadow 0.15s ease;
  position: relative;
  overflow: hidden;
}

.menu :deep(.el-menu-item::before),
.menu :deep(.el-sub-menu__title::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) translateX(-100%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #38bdf8, #22c55e);
  border-radius: 0 4px 4px 0;
  transition: all 0.3s ease;
}

.menu :deep(.el-menu-item:hover),
.menu :deep(.el-sub-menu__title:hover) {
  background: #eef2ff;
  color: var(--primary-color);
  transform: translateX(2px);
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.45);
}

.menu :deep(.el-menu-item:hover::before),
.menu :deep(.el-sub-menu__title:hover::before) {
  height: 70%;
  transform: translateY(-50%) translateX(0);
}

.menu :deep(.el-sub-menu__title .el-sub-menu__icon-arrow) {
  color: inherit;
}

.menu :deep(.el-menu-item.is-active) {
  background: #e0ecff;
  color: var(--primary-color);
  box-shadow:
    0 0 0 1px rgba(37, 99, 235, 0.6),
    0 4px 12px rgba(37, 99, 235, 0.35);
  font-weight: 600;
}

.menu :deep(.el-menu-item.is-active .el-icon) {
  color: var(--primary-color);
}

.menu :deep(.el-menu-item.is-active::before) {
  height: 70%;
  transform: translateY(-50%) translateX(0);
}

.menu :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  background: #eef2ff;
  color: var(--primary-color);
  box-shadow: inset 0 0 0 1px rgba(129, 140, 248, 0.35);
}

.menu :deep(.el-sub-menu .el-menu-item) {
  margin-left: 8px;
  background: transparent;
  border-radius: 10px;
}

.menu :deep(.el-sub-menu .el-menu) {
  padding: 2px 0 6px 4px;
  border-left: none;
  margin-left: 0;
}

.menu :deep(.el-sub-menu .el-sub-menu .el-menu-item) {
  margin-left: 16px;
  padding-left: 32px !important;
  border-radius: 10px;
  background: #f1f5f9;
}

.menu :deep(.el-menu--popup) {
  border-radius: 14px;
  border: 1px solid var(--ph-border-subtle);
  box-shadow: var(--ph-shadow-soft);
  background: #ffffff;
  padding: 8px;
}

.menu :deep(.el-menu--popup .el-menu-item) {
  border-radius: 10px;
}

.menu :deep(.el-menu--collapse) {
  width: 100%;
}

.menu :deep(.el-menu--collapse .el-menu-item span),
.menu :deep(.el-menu--collapse .el-sub-menu__title span) {
  height: 0;
  width: 0;
  overflow: hidden;
  visibility: hidden;
  display: inline-block;
}

.menu :deep(.el-menu--collapse .el-menu-item),
.menu :deep(.el-menu--collapse .el-sub-menu__title) {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 !important;
  width: 100%;
}

.menu :deep(.el-menu--collapse .el-menu-item .el-icon),
.menu :deep(.el-menu--collapse .el-sub-menu__title .el-icon) {
  margin: 0 !important;
}

.menu :deep(.el-menu--collapse .el-sub-menu__icon-arrow) {
  display: none;
}

.menu-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: var(--text-secondary);
  font-size: 13px;
}

.menu-loading .el-icon {
  font-size: 16px;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
  padding: 0 24px;
  position: relative;
  z-index: 10;
}

.page-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 24px;
}

.page-meta h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.back-button {
  border: 1px solid rgba(148, 163, 184, 0.7);
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-tertiary);
  transition: all 0.2s ease;
}

.back-button:hover {
  background: var(--primary-light);
  border-color: rgba(37, 99, 235, 0.8);
  color: var(--primary-color);
  transform: translateX(-2px);
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-shrink: 0;
}

.command-k-button {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  max-width: 420px;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid var(--ph-border-subtle);
  background: var(--surface-color);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  text-align: left;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease,
    color 0.15s ease,
    box-shadow 0.15s ease,
    transform 0.08s ease-out;
}

.command-k-button:hover {
  border-color: rgba(37, 99, 235, 0.55);
  background: var(--primary-light);
  color: var(--primary-color);
  box-shadow: var(--ph-shadow-soft);
  transform: translateY(-1px);
}

.command-k-button:focus-visible {
  outline: none;
  box-shadow:
    0 0 0 1px rgba(129, 140, 248, 0.9),
    0 0 0 3px rgba(129, 140, 248, 0.5);
}

.command-k-icon {
  font-size: 14px;
  color: var(--text-tertiary);
}

.command-k-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.command-k-kbd-group {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--text-tertiary);
  font-size: 11px;
}

.command-k-kbd-group kbd {
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid rgba(148, 163, 184, 0.6);
  background: #f9fafb;
  font-size: 11px;
  line-height: 1.3;
}

.command-k-plus {
  opacity: 0.7;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.user-dropdown:hover {
  background-color: rgba(15, 23, 42, 0.04);
}

.user-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  color: white;
  font-weight: 700;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.9);
  transition: all 0.3s ease;
}

.user-dropdown:hover .user-avatar {
  transform: scale(1.05);
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.95);
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.4;
  min-width: 0;
}

.user-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.user-role {
  font-size: 12px;
  color: var(--text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.dropdown-icon {
  color: var(--text-tertiary);
  font-size: 14px;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.user-dropdown:hover .dropdown-icon {
  color: #a5b4fc;
}

.layout-main {
  padding: 32px;
  background: transparent;
}

.notification-button {
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.7);
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-tertiary);
}

.notification-button:hover {
  background: var(--primary-light);
  border-color: rgba(37, 99, 235, 0.8);
  color: var(--primary-color);
}

.notification-popper {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid var(--ph-border-subtle);
  box-shadow: var(--ph-shadow-soft);
  padding: 10px;
}

.notification-panel {
  max-height: 420px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.notification-title-text {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.notification-subtitle {
  font-size: 12px;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-icon {
  font-size: 14px;
  vertical-align: middle;
}

.status-connecting {
  color: #409eff;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.status-connected {
  color: #67c23a;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.status-disconnected {
  color: #e6a23c;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.notification-list {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.notification-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.section-title {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-tertiary);
  padding: 4px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.notification-item {
  padding: 8px 10px;
  border-radius: 10px;
  cursor: pointer;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.9);
  transition: background-color 0.15s ease, border-color 0.15s ease, transform 0.08s ease-out;
}

.notification-item.is-unread {
  border-color: rgba(129, 140, 248, 0.9);
}

.notification-item:hover {
  background: #eff6ff;
  transform: translateY(-1px);
}

.notification-item__title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.notification-item__content {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.notification-item__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: var(--text-tertiary);
}

.command-dialog {
  --el-dialog-bg-color: rgba(255, 255, 255, 0.96);
  --el-text-color-primary: var(--text-primary);
  --el-border-color: var(--ph-border-subtle);
  --el-dialog-box-shadow: var(--ph-shadow-soft);
}

.command-dialog :deep(.el-dialog__header) {
  display: none;
}

.command-dialog :deep(.el-dialog__body) {
  padding: 14px 14px 10px;
}

.command-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.command-dialog-input :deep(.el-input__wrapper) {
  border-radius: 999px;
  background-color: rgba(255, 255, 255, 0.96);
  box-shadow:
    0 0 0 1px rgba(148, 163, 184, 0.6),
    0 18px 40px rgba(15, 23, 42, 0.12);
}

.command-dialog-list {
  margin-top: 4px;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 0.98);
  max-height: 320px;
  overflow-y: auto;
}

.command-dialog-item {
  width: 100%;
  padding: 8px 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  text-align: left;
  transition: background-color 0.15s ease, color 0.15s ease, transform 0.08s ease-out;
}

.command-dialog-item + .command-dialog-item {
  border-top: 1px solid rgba(226, 232, 240, 0.9);
}

.command-dialog-item:hover {
  background: #eff6ff;
  color: var(--primary-color);
  transform: translateY(-1px);
}

.command-dialog-item-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.command-label {
  font-size: 13px;
  font-weight: 500;
}

.command-desc {
  font-size: 12px;
  color: var(--text-tertiary);
}

.command-enter {
  font-size: 12px;
  color: var(--text-tertiary);
}

.command-dialog-empty {
  padding: 10px 12px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.command-dialog-footer {
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-tertiary);
  text-align: right;
}

/* 移动端菜单按钮样式 */
.menu-button {
  border: 1px solid rgba(148, 163, 184, 0.7);
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-tertiary);
  transition: all 0.2s ease;
  margin-right: 8px;
}

.menu-button:hover {
  background: var(--primary-light);
  border-color: rgba(37, 99, 235, 0.8);
  color: var(--primary-color);
}

/* 移动端遮罩层样式 */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 998;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 移动端侧边栏样式 */
.layout-aside.is-mobile {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 999;
  transition: width 0.3s ease, transform 0.3s ease;
  transform: translateX(-100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.layout-aside.is-mobile.is-mobile-open {
  transform: translateX(0);
}

/* 移动端Header样式优化 */
@media (max-width: 767px) {
  .layout-header {
    padding: 0 16px;
    height: 56px !important;
  }

  .page-meta h1 {
    font-size: 18px;
  }
}

/* 移动端主内容区样式 */
@media (max-width: 767px) {
  .layout-main {
    padding: 16px;
  }
}
</style>

