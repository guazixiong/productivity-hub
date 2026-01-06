import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useAuthStore } from './auth'
import { notificationApi } from '@/services/api'

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
const POLL_INTERVAL = 60000 // 60秒轮询一次
const FETCH_PAGE_SIZE = 30 // 每次拉取的消息数量

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
  const pollTimer = ref<number | null>(null)
  const notifications = ref<NotificationItem[]>([])
  const lastError = ref<string | null>(null)
  const fetching = ref(false)

  const unreadCount = computed(() => notifications.value.filter((item) => !item.read).length)

  const reset = () => {
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value)
      reconnectTimer.value = null
    }
    if (pollTimer.value) {
      clearInterval(pollTimer.value)
      pollTimer.value = null
    }
    if (socket.value) {
      socket.value.close()
    }
    socket.value = null
    connected.value = false
    connecting.value = false
    notifications.value = []
    lastError.value = null
    fetching.value = false
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
    // 检查是否已存在相同 ID 的通知
    const existingIndex = notifications.value.findIndex((item) => item.id === payload.id)
    if (existingIndex >= 0) {
      // 如果已存在，更新它（保留已读状态）
      const existing = notifications.value[existingIndex]
      if (existing) {
        notifications.value[existingIndex] = {
          ...payload,
          read: existing.read || payload.read,
        }
      }
    } else {
      // 如果不存在，添加到列表开头
      const list = [payload, ...notifications.value]
      if (list.length > MAX_NOTIFICATIONS) {
        list.length = MAX_NOTIFICATIONS
      }
      notifications.value = list
    }
  }

  // 从服务器拉取消息
  const fetchNotifications = async () => {
    if (fetching.value || !authStore.isAuthenticated) {
      return
    }
    try {
      fetching.value = true
      const result = await notificationApi.list({ page: 1, pageSize: FETCH_PAGE_SIZE })
      if (result && result.items) {
        // 将服务器返回的消息转换为本地格式
        const serverNotifications: NotificationItem[] = result.items.map((item) => ({
          id: item.id,
          title: item.title,
          content: item.content,
          link: item.link,
          data: item.extra || {},
          receivedAt: item.createdAt ? new Date(item.createdAt).getTime() : Date.now(),
          read: item.read || false,
        }))

        // 合并服务器消息到本地列表（保留已读状态）
        const notificationMap = new Map<string, NotificationItem>()
        // 先添加服务器消息
        serverNotifications.forEach((item) => {
          notificationMap.set(item.id, item)
        })
        // 再添加本地消息（如果服务器没有）
        notifications.value.forEach((item) => {
          if (!notificationMap.has(item.id)) {
            notificationMap.set(item.id, item)
          }
        })

        // 按时间倒序排序
        notifications.value = Array.from(notificationMap.values()).sort(
          (a, b) => b.receivedAt - a.receivedAt,
        )

        // 限制最大数量
        if (notifications.value.length > MAX_NOTIFICATIONS) {
          notifications.value = notifications.value.slice(0, MAX_NOTIFICATIONS)
        }
      }
    } catch (e) {
      // 忽略拉取消息失败
    } finally {
      fetching.value = false
    }
  }

  // 启动定期轮询
  const startPolling = () => {
    if (pollTimer.value) {
      return
    }
    pollTimer.value = window.setInterval(() => {
      fetchNotifications()
    }, POLL_INTERVAL)
  }

  // 停止定期轮询
  const stopPolling = () => {
    if (pollTimer.value) {
      clearInterval(pollTimer.value)
      pollTimer.value = null
    }
  }

  const handleIncoming = (raw: string) => {
    try {
      const parsed = safeParse<Record<string, any>>(raw)
      const dto = parsed && typeof parsed === 'object' ? parsed : null

      const nested = dto?.message && typeof dto.message === 'string' ? safeParse<Record<string, any>>(dto.message) : null

      // 优先使用服务器返回的通知 ID
      const notificationId = nested?.id || dto?.id || buildId()
      const title = nested?.title || dto?.title || '系统通知'
      const content = nested?.content || dto?.message || raw
      const link = nested?.path || dto?.summaryMessage?.path || dto?.path

      addNotification({
        id: notificationId,
        title,
        content,
        link,
        data: nested || dto || { raw },
        receivedAt: Date.now(),
        read: false,
      })
    } catch (e) {
      // 消息处理失败不影响连接
    }
  }

  const scheduleReconnect = () => {
    try {
      if (reconnectTimer.value || !authStore.isAuthenticated) return
      reconnectTimer.value = window.setTimeout(() => {
        reconnectTimer.value = null
        connect(true)
      }, RECONNECT_DELAY)
    } catch (e) {
      // 重连调度失败不影响用户正常使用
    }
  }

  const connect = (forceReconnect = false) => {
    try {
      if (!authStore.isAuthenticated || !authStore.user?.id) {
        return
      }
      if (connecting.value) {
        return
      }

      if (socket.value && connected.value && !forceReconnect) {
        return
      }

      if (socket.value && forceReconnect) {
        try {
          socket.value.close()
        } catch (e) {
          // 忽略关闭时的错误
        }
        socket.value = null
      }

      // 优先使用环境变量配置的 API 基础地址
      // 如果没有配置，则使用当前页面的 host（开发环境通过 Vite 代理）
      let wsHost: string
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
      
      if (apiBaseUrl) {
        // 从 API 基础地址提取 host
        try {
          const urlObj = new URL(apiBaseUrl)
          wsHost = urlObj.host
        } catch (e) {
          // 如果解析失败，使用 window.location.host
          wsHost = window.location.host
        }
      } else {
        // 开发环境：使用当前 host（通过 Vite 代理）
        wsHost = window.location.host
      }
      
      // 根据 API 基础地址的协议确定 WebSocket 协议
      const wsProtocol = apiBaseUrl && apiBaseUrl.startsWith('https') ? 'wss' : 'ws'
      const url = `${wsProtocol}://${wsHost}/socketServer/${CLIENT}/${encodeURIComponent(authStore.user.id)}`

      let ws: WebSocket
      try {
        ws = new WebSocket(url)
      } catch (e) {
        // WebSocket 创建失败，不影响用户正常使用
        connecting.value = false
        connected.value = false
        lastError.value = '连接失败，稍后自动重试'
        scheduleReconnect()
        return
      }

      socket.value = ws
      connecting.value = true
      lastError.value = null

      ws.onopen = () => {
        connecting.value = false
        connected.value = true
        lastError.value = null
        // 连接成功后，先拉取一次消息列表
        fetchNotifications()
        // 启动定期轮询
        startPolling()
      }

      ws.onmessage = (event: MessageEvent<string>) => {
        try {
          handleIncoming(event.data)
        } catch (e) {
          // 消息处理失败不影响连接
        }
      }

      ws.onerror = (error) => {
        // 连接错误，不影响用户正常使用
        lastError.value = '连接异常，稍后自动重试'
        connecting.value = false
      }

      ws.onclose = (event) => {
        connected.value = false
        connecting.value = false
        // 停止轮询
        stopPolling()
        // 只有在非正常关闭时才重连（code 1000 是正常关闭）
        if (event.code !== 1000) {
          scheduleReconnect()
        }
      }
    } catch (e) {
      // 捕获所有未预期的错误，确保不影响用户正常使用
      connecting.value = false
      connected.value = false
      lastError.value = '连接失败，稍后自动重试'
      scheduleReconnect()
    }
  }

  return {
    notifications,
    unreadCount,
    connected,
    connecting,
    lastError,
    fetching,
    connect,
    reset,
    markAllRead,
    markRead,
    fetchNotifications,
    startPolling,
    stopPolling,
  }
})

