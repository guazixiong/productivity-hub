import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNavigationStore } from '@/stores/navigation'

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
      { path: '', redirect: '/home' },
      {
        path: 'home',
        name: 'Home',
        meta: { title: '首页' },
        component: () => import('@/views/home/HomeView.vue'),
      },
      {
        path: 'hot-sections',
        name: 'HotSections',
        meta: { title: '热点速览' },
        component: () => import('@/views/hot-sections/HotSectionsView.vue'),
      },
      {
        path: 'config',
        name: 'Config',
        meta: { title: '全局参数配置' },
        component: () => import('@/views/config/ConfigView.vue'),
      },
      {
        path: 'settings',
        name: 'Settings',
        meta: { title: '设置' },
        component: () => import('@/views/settings/SettingsView.vue'),
      },
      {
        path: 'tools',
        name: 'Tools',
        meta: { title: '工具箱' },
        component: () => import('@/views/tools/ToolsView.vue'),
      },
      {
        path: 'tools/blueprint',
        name: 'BlueprintRoadmap',
        meta: { title: 'AI成长蓝图' },
        component: () => import('@/views/tools/BlueprintRoadmapView.vue'),
      },
      {
        path: 'tools/git-toolbox',
        name: 'GitToolbox',
        meta: { title: 'Git工具箱速查' },
        component: () => import('@/views/tools/GitToolboxView.vue'),
      },
      {
        path: 'tools/food',
        name: 'FoodWheel',
        meta: { title: '今天吃什么' },
        component: () => import('@/views/tools/FoodWheelView.vue'),
      },
      {
        path: 'tools/json',
        name: 'JsonFormatter',
        meta: { title: 'JSON格式化' },
        component: () => import('@/views/tools/JsonFormatterView.vue'),
      },
      {
        path: 'tools/time',
        name: 'TimeConverter',
        meta: { title: '时间转换' },
        component: () => import('@/views/tools/TimeConverterView.vue'),
      },
      {
        path: 'tools/clock',
        name: 'ScreenClock',
        meta: { title: '屏幕时钟' },
        component: () => import('@/views/tools/ScreenClockView.vue'),
      },
      {
        path: 'tools/password',
        name: 'PasswordGenerator',
        meta: { title: '密码生成器' },
        component: () => import('@/views/tools/PasswordGeneratorView.vue'),
      },
      {
        path: 'tools/cron',
        name: 'CronHelper',
        meta: { title: 'Cron表达式' },
        component: () => import('@/views/tools/CronExpressionView.vue'),
      },
      {
        path: 'tools/regex',
        name: 'RegexTester',
        meta: { title: '正则表达式' },
        component: () => import('@/views/tools/RegexTesterView.vue'),
      },
      {
        path: 'tools/yaml',
        name: 'YamlValidator',
        meta: { title: 'YAML格式检查' },
        component: () => import('@/views/tools/YamlValidatorView.vue'),
      },
      {
        path: 'tools/workday',
        name: 'WorkdayCalculator',
        meta: { title: '工作日计算' },
        component: () => import('@/views/tools/WorkdayCalculatorView.vue'),
      },
      {
        path: 'tools/cursor-shop',
        name: 'CursorShopInventory',
        meta: { title: 'Cursor小店库存' },
        component: () => import('@/views/tools/CursorShopInventoryView.vue'),
      },
      {
        path: 'tools/image-base64',
        name: 'ImageBase64',
        meta: { title: '图片Base64编码' },
        component: () => import('@/views/tools/ImageBase64View.vue'),
      },
      {
        path: 'tools/sql-formatter',
        name: 'SqlFormatter',
        meta: { title: 'SQL格式化' },
        component: () => import('@/views/tools/SqlFormatterView.vue'),
      },
      {
        path: 'tools/unit-converter',
        name: 'UnitConverter',
        meta: { title: '单位换算' },
        component: () => import('@/views/tools/UnitConverterView.vue'),
      },
      {
        path: 'code-generator',
        name: 'CodeGenerator',
        meta: { title: '低代码生成' },
        component: () => import('@/views/tools/CodeGeneratorView.vue'),
      },
      {
        path: 'messages',
        name: 'Messages',
        meta: { title: '消息推送' },
        component: () => import('@/views/messages/MessagesView.vue'),
      },
      {
        path: 'messages/history',
        redirect: (to) => ({
          path: '/messages',
          query: { ...to.query, tab: 'history' },
        }),
      },
      {
        path: 'messages/composer/:channel',
        redirect: (to) => ({
          path: '/messages',
          query: {
            ...to.query,
            channel: Array.isArray(to.params.channel) ? to.params.channel[0] : (to.params.channel as string),
          },
        }),
      },
      {
        path: 'agents',
        name: 'AgentOps',
        meta: { title: '智能体调用' },
        component: () => import('@/views/agents/AgentsView.vue'),
      },
      {
        path: 'reset-password',
        name: 'ResetPassword',
        meta: { title: '重置密码' },
        component: () => import('@/views/auth/ResetPasswordView.vue'),
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

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const navigationStore = useNavigationStore()

  if (to.meta.public) {
    if (to.name === 'Login' && authStore.isAuthenticated) {
      next((to.query.redirect as string) || '/home')
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

  // 记录导航历史（记录来源页面）
  if (from.path && to.path) {
    navigationStore.recordNavigation(to.path, from.path)
  }

  next()
})

export default router

