/**
 * 触摸滑动支持Composable
 * 
 * 提供触摸滑动操作支持，包括上下滑动、左右滑动等
 */

import { ref, onMounted, onBeforeUnmount, type Ref } from 'vue'

/**
 * 滑动方向枚举
 */
export const SwipeDirection = {
  UP: 'up',
  DOWN: 'down',
  LEFT: 'left',
  RIGHT: 'right',
} as const

export type SwipeDirection = typeof SwipeDirection[keyof typeof SwipeDirection]

/**
 * 触摸滑动配置选项
 */
export interface TouchSwipeOptions {
  /** 最小滑动距离（像素），默认10px */
  threshold?: number
  /** 最大滑动时间（毫秒），默认300ms */
  maxTime?: number
  /** 是否阻止默认行为，默认false */
  preventDefault?: boolean
  /** 滑动回调函数 */
  onSwipe?: (direction: SwipeDirection, distance: number) => void
  /** 滑动开始回调函数 */
  onSwipeStart?: (event: TouchEvent) => void
  /** 滑动结束回调函数 */
  onSwipeEnd?: (event: TouchEvent) => void
}

/**
 * 触摸滑动Composable返回值类型
 */
export interface UseTouchSwipeReturn {
  /** 是否正在滑动 */
  isSwiping: Ref<boolean>
  /** 滑动方向 */
  swipeDirection: Ref<SwipeDirection | null>
  /** 滑动距离 */
  swipeDistance: Ref<number>
  /** 开始滑动 */
  startSwipe: (event: TouchEvent) => void
  /** 移动滑动 */
  moveSwipe: (event: TouchEvent) => void
  /** 结束滑动 */
  endSwipe: (event: TouchEvent) => void
}

/**
 * 触摸滑动Composable
 * 
 * @param element 要绑定触摸事件的元素，默认为document
 * @param options 配置选项
 * @returns 触摸滑动相关的响应式状态和方法
 */
export function useTouchSwipe(
  element?: HTMLElement | null,
  options: TouchSwipeOptions = {}
): UseTouchSwipeReturn {
  const {
    threshold = 10,
    maxTime = 300,
    preventDefault = false,
    onSwipe,
    onSwipeStart,
    onSwipeEnd,
  } = options

  const isSwiping = ref(false)
  const swipeDirection = ref<SwipeDirection | null>(null)
  const swipeDistance = ref(0)

  let startX = 0
  let startY = 0
  let startTime = 0
  let currentX = 0
  let currentY = 0

  /**
   * 计算滑动方向
   */
  const calculateDirection = (
    deltaX: number,
    deltaY: number
  ): SwipeDirection | null => {
    const absX = Math.abs(deltaX)
    const absY = Math.abs(deltaY)

    if (absX < threshold && absY < threshold) {
      return null
    }

    if (absX > absY) {
      return deltaX > 0 ? SwipeDirection.RIGHT : SwipeDirection.LEFT
    } else {
      return deltaY > 0 ? SwipeDirection.DOWN : SwipeDirection.UP
    }
  }

  /**
   * 开始滑动
   */
  const startSwipe = (event: TouchEvent) => {
    if (preventDefault) {
      event.preventDefault()
    }

    const touch = event.touches[0]
    if (!touch) return

    startX = touch.clientX
    startY = touch.clientY
    startTime = Date.now()
    currentX = startX
    currentY = startY
    isSwiping.value = true
    swipeDirection.value = null
    swipeDistance.value = 0

    onSwipeStart?.(event)
  }

  /**
   * 移动滑动
   */
  const moveSwipe = (event: TouchEvent) => {
    if (!isSwiping.value) return

    if (preventDefault) {
      event.preventDefault()
    }

    const touch = event.touches[0]
    if (!touch) return

    currentX = touch.clientX
    currentY = touch.clientY

    const deltaX = currentX - startX
    const deltaY = currentY - startY
    const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY)

    swipeDistance.value = distance
    swipeDirection.value = calculateDirection(deltaX, deltaY)
  }

  /**
   * 结束滑动
   */
  const endSwipe = (event: TouchEvent) => {
    if (!isSwiping.value) return

    if (preventDefault) {
      event.preventDefault()
    }

    const endTime = Date.now()
    const timeDiff = endTime - startTime

    const deltaX = currentX - startX
    const deltaY = currentY - startY
    const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY)

    // 检查是否满足滑动条件
    if (distance >= threshold && timeDiff <= maxTime && swipeDirection.value) {
      onSwipe?.(swipeDirection.value, distance)
    }

    // 重置状态
    isSwiping.value = false
    swipeDirection.value = null
    swipeDistance.value = 0

    onSwipeEnd?.(event)
  }

  // 绑定触摸事件
  onMounted(() => {
    const target = element || document.documentElement

    target.addEventListener('touchstart', startSwipe, { passive: !preventDefault })
    target.addEventListener('touchmove', moveSwipe, { passive: !preventDefault })
    target.addEventListener('touchend', endSwipe, { passive: !preventDefault })
    target.addEventListener('touchcancel', endSwipe, { passive: !preventDefault })
  })

  // 解绑触摸事件
  onBeforeUnmount(() => {
    const target = element || document.documentElement

    target.removeEventListener('touchstart', startSwipe)
    target.removeEventListener('touchmove', moveSwipe)
    target.removeEventListener('touchend', endSwipe)
    target.removeEventListener('touchcancel', endSwipe)
  })

  return {
    isSwiping,
    swipeDirection,
    swipeDistance,
    startSwipe,
    moveSwipe,
    endSwipe,
  }
}

