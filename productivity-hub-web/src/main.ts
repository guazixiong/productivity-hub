import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import './styles/responsive.scss'
import './styles/user-experience.scss'
import './styles/compatibility.scss'
import App from './App.vue'
import router from './router'
import { setupButtonLockDirective } from './directives/buttonLock'
import { initErrorHandler, handleVueError } from './utils/errorHandler'
import { initPerformanceMonitor } from './utils/performance'
import { checkBrowserCompatibility } from './utils/compatibility'

// 初始化全局错误处理
initErrorHandler({
  enableConsole: true,
  enableReport: false,
  maxErrors: 100,
})

// 初始化性能监控 - REQ-001, TASK-REQ-001-29
initPerformanceMonitor()

// 检查浏览器兼容性 - TASK-FRONTEND-22
if (import.meta.env.MODE === 'development') {
  checkBrowserCompatibility()
}

const app = createApp(App)

// Vue错误处理 - REQ-001, TASK-REQ-001-28
app.config.errorHandler = handleVueError

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
setupButtonLockDirective(app)

app.mount('#app')
