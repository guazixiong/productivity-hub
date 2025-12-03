<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNavigationStore } from '@/stores/navigation'
import { Setting, Message, Cpu, Lock, SwitchButton, ArrowDownBold, HomeFilled, Tools, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import logoIcon from '@/assets/logo.svg'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const navigationStore = useNavigationStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/home')) return '/home'
  if (route.path.startsWith('/config')) return '/config'
  if (route.path.startsWith('/messages')) return '/messages'
  if (route.path.startsWith('/tools')) return '/tools'
  if (route.path.startsWith('/agents')) return '/agents'
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

const handleLogout = () => {
  authStore.logout()
  router.replace({ name: 'Login' })
}

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
  
  // 二级菜单项：消息推送下的页面
  if (path === '/messages' || path.startsWith('/messages/')) {
    return true
  }
  
  // 二级菜单项：设置下的页面
  if (path === '/config') {
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

</script>

<template>
  <el-container class="layout-shell">
    <el-aside width="228px" class="layout-aside">
      <div class="logo">
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
        <el-sub-menu index="/settings">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </template>
          <el-menu-item index="/config">
            <span>全局参数配置</span>
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
      <el-main class="layout-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-shell {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #eef2ff 50%, #f8fafc 100%);
  color: #0f172a;
}

.layout-aside {
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.8), rgba(224, 231, 255, 0.9)),
    linear-gradient(180deg, rgba(248, 250, 252, 0.9), rgba(224, 231, 255, 0.9));
  border-right: 1px solid rgba(99, 102, 241, 0.15);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  box-shadow: 16px 0 40px rgba(15, 23, 42, 0.08);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #312e81;
  font-weight: 700;
  font-size: 20px;
  padding: 28px 24px 20px;
  letter-spacing: 0.3px;
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
  border-radius: 12px;
  margin: 4px 0;
  height: 44px;
  color: #334155;
  font-weight: 500;
  transition: all 0.2s ease;
}

.menu :deep(.el-menu-item:hover),
.menu :deep(.el-sub-menu__title:hover) {
  background: rgba(99, 102, 241, 0.08);
  color: #1e1b4b;
}

.menu :deep(.el-sub-menu__title .el-sub-menu__icon-arrow) {
  color: inherit;
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(79, 70, 229, 0.12);
  color: #312e81;
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.18);
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
    radial-gradient(circle at top right, rgba(99, 102, 241, 0.08), transparent 55%),
    rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(99, 102, 241, 0.12);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
  padding: 0 24px;
}

.page-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-meta h1 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
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
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-weight: 600;
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

