<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { NotificationItem } from '@/types/notifications'
import type { PageResult } from '@/types/common'
import { notificationApi } from '@/services/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const enabled = ref(true)
const items = ref<NotificationItem[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const drawerVisible = ref(false)
const current = ref<NotificationItem | null>(null)

const fetchData = async (page = pageNum.value, size = pageSize.value) => {
  loading.value = true
  try {
    const res = await notificationApi.list({ page, pageSize: size })
    const pageData = res as PageResult<NotificationItem>
    items.value = pageData.items
    pageNum.value = pageData.pageNum
    pageSize.value = pageData.pageSize
    total.value = pageData.total
  } catch (error) {
    const msg = error instanceof Error ? error.message : '加载通知失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const handleRowClick = (row: NotificationItem) => {
  current.value = row
  drawerVisible.value = true
  if (!row.read) {
    notificationApi
      .markRead(row.id)
      .then(() => {
        row.read = true
      })
      .catch(() => {
        // ignore
      })
  }
}

const handlePageChange = (page: number) => {
  fetchData(page, pageSize.value)
}

const handlePageSizeChange = (size: number) => {
  fetchData(1, size)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="notification-center">
    <el-card shadow="never" class="notification-card">
      <template #header>
        <div class="card-header">
          <div>
            <h3>通知中心</h3>
            <p>查看系统推送的实时通知，支持分页与详情查看</p>
          </div>
          <div class="card-header-actions">
            <el-tooltip content="启用 / 停用通知中心" placement="left">
              <el-switch v-model="enabled" />
            </el-tooltip>
          </div>
        </div>
      </template>

      <el-table
        :data="enabled ? items : []"
        :loading="loading"
        border
        stripe
        @row-click="handleRowClick"
        :class="['notification-table', { 'notification-table--disabled': !enabled }]"
      >
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.read ? 'info' : 'success'">
              {{ row.read ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="handleRowClick(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          :disabled="!enabled"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </el-card>

    <el-drawer v-model="drawerVisible" title="通知详情" size="40%">
      <template v-if="current">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">
            {{ current.title }}
          </el-descriptions-item>
          <el-descriptions-item label="时间">
            {{ current.createdAt }}
          </el-descriptions-item>
          <el-descriptions-item label="跳转路径" v-if="current.link">
            {{ current.link }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="current.read ? 'info' : 'success'">
              {{ current.read ? '已读' : '未读' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4>内容</h4>
          <p class="detail-content">
            {{ current.content }}
          </p>
        </div>

        <div class="detail-section" v-if="current.extra && Object.keys(current.extra).length">
          <h4>附加数据</h4>
          <pre class="extra-json">{{ JSON.stringify(current.extra, null, 2) }}</pre>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.notification-center {
  padding: 20px 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background-color: #f5f7fb;
}

.notification-card {
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.card-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.card-header p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.notification-table :deep(.el-table__header-wrapper th) {
  padding: 14px 8px;
  font-size: 13px;
  background-color: #f8fafc;
}

.notification-table :deep(.el-table__row) {
  height: 52px;
}

.notification-table :deep(.cell) {
  padding: 8px 10px;
  font-size: 13px;
}

.notification-table {
  margin-top: 4px;
}

.notification-table--disabled {
  opacity: 0.55;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-section {
  margin-top: 24px;
}

.detail-section h4 {
  margin: 0 0 10px;
}

.detail-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.extra-json {
  margin: 0;
  background: #0f172a;
  color: #e5e7eb;
  padding: 14px 16px;
  border-radius: 10px;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>


