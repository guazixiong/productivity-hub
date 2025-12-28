<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElDialog, ElButton, ElTag, ElMessage, ElEmpty } from 'element-plus'
import { Close, Bell, Warning, InfoFilled, CircleCheck, ArrowRight, Loading } from '@element-plus/icons-vue'
import type { Announcement } from '@/types/announcement'
import { announcementApi } from '@/services/announcementApi'

interface Props {
  modelValue: boolean
  announcements: Announcement[]
  loading?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'read', announcementId: string): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<Emits>()

const currentIndex = ref(0)
const markingRead = ref(false)

// 对公告列表按优先级排序（优先级高的在前，相同优先级按创建时间倒序）
const sortedAnnouncements = computed(() => {
  return [...props.announcements].sort((a, b) => {
    if (a.priority !== b.priority) {
      return b.priority - a.priority // 优先级高的在前
    }
    // 相同优先级按创建时间倒序
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })
})

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const currentAnnouncement = computed(() => {
  return sortedAnnouncements.value[currentIndex.value]
})

const hasMultiple = computed(() => sortedAnnouncements.value.length > 1)

const isEmpty = computed(() => sortedAnnouncements.value.length === 0)

const typeConfig = computed(() => {
  const type = currentAnnouncement.value?.type || 'NORMAL'
  const configs = {
    URGENT: { label: '紧急', color: 'danger', icon: Warning },
    WARNING: { label: '警告', color: 'warning', icon: Warning },
    INFO: { label: '信息', color: 'info', icon: InfoFilled },
    NORMAL: { label: '普通', color: '', icon: Bell },
  }
  return configs[type] || configs.NORMAL
})

const handleClose = () => {
  visible.value = false
}

const handleNext = () => {
  if (currentIndex.value < sortedAnnouncements.value.length - 1) {
    currentIndex.value++
  }
}

const handlePrev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const handleMarkRead = async () => {
  if (!currentAnnouncement.value || markingRead.value) return
  
  markingRead.value = true
  try {
    await announcementApi.markRead(currentAnnouncement.value.id)
    emit('read', currentAnnouncement.value.id)
    
    // 如果还有下一个公告，切换到下一个
    if (currentIndex.value < sortedAnnouncements.value.length - 1) {
      currentIndex.value++
    } else {
      // 如果没有更多公告，关闭弹窗
      visible.value = false
    }
  } catch (error) {
    ElMessage.error('标记已读失败')
  } finally {
    markingRead.value = false
  }
}

const handleConfirm = async () => {
  if (currentAnnouncement.value?.requireConfirm === 1) {
    await handleMarkRead()
  } else {
    handleClose()
  }
}

// 当弹窗打开时，重置到第一个公告
watch(visible, (val) => {
  if (val) {
    currentIndex.value = 0
  }
})

// 当公告列表变化时，确保索引有效
watch(sortedAnnouncements, (newList) => {
  if (currentIndex.value >= newList.length) {
    currentIndex.value = Math.max(0, newList.length - 1)
  }
})

// 格式化日期时间
const formatDateTime = (dateStr?: string): string => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch {
    return dateStr
  }
}

// 格式化相对时间（如：2小时前、3天前）
const formatRelativeTime = (dateStr?: string): string => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / (1000 * 60))
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
    
    if (diffMins < 1) return '刚刚'
    if (diffMins < 60) return `${diffMins}分钟前`
    if (diffHours < 24) return `${diffHours}小时前`
    if (diffDays < 7) return `${diffDays}天前`
    return formatDateTime(dateStr)
  } catch {
    return formatDateTime(dateStr)
  }
}

// 检查公告是否过期
const isExpired = computed(() => {
  if (!currentAnnouncement.value?.expireTime) return false
  try {
    const expireDate = new Date(currentAnnouncement.value.expireTime)
    const now = new Date()
    return expireDate < now
  } catch {
    return false
  }
})

// 检查公告是否在有效期内
const isEffective = computed(() => {
  if (!currentAnnouncement.value) return false
  const now = new Date()
  
  // 检查生效时间
  if (currentAnnouncement.value.effectiveTime) {
    try {
      const effectiveDate = new Date(currentAnnouncement.value.effectiveTime)
      if (effectiveDate > now) return false
    } catch {
      // 忽略解析错误
    }
  }
  
  // 检查过期时间
  if (currentAnnouncement.value.expireTime) {
    try {
      const expireDate = new Date(currentAnnouncement.value.expireTime)
      if (expireDate < now) return false
    } catch {
      // 忽略解析错误
    }
  }
  
  return true
})

// 提取链接的友好显示文本
const getLinkText = (link?: string): string => {
  if (!link) return ''
  try {
    const url = new URL(link)
    return url.hostname + url.pathname
  } catch {
    // 如果不是完整URL，返回原文本（可能是相对路径）
    return link.length > 50 ? link.substring(0, 50) + '...' : link
  }
}

// 键盘快捷键处理
const handleKeydown = (e: KeyboardEvent) => {
  if (!visible.value) return
  
  // ESC键：关闭弹窗（如果不需要确认）
  if (e.key === 'Escape' && currentAnnouncement.value?.requireConfirm !== 1) {
    e.preventDefault()
    handleClose()
    return
  }
  
  // 左箭头：上一条
  if (e.key === 'ArrowLeft' && hasMultiple.value && currentIndex.value > 0) {
    e.preventDefault()
    handlePrev()
    return
  }
  
  // 右箭头：下一条
  if (e.key === 'ArrowRight' && hasMultiple.value && currentIndex.value < sortedAnnouncements.value.length - 1) {
    e.preventDefault()
    handleNext()
    return
  }
}

// 点击链接时标记为已读（如果不需要确认）
const handleLinkClick = () => {
  if (currentAnnouncement.value?.requireConfirm !== 1) {
    handleMarkRead()
  }
}

// 注册键盘事件
onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <ElDialog
    v-model="visible"
    :title="null"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="currentAnnouncement?.requireConfirm !== 1"
    :show-close="false"
    class="announcement-dialog"
    role="dialog"
    :aria-labelledby="currentAnnouncement ? 'announcement-title' : undefined"
    :aria-describedby="currentAnnouncement ? 'announcement-content' : undefined"
  >
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <el-icon class="header-icon" :size="20">
            <component :is="typeConfig.icon" />
          </el-icon>
          <span class="header-title">系统公告</span>
          <ElTag :type="typeConfig.color" size="small" class="type-tag">
            {{ typeConfig.label }}
          </ElTag>
          <ElTag v-if="isExpired" type="info" size="small" class="expired-tag">
            已过期
          </ElTag>
          <span v-if="hasMultiple" class="announcement-counter">
            {{ currentIndex + 1 }} / {{ sortedAnnouncements.length }}
          </span>
        </div>
        <div class="header-right">
          <ElButton
            text
            :icon="Close"
            circle
            size="small"
            aria-label="关闭公告"
            @click="handleClose"
          />
        </div>
      </div>
    </template>

    <div class="dialog-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
      
      <!-- 空状态 -->
      <ElEmpty v-else-if="isEmpty" description="暂无公告" />
      
      <!-- 公告内容 -->
      <transition name="fade-slide" mode="out-in">
        <div v-if="currentAnnouncement" :key="currentAnnouncement.id" class="announcement-content">
          <div class="announcement-meta">
            <span v-if="currentAnnouncement.createdAt" class="meta-time">
              {{ formatRelativeTime(currentAnnouncement.createdAt) }}
            </span>
            <span v-if="currentAnnouncement.expireTime" class="meta-expire">
              有效期至：{{ formatDateTime(currentAnnouncement.expireTime) }}
            </span>
          </div>
          <h3 id="announcement-title" class="announcement-title">{{ currentAnnouncement.title || '无标题' }}</h3>
          
          <div id="announcement-content" class="announcement-body">
          <div
            v-if="currentAnnouncement.richContent"
            class="rich-content"
            v-html="currentAnnouncement.richContent"
          />
          <div
            v-else-if="currentAnnouncement.content"
            class="text-content"
          >
            {{ currentAnnouncement.content }}
          </div>
          <div v-else class="empty-content">
            <p>该公告暂无内容</p>
          </div>
          
          <div v-if="currentAnnouncement.link" class="announcement-link">
            <a
              :href="currentAnnouncement.link"
              target="_blank"
              rel="noopener noreferrer"
              @click="handleLinkClick"
              class="link-text"
            >
              <span class="link-label">相关链接：</span>
              <span class="link-url">{{ getLinkText(currentAnnouncement.link) }}</span>
              <el-icon><ArrowRight /></el-icon>
            </a>
          </div>
          </div>

          <div v-if="hasMultiple" class="navigation-buttons" role="navigation" aria-label="公告导航">
            <ElButton
              :disabled="currentIndex === 0"
              aria-label="上一条公告"
              @click="handlePrev"
            >
              上一条
            </ElButton>
            <ElButton
              :disabled="currentIndex === sortedAnnouncements.length - 1"
              aria-label="下一条公告"
              @click="handleNext"
            >
              下一条
            </ElButton>
          </div>
        </div>
      </transition>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <ElButton
          v-if="!isEmpty && !loading && currentAnnouncement?.requireConfirm === 1"
          type="primary"
          :loading="markingRead"
          :icon="CircleCheck"
          :disabled="markingRead"
          aria-label="确认已阅读"
          @click="handleConfirm"
        >
          我已阅读
        </ElButton>
        <ElButton
          v-else-if="!isEmpty && !loading"
          type="primary"
          aria-label="关闭公告"
          @click="handleClose"
        >
          知道了
        </ElButton>
        <ElButton
          v-else-if="isEmpty"
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
.announcement-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.announcement-dialog :deep(.el-dialog__body) {
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

.type-tag {
  margin-left: 4px;
}

.announcement-counter {
  margin-left: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.dialog-content {
  padding: 24px 20px;
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
}

.announcement-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.5;
}

.announcement-body {
  flex: 1;
}

.text-content {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.rich-content {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.8;
  word-break: break-word;
}

.rich-content :deep(p) {
  margin: 0 0 12px 0;
}

.rich-content :deep(img) {
  max-width: 100%;
  height: auto;
}

.announcement-link {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.announcement-link a {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--el-color-primary);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.announcement-link a:hover {
  color: var(--el-color-primary-light-3);
}

.navigation-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--el-text-color-secondary);
}

.loading-icon {
  font-size: 32px;
  margin-bottom: 12px;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.empty-content {
  padding: 20px;
  text-align: center;
  color: var(--el-text-color-placeholder);
  font-size: 14px;
}

.announcement-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.meta-time {
  display: inline-block;
}

.meta-expire {
  display: inline-block;
  color: var(--el-text-color-placeholder);
}

.expired-tag {
  margin-left: 8px;
}

.announcement-content {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
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

