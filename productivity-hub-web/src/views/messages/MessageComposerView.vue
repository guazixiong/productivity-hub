<script setup lang="ts">
import { computed, reactive, ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Message, Document, User, SwitchButton, InfoFilled } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { useMessageStore } from '@/stores/messages'
import type { MessageChannel } from '@/types/messages'

marked.setOptions({ gfm: true, breaks: true })

type FieldSchema = {
  key: string
  label: string
  type: 'text' | 'textarea' | 'select' | 'switch'
  placeholder?: string
  required?: boolean
  options?: { label: string; value: string }[]
  rows?: number
}

type ChannelSchema = {
  label: string
  description: string
  fields: FieldSchema[]
}

const schemas: Record<MessageChannel, ChannelSchema> = {
  sendgrid: {
    label: 'SendGrid 邮件',
    description: '支持多收件人与富文本正文，发送结果实时返回',
    fields: [
      { key: 'recipients', label: '收件人', type: 'text', required: true, placeholder: '多个收件人请用逗号分隔，例如：user1@example.com, user2@example.com' },
      { key: 'subject', label: '邮件主题', type: 'text', required: true, placeholder: '请输入邮件主题' },
      { key: 'content', label: 'HTML 内容', type: 'textarea', rows: 10, required: true, placeholder: '请输入HTML格式的邮件内容' },
    ],
  },
  dingtalk: {
    label: '钉钉 Webhook',
    description: '机器人签名与消息类型支持文本、Markdown、链接',
    fields: [
      { key: 'webhook', label: 'Webhook 地址', type: 'text', required: true },
      {
        key: 'msgType',
        label: '消息类型',
        type: 'select',
        required: true,
        options: [
          { label: '文本', value: 'text' },
          { label: 'Markdown', value: 'markdown' },
          { label: '链接', value: 'link' },
        ],
      },
      { key: 'content', label: '消息内容', type: 'textarea', rows: 4, required: true },
      { key: 'atMobiles', label: '@手机号（逗号分隔）', type: 'text' },
    ],
  },
}

const router = useRouter()
const formRef = ref<FormInstance>()
const messageStore = useMessageStore()
const markdownPreviewVisible = ref(false)

const channel = computed(() => {
  const routeChannel = router.currentRoute.value.params.channel as MessageChannel
  return routeChannel || 'sendgrid'
})

const form = reactive({
  data: {} as Record<string, unknown>,
})

const activeSchema = computed(() => schemas[channel.value])

const showMarkdownPreview = computed(() => channel.value === 'dingtalk' && form.data.msgType === 'markdown')

const markdownPreviewWithDefault = computed(() => {
  const content = form.data.content as string
  if (!content) return ''
  return marked(content)
})

const parseRecipients = (value?: string) => {
  if (!value) return []
  return value
    .split(/[,，;；\s]+/)
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
}
const getRecipientList = () => parseRecipients(form.data.recipients as string)
const updateRecipientList = (list: string[]) => {
  form.data.recipients = list.join(',')
}
const addRecipients = (candidates: string[]) => {
  if (!candidates.length) return
  const list = getRecipientList()
  let changed = false
  candidates.forEach((item) => {
    if (!list.includes(item)) {
      list.push(item)
      changed = true
    }
  })
  if (changed) {
    updateRecipientList(list)
  }
}
const removeRecipient = (target: string) => {
  const next = getRecipientList().filter((item) => item !== target)
  updateRecipientList(next)
}
const recipientTags = computed<string[]>(() => getRecipientList())

// 收件人输入框的当前查询，用于手动处理回车确认
const recipientsQuery = ref('')
const handleRecipientsFilter = (value: string) => {
  recipientsQuery.value = value
}
const commitPendingRecipient = () => {
  const values = parseRecipients(recipientsQuery.value)
  if (!values.length) return
  addRecipients(values)
  recipientsQuery.value = ''
}
const handleRecipientsEnter = () => {
  commitPendingRecipient()
}

const resetFormPayload = () => {
  activeSchema.value.fields.forEach((field) => {
    form.data[field.key] = field.type === 'switch' ? false : ''
  })
}

watch(
  () => channel.value,
  () => {
    resetFormPayload()
    if (formRef.value) {
      formRef.value.clearValidate()
    }
  },
  { immediate: true },
)

const submitMessage = async () => {
  if (!formRef.value) return
  // 确保输入框中未回车确认的收件人也会加入校验
  commitPendingRecipient()
  await nextTick()
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await messageStore.sendMessage({
      channel: channel.value,
      data: { ...form.data },
    })
    ElMessage.success('推送成功，已写入历史')
    // 发送成功后重置表单，避免新邮件在上一次的基础上继续追加收件人等信息
    resetFormPayload()
    formRef.value.clearValidate()
    router.push('/messages/history')
  } catch (error) {
    ElMessage.error((error as string) ?? '推送失败')
  }
}

</script>

<template>
  <div class="message-composer">
    <!-- 页面头部 -->
    <div class="composer-header">
      <div class="header-content">
        <div class="header-title-section">
          <div class="title-icon">
            <el-icon :size="28"><Message /></el-icon>
          </div>
          <div class="title-text">
            <h1 class="page-title">{{ activeSchema.label }}</h1>
            <p class="page-description">{{ activeSchema.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 表单卡片 -->
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>邮件配置</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form.data" label-width="120px" class="message-form" :validate-on-rule-change="false">
        <!-- 表单字段 -->
        <template v-for="field in activeSchema.fields" :key="field.key">
          <el-form-item
            :label="field.label"
            :prop="field.key"
            :rules="field.required ? [{ required: true, message: `请输入${field.label}` }] : []"
            class="form-field-item"
          >
            <!-- 文本输入 -->
            <template v-if="field.type === 'text'">
              <template v-if="field.key === 'recipients'">
                <el-input
                  v-model="recipientsQuery"
                  :placeholder="field.placeholder || '输入邮箱后按回车'"
                  class="custom-input recipients-input"
                  size="large"
                  :prefix-icon="Document"
                  @keyup.enter.stop.prevent="handleRecipientsEnter"
                  @input="handleRecipientsFilter($event as string)"
                />
                <div v-if="recipientTags.length" class="recipients-tags">
                  <el-tag
                    v-for="item in recipientTags"
                    :key="item"
                    closable
                    @close="removeRecipient(item)"
                  >
                    {{ item }}
                  </el-tag>
                </div>
                <div class="textarea-hint recipients-hint">
                  <el-icon><InfoFilled /></el-icon>
                  <span>单个邮箱回车即成标签，提交时将以逗号分隔存储</span>
                </div>
              </template>
              <el-input
                v-else
                v-model="form.data[field.key]"
                :placeholder="field.placeholder"
                class="custom-input"
                size="large"
                :prefix-icon="Document"
              />
            </template>
            
            <!-- 文本域 -->
            <div v-else-if="field.type === 'textarea'" class="textarea-wrapper">
              <div v-if="showMarkdownPreview && field.key === 'content'" class="markdown-editor-layout">
                <div class="markdown-editor-left">
                  <el-input
                    v-model="form.data[field.key]"
                    type="textarea"
                    :rows="field.rows ?? 3"
                    :placeholder="field.placeholder"
                    class="custom-textarea"
                  />
                  <el-button
                    v-if="form.data.content"
                    type="primary"
                    size="small"
                    style="margin-top: 8px"
                    @click="markdownPreviewVisible = true"
                  >
                    预览 Markdown
                  </el-button>
                </div>
                <div class="markdown-preview-right">
                  <div class="preview-header">
                    <span>Markdown 预览</span>
                  </div>
                  <div class="preview-content" v-html="markdownPreviewWithDefault" />
                </div>
              </div>
              <template v-else>
                <el-input
                  v-model="form.data[field.key]"
                  type="textarea"
                  :rows="field.rows ?? 10"
                  :placeholder="field.placeholder"
                  class="custom-textarea html-textarea"
                  resize="vertical"
                />
                <div v-if="field.key === 'content'" class="textarea-hint">
                  <el-icon><InfoFilled /></el-icon>
                  <span>支持HTML格式，可直接输入HTML代码</span>
                </div>
              </template>
            </div>
            
            <!-- 下拉选择 -->
            <el-select
              v-else-if="field.type === 'select'"
              v-model="form.data[field.key]"
              :placeholder="field.placeholder"
              class="custom-select"
              size="large"
            >
              <el-option
                v-for="option in field.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            
            <!-- 开关 -->
            <div v-else-if="field.type === 'switch'" class="switch-wrapper">
              <el-switch
                v-model="form.data[field.key]"
                size="large"
              />
              <span class="switch-label">启用后将追踪邮件的打开情况</span>
            </div>
          </el-form-item>
        </template>

        <!-- 提交按钮 -->
        <el-form-item class="submit-section">
          <el-button 
            type="primary" 
            size="large" 
            class="submit-button"
            :icon="Message"
            @click="submitMessage"
          >
            发送邮件
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Markdown预览对话框 -->
    <el-dialog v-model="markdownPreviewVisible" title="Markdown 预览" width="800px">
      <div class="markdown-preview-dialog" v-html="markdownPreviewWithDefault" />
    </el-dialog>
  </div>
</template>

<style scoped>
.message-composer {
  padding: 0;
  min-height: 100%;
}

/* 页面头部样式 */
.composer-header {
  margin-bottom: 24px;
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.title-text {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
  margin-bottom: 8px;
}

.page-description {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

/* 表单卡片样式 */
.form-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.form-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
  padding: 20px 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.card-header .el-icon {
  color: #6366f1;
  font-size: 18px;
}

.form-card :deep(.el-card__body) {
  padding: 32px;
}

/* 表单样式 */
.message-form {
  max-width: 100%;
}

.form-field-item {
  margin-bottom: 24px;
}

.form-field-item :deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  font-size: 14px;
}

/* 输入框样式 */
.custom-input {
  width: 100%;
}

.custom-input :deep(.el-input__inner) {
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  transition: all 0.2s;
}

.custom-input :deep(.el-input__inner):focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

/* 文本域样式 */
.textarea-wrapper {
  width: 100%;
}

.custom-textarea {
  width: 100%;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace;
}

.custom-textarea :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  transition: all 0.2s;
  font-size: 13px;
  line-height: 1.6;
}

.custom-textarea :deep(.el-textarea__inner):focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.html-textarea :deep(.el-textarea__inner) {
  background: #f8fafc;
  color: #1e293b;
}

.textarea-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
}

.textarea-hint .el-icon {
  font-size: 14px;
  color: #6366f1;
}

/* 开关样式 */
.switch-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-label {
  font-size: 13px;
  color: #64748b;
}

/* 下拉选择样式 */
.custom-select {
  width: 100%;
}

.custom-select :deep(.el-input__inner) {
  border-radius: 8px;
  border: 1px solid #cbd5e1;
}

.recipients-select :deep(.el-select__tags) {
  max-width: 100%;
  flex-wrap: wrap;
}

.select-empty-hint {
  padding: 8px 0;
  color: #94a3b8;
  font-size: 13px;
}

.recipients-hint {
  margin-top: 6px;
}

/* 提交按钮区域 */
.submit-section {
  margin-top: 32px;
  margin-bottom: 0;
}

.submit-section :deep(.el-form-item__content) {
  display: flex;
  justify-content: center;
}

.submit-button {
  min-width: 200px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.submit-button:active {
  transform: translateY(0);
}

/* Markdown编辑器布局 */
.markdown-editor-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.markdown-editor-left {
  display: flex;
  flex-direction: column;
}

.markdown-preview-right {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.preview-header {
  background: #f9fafb;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
}

.preview-content {
  padding: 16px;
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
  background: white;
}

.markdown-preview-dialog {
  padding: 16px;
  max-height: 600px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-title-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .title-icon {
    width: 48px;
    height: 48px;
  }

  .page-title {
    font-size: 24px;
  }

  .form-card :deep(.el-card__body) {
    padding: 20px;
  }

  .message-form :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left;
    margin-bottom: 8px;
  }

  .markdown-editor-layout {
    grid-template-columns: 1fr;
  }

  .submit-button {
    width: 100%;
  }
}
</style>

