import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { setupButtonLockDirective } from './directives/buttonLock'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
setupButtonLockDirective(app)

app.mount('#app')
