<script setup lang="ts">
import { computed, watch, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElDialog, ElButton, ElTag, ElMessage } from 'element-plus'
import { Close, Bell, ArrowRight } from '@element-plus/icons-vue'
import type { NotificationItem } from '@/stores/notifications'
import { notificationApi } from '@/services/api'

const router = useRouter()

interface Props {
  modelValue: boolean
  notification: NotificationItem | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'read', notificationId: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const markingRead = ref(false)

const handleClose = () => {
  visible.value = false
}

// 格式化日期时间
const formatDateTime = (timestamp: number): string => {
  try {
    const date = new Date(timestamp)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch {
    return ''
  }
}

// 格式化相对时间（如：2小时前、3天前）
const formatRelativeTime = (timestamp: number): string => {
  try {
    const date = new Date(timestamp)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / (1000 * 60))
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
    
    if (diffMins < 1) return '刚刚'
    if (diffMins < 60) return `${diffMins}分钟前`
    if (diffHours < 24) return `${diffHours}小时前`
    if (diffDays < 7) return `${diffDays}天前`
    return formatDateTime(timestamp)
  } catch {
    return formatDateTime(timestamp)
  }
}

// 提取链接的友好显示文本
const getLinkText = (link?: string): string => {
  if (!link) return ''
  try {
    // 如果是相对路径，直接返回
    if (link.startsWith('/')) {
      return link.length > 50 ? link.substring(0, 50) + '...' : link
    }
    const url = new URL(link)
    return url.hostname + url.pathname
  } catch {
    // 如果不是完整URL，返回原文本（可能是相对路径）
    return link.length > 50 ? link.substring(0, 50) + '...' : link
  }
}

// 处理链接点击
const handleLinkClick = () => {
  if (props.notification?.link) {
    // 如果是相对路径，使用 router
    if (props.notification.link.startsWith('/')) {
      router.push(props.notification.link)
      // 关闭弹窗
      visible.value = false
    } else {
      window.open(props.notification.link, '_blank', 'noopener,noreferrer')
    }
  }
}

// 当弹窗打开时，如果是未读消息，标记为已读
watch(visible, async (val) => {
  if (val && props.notification && !props.notification.read && !markingRead.value) {
    markingRead.value = true
    try {
      await notificationApi.markRead(props.notification.id)
      emit('read', props.notification.id)
    } catch (error) {
      console.error('标记已读失败:', error)
      ElMessage.error('标记已读失败')
    } finally {
      markingRead.value = false
    }
  }
})
</script>

<template>
  <ElDialog
    v-model="visible"
    title="消息详情"
    width="500px"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    class="notification-detail-dialog"
  >
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <el-icon class="header-icon" :size="20">
            <Bell />
          </el-icon>
          <span class="header-title">消息详情</span>
          <ElTag v-if="notification && !notification.read" type="warning" size="small" class="unread-tag">
            未读
          </ElTag>
        </div>
      </div>
    </template>

    <div v-if="notification" class="dialog-content">
      <div class="notification-meta">
        <span class="meta-time">
          {{ formatRelativeTime(notification.receivedAt) }}
        </span>
        <span class="meta-full-time">
          {{ formatDateTime(notification.receivedAt) }}
        </span>
      </div>
      
      <h3 class="notification-title">{{ notification.title || '无标题' }}</h3>
      
      <div class="notification-body">
        <div class="text-content">
          {{ notification.content }}
        </div>
        
        <div v-if="notification.link" class="notification-link">
          <a
            :href="notification.link"
            target="_blank"
            rel="noopener noreferrer"
            @click.prevent="handleLinkClick"
            class="link-text"
          >
            <span class="link-label">相关链接：</span>
            <span class="link-url">{{ getLinkText(notification.link) }}</span>
            <el-icon><ArrowRight /></el-icon>
          </a>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <ElButton
          type="primary"
          aria-label="关闭"
          @click="handleClose"
        >
          关闭
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<style scoped>
.notification-detail-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.notification-detail-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  color: var(--el-color-primary);
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.unread-tag {
  margin-left: 8px;
}

.dialog-content {
  padding: 24px 20px;
  min-height: 150px;
}

.notification-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.meta-time {
  display: inline-block;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.meta-full-time {
  display: inline-block;
  color: var(--el-text-color-placeholder);
  font-size: 11px;
}

.notification-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.5;
}

.notification-body {
  flex: 1;
}

.text-content {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 16px;
}

.notification-link {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.notification-link a {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--el-color-primary);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.notification-link a:hover {
  color: var(--el-color-primary-light-3);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.link-text {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.link-label {
  color: var(--el-text-color-regular);
  font-weight: 500;
}

.link-url {
  color: var(--el-color-primary);
  word-break: break-all;
}
</style>

