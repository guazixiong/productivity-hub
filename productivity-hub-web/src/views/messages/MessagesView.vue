<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useMessageStore } from '@/stores/messages'
import type { MessageChannel, MessageHistoryItem } from '@/types/messages'

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
      { key: 'recipients', label: '收件人（逗号分隔）', type: 'text', required: true },
      { key: 'subject', label: '主题', type: 'text', required: true },
      { key: 'content', label: 'HTML 内容', type: 'textarea', rows: 6, required: true },
      { key: 'trackOpens', label: '开启打开追踪', type: 'switch' },
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
  dify: {
    label: 'Dify 智能体',
    description: '输入智能体 ID 与上下文参数，支持同步 / 异步执行',
    fields: [
      { key: 'agentId', label: '智能体 ID', type: 'text', required: true },
      { key: 'mode', label: '执行模式', type: 'select', required: true, options: [{ label: '同步', value: 'sync' }, { label: '异步', value: 'async' }] },
      { key: 'context', label: '上下文（JSON）', type: 'textarea', rows: 4 },
      { key: 'input', label: '输入内容', type: 'textarea', rows: 4, required: true },
    ],
  },
}

const formRef = ref<FormInstance>()
const messageStore = useMessageStore()
const historyFilter = ref<'all' | MessageChannel>('all')
const activeTab = ref<'composer' | 'history'>('composer')
const historyDetailVisible = ref(false)
const selectedHistory = ref<MessageHistoryItem | null>(null)

const form = reactive({
  channel: 'sendgrid' as MessageChannel,
  data: {} as Record<string, unknown>,
})

const schemaEntries = computed(() => Object.entries(schemas) as [MessageChannel, ChannelSchema][])
const activeSchema = computed(() => schemas[form.channel])

const filteredHistory = computed(() => {
  if (historyFilter.value === 'all') return messageStore.history
  return messageStore.history.filter((item) => item.channel === historyFilter.value)
})

const resetFormPayload = () => {
  Object.keys(form.data).forEach((key) => delete form.data[key])
  activeSchema.value.fields.forEach((field) => {
    form.data[field.key] = field.type === 'switch' ? false : ''
  })
}

watch(
  () => form.channel,
  () => {
    resetFormPayload()
  },
  { immediate: true },
)

const submitMessage = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await messageStore.sendMessage({
      channel: form.channel,
      data: { ...form.data },
    })
    ElMessage.success('推送成功，已写入历史')
  } catch (error) {
    ElMessage.error((error as string) ?? '推送失败')
  }
}

const formatChannel = (channel: MessageChannel) => schemas[channel].label
const selectChannel = (channel: MessageChannel) => {
  form.channel = channel
}
const stringifyPayload = (payload?: Record<string, unknown>) => JSON.stringify(payload ?? {}, null, 2)

const openHistoryDetail = (entry: MessageHistoryItem) => {
  selectedHistory.value = entry
  historyDetailVisible.value = true
}

onMounted(() => {
  messageStore.fetchHistory()
})
</script>

<template>
  <div class="messages-shell">
    <el-card class="messages-card">
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="推送中心" name="composer">
          <div class="channel-grid">
            <div
              v-for="[key, schema] in schemaEntries"
              :key="key"
              :class="['channel-card', form.channel === key && 'active']"
              role="button"
              tabindex="0"
              @click="selectChannel(key as MessageChannel)"
              @keydown.enter="selectChannel(key as MessageChannel)"
            >
              <div class="channel-card__title">
                <h3>{{ schema.label }}</h3>
                <el-tag size="small" effect="dark">{{ key }}</el-tag>
              </div>
              <p>{{ schema.description }}</p>
            </div>
          </div>

          <el-form ref="formRef" :model="form.data" label-width="140px" class="message-form">
            <template v-for="field in activeSchema.fields" :key="field.key">
              <el-form-item
                :label="field.label"
                :prop="field.key"
                :rules="field.required ? [{ required: true, message: `请输入${field.label}` }] : []"
              >
                <el-input
                  v-if="field.type === 'text'"
                  v-model="form.data[field.key]"
                  :placeholder="field.placeholder"
                />
                <el-input
                  v-else-if="field.type === 'textarea'"
                  v-model="form.data[field.key]"
                  type="textarea"
                  :rows="field.rows ?? 3"
                  :placeholder="field.placeholder"
                />
                <el-select
                  v-else-if="field.type === 'select'"
                  v-model="form.data[field.key]"
                  :placeholder="field.placeholder"
                >
                  <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
                <el-switch v-else-if="field.type === 'switch'" v-model="form.data[field.key]" />
              </el-form-item>
            </template>
            <el-form-item>
              <el-button type="primary" :loading="messageStore.sending" @click="submitMessage">发送</el-button>
              <el-button @click="resetFormPayload">重置</el-button>
            </el-form-item>
          </el-form>

          <el-alert
            v-if="messageStore.latestResult"
            type="success"
            :closable="false"
            class="result-alert"
            :title="`请求 ${messageStore.latestResult.requestId} - ${messageStore.latestResult.status}`"
            :description="messageStore.latestResult.detail"
          />
        </el-tab-pane>

        <el-tab-pane label="历史记录" name="history">
          <div class="history-toolbar">
            <el-select v-model="historyFilter" class="filter-select">
              <el-option label="全部渠道" value="all" />
              <el-option v-for="(schema, key) in schemas" :key="key" :label="schema.label" :value="key" />
            </el-select>
            <el-button text type="primary" @click="messageStore.fetchHistory()" :loading="messageStore.loadingHistory">刷新</el-button>
          </div>
          <el-table :data="filteredHistory" :loading="messageStore.loadingHistory" border>
            <el-table-column prop="channel" label="渠道" width="160">
              <template #default="{ row }">
                <el-tag>{{ formatChannel(row.channel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.status === 'success' ? 'success' : 'danger'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="190" />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button link type="primary" @click="openHistoryDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-drawer v-model="historyDetailVisible" title="推送详情" size="40%">
      <template v-if="selectedHistory">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="渠道">{{ formatChannel(selectedHistory.channel) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedHistory.status === 'success' ? 'success' : 'danger'">{{ selectedHistory.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="时间">{{ selectedHistory.createdAt }}</el-descriptions-item>
        </el-descriptions>
        <div class="history-json">
          <h4>请求参数</h4>
          <pre>{{ stringifyPayload(selectedHistory.request) }}</pre>
        </div>
        <div class="history-json">
          <h4>响应内容</h4>
          <pre>{{ selectedHistory.response }}</pre>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.messages-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.messages-card {
  background: var(--surface-color);
  border-radius: 20px;
  border: 1px solid rgba(99, 102, 241, 0.16);
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.channel-card {
  border: 1px solid transparent;
  border-radius: 16px;
  padding: 16px;
  background: #f8fafc;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.channel-card:hover {
  border-color: #c7d2fe;
  transform: translateY(-2px);
}

.channel-card.active {
  border-color: #6366f1;
  background: #eef2ff;
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.18);
}

.channel-card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.channel-card h3 {
  margin: 0;
  font-size: 16px;
  color: #1e1b4b;
}

.channel-card p {
  margin: 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.4;
}

.message-form {
  margin-top: 12px;
}

.result-alert {
  margin-top: 16px;
}

.history-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.filter-select {
  width: 220px;
}

.history-json {
  margin-top: 20px;
  background: #0f172a;
  color: #f8fafc;
  border-radius: 12px;
  padding: 16px;
}

.history-json h4 {
  margin: 0 0 8px;
}

.history-json pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
}

@media (max-width: 768px) {
  .channel-grid {
    grid-template-columns: 1fr;
  }

  .filter-select {
    width: 100%;
  }
}
</style>

