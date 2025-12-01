import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    meta: { public: true },
    component: () => import('@/views/auth/LoginView.vue'),
  },
  {
    path: '/',
    component: () => import('@/layouts/DashboardLayout.vue'),
    children: [
      { path: '', redirect: '/configs' },
      {
        path: 'configs',
        name: 'ConfigCenter',
        meta: { title: '全局参数配置' },
        component: () => import('@/views/config/ConfigView.vue'),
      },
      {
        path: 'messages',
        name: 'MessageHub',
        meta: { title: '消息推送' },
        component: () => import('@/views/messages/MessagesView.vue'),
      },
      {
        path: 'agents',
        name: 'AgentOps',
        meta: { title: '智能体调用' },
        component: () => import('@/views/agents/AgentsView.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    meta: { public: true },
    component: () => import('@/views/misc/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    if (to.name === 'Login' && authStore.isAuthenticated) {
      next((to.query.redirect as string) || '/configs')
      return
    }
    next()
    return
  }

  if (!authStore.isHydrated) {
    authStore.hydrateFromCache()
  }

  if (!authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  next()
})

export default router

