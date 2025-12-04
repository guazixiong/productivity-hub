<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useMessageStore } from '@/stores/messages'
import type { MessageChannel, MessageHistoryItem } from '@/types/messages'

const messageStore = useMessageStore()
const channelFilter = ref<'all' | MessageChannel>('all')
const statusFilter = ref<'all' | MessageHistoryItem['status']>('all')
const dateRange = ref<[Date, Date] | null>(null)
const historyDetailVisible = ref(false)
const selectedHistory = ref<MessageHistoryItem | null>(null)

const schemas: Record<MessageChannel, string> = {
  sendgrid: 'SendGrid 邮件',
  dingtalk: '钉钉 Webhook',
}

const historyPagination = computed(() => messageStore.historyPagination)

const filteredHistory = computed(() => {
  let result = [...messageStore.history]

  if (channelFilter.value !== 'all') {
    result = result.filter((item) => item.channel === channelFilter.value)
  }

  if (statusFilter.value !== 'all') {
    result = result.filter((item) => item.status === statusFilter.value)
  }

  if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
    const [start, end] = dateRange.value
    const startTime = new Date(start).setHours(0, 0, 0, 0)
    const endTime = new Date(end).setHours(23, 59, 59, 999)
    result = result.filter((item) => {
      const createdTime = new Date(item.createdAt).getTime()
      return createdTime >= startTime && createdTime <= endTime
    })
  }

  return result
})

const formatChannel = (channel: MessageChannel) => schemas[channel] || channel

const stringifyPayload = (payload?: Record<string, unknown>) => JSON.stringify(payload ?? {}, null, 2)

const openHistoryDetail = (entry: MessageHistoryItem) => {
  selectedHistory.value = entry
  historyDetailVisible.value = true
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

onMounted(() => {
  refreshHistory()
})

const resetFilters = () => {
  channelFilter.value = 'all'
  statusFilter.value = 'all'
  dateRange.value = null
}

const hasActiveFilters = computed(() => channelFilter.value !== 'all' || statusFilter.value !== 'all' || !!dateRange.value)
</script>

<template>
  <div class="message-history">
    <div class="history-header">
      <div>
        <h1>推送历史</h1>
        <p>按渠道、状态与时间聚合的推送记录，便于回溯排查。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="messageStore.loadingHistory" @click="refreshHistory">刷新数据</el-button>
        <el-button text type="primary" v-if="hasActiveFilters" @click="resetFilters">重置筛选</el-button>
      </div>
    </div>

    <el-card class="history-card" shadow="never">
      <div class="history-toolbar">
        <div class="filter-group">
          <div class="filter-item">
            <span>渠道</span>
            <el-select v-model="channelFilter" placeholder="选择渠道" popper-class="history-select-popper">
              <el-option label="全部渠道" value="all" />
              <el-option v-for="(label, key) in schemas" :key="key" :label="label" :value="key" />
            </el-select>
          </div>
          <div class="filter-item">
            <span>状态</span>
            <el-select v-model="statusFilter" placeholder="选择状态" popper-class="history-select-popper">
              <el-option label="全部状态" value="all" />
              <el-option label="成功" value="success" />
              <el-option label="失败" value="failed" />
            </el-select>
          </div>
          <div class="filter-item">
            <span>时间范围</span>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              unlink-panels
              clearable
            />
          </div>
        </div>
      </div>

      <el-table :data="filteredHistory" :loading="messageStore.loadingHistory" border stripe class="history-table">
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
      <div class="table-pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="historyPagination.total"
          :page-size="historyPagination.pageSize"
          :current-page="historyPagination.pageNum"
          :page-sizes="[10, 20, 50]"
          @current-change="handleHistoryPageChange"
          @size-change="handleHistoryPageSizeChange"
        />
      </div>
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
.message-history {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.history-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 28px;
  border-radius: 20px;
  background: linear-gradient(120deg, rgba(99, 102, 241, 0.12), rgba(14, 165, 233, 0.12));
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.history-header h1 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #0f172a;
}

.history-header p {
  margin: 0;
  color: #475569;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.history-card {
  border-radius: 20px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
}

.history-toolbar {
  margin-bottom: 16px;
}

.filter-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-item span {
  font-size: 13px;
  color: #475569;
}

.history-table :deep(.el-table__row) {
  transition: background 0.2s;
}

.history-table :deep(.el-table__row:hover) {
  background-color: rgba(99, 102, 241, 0.08);
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

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

