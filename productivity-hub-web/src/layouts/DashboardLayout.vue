<script setup lang="ts">
import { computed, watch, onMounted, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNavigationStore } from '@/stores/navigation'
import { useTabsStore } from '@/stores/tabs'
import { useNotificationStore } from '@/stores/notifications'
import TabsView from '@/components/TabsView.vue'
import ChatWidget from '@/components/ChatWidget.vue'
import { Setting, Message, Cpu, Lock, SwitchButton, ArrowDownBold, HomeFilled, Tools, ArrowLeft, Document, TrendCharts, Collection, Bell, Search, Fold, Expand } from '@element-plus/icons-vue'
import logoIcon from '@/assets/logo.svg'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const navigationStore = useNavigationStore()
const tabsStore = useTabsStore()
const notificationStore = useNotificationStore()
const notificationVisible = ref(false)
const isCollapsed = ref(false)

const activeMenu = computed(() => {
  if (route.path.startsWith('/home')) return '/home'
  if (route.path.startsWith('/hot-sections')) return '/hot-sections'
  if (route.path.startsWith('/config')) return '/config'
  if (route.path.startsWith('/settings/users')) return '/settings/users'
  if (route.path.startsWith('/messages')) return '/messages'
  if (route.path.startsWith('/tools')) return '/tools'
  if (route.path.startsWith('/agents')) return '/agents'
  if (route.path.startsWith('/code-generator')) return '/code-generator'
  if (route.path.startsWith('/bookmark')) return '/bookmark'
  return route.path
})

const pageTitle = computed(() => route.meta.title ?? '控制台')

const defaultOpenMenus = computed(() => {
  const openeds: string[] = []
  if (route.path.startsWith('/config') || route.path.startsWith('/settings')) {
    openeds.push('/settings')
  }
  return openeds
})

const isAdminUser = computed(() => authStore.user?.roles?.includes('admin'))

// 切换导航栏折叠状态
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 左上角 Logo 点击返回首页
const handleLogoClick = () => {
  router.push('/home')
}

const handleLogout = () => {
  authStore.logout()
  tabsStore.clearTabs()
  notificationStore.reset()
  router.replace({ name: 'Login' })
}

const handleNotificationClick = (id: string, link?: string) => {
  notificationStore.markRead(id)
  notificationVisible.value = false
  if (link) {
    router.push(link)
  }
}

onMounted(() => {
  if (!authStore.isHydrated) {
    authStore.hydrateFromCache()
  }
  if (authStore.isAuthenticated) {
    notificationStore.connect()
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
  
  // 一级菜单：工具箱主页面
  if (path === '/tools') {
    return true
  }
  
  // 一级菜单：智能体调用
  if (path === '/agents') {
    return true
  }
  
  // 一级菜单：低代码生成
  if (path === '/code-generator') {
    return true
  }
  
  // 一级菜单：宝藏类网址
  if (path === '/bookmark') {
    return true
  }
  
  // 一级菜单：热点速览
  if (path === '/hot-sections') {
    return true
  }
  
  // 二级菜单项：消息推送下的页面
  if (path === '/messages' || path.startsWith('/messages/')) {
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
  },
  { immediate: true }
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
      notificationStore.connect(true)
    } else {
      notificationStore.reset()
    }
  },
  { immediate: true },
)

watch(
  () => authStore.user?.id,
  (id) => {
    if (id) {
      notificationStore.connect(true)
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
  { id: 'go-home', label: '前往首页', description: '查看总览与快捷入口', route: '/home' },
  { id: 'go-hot', label: '查看热点速览', description: '快速浏览热点与动态', route: '/hot-sections' },
  { id: 'go-tools', label: '打开工具箱', description: '常用工程工具与小组件', route: '/tools' },
  { id: 'go-messages', label: '消息推送中心', description: '查看与配置消息推送', route: '/messages' },
  { id: 'go-agents', label: '智能体调用', description: '管理与调用智能体', route: '/agents' },
  { id: 'go-codegen', label: '低代码生成', description: '快速搭建页面与脚本', route: '/code-generator' },
  { id: 'go-bookmark', label: '宝藏类网址', description: '站点收藏与导航', route: '/bookmark' },
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
    <el-aside :width="isCollapsed ? '64px' : '228px'" class="layout-aside" :class="{ 'is-collapsed': isCollapsed }">
      <div class="aside-content">
        <div class="logo" @click="handleLogoClick">
          <img :src="logoIcon" alt="工作台" class="logo-icon" />
          <span v-show="!isCollapsed">工作台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :default-openeds="defaultOpenMenus"
          :collapse="isCollapsed"
          router
          class="menu"
          background-color="transparent"
          text-color="var(--text-secondary)"
          active-text-color="var(--primary-color)"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item index="/hot-sections">
            <el-icon><TrendCharts /></el-icon>
            <template #title>热点速览</template>
          </el-menu-item>
          <el-menu-item index="/messages">
            <el-icon><Message /></el-icon>
            <template #title>消息推送</template>
          </el-menu-item>
          <el-menu-item index="/tools">
            <el-icon><Tools /></el-icon>
            <template #title>工具箱</template>
          </el-menu-item>
          <el-menu-item index="/agents">
            <el-icon><Cpu /></el-icon>
            <template #title>智能体调用</template>
          </el-menu-item>
          <el-menu-item index="/code-generator">
            <el-icon><Document /></el-icon>
            <template #title>低代码生成</template>
          </el-menu-item>
          <el-menu-item index="/bookmark">
            <el-icon><Collection /></el-icon>
            <template #title>宝藏类网址</template>
          </el-menu-item>
          <el-sub-menu index="/settings">
            <template #title>
              <el-icon><Setting /></el-icon>
              <template v-if="!isCollapsed">设置</template>
            </template>
            <el-menu-item index="/config">
              <span>全局参数配置</span>
            </el-menu-item>
            <el-menu-item v-if="isAdminUser" index="/settings/users">
              <span>系统用户管理</span>
            </el-menu-item>
          </el-sub-menu>
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
      <el-header class="layout-header">
        <div class="page-meta">
          <el-button
            v-if="showBackButton"
            :icon="ArrowLeft"
            circle
            class="back-button"
            @click="handleGoBack"
          />
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="header-center">
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
        <div class="header-actions">
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
                  @click="notificationVisible = !notificationVisible"
                />
              </el-badge>
            </template>
            <div class="notification-panel">
              <div class="notification-header">
                <div>
                  <div class="notification-title-text">消息提醒</div>
                  <div class="notification-subtitle">
                    {{ notificationStore.connected ? '已连接' : '未连接，稍后自动重试' }}
                    <span v-if="notificationStore.unreadCount"> · {{ notificationStore.unreadCount }} 未读</span>
                  </div>
                </div>
                <el-button text size="small" @click="notificationStore.markAllRead">全部已读</el-button>
              </div>
              <el-empty v-if="!notificationStore.notifications.length" description="暂无消息" />
              <div v-else class="notification-list">
                <div
                  v-for="item in notificationStore.notifications"
                  :key="item.id"
                  :class="['notification-item', !item.read && 'is-unread']"
                  @click="handleNotificationClick(item.id, item.link)"
                >
                  <div class="notification-item__title">{{ item.title }}</div>
                  <div class="notification-item__content">{{ item.content }}</div>
                  <div class="notification-item__meta">
                    <span>{{ new Date(item.receivedAt).toLocaleString() }}</span>
                    <el-tag v-if="item.link" size="small" type="info" effect="plain">查看详情</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-popover>
          <el-dropdown trigger="click" @command="(cmd) => cmd === 'logout' && handleLogout()">
            <div class="user-dropdown">
              <el-avatar :size="36" class="user-avatar">
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
          <keep-alive>
            <component 
              :is="Component" 
              :key="currentRoute.fullPath"
              v-if="Component"
            />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
    <!-- 全局聊天组件 -->
    <ChatWidget />
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
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.6), rgba(8, 47, 73, 0.95));
  color: #e5e7eb;
  box-shadow:
    inset 0 0 0 1px rgba(129, 140, 248, 0.6),
    0 6px 18px rgba(15, 23, 42, 0.8);
  font-weight: 600;
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
  margin-left: 12px;
  background: transparent;
  border-radius: 10px;
}

.menu :deep(.el-sub-menu .el-menu) {
  padding: 4px 0 4px 12px;
  border-left: 1px solid rgba(148, 163, 184, 0.5);
  margin-left: 4px;
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
}

.notification-list {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 6px;
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
</style>

