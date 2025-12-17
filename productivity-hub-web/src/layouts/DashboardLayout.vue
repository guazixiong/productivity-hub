<script setup lang="ts">
import { computed, watch, onMounted, ref } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNavigationStore } from '@/stores/navigation'
import { useTabsStore } from '@/stores/tabs'
import { useNotificationStore } from '@/stores/notifications'
import TabsView from '@/components/TabsView.vue'
import ChatWidget from '@/components/ChatWidget.vue'
import { Setting, Message, Cpu, Lock, SwitchButton, ArrowDownBold, HomeFilled, Tools, ArrowLeft, Document, TrendCharts, Collection, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import logoIcon from '@/assets/logo.svg'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const navigationStore = useNavigationStore()
const tabsStore = useTabsStore()
const notificationStore = useNotificationStore()
const notificationVisible = ref(false)

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
    <el-aside width="228px" class="layout-aside">
      <div class="logo" @click="handleLogoClick">
        <img :src="logoIcon" alt="工作台" class="logo-icon" />
        <span>工作台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :default-openeds="defaultOpenMenus"
        router
        class="menu"
        background-color="transparent"
        text-color="#475569"
        active-text-color="#312e81"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/hot-sections">
          <el-icon><TrendCharts /></el-icon>
          <span>热点速览</span>
        </el-menu-item>
        <el-menu-item index="/messages">
          <el-icon><Message /></el-icon>
          <span>消息推送</span>
        </el-menu-item>
        <el-menu-item index="/tools">
          <el-icon><Tools /></el-icon>
          <span>工具箱</span>
        </el-menu-item>
        <el-menu-item index="/agents">
          <el-icon><Cpu /></el-icon>
          <span>智能体调用</span>
        </el-menu-item>
        <el-menu-item index="/code-generator">
          <el-icon><Document /></el-icon>
          <span>低代码生成</span>
        </el-menu-item>
        <el-menu-item index="/bookmark">
          <el-icon><Collection /></el-icon>
          <span>宝藏类网址</span>
        </el-menu-item>
        <el-sub-menu index="/settings">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </template>
          <el-menu-item index="/config">
            <span>全局参数配置</span>
          </el-menu-item>
          <el-menu-item v-if="isAdminUser" index="/settings/users">
            <span>系统用户管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
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
  </el-container>
</template>

<style scoped>
.layout-shell {
  min-height: 100vh;
  background: 
    radial-gradient(circle at 10% 20%, rgba(139, 92, 246, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 90% 80%, rgba(99, 102, 241, 0.06) 0%, transparent 50%),
    linear-gradient(180deg, #f8fafc 0%, #eef2ff 50%, #f8fafc 100%);
  background-attachment: fixed;
  color: #0f172a;
}

.layout-aside {
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.95), transparent 70%),
    radial-gradient(circle at bottom right, rgba(224, 231, 255, 0.8), transparent 70%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.85) 100%);
  border-right: 1px solid rgba(99, 102, 241, 0.12);
  backdrop-filter: blur(20px) saturate(180%);
  display: flex;
  flex-direction: column;
  box-shadow: 
    20px 0 50px rgba(15, 23, 42, 0.08),
    inset -1px 0 0 rgba(255, 255, 255, 0.5);
  position: relative;
}

.layout-aside::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 1px;
  height: 100%;
  background: linear-gradient(180deg, transparent, rgba(99, 102, 241, 0.2), transparent);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 800;
  font-size: 22px;
  padding: 28px 24px 20px;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.logo:hover {
  transform: translateX(2px);
  filter: drop-shadow(0 2px 8px rgba(99, 102, 241, 0.3));
}

.logo-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.menu {
  border-right: none;
  flex: 1;
  padding: 8px 12px 24px;
  background: transparent;
}

.menu :deep(.el-menu-item),
.menu :deep(.el-sub-menu__title) {
  border-radius: 14px;
  margin: 6px 0;
  height: 48px;
  color: #334155;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
  width: 4px;
  height: 0;
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  border-radius: 0 4px 4px 0;
  transition: all 0.3s ease;
}

.menu :deep(.el-menu-item:hover),
.menu :deep(.el-sub-menu__title:hover) {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.08) 100%);
  color: #1e1b4b;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.1);
}

.menu :deep(.el-menu-item:hover::before),
.menu :deep(.el-sub-menu__title:hover::before) {
  height: 60%;
  transform: translateY(-50%) translateX(0);
}

.menu :deep(.el-sub-menu__title .el-sub-menu__icon-arrow) {
  color: inherit;
}

.menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.12) 100%);
  color: #312e81;
  box-shadow: 
    inset 0 0 0 1px rgba(99, 102, 241, 0.2),
    0 4px 12px rgba(99, 102, 241, 0.15);
  font-weight: 600;
}

.menu :deep(.el-menu-item.is-active::before) {
  height: 70%;
  transform: translateY(-50%) translateX(0);
}

.menu :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  background: rgba(99, 102, 241, 0.1);
  color: #1f1d47;
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.12);
}

.menu :deep(.el-sub-menu .el-menu-item) {
  margin-left: 12px;
  background: transparent;
  border-radius: 10px;
}

.menu :deep(.el-sub-menu .el-menu) {
  padding: 4px 0 4px 12px;
  border-left: 1px solid rgba(99, 102, 241, 0.12);
  margin-left: 4px;
}

.menu :deep(.el-sub-menu .el-sub-menu .el-menu-item) {
  margin-left: 16px;
  padding-left: 32px !important;
  border-radius: 10px;
  background: rgba(99, 102, 241, 0.04);
}

.menu :deep(.el-menu--popup) {
  border-radius: 14px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
  background: rgba(255, 255, 255, 0.96);
  padding: 8px;
}

.menu :deep(.el-menu--popup .el-menu-item) {
  border-radius: 10px;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background:
    radial-gradient(circle at top right, rgba(99, 102, 241, 0.1), transparent 60%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.1);
  box-shadow: 
    0 8px 32px rgba(15, 23, 42, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  padding: 0 28px;
  position: relative;
  z-index: 10;
}

.page-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-meta h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #0f172a 0%, #475569 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.3px;
}

.back-button {
  border: 1px solid rgba(99, 102, 241, 0.2);
  background: rgba(255, 255, 255, 0.8);
  color: #6366f1;
  transition: all 0.2s ease;
}

.back-button:hover {
  background: rgba(99, 102, 241, 0.1);
  border-color: #6366f1;
  transform: translateX(-2px);
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
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
  background-color: rgba(99, 102, 241, 0.08);
}

.user-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  color: white;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  transition: all 0.3s ease;
}

.user-dropdown:hover .user-avatar {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
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
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.user-role {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.dropdown-icon {
  color: #94a3b8;
  font-size: 14px;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.user-dropdown:hover .dropdown-icon {
  color: #6366f1;
}

.layout-main {
  padding: 32px;
  background: transparent;
}
</style>

