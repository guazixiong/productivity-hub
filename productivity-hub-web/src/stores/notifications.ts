import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useAuthStore } from './auth'

export interface NotificationItem {
  id: string
  title: string
  content: string
  link?: string
  data?: Record<string, unknown>
  receivedAt: number
  read: boolean
}

const CLIENT = 'web'
const RECONNECT_DELAY = 5000
const MAX_NOTIFICATIONS = 30

const safeParse = <T = Record<string, unknown>>(payload: string): T | null => {
  try {
    return JSON.parse(payload) as T
  } catch {
    return null
  }
}

const buildId = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

export const useNotificationStore = defineStore('notifications', () => {
  const authStore = useAuthStore()

  const socket = ref<WebSocket | null>(null)
  const connecting = ref(false)
  const connected = ref(false)
  const reconnectTimer = ref<number | null>(null)
  const notifications = ref<NotificationItem[]>([])
  const lastError = ref<string | null>(null)

  const unreadCount = computed(() => notifications.value.filter((item) => !item.read).length)

  const reset = () => {
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value)
      reconnectTimer.value = null
    }
    if (socket.value) {
      socket.value.close()
    }
    socket.value = null
    connected.value = false
    connecting.value = false
    notifications.value = []
    lastError.value = null
  }

  const markAllRead = () => {
    notifications.value = notifications.value.map((item) => ({ ...item, read: true }))
  }

  const markRead = (id: string) => {
    notifications.value = notifications.value.map((item) =>
      item.id === id ? { ...item, read: true } : item,
    )
  }

  const addNotification = (payload: NotificationItem) => {
    const list = [payload, ...notifications.value]
    if (list.length > MAX_NOTIFICATIONS) {
      list.length = MAX_NOTIFICATIONS
    }
    notifications.value = list
  }

  const handleIncoming = (raw: string) => {
    const parsed = safeParse<Record<string, any>>(raw)
    const dto = parsed && typeof parsed === 'object' ? parsed : null

    const nested = dto?.message && typeof dto.message === 'string' ? safeParse<Record<string, any>>(dto.message) : null

    const title = nested?.title || dto?.title || '系统通知'
    const content = nested?.content || dto?.message || raw
    const link = nested?.path || dto?.summaryMessage?.path

    addNotification({
      id: buildId(),
      title,
      content,
      link,
      data: nested || dto || { raw },
      receivedAt: Date.now(),
      read: false,
    })
  }

  const scheduleReconnect = () => {
    if (reconnectTimer.value || !authStore.isAuthenticated) return
    reconnectTimer.value = window.setTimeout(() => {
      reconnectTimer.value = null
      connect(true)
    }, RECONNECT_DELAY)
  }

  const connect = (forceReconnect = false) => {
    if (!authStore.isAuthenticated || !authStore.user?.id) return
    if (connecting.value) return

    if (socket.value && connected.value && !forceReconnect) {
      return
    }

    if (socket.value && forceReconnect) {
      socket.value.close()
      socket.value = null
    }

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    const url = `${protocol}://${window.location.host}/socketServer/${CLIENT}/${encodeURIComponent(authStore.user.id)}`

    const ws = new WebSocket(url)
    socket.value = ws
    connecting.value = true
    lastError.value = null

    ws.onopen = () => {
      connecting.value = false
      connected.value = true
    }

    ws.onmessage = (event: MessageEvent<string>) => {
      handleIncoming(event.data)
    }

    ws.onerror = () => {
      lastError.value = 'WebSocket 连接异常'
    }

    ws.onclose = () => {
      connected.value = false
      connecting.value = false
      scheduleReconnect()
    }
  }

  return {
    notifications,
    unreadCount,
    connected,
    lastError,
    connect,
    reset,
    markAllRead,
    markRead,
  }
})

