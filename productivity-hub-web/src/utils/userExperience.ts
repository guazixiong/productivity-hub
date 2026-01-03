/**
 * 用户体验优化工具
 * 提供成功反馈、交互反馈等功能
 */

import { ElMessage, ElNotification } from 'element-plus'
import type { ElMessageOptions } from 'element-plus'

/**
 * 成功反馈类型
 */
export type SuccessFeedbackType = 'toast' | 'notification' | 'message'

/**
 * 显示成功反馈
 */
export function showSuccessFeedback(
  message: string,
  type: SuccessFeedbackType = 'toast',
  options?: Partial<ElMessageOptions>
) {
  switch (type) {
    case 'toast':
      ElMessage.success({
        message,
        duration: 2000,
        ...options,
      })
      break
    case 'notification':
      ElNotification.success({
        title: '成功',
        message,
        duration: 3000,
        ...options,
      })
      break
    case 'message':
      ElMessage.success({
        message,
        duration: 2000,
        ...options,
      })
      break
  }
}

/**
 * 显示错误反馈
 */
export function showErrorFeedback(
  message: string,
  type: SuccessFeedbackType = 'toast',
  options?: Partial<ElMessageOptions>
) {
  switch (type) {
    case 'toast':
      ElMessage.error({
        message,
        duration: 3000,
        ...options,
      })
      break
    case 'notification':
      ElNotification.error({
        title: '错误',
        message,
        duration: 4000,
        ...options,
      })
      break
    case 'message':
      ElMessage.error({
        message,
        duration: 3000,
        ...options,
      })
      break
  }
}

/**
 * 显示警告反馈
 */
export function showWarningFeedback(
  message: string,
  type: SuccessFeedbackType = 'toast',
  options?: Partial<ElMessageOptions>
) {
  switch (type) {
    case 'toast':
      ElMessage.warning({
        message,
        duration: 2500,
        ...options,
      })
      break
    case 'notification':
      ElNotification.warning({
        title: '警告',
        message,
        duration: 3000,
        ...options,
      })
      break
    case 'message':
      ElMessage.warning({
        message,
        duration: 2500,
        ...options,
      })
      break
  }
}

/**
 * 显示信息反馈
 */
export function showInfoFeedback(
  message: string,
  type: SuccessFeedbackType = 'toast',
  options?: Partial<ElMessageOptions>
) {
  switch (type) {
    case 'toast':
      ElMessage.info({
        message,
        duration: 2000,
        ...options,
      })
      break
    case 'notification':
      ElNotification.info({
        title: '提示',
        message,
        duration: 3000,
        ...options,
      })
      break
    case 'message':
      ElMessage.info({
        message,
        duration: 2000,
        ...options,
      })
      break
  }
}

/**
 * 按钮点击反馈（视觉反馈）
 */
export function addButtonClickFeedback(element: HTMLElement) {
  element.classList.add('button-click-feedback')
  setTimeout(() => {
    element.classList.remove('button-click-feedback')
  }, 200)
}

/**
 * 表单验证反馈样式类
 */
export const FORM_VALIDATION_CLASSES = {
  error: 'form-field-error',
  success: 'form-field-success',
  warning: 'form-field-warning',
} as const

/**
 * 添加表单字段验证反馈
 */
export function addFormFieldFeedback(
  element: HTMLElement,
  type: 'error' | 'success' | 'warning'
) {
  // 移除所有反馈类
  Object.values(FORM_VALIDATION_CLASSES).forEach((cls) => {
    element.classList.remove(cls)
  })
  // 添加新的反馈类
  element.classList.add(FORM_VALIDATION_CLASSES[type])
}

/**
 * 移除表单字段验证反馈
 */
export function removeFormFieldFeedback(element: HTMLElement) {
  Object.values(FORM_VALIDATION_CLASSES).forEach((cls) => {
    element.classList.remove(cls)
  })
}

/**
 * 操作成功后的反馈（带页面跳转）
 */
export function handleSuccessWithRedirect(
  message: string,
  redirectPath: string,
  router: any
) {
  showSuccessFeedback(message, 'toast')
  setTimeout(() => {
    router.push(redirectPath)
  }, 1500)
}

/**
 * 操作成功后的反馈（带状态更新）
 */
export function handleSuccessWithRefresh(
  message: string,
  refreshFn: () => void | Promise<void>
) {
  showSuccessFeedback(message, 'toast')
  setTimeout(() => {
    refreshFn()
  }, 1000)
}

