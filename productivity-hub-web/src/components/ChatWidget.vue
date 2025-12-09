<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { ChatDotRound, Close, Loading, Delete, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useConfigStore } from '@/stores/config'
import { chatWithDify } from '@/services/api'
import { configApi } from '@/services/api'

const configStore = useConfigStore()

const visible = ref(false)
const messages = ref<Array<{ role: 'user' | 'assistant'; content: string; streaming?: boolean }>>([])
const inputMessage = ref('')
const isLoading = ref(false)
const chatContainerRef = ref<HTMLElement>()
const currentConversationId = ref<string>('')

// 从配置中获取dify配置
const difyConfig = computed(() => {
  const apiUrl = configStore.configs.find(
    (c) => c.module === 'dify' && c.key === 'api.url'
  )?.value || ''
  const apiKey = configStore.configs.find(
    (c) => c.module === 'dify' && c.key === 'api.key'
  )?.value || ''
  
  return { apiUrl, apiKey }
})

// 保存会话ID到配置
const saveConversationId = async (conversationId: string) => {
  if (!conversationId) return
  
  try {
    await configApi.createOrUpdate({
      module: 'dify',
      key: 'conversation.id',
      value: conversationId,
      description: 'Dify 会话ID，用于保持对话上下文',
    })
    // 刷新配置缓存
    await configStore.fetchConfigs(true)
    currentConversationId.value = conversationId
  } catch (error) {
    console.warn('Failed to save conversation ID:', error)
  }
}

// 检查配置是否完整
const isConfigValid = computed(() => {
  return difyConfig.value.apiUrl && difyConfig.value.apiKey
})

const toggleChat = async () => {
  visible.value = !visible.value
  if (visible.value) {
    // 确保配置已加载
    await configStore.fetchConfigs()
    // 加载会话ID
    const savedConversationId = configStore.configs.find(
      (c) => c.module === 'dify' && c.key === 'conversation.id'
    )?.value || ''
    currentConversationId.value = savedConversationId
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  if (!isConfigValid.value) {
    ElMessage.warning('请先在全局配置中配置 Dify API 信息（模块：dify，key：api.url 和 api.key）')
    return
  }

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage,
  })
  
  scrollToBottom()
  
  // 添加助手消息占位符
  const assistantMessageIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    streaming: true,
  })
  
  isLoading.value = true
  
  try {
    const { apiUrl, apiKey } = difyConfig.value
    const conversationId = currentConversationId.value || 
      configStore.configs.find((c) => c.module === 'dify' && c.key === 'conversation.id')?.value || ''
    
    let newConversationId = conversationId
    
    await chatWithDify({
      apiUrl,
      apiKey,
      conversationId: conversationId || undefined,
      message: userMessage,
      onChunk: (chunk: string) => {
        // 流式更新助手消息
        if (messages.value[assistantMessageIndex]) {
          messages.value[assistantMessageIndex].content += chunk
          scrollToBottom()
        }
      },
      onComplete: (returnedConversationId?: string) => {
        // 标记流式传输完成
        if (messages.value[assistantMessageIndex]) {
          messages.value[assistantMessageIndex].streaming = false
        }
        isLoading.value = false
        
        // 保存返回的会话ID
        if (returnedConversationId && returnedConversationId !== newConversationId) {
          newConversationId = returnedConversationId
          saveConversationId(returnedConversationId)
        }
      },
      onError: (error: string) => {
        if (messages.value[assistantMessageIndex]) {
          messages.value[assistantMessageIndex].content = `错误: ${error}`
          messages.value[assistantMessageIndex].streaming = false
        }
        isLoading.value = false
        ElMessage.error(`聊天失败: ${error}`)
      },
    })
  } catch (error) {
    isLoading.value = false
    if (messages.value[assistantMessageIndex]) {
      messages.value[assistantMessageIndex].content = `错误: ${(error as Error).message}`
      messages.value[assistantMessageIndex].streaming = false
    }
    ElMessage.error(`聊天失败: ${(error as Error).message}`)
  }
}

const clearMessages = async () => {
  messages.value = []
  currentConversationId.value = ''
  // 清空配置中的会话ID
  try {
    await configApi.createOrUpdate({
      module: 'dify',
      key: 'conversation.id',
      value: '',
      description: 'Dify 会话ID，用于保持对话上下文',
    })
    await configStore.fetchConfigs(true)
  } catch (error) {
    console.warn('Failed to clear conversation ID:', error)
  }
}

// 格式化消息内容（支持换行和基本格式化）
const formatMessage = (content: string) => {
  if (!content) return ''
  // 转义 HTML 防止 XSS
  const escaped = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
  // 将换行符转换为 <br>
  return escaped.replace(/\n/g, '<br>')
}

// 监听消息变化，自动滚动到底部
watch(
  () => messages.value.length,
  () => {
    scrollToBottom()
  }
)
</script>

<template>
  <div class="chat-widget">
    <!-- 聊天按钮 -->
    <el-button
      v-if="!visible"
      class="chat-toggle-button"
      type="primary"
      :icon="ChatDotRound"
      circle
      size="large"
      @click="toggleChat"
    />
    
    <!-- 聊天窗口 -->
    <transition name="chat-slide">
      <el-card v-if="visible" class="chat-window" shadow="always">
        <template #header>
          <div class="chat-header">
            <div class="chat-header-left">
              <div class="chat-title-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="chat-header-info">
                <span class="chat-title">Dify 聊天助手</span>
                <span v-if="isConfigValid" class="chat-status online">在线</span>
                <span v-else class="chat-status offline">未配置</span>
              </div>
            </div>
            <div class="chat-actions">
              <el-button
                v-if="messages.length > 0"
                text
                :icon="Delete"
                circle
                size="small"
                title="清空对话"
                @click="clearMessages"
              />
              <el-button
                text
                :icon="Close"
                circle
                size="small"
                title="关闭"
                @click="toggleChat"
              />
            </div>
          </div>
        </template>
        
        <div class="chat-content">
          <div ref="chatContainerRef" class="chat-messages">
            <div v-if="messages.length === 0" class="chat-empty">
              <div class="empty-icon">
                <el-icon :size="64"><ChatDotRound /></el-icon>
              </div>
              <p class="empty-title">开始与 Dify 助手对话吧！</p>
              <p class="empty-desc">我可以帮助你解答问题、提供建议和协助工作</p>
              <div v-if="!isConfigValid" class="config-warning">
                <el-icon><Loading /></el-icon>
                <span>提示：请先在全局配置中配置 Dify API 信息</span>
              </div>
            </div>
            
            <div
              v-for="(message, index) in messages"
              :key="index"
              :class="['chat-message', `chat-message-${message.role}`]"
            >
              <div class="message-avatar">
                <el-avatar v-if="message.role === 'user'" :size="36" :style="{ background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)' }">
                  <el-icon><ChatDotRound /></el-icon>
                </el-avatar>
                <el-avatar v-else :size="36" :style="{ background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)' }">
                  <el-icon><ChatDotRound /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-text" v-html="formatMessage(message.content)"></div>
                <div v-if="message.streaming" class="message-streaming">
                  <span class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="chat-input-area">
            <div class="input-wrapper">
              <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="2"
                :maxlength="2000"
                show-word-limit
                placeholder="输入消息... (Enter 发送，Shift+Enter 换行)"
                :disabled="isLoading || !isConfigValid"
                @keydown.enter.exact.prevent="sendMessage"
                @keydown.enter.shift.exact.prevent="inputMessage += '\n'"
              />
            </div>
            <div class="chat-input-actions">
              <div class="input-tips">
                <span v-if="!isConfigValid" class="tip-warning">请先配置 API 信息</span>
              </div>
              <div class="input-buttons">
                <el-button
                  type="primary"
                  :icon="Promotion"
                  :loading="isLoading"
                  :disabled="!inputMessage.trim() || !isConfigValid"
                  @click="sendMessage"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </transition>
  </div>
</template>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

.chat-toggle-button {
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  transition: all 0.3s ease;
}

.chat-toggle-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.chat-window {
  width: 420px;
  height: 650px;
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.2);
  border: 1px solid #e2e8f0;
}

.chat-window :deep(.el-card__header) {
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
}

.chat-window :deep(.el-card__body) {
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 0;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-title-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 10px;
  color: white;
}

.chat-header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chat-title {
  font-weight: 600;
  font-size: 16px;
  color: #0f172a;
  line-height: 1.2;
}

.chat-status {
  font-size: 12px;
  line-height: 1.2;
}

.chat-status.online {
  color: #10b981;
}

.chat-status.offline {
  color: #f59e0b;
}

.chat-actions {
  display: flex;
  gap: 4px;
}

.chat-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #f8fafc;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  margin-bottom: 16px;
  color: #cbd5e1;
  opacity: 0.6;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 24px 0;
}

.config-warning {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #fef3c7;
  border: 1px solid #fde68a;
  border-radius: 8px;
  font-size: 12px;
  color: #92400e;
  margin-top: 8px;
}

.chat-message {
  display: flex;
  gap: 12px;
  animation: messageSlideIn 0.3s ease;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-avatar :deep(.el-avatar) {
  color: white;
  font-weight: 500;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-message-user .message-content {
  align-items: flex-end;
}

.chat-message-assistant .message-content {
  align-items: flex-start;
}

.message-text {
  padding: 12px 16px;
  border-radius: 16px;
  max-width: 85%;
  word-wrap: break-word;
  line-height: 1.6;
  font-size: 14px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.chat-message-user .message-text {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.chat-message-assistant .message-text {
  background: white;
  color: #1e293b;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 4px;
}

.message-streaming {
  display: flex;
  align-items: center;
  margin-top: 8px;
  padding-left: 4px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #94a3b8;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.chat-input-area {
  padding: 16px;
  border-top: 1px solid #e2e8f0;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-wrapper {
  background: white;
  border-radius: 12px;
  padding: 4px;
  border: 1px solid #e2e8f0;
  transition: border-color 0.2s;
}

.input-wrapper:focus-within {
  border-color: #6366f1;
}

.input-wrapper :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  padding: 8px 12px;
  resize: none;
}

.input-wrapper :deep(.el-input__count) {
  background: transparent;
  right: 8px;
  bottom: 8px;
}

.chat-input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-tips {
  flex: 1;
}

.tip-warning {
  font-size: 12px;
  color: #f59e0b;
}

.input-buttons {
  display: flex;
  gap: 8px;
}

/* 动画 */
.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: all 0.3s ease;
}

.chat-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.chat-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}
</style>

