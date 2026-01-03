<script setup lang="ts">
/**
 * 公告管理页面组件
 */
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh, Search, Upload, Timer } from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'
import { announcementApi } from '@/services/announcementApi'
import type { Announcement, AnnouncementCreateDTO, AnnouncementStats } from '@/types/announcement'
import type { PageResult } from '@/types/common'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

interface QueryParams {
  page: number
  pageSize: number
  status: '' | 'DRAFT' | 'PUBLISHED' | 'WITHDRAWN'
  keyword: string
}

type FormMode = 'create' | 'edit'

const loading = ref(false)
const saving = ref(false)
const statsLoading = ref(false)
const tableData = ref<Announcement[]>([])
const total = ref(0)

const query = reactive<QueryParams>({
  page: 1,
  pageSize: 10,
  status: '',
  keyword: '',
})

const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const formRef = ref<FormInstance>()
const formModel = reactive<AnnouncementCreateDTO & { id?: string; requireConfirm: number }>({
  title: '',
  content: '',
  richContent: '',
  link: '',
  type: 'NORMAL',
  priority: 1,
  pushStrategy: 'IMMEDIATE',
  requireConfirm: 0,
  effectiveTime: '',
  expireTime: '',
  scheduledTime: '',
})

const formRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }],
  pushStrategy: [{ required: true, message: '请选择推送策略', trigger: 'change' }],
  scheduledTime: [
    {
      validator: (_rule, value, callback) => {
        if (formModel.pushStrategy === 'SCHEDULED' && !value) {
          callback(new Error('定时推送需选择时间'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

const statsVisible = ref(false)
const stats = ref<AnnouncementStats | null>(null)

const isScheduled = computed(() => formModel.pushStrategy === 'SCHEDULED')

const statusTagType = (status: Announcement['status']) => {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'WITHDRAWN') return 'warning'
  return 'info'
}

const typeTagType = (type: Announcement['type']) => {
  if (type === 'URGENT') return 'danger'
  if (type === 'WARNING') return 'warning'
  if (type === 'INFO') return 'info'
  return ''
}

const pushStrategyLabel = (strategy: Announcement['pushStrategy']) => {
  const map = {
    IMMEDIATE: '立即推送',
    LOGIN: '登录提醒',
    SCHEDULED: '定时推送',
  }
  return map[strategy] || strategy
}

const fetchList = async () => {
  loading.value = true
  try {
    const res: PageResult<Announcement> = await announcementApi.admin.page({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
    })
    tableData.value = res.items ?? []
    total.value = res.total ?? 0
  } catch (error) {
    ElMessage.error('加载公告列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleReset = () => {
  query.status = ''
  query.keyword = ''
  query.page = 1
  fetchList()
}

const filteredTableData = computed(() => {
  if (!query.keyword.trim()) return tableData.value
  const kw = query.keyword.trim().toLowerCase()
  return tableData.value.filter((item) => item.title.toLowerCase().includes(kw))
})

const openCreate = () => {
  formMode.value = 'create'
  Object.assign(formModel, {
    id: undefined,
    title: '',
    content: '',
    richContent: '',
    link: '',
    type: 'NORMAL' as Announcement['type'],
    priority: 1,
    pushStrategy: 'IMMEDIATE' as Announcement['pushStrategy'],
    requireConfirm: 0,
    effectiveTime: '',
    expireTime: '',
    scheduledTime: '',
  })
  formVisible.value = true
}

const openEdit = (row: Announcement) => {
  formMode.value = 'edit'
  Object.assign(formModel, {
    id: row.id,
    title: row.title,
    content: row.content ?? '',
    richContent: row.richContent ?? '',
    link: row.link ?? '',
    type: row.type,
    priority: row.priority,
    pushStrategy: row.pushStrategy,
    requireConfirm: row.requireConfirm ?? 0,
    effectiveTime: row.effectiveTime ?? '',
    expireTime: row.expireTime ?? '',
    scheduledTime: row.scheduledTime ?? '',
  })
  formVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    if (formMode.value === 'create') {
      await announcementApi.admin.create(formModel)
      ElMessage.success('创建成功')
    } else if (formModel.id) {
      await announcementApi.admin.update(formModel.id, formModel)
      ElMessage.success('更新成功')
    }
    formVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
    console.error(error)
  } finally {
    saving.value = false
  }
}

const handlePublish = async (row: Announcement) => {
  await ElMessageBox.confirm(`确认发布「${row.title}」吗？`, '发布公告', { type: 'warning' })
  try {
    await announcementApi.admin.publish(row.id)
    ElMessage.success('已发布')
    fetchList()
  } catch (error) {
    ElMessage.error('发布失败')
    console.error(error)
  }
}

const handleWithdraw = async (row: Announcement) => {
  await ElMessageBox.confirm(`确认撤回「${row.title}」吗？`, '撤回公告', { type: 'warning' })
  try {
    await announcementApi.admin.withdraw(row.id)
    ElMessage.success('已撤回')
    fetchList()
  } catch (error) {
    ElMessage.error('撤回失败')
    console.error(error)
  }
}

const handleDelete = async (row: Announcement) => {
  await ElMessageBox.confirm(`确认删除「${row.title}」吗？删除后不可恢复。`, '删除公告', { type: 'warning' })
  try {
    await announcementApi.admin.delete(row.id)
    ElMessage.success('已删除')
    fetchList()
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

const openStats = async (row: Announcement) => {
  statsVisible.value = true
  statsLoading.value = true
  try {
    stats.value = await announcementApi.admin.getStats(row.id)
  } catch (error) {
    ElMessage.error('获取统计失败')
    console.error(error)
  } finally {
    statsLoading.value = false
  }
}

const handlePageChange = (page: number) => {
  query.page = page
  fetchList()
}

const handlePageSizeChange = (size: number) => {
  query.pageSize = size
  query.page = 1
  fetchList()
}

watch(
  () => formModel.pushStrategy,
  (val) => {
    if (val !== 'SCHEDULED') {
      formModel.scheduledTime = ''
    }
  }
)

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div class="page-title">
        <h2>公告管理</h2>
      </div>
      <div class="page-actions">
        <el-button :icon="Refresh" @click="fetchList">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新建公告</el-button>
      </div>
    </div>

    <div class="filters">
      <el-form inline label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 180px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已撤回" value="WITHDRAWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题搜索">
          <el-input
            v-model="query.keyword"
            placeholder="输入标题关键词"
            clearable
            style="width: 220px"
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="table-wrapper">
        <el-table
          v-loading="loading"
          :data="filteredTableData"
          border
          class="announcement-table"
          empty-text="暂无公告"
        >
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" effect="plain">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" />
        <el-table-column label="推送策略" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.pushStrategy === 'SCHEDULED'" size="small" type="info" effect="plain">
              <el-icon class="tag-icon"><Timer /></el-icon>
              定时
            </el-tag>
            <span v-else>{{ pushStrategyLabel(row.pushStrategy) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="220">
          <template #default="{ row }">
            <div class="cell-text">
              <div v-if="row.effectiveTime">生效：{{ row.effectiveTime }}</div>
              <div v-if="row.expireTime">过期：{{ row.expireTime }}</div>
              <div v-if="!row.effectiveTime && !row.expireTime" class="text-muted">未设置</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" fixed="right" width="260">
          <template #default="{ row }">
            <el-space size="8">
              <el-button type="primary" text size="small" @click="openEdit(row)">编辑</el-button>
              <el-button
                type="success"
                text
                size="small"
                @click="handlePublish(row)"
                :disabled="row.status === 'PUBLISHED'"
              >
                发布
              </el-button>
              <el-button
                type="warning"
                text
                size="small"
                @click="handleWithdraw(row)"
                :disabled="row.status !== 'PUBLISHED'"
              >
                撤回
              </el-button>
              <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
              <el-button text size="small" :icon="Upload" @click="openStats(row)">统计</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          background
          layout="prev, pager, next, jumper, ->, sizes, total"
          :page-sizes="[10, 20, 50]"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新建公告' : '编辑公告'"
      :width="isMobile ? '100%' : '700px'"
      :fullscreen="isMobile"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px" label-position="right">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formModel.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="formModel.content" type="textarea" :rows="3" placeholder="支持纯文本或富文本" />
        </el-form-item>
        <el-form-item label="链接" prop="link">
          <el-input v-model="formModel.link" placeholder="可选，点击跳转的链接" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formModel.type" style="width: 200px">
            <el-option label="普通" value="NORMAL" />
            <el-option label="信息" value="INFO" />
            <el-option label="警告" value="WARNING" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="formModel.priority" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="推送策略" prop="pushStrategy">
          <el-radio-group v-model="formModel.pushStrategy">
            <el-radio-button label="IMMEDIATE">立即</el-radio-button>
            <el-radio-button label="LOGIN">登录提醒</el-radio-button>
            <el-radio-button label="SCHEDULED">定时</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="isScheduled" label="定时时间" prop="scheduledTime">
          <el-date-picker
            v-model="formModel.scheduledTime"
            type="datetime"
            placeholder="选择发布时间"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="有效期">
          <el-space wrap>
            <el-date-picker
              v-model="formModel.effectiveTime"
              type="datetime"
              placeholder="生效时间（可选）"
              style="width: 220px"
            />
            <el-date-picker
              v-model="formModel.expireTime"
              type="datetime"
              placeholder="失效时间（可选）"
              style="width: 220px"
            />
          </el-space>
        </el-form-item>
        <el-form-item label="需要确认">
          <el-switch
            v-model="formModel.requireConfirm"
            inline-prompt
            :active-value="1"
            :inactive-value="0"
            active-text="是"
            inactive-text="否"
          />
        </el-form-item>
        <el-alert
          v-if="formModel.requireConfirm === 1"
          title="开启后，用户需点确认才算已读"
          type="info"
          show-icon
          :closable="false"
        />
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="statsVisible" title="阅读统计" :size="isMobile ? '100%' : '360px'">
      <el-skeleton v-if="statsLoading" rows="4" animated />
      <el-empty v-else-if="!stats" description="暂无数据" />
      <div v-else class="stats-panel">
        <div class="stat-item">
          <div class="stat-label">推送用户数</div>
          <div class="stat-value">{{ stats.totalUsers }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">已读用户数</div>
          <div class="stat-value">{{ stats.readUsers }}</div>
        </div>
        <div class="stat-item highlight">
          <div class="stat-label">阅读率</div>
          <div class="stat-value">{{ (stats.readRate * 100).toFixed(1) }}%</div>
        </div>
        <el-alert
          v-if="(stats.readRate ?? 0) < 0.5"
          type="warning"
          :title="`阅读率偏低，建议调整文案或推送策略`"
          show-icon
          :closable="false"
        />
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 0;
  color: var(--text-tertiary);
  font-size: 13px;
}

.page-actions {
  display: flex;
  gap: 8px;
}

.filters {
  background: #fff;
  border: 1px solid var(--el-border-color);
  border-radius: 10px;
  padding: 12px;
}

.table-card {
  border-radius: 10px;
}

.table-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.table-wrapper::-webkit-scrollbar {
  height: 6px;
}

.table-wrapper::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.table-wrapper::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.table-wrapper::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.announcement-table {
  min-width: 1000px;
}

.cell-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.text-muted {
  color: var(--text-tertiary);
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 4px 0;
}

.stats-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  padding: 10px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: #fafafa;
}

.stat-item.highlight {
  border-color: #dbeafe;
  background: #eff6ff;
}

.stat-label {
  color: var(--text-tertiary);
  font-size: 13px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  margin-top: 4px;
}

.tag-icon {
  margin-right: 4px;
}

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .page {
    gap: 8px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .page-title h2 {
    font-size: 18px;
  }

  .page-actions {
    width: 100%;
    flex-direction: column;
    gap: 8px;
  }

  .page-actions .el-button {
    width: 100%;
  }

  .filters {
    padding: 12px 8px;
  }

  .filters :deep(.el-form) {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .filters :deep(.el-form-item) {
    margin: 0;
    width: 100%;
  }

  .filters :deep(.el-form-item__label) {
    width: 80px !important;
    text-align: left;
  }

  .filters :deep(.el-select),
  .filters :deep(.el-input) {
    width: 100% !important;
  }

  .filters :deep(.el-form-item__content) {
    flex: 1;
  }

  .filters :deep(.el-button) {
    width: 100%;
  }

  /* 表格横向滚动支持 - REQ-001-05 */
  .table-card :deep(.el-card__body) {
    padding: 12px;
  }

  .table-card :deep(.el-table__cell) {
    padding: 8px 6px;
    font-size: 13px;
  }

  .table-card :deep(.el-table__header-wrapper th) {
    padding: 8px 6px;
    font-size: 12px;
  }

  /* 禁用移动端hover效果 */
  .table-card :deep(.el-table__body tr:hover > td) {
    background: transparent !important;
  }

  .pager {
    padding: 12px 4px 0;
  }

  .pager :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }

  .pager :deep(.el-pagination__sizes),
  .pager :deep(.el-pagination__jump) {
    margin-top: 8px;
    width: 100%;
    justify-content: center;
  }

  /* 对话框移动端适配 */
  .el-dialog {
    margin: 0 !important;
    max-height: 100vh;
  }

  .el-dialog :deep(.el-dialog__body) {
    max-height: calc(100vh - 120px);
    overflow-y: auto;
  }

  .el-dialog :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  .el-dialog :deep(.el-form-item__label) {
    width: 96px !important;
  }

  .el-dialog :deep(.el-radio-group) {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .el-dialog :deep(.el-radio-button) {
    width: 100%;
  }

  .el-dialog :deep(.el-radio-button__inner) {
    width: 100%;
  }

  .el-dialog :deep(.el-space) {
    width: 100%;
    flex-direction: column;
  }

  .el-dialog :deep(.el-space__item) {
    width: 100%;
  }

  .el-dialog :deep(.el-date-picker) {
    width: 100% !important;
  }

  /* 抽屉移动端适配 */
  .el-drawer {
    border-radius: 0;
  }

  .stats-panel {
    gap: 10px;
  }

  .stat-item {
    padding: 12px;
  }

  .stat-value {
    font-size: 18px;
  }
}
</style>

