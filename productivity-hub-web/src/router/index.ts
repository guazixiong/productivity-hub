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
        path: 'todo',
        name: 'Todo',
        meta: { title: '行为级Todo' },
        component: () => import('@/views/todo/TodoView.vue'),
      },
      {
        path: 'todo/dashboard',
        name: 'TodoDashboardPage',
        meta: { title: 'TODO任务大屏' },
        component: () => import('@/views/todo/TodoDashboardPage.vue'),
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
        path: 'settings/users',
        name: 'UserManagement',
        meta: { title: '系统用户管理', requiresAdmin: true },
        component: () => import('@/views/settings/UserManagementView.vue'),
      },
      {
        path: 'settings/schedules',
        name: 'ScheduleTasks',
        meta: { title: '定时任务管理' },
        component: () => import('@/views/settings/ScheduleTasksView.vue'),
      },
      {
        path: 'settings/announcements',
        name: 'AnnouncementManage',
        meta: { title: '公告管理', requiresAdmin: true },
        component: () => import('@/views/settings/AnnouncementManageView.vue'),
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
        path: 'tools/cursor-inventory',
        name: 'CursorInventory',
        meta: { title: 'Cursor库存' },
        component: () => import('@/views/tools/CursorInventoryView.vue'),
      },
      {
        path: 'tools/cursor-shop',
        redirect: '/tools/cursor-inventory',
      },
      {
        path: 'tools/ldxp-cursor-shop',
        redirect: '/tools/cursor-inventory',
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
        path: 'tools/worth-buying',
        name: 'WorthBuying',
        meta: { title: '值不值得买' },
        component: () => import('@/views/tools/WorthBuyingView.vue'),
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
        path: 'ai/prompt',
        name: 'Prompt',
        meta: { title: 'Prompt' },
        component: () => import('@/views/ai/PromptView.vue'),
      },
      {
        path: 'ai/knowledge-base',
        name: 'KnowledgeBase',
        meta: { title: '知识库' },
        component: () => import('@/views/ai/KnowledgeBaseView.vue'),
      },
      {
        path: 'ai/image-generation',
        name: 'ImageGeneration',
        meta: { title: 'AI生图' },
        component: () => import('@/views/ai/ImageGenerationView.vue'),
      },
      {
        path: 'ai/statistics',
        name: 'AIStatistics',
        meta: { title: 'AI统计报表' },
        component: () => import('@/views/ai/StatisticsView.vue'),
      },
      {
        path: 'ai/dify-assistant',
        name: 'DifyAssistant',
        meta: { title: 'Dify助手' },
        component: () => import('@/views/ai/DifyAssistantView.vue'),
      },
      {
        path: 'ai/assistant',
        name: 'AIAssistant',
        meta: { title: 'AI助手' },
        component: () => import('@/views/ai/AssistantView.vue'),
      },
      {
        path: 'bookmark',
        name: 'Bookmark',
        meta: { title: '宝藏类网址' },
        component: () => import('@/views/bookmark/BookmarkDisplayView.vue'),
      },
      {
        path: 'bookmark/manage',
        name: 'BookmarkManage',
        meta: { title: '数据维护' },
        component: () => import('@/views/bookmark/BookmarkManageView.vue'),
      },
      {
        path: 'reset-password',
        name: 'ResetPassword',
        meta: { title: '重置密码' },
        component: () => import('@/views/auth/ResetPasswordView.vue'),
      },
      {
        path: 'profile',
        name: 'UserProfile',
        meta: { title: '我的信息' },
        component: () => import('@/views/auth/UserProfileView.vue'),
      },
      {
        path: 'image',
        name: 'Image',
        meta: { title: '图片管理' },
        component: () => import('@/views/image/ImageView.vue'),
      },
      {
        path: 'health',
        name: 'Health',
        meta: { title: '健康状况' },
        component: () => import('@/views/health/HealthView.vue'),
      },
      {
        path: 'health/statistics',
        name: 'HealthStatistics',
        meta: { title: '健康统计' },
        component: () => import('@/views/health/HealthStatisticsView.vue'),
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

  // 权限校验：仅管理员可访问 requiresAdmin 路由
  if (to.meta.requiresAdmin) {
    const roles = authStore.user?.roles || []
    if (!roles.includes('admin')) {
      next({ name: 'NotFound' })
      return
    }
  }

  // 记录导航历史（记录来源页面）
  if (from.path && to.path) {
    navigationStore.recordNavigation(to.path, from.path)
  }

  next()
})

export default router

