<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { useRoute } from 'vue-router'
import { useConfigStore } from '@/stores/config'
import { useMessageStore } from '@/stores/messages'
import type { MessageChannel, MessageHistoryItem } from '@/types/messages'

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
  dingtalk: {
    label: '钉钉 Webhook',
    description: '机器人签名与消息类型支持文本、Markdown、链接',
    fields: [
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
  resend: {
    label: 'Resend 邮件',
    description: '支持HTML邮件内容',
    fields: [
      { key: 'recipients', label: '收件人', type: 'text' },
      { key: 'title', label: '邮件标题', type: 'text', required: true },
      { key: 'html', label: 'HTML 内容', type: 'textarea', rows: 8, required: true },
    ],
  },
  sendgrid: {
    label: 'SendGrid 邮件',
    description: '支持多收件人与富文本正文，发送结果实时返回',
    fields: [
      { key: 'recipients', label: '收件人', type: 'text', required: true },
      { key: 'subject', label: '主题', type: 'text', required: true },
      { key: 'content', label: 'HTML 内容', type: 'textarea', rows: 6, required: true },
    ],
  },
}

const formRef = ref<FormInstance>()
const configStore = useConfigStore()
const messageStore = useMessageStore()
const route = useRoute()
type HistoryFilterState = {
  channel: 'all' | MessageChannel
  status: 'all' | MessageHistoryItem['status']
  dateRange: [Date, Date] | null
}

const historyFilters = reactive<HistoryFilterState>({
  channel: 'all',
  status: 'all',
  dateRange: null,
})
const activeTab = ref<'composer' | 'history'>('composer')
const composerView = ref<'channels' | 'form'>('channels')
const historyDetailVisible = ref(false)
const selectedHistory = ref<MessageHistoryItem | null>(null)
const markdownPreviewVisible = ref(false)

const form = reactive({
  channel: 'dingtalk' as MessageChannel,
  data: {} as Record<string, unknown>,
})

const schemaEntries = computed(() => Object.entries(schemas) as [MessageChannel, ChannelSchema][])
const activeSchema = computed(() => schemas[form.channel])

const recipientsList = computed<string[]>({
  get() {
    const raw = String((form.data as Record<string, unknown>).recipients ?? '')
    if (!raw) return []
    return String(raw)
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
  },
  set(val: string[]) {
    ;(form.data as Record<string, unknown>).recipients = val.join(', ')
  },
})

// 收件人输入框的当前查询，用于手动处理回车确认
const recipientsQuery = ref('')
const handleRecipientsFilter = (value: string) => {
  recipientsQuery.value = value
}
const handleRecipientsEnter = () => {
  const value = recipientsQuery.value.trim()
  if (!value) return

  const list = [...recipientsList.value]
  if (!list.includes(value)) {
    recipientsList.value = [...list, value]
  }
  // 不再保留当前输入，避免再次回车选中历史值
  recipientsQuery.value = ''
}

watch(
  () => (form.data as Record<string, unknown>).recipients,
  () => {
    if (formRef.value) {
      formRef.value.validateField('recipients').catch(() => {
        /* ignore validation errors during typing */
      })
    }
  },
)

const syncStateFromRoute = () => {
  const tabQuery = Array.isArray(route.query.tab) ? route.query.tab[0] : route.query.tab
  if (tabQuery === 'composer' || tabQuery === 'history') {
    activeTab.value = tabQuery
  }

  const channelQuery = Array.isArray(route.query.channel) ? route.query.channel[0] : route.query.channel
  if (channelQuery && channelQuery in schemas) {
    form.channel = channelQuery as MessageChannel
    composerView.value = 'form'
  }
}


// Markdown预览
const markdownPreview = computed(() => {
  if (form.channel === 'dingtalk' && form.data.msgType === 'markdown' && form.data.content) {
    return marked.parse(String(form.data.content))
  }
  return ''
})

const markdownPreviewWithDefault = computed(() => {
  return markdownPreview.value || '<p style="color: #94a3b8;">请输入内容以查看预览</p>'
})

const markdownPreviewDialog = computed(() => {
  return markdownPreview.value || '<p style="color: #94a3b8; text-align: center; padding: 40px;">暂无内容</p>'
})

// 是否显示Markdown预览
const showMarkdownPreview = computed(() => {
  return form.channel === 'dingtalk' && form.data.msgType === 'markdown'
})

const historyPagination = computed(() => messageStore.historyPagination)

const filteredHistory = computed(() => {
  return messageStore.history.filter((item) => {
    if (historyFilters.channel !== 'all' && item.channel !== historyFilters.channel) return false
    if (historyFilters.status !== 'all' && item.status !== historyFilters.status) return false
    if (historyFilters.dateRange && historyFilters.dateRange[0] && historyFilters.dateRange[1]) {
      const [start, end] = historyFilters.dateRange
      const startTime = new Date(start).setHours(0, 0, 0, 0)
      const endTime = new Date(end).setHours(23, 59, 59, 999)
      const createdTime = new Date(item.createdAt).getTime()
      if (createdTime < startTime || createdTime > endTime) return false
    }
    return true
  })
})

const hasHistoryFilters = computed(() => {
  return (
    historyFilters.channel !== 'all' ||
    historyFilters.status !== 'all' ||
    !!historyFilters.dateRange
  )
})

const resetHistoryFilters = () => {
  historyFilters.channel = 'all'
  historyFilters.status = 'all'
  historyFilters.dateRange = null
}

const refreshHistory = () => {
  messageStore.fetchHistory({
    page: historyPagination.value.pageNum,
    pageSize: historyPagination.value.pageSize,
  })
}

const handleHistoryPageChange = (page: number) => {
  messageStore.fetchHistory({ page })
}

const handleHistoryPageSizeChange = (size: number) => {
  messageStore.fetchHistory({ page: 1, pageSize: size })
}

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
    // 发送成功后重置表单，避免新邮件在上一次的基础上继续追加收件人等信息
    resetFormPayload()
    formRef.value.clearValidate()
  } catch (error) {
    const errorMessage =
      error instanceof Error ? error.message : error ? String(error) : '推送失败'
    ElMessage.error(errorMessage || '推送失败')
  }
}

const formatChannel = (channel: MessageChannel) => schemas[channel].label
const selectChannel = (channel: MessageChannel) => {
  form.channel = channel
  composerView.value = 'form'
}
const backToChannelList = () => {
  composerView.value = 'channels'
}
const stringifyPayload = (payload?: Record<string, unknown>) => JSON.stringify(payload ?? {}, null, 2)

const openHistoryDetail = (entry: MessageHistoryItem) => {
  selectedHistory.value = entry
  historyDetailVisible.value = true
}

const resendRecipients = computed(() => {
  const entry = configStore.configs.find((item) => item.module === 'resend' && item.key === 'resend.toEmail')
  return entry?.value ?? ''
})

watch(
  [resendRecipients, () => form.channel],
  ([recipientsValue, channel]) => {
    if (channel === 'resend') {
      ;(form.data as Record<string, unknown>).recipients = recipientsValue
    }
  },
  { immediate: true },
)

onMounted(() => {
  configStore.fetchConfigs()
})

watch(
  () => route.fullPath,
  () => {
    syncStateFromRoute()
  },
  { immediate: true },
)

watch(
  () => activeTab.value,
  (tab) => {
    if (tab === 'history' && !messageStore.history.length && !messageStore.loadingHistory) {
      messageStore.fetchHistory({
        page: historyPagination.value.pageNum,
        pageSize: historyPagination.value.pageSize,
      })
    }
  },
  { immediate: true },
)

</script>

<template>
  <div class="messages-shell">
    <el-card class="messages-card">
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="推送中心" name="composer">
          <div v-if="composerView === 'channels'" class="composer-layout composer-layout--channels">
            <section class="channel-panel">
              <div class="panel-header">
                <div>
                  <h3>推送渠道</h3>
                  <p>当前可用 {{ schemaEntries.length }} 个渠道</p>
                </div>
              </div>
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
            </section>
          </div>

          <div v-else class="composer-layout composer-layout--form">
            <section class="composer-panel">
              <div class="composer-panel-header">
                <el-button text type="primary" @click="backToChannelList">← 返回渠道选择</el-button>
              </div>
              <el-card class="composer-card" shadow="never">
                <template #header>
                  <div class="card-title">
                    <div>
                      <h3>{{ activeSchema.label }}</h3>
                      <p>{{ activeSchema.description }}</p>
                    </div>
                    <el-tag effect="plain">{{ form.channel }}</el-tag>
                  </div>
                </template>

                <el-form ref="formRef" :model="form.data" label-width="140px" class="message-form" :validate-on-rule-change="false">
                  <template v-for="field in activeSchema.fields" :key="field.key">
                    <el-form-item
                      :label="field.label"
                      :prop="field.key"
                      :rules="field.required ? [{ required: true, message: `请输入${field.label}` }] : []"
                    >
                      <template v-if="field.key === 'recipients'">
                        <template v-if="form.channel === 'resend'">
                          <el-input
                            :model-value="resendRecipients"
                            placeholder="从全局参数配置读取 resend.toEmail"
                            class="custom-input recipients-input"
                            disabled
                          />
                        </template>
                        <template v-else>
                          <el-input
                            v-model="recipientsQuery"
                            placeholder="输入邮箱后回车，可添加多个收件人"
                            class="custom-input recipients-input"
                            @keyup.enter.stop.prevent="handleRecipientsEnter"
                            @input="handleRecipientsFilter($event as string)"
                          />
                          <div v-if="recipientsList.length" class="recipients-tags">
                            <el-tag
                              v-for="item in recipientsList"
                              :key="item"
                              closable
                              @close="recipientsList = recipientsList.filter(tag => tag !== item)"
                            >
                              {{ item }}
                            </el-tag>
                          </div>
                        </template>
                      </template>
                      <el-input
                        v-else-if="field.type === 'text'"
                        v-model="form.data[field.key]"
                        :placeholder="field.placeholder"
                        class="custom-input"
                      />
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
                          </div>
                        </div>
                        <template v-else>
                          <el-input
                            v-model="form.data[field.key]"
                            type="textarea"
                            :rows="field.rows ?? 3"
                            :placeholder="field.placeholder"
                            class="custom-textarea"
                          />
                        </template>
                      </div>
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
                  <el-form-item class="form-actions">
                    <el-button type="primary" :loading="messageStore.sending" @click="submitMessage">发送</el-button>
                    <el-button
                      v-if="showMarkdownPreview && form.data.content"
                      type="primary"
                      plain
                      @click="markdownPreviewVisible = true"
                    >
                      预览 Markdown
                    </el-button>
                    <el-button @click="resetFormPayload">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-card>

              <el-card v-if="messageStore.latestResult" class="result-card" shadow="never">
                <template #header>
                  <div class="card-title">
                    <h3>最近一次推送结果</h3>
                    <el-tag size="large" :type="messageStore.latestResult.status === 'success' ? 'success' : messageStore.latestResult.status === 'queued' ? 'warning' : 'danger'">
                      {{ messageStore.latestResult.status }}
                    </el-tag>
                  </div>
                </template>
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="请求 ID">{{ messageStore.latestResult.requestId }}</el-descriptions-item>
                  <el-descriptions-item label="说明">{{ messageStore.latestResult.detail }}</el-descriptions-item>
                </el-descriptions>
              </el-card>
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="推送历史" name="history">
          <div class="history-toolbar">
            <div class="history-filters-left">
              <div class="history-status-wrapper">
                <span class="history-filter-label">筛选条件</span>
                <el-radio-group v-model="historyFilters.status" class="status-filter-group">
                  <el-radio-button label="all">全部</el-radio-button>
                  <el-radio-button label="success">成功</el-radio-button>
                  <el-radio-button label="failed">失败</el-radio-button>
                </el-radio-group>
              </div>
              <el-select v-model="historyFilters.channel" class="channel-select" placeholder="选择渠道">
                <el-option label="全部渠道" value="all" />
                <el-option v-for="(schema, key) in schemas" :key="key" :label="schema.label" :value="key" />
              </el-select>
              <el-date-picker
                v-model="historyFilters.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                unlink-panels
                clearable
                class="history-date-picker"
              />
            </div>
            <div class="history-filters-right">
              <el-button text type="primary" :disabled="!hasHistoryFilters" @click="resetHistoryFilters">
                清空筛选
              </el-button>
              <el-button text type="primary" @click="refreshHistory" :loading="messageStore.loadingHistory">
                刷新
              </el-button>
            </div>
          </div>
          <el-empty v-if="!messageStore.loadingHistory && !filteredHistory.length" description="暂无推送记录" />
          <template v-else>
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
              <el-table-column prop="createdAt" label="时间" width="200" />
              <el-table-column label="请求参数" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ stringifyPayload(row.request) }}
                </template>
              </el-table-column>
              <el-table-column label="响应内容" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.response }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openHistoryDetail(row)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="history-pagination">
              <el-pagination
                background
                layout="total, sizes, prev, pager, next"
                :current-page="historyPagination.pageNum"
                :page-size="historyPagination.pageSize"
                :page-sizes="[10, 20, 50]"
                :total="historyPagination.total"
                @current-change="handleHistoryPageChange"
                @size-change="handleHistoryPageSizeChange"
              />
            </div>
          </template>
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

    <el-dialog v-model="markdownPreviewVisible" title="Markdown 预览" width="70%">
      <div class="markdown-dialog-content" v-html="markdownPreviewDialog" />
    </el-dialog>
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
  border-radius: 24px;
  border: 1px solid rgba(99, 102, 241, 0.16);
}

.composer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.composer-header h2 {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
}

.composer-header p {
  margin: 6px 0 0;
  color: #475569;
}

.composer-layout {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) minmax(320px, 1.2fr);
  gap: 24px;
}

.composer-layout--channels {
  grid-template-columns: 1fr;
}

.composer-layout--form {
  grid-template-columns: 1fr;
}

.channel-panel {
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 20px;
  padding: 20px;
  background: rgba(248, 250, 252, 0.9);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.panel-header h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #0f172a;
}

.panel-header p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-top: 16px;
}

.channel-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 1px 2px rgba(0, 0, 0, 0.1);
}

.channel-card:hover {
  border-color: #c7d2fe;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15), 0 2px 4px rgba(0, 0, 0, 0.1);
}

.channel-card.active {
  border-color: #6366f1;
  border-width: 2px;
  background: #eef2ff;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.2), 0 2px 8px rgba(99, 102, 241, 0.15);
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

.composer-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.composer-panel-header {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 8px;
}

.composer-card,
.result-card {
  border-radius: 20px;
  border: 1px solid rgba(99, 102, 241, 0.12);
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.card-title h3 {
  margin: 0;
  font-size: 18px;
}

.card-title p {
  margin: 4px 0 0;
  color: #475569;
  font-size: 13px;
}

.message-form {
  margin-top: 12px;
}

.form-actions :deep(.el-form-item__content) {
  justify-content: flex-end;
  gap: 12px;
}

.history-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.history-filters-left,
.history-filters-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.history-filters-left {
  flex: 1;
  min-width: 0;
}

.history-filters-right {
  flex-shrink: 0;
}

.history-status-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.history-filter-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  margin-right: 4px;
  white-space: nowrap;
}

.status-filter-group {
  display: inline-flex !important;
  flex-direction: row !important;
  flex-wrap: nowrap !important;
  white-space: nowrap;
}

.status-filter-group :deep(.el-radio-button) {
  flex: 0 0 auto;
  display: inline-flex;
  width: auto;
}

.history-date-picker {
  width: 220px;
}

.channel-select {
  width: 180px;
  flex-shrink: 0;
}

@media (max-width: 1200px) {
  .history-toolbar {
    flex-wrap: wrap;
    align-items: flex-start;
  }

  .history-filters-right {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .history-date-picker {
    width: 260px;
  }
}

.recipients-select {
  width: 100%;
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

.history-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}


.textarea-wrapper {
  width: 100%;
}

.custom-input :deep(.el-input__inner),
.custom-textarea :deep(.el-textarea__inner) {
  white-space: pre-wrap;
  word-break: break-word;
}

.markdown-editor-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.markdown-editor-left {
  flex: 1;
  min-width: 0;
}

.markdown-preview-right {
  flex: 1;
  min-width: 0;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  overflow: hidden;
}

.preview-header {
  padding: 12px 16px;
  background: #eef2ff;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
  color: #312e81;
  font-size: 14px;
}

.markdown-preview {
  margin-top: 16px;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.markdown-dialog-content {
  padding: 20px;
  min-height: 300px;
  max-height: 70vh;
  overflow-y: auto;
  line-height: 1.6;
  color: #0f172a;
}

.markdown-dialog-content :deep(h1),
.markdown-dialog-content :deep(h2),
.markdown-dialog-content :deep(h3) {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
}

.markdown-dialog-content :deep(p) {
  margin: 8px 0;
}

.markdown-dialog-content :deep(code) {
  background: #e2e8f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}

.markdown-dialog-content :deep(pre) {
  background: #0f172a;
  color: #f8fafc;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-dialog-content :deep(ul),
.markdown-dialog-content :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.markdown-dialog-content :deep(blockquote) {
  border-left: 4px solid #6366f1;
  padding-left: 12px;
  margin: 8px 0;
  color: #64748b;
}

.preview-content {
  line-height: 1.6;
  color: #0f172a;
}

.preview-content :deep(h1),
.preview-content :deep(h2),
.preview-content :deep(h3) {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
}

.preview-content :deep(p) {
  margin: 8px 0;
}

.preview-content :deep(code) {
  background: #e2e8f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}

.preview-content :deep(pre) {
  background: #0f172a;
  color: #f8fafc;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.preview-content :deep(ul),
.preview-content :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.preview-content :deep(blockquote) {
  border-left: 4px solid #6366f1;
  padding-left: 12px;
  margin: 8px 0;
  color: #64748b;
}

@media (max-width: 1024px) {
  .composer-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .history-date-picker {
    width: 100%;
  }

  .channel-grid {
    grid-template-columns: 1fr;
  }
}
</style>

