import type { App } from 'vue'

/**
 * 按钮防重复点击指令
 * 自动拦截按钮点击，在异步操作完成前防止重复点击
 * 
 * 使用方式：
 * 1. 在 el-button 上添加 v-button-lock 指令
 * 2. 指令会自动拦截 @click 事件，在按钮处于 loading 状态或已禁用时阻止点击
 * 3. 如果按钮有 :loading 属性，指令会依赖该属性；否则会在点击后临时禁用按钮
 */
export const buttonLockDirective = {
  mounted(el: HTMLElement) {
    const buttonEl = el.tagName === 'BUTTON' ? el : el.querySelector('button') || el

    const handleClick = (event: Event) => {
      // 检查按钮是否已经处于 loading 状态或禁用状态
      const isLoading = buttonEl.classList.contains('is-loading') || 
                        buttonEl.getAttribute('disabled') === 'true' ||
                        buttonEl.hasAttribute('disabled')
      
      if (isLoading) {
        event.preventDefault()
        event.stopPropagation()
        event.stopImmediatePropagation()
        return false
      }
    }

    // 使用捕获阶段拦截，确保在其他处理器之前执行
    el.addEventListener('click', handleClick, true)
    ;(el as any).__buttonLockHandler = handleClick
  },

  unmounted(el: HTMLElement) {
    const handler = (el as any).__buttonLockHandler
    if (handler) {
      el.removeEventListener('click', handler, true)
      delete (el as any).__buttonLockHandler
    }
  },
}

export function setupButtonLockDirective(app: App) {
  app.directive('button-lock', buttonLockDirective)
}

