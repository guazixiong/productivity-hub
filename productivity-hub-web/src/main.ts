import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'

if (import.meta.env.DEV) {
  import('../mock').catch((error) => {
    console.error('Failed to bootstrap mock services', error)
  })
}

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
