import { ref, type Ref } from 'vue'

/**
 * 按钮防重复点击 composable
 * 用于在接口请求完成前禁止重复点击按钮
 */
export function useButtonLock() {
  const isLocked: Ref<boolean> = ref(false)

  /**
   * 执行带锁定的异步操作
   * @param asyncFn 异步函数
   * @returns Promise
   */
  const withLock = async <T>(asyncFn: () => Promise<T>): Promise<T> => {
    if (isLocked.value) {
      return Promise.reject(new Error('操作正在进行中，请勿重复点击'))
    }

    isLocked.value = true
    try {
      const result = await asyncFn()
      return result
    } finally {
      isLocked.value = false
    }
  }

  /**
   * 手动锁定
   */
  const lock = () => {
    isLocked.value = true
  }

  /**
   * 手动解锁
   */
  const unlock = () => {
    isLocked.value = false
  }

  return {
    isLocked,
    withLock,
    lock,
    unlock,
  }
}

