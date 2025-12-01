<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Setting, Message, Cpu } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/configs')) return '/configs'
  if (route.path.startsWith('/messages')) return '/messages'
  if (route.path.startsWith('/agents')) return '/agents'
  return route.path
})

const pageTitle = computed(() => route.meta.title ?? '控制台')

const handleLogout = () => {
  authStore.logout()
  router.replace({ name: 'Login' })
}
</script>

<template>
  <el-container class="layout-shell">
    <el-aside width="228px" class="layout-aside">
      <div class="logo">
        <span>Productivity Hub</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="menu"
        background-color="transparent"
        text-color="#475569"
        active-text-color="#312e81"
      >
        <el-menu-item index="/configs">
          <el-icon><Setting /></el-icon>
          <span>全局参数</span>
        </el-menu-item>
        <el-menu-item index="/messages">
          <el-icon><Message /></el-icon>
          <span>消息推送</span>
        </el-menu-item>
        <el-menu-item index="/agents">
          <el-icon><Cpu /></el-icon>
          <span>智能体调用</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="page-meta">
          <h1>{{ pageTitle }}</h1>
          <p class="route-path">{{ route.fullPath }}</p>
        </div>
        <div class="header-actions">
          <div class="user-meta">
            <span class="user-name">{{ authStore.user?.name ?? '访客' }}</span>
            <small>{{ authStore.user?.roles?.join(', ') }}</small>
          </div>
          <el-button type="primary" plain size="small" @click="handleLogout">退出</el-button>
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
  background: linear-gradient(180deg, #f7f9fc 0%, #ebf1ff 100%);
  color: #111827;
}

.layout-aside {
  background: linear-gradient(180deg, #eef2ff 0%, #e0e7ff 100%);
  border-right: 1px solid rgba(99, 102, 241, 0.2);
  display: flex;
  flex-direction: column;
}

.logo {
  color: #312e81;
  font-weight: 700;
  font-size: 20px;
  padding: 28px 24px 20px;
  letter-spacing: 0.3px;
}

.menu {
  border-right: none;
  flex: 1;
}

.menu :deep(.el-menu-item) {
  border-radius: 12px;
  margin: 4px 12px;
}

.menu :deep(.is-active) {
  background: rgba(99, 102, 241, 0.1);
  color: #312e81;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(99, 102, 241, 0.12);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.page-meta h1 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.route-path {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.user-meta {
  text-align: right;
  line-height: 1.2;
}

.user-name {
  font-weight: 600;
  display: block;
}

.layout-main {
  padding: 24px;
  background: transparent;
}
</style>

