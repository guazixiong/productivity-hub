<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { useDevice } from '@/composables/useDevice'
import { useAgentStore } from '@/stores/agents'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const agentStore = useAgentStore()
const searchKeyword = ref('')
const formRef = ref<FormInstance>()
const activeTab = ref<'catalog' | 'history'>('catalog')
const catalogMode = ref<'grid' | 'detail'>('grid')
const invocationForm = reactive({
  agentId: '',
  mode: 'sync',
  context: '',
  input: '',
})

const logFilters = reactive({
  status: 'all',
  search: '',
  onlySelected: true,
})

const detailDrawerVisible = ref(false)
const viewingLog = ref<typeof agentStore.logs[0] | null>(null)

marked.setOptions({ gfm: true, breaks: true })

const rules: FormRules = {
  input: [{ required: true, message: '请输入调用输入内容', trigger: 'blur' }],
}

const filteredAgents = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return agentStore.agents
  return agentStore.agents.filter(
    (agent) => agent.name.toLowerCase().includes(keyword) || agent.description.toLowerCase().includes(keyword),
  )
})

const selectedAgent = computed(() => agentStore.selectedAgent)
const lastInvocation = computed(() => agentStore.lastInvocation)

const logsPagination = computed(() => agentStore.logsPagination)

const filteredLogs = computed(() => {
  const keyword = logFilters.search.trim().toLowerCase()
  return agentStore.logs.filter((log) => {
    if (logFilters.onlySelected && selectedAgent.value && log.agentId !== selectedAgent.value.id) return false
    if (logFilters.status !== 'all' && log.status !== logFilters.status) return false
    if (!keyword) return true
    return log.input.toLowerCase().includes(keyword) || log.output.toLowerCase().includes(keyword)
  })
})

const structuredData = computed<Record<string, unknown> | Record<string, unknown>[] | null>(() => {
  const raw = lastInvocation.value?.output
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw)
    if (parsed && typeof parsed === 'object') {
      return parsed as Record<string, unknown> | Record<string, unknown>[]
    }
  } catch {
    return null
  }
  return null
})

const structuredRows = computed<Record<string, unknown>[]>(() => {
  if (!structuredData.value) return []
  if (Array.isArray(structuredData.value)) {
    return structuredData.value.filter(
      (item): item is Record<string, unknown> => item !== null && typeof item === 'object',
    )
  }
  return [structuredData.value]
})

const structuredColumns = computed(() => {
  if (!structuredRows.value.length) return []
  const keys = new Set<string>()
  structuredRows.value.forEach((row) => {
    Object.keys(row).forEach((key) => keys.add(key))
  })
  return Array.from(keys)
})

const renderedOutput = computed(() => {
  const raw = lastInvocation.value?.output
  if (!raw) return ''
  return marked.parse(raw)
})

const handleSelectAgent = (id: string) => {
  agentStore.selectAgent(id)
}

const enterAgentDetail = (id: string) => {
  handleSelectAgent(id)
  catalogMode.value = 'detail'
}

const backToAgentList = () => {
  catalogMode.value = 'grid'
}

const statusTagType = (status: string) => {
  if (status === 'success') return 'success'
  if (status === 'running') return 'warning'
  return 'danger'
}

const formatDateTime = (value?: string) => {
  if (!value) return '--'
  return new Intl.DateTimeFormat('zh-CN', {
    hour12: false,
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

const formatDurationMs = (start?: string, end?: string) => {
  if (!start || !end) return '--'
  const startTime = new Date(start).getTime()
  const endTime = new Date(end).getTime()
  if (Number.isNaN(startTime) || Number.isNaN(endTime)) return '--'
  return `${Math.max(endTime - startTime, 0)} ms`
}

const handleInvoke = async () => {
  if (!formRef.value) return
  invocationForm.agentId = selectedAgent.value?.id ?? ''
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await agentStore.invokeAgent({
      agentId: invocationForm.agentId,
      input: { prompt: invocationForm.input },
      mode: invocationForm.mode as 'sync' | 'async',
      context: invocationForm.context,
    })
    ElMessage.success('调用已提交')
    invocationForm.input = ''
  } catch (error) {
    ElMessage.error((error as string) ?? '调用失败')
  }
}

const handleCopyOutput = async () => {
  if (!lastInvocation.value?.output) return
  try {
    await navigator.clipboard?.writeText(lastInvocation.value.output)
    ElMessage.success('已复制调用结果')
  } catch {
    ElMessage.warning('复制失败，请手动选择文本')
  }
}

const openLogDetail = (log: typeof agentStore.logs[0]) => {
  viewingLog.value = log
  detailDrawerVisible.value = true
}

const handleLogsPageChange = (page: number) => {
  agentStore.fetchLogs(page)
}

const handleLogsPageSizeChange = (size: number) => {
  agentStore.fetchLogs(1, size)
}

const renderedLogOutput = computed(() => {
  if (!viewingLog.value?.output) return ''
  if (isStructuredLogOutput.value) {
    return JSON.stringify(structuredLogData.value, null, 2)
  }
  return marked.parse(viewingLog.value.output)
})

const isStructuredLogOutput = computed(() => {
  if (!viewingLog.value?.output) return false
  try {
    const parsed = JSON.parse(viewingLog.value.output)
    return parsed && typeof parsed === 'object'
  } catch {
    return false
  }
})

const structuredLogData = computed<Record<string, unknown> | Record<string, unknown>[] | null>(() => {
  if (!viewingLog.value?.output || !isStructuredLogOutput.value) return null
  try {
    const parsed = JSON.parse(viewingLog.value.output)
    if (parsed && typeof parsed === 'object') {
      return parsed as Record<string, unknown> | Record<string, unknown>[]
    }
  } catch {
    return null
  }
  return null
})

const structuredLogRows = computed<Record<string, unknown>[]>(() => {
  if (!structuredLogData.value) return []
  if (Array.isArray(structuredLogData.value)) {
    return structuredLogData.value.filter(
      (item): item is Record<string, unknown> => item !== null && typeof item === 'object',
    )
  }
  return [structuredLogData.value]
})

const structuredLogColumns = computed(() => {
  if (!structuredLogRows.value.length) return []
  const keys = new Set<string>()
  structuredLogRows.value.forEach((row) => {
    Object.keys(row).forEach((key) => keys.add(key))
  })
  return Array.from(keys)
})

const agentUsageStats = computed(() => {
  const stats = new Map<string, number>()
  agentStore.logs.forEach((log) => {
    const current = stats.get(log.agentId) ?? 0
    stats.set(log.agentId, current + 1)
  })
  return stats
})

const hotAgentIdSet = computed(() => {
  const sorted = [...agentUsageStats.value.entries()].sort((a, b) => b[1] - a[1])
  const top = sorted.filter(([, count]) => count > 0).slice(0, 3)
  return new Set(top.map(([agentId]) => agentId))
})

watch(
  () => agentStore.selectedAgentId,
  (id) => {
    if (id) {
      invocationForm.agentId = id
    } else {
      catalogMode.value = 'grid'
    }
  },
)

watch(
  () => activeTab.value,
  (tab) => {
    if (tab === 'history' && !agentStore.logs.length) {
      agentStore.fetchLogs()
    }
  },
)

onMounted(async () => {
  await agentStore.fetchAgents()
  await agentStore.fetchLogs()
})
</script>

<template>
  <div class="agents-shell">
    <el-card class="agents-card">
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="智能体列表" name="catalog">
          <template v-if="catalogMode === 'grid'">
            <div class="catalog-header">
              <h2>智能体目录</h2>
              <div class="catalog-filters">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索智能体名称或描述"
                  clearable
                  :prefix-icon="Search"
                  class="catalog-search"
                />
                <el-button text type="primary" @click="agentStore.fetchAgents(true)" :loading="agentStore.loadingAgents">
                  刷新
                </el-button>
              </div>
            </div>
            <el-skeleton :loading="agentStore.loadingAgents" animated>
              <template #default>
                <el-empty v-if="!filteredAgents.length" description="暂无智能体" />
                <div v-else class="agent-grid">
                  <el-card
                    v-for="agent in filteredAgents"
                    :key="agent.id"
                    class="agent-tile"
                    shadow="hover"
                    :class="agent.id === agentStore.selectedAgentId && 'active'"
                    @click="enterAgentDetail(agent.id)"
                  >
                    <div class="agent-hot-badge" v-if="hotAgentIdSet.has(agent.id)">热</div>
                    <div class="card-title">
                      <h3>{{ agent.name }}</h3>
                      <el-tag size="small">{{ agent.version }}</el-tag>
                    </div>
                    <p>{{ agent.description }}</p>
                    <div class="tag-group">
                      <el-tag v-for="tag in agent.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
                    </div>
                    <small>负责人：{{ agent.owner }} · 延迟 {{ agent.latencyMs }}ms</small>
                  </el-card>
                </div>
              </template>
            </el-skeleton>
          </template>
          <template v-else>
            <div class="detail-header">
              <el-button text type="primary" @click="backToAgentList">← 返回列表</el-button>
              <div class="detail-title" v-if="selectedAgent">
                <div>
                  <h2>{{ selectedAgent.name }}</h2>
                  <p>{{ selectedAgent.description }}</p>
                </div>
                <el-tag size="large">{{ selectedAgent.version }}</el-tag>
              </div>
            </div>
            <el-empty v-if="!selectedAgent" description="请选择智能体" />
            <template v-else>
              <div class="tag-group">
                <el-tag v-for="tag in selectedAgent.tags" :key="tag" effect="light">{{ tag }}</el-tag>
              </div>
              <el-descriptions :column="3" size="small" border class="agent-meta">
                <el-descriptions-item label="负责人">{{ selectedAgent.owner }}</el-descriptions-item>
                <el-descriptions-item label="平均延迟">{{ selectedAgent.latencyMs }}ms</el-descriptions-item>
                <el-descriptions-item label="版本号">{{ selectedAgent.version }}</el-descriptions-item>
              </el-descriptions>
              <div class="detail-body">
                <el-card class="invoke-card">
                  <template #header>
                    <h3>发起调用</h3>
                  </template>
                  <el-form ref="formRef" :model="invocationForm" :rules="rules" label-position="top">
                    <el-form-item label="执行模式">
                      <el-radio-group v-model="invocationForm.mode">
                        <el-radio-button label="sync">同步</el-radio-button>
                        <el-radio-button label="async">异步</el-radio-button>
                      </el-radio-group>
                    </el-form-item>
                    <el-form-item label="上下文（可选）">
                      <el-input
                        v-model="invocationForm.context"
                        type="textarea"
                        rows="3"
                        placeholder="传入额外上下文，例如会话配置 / 业务参数"
                      />
                    </el-form-item>
                    <el-form-item label="输入内容" prop="input">
                      <el-input
                        v-model="invocationForm.input"
                        type="textarea"
                        rows="5"
                        placeholder="请输入 JSON 或自然语言，将被注入智能体"
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" :loading="agentStore.invoking" @click="handleInvoke">调用智能体</el-button>
                    </el-form-item>
                  </el-form>
                </el-card>

                <el-card class="result-card">
                  <template #header>
                    <div class="card-title">
                      <h3>最近一次调用结果</h3>
                      <el-button text size="small" :disabled="!lastInvocation" @click="handleCopyOutput">复制结果</el-button>
                    </div>
                  </template>
                  <el-empty v-if="!lastInvocation" description="还没有调用记录" />
                  <template v-else>
                    <div class="result-header">
                      <div>
                        <small>任务 ID</small>
                        <p class="task-id">{{ lastInvocation.taskId }}</p>
                      </div>
                      <el-tag :type="statusTagType(lastInvocation.status)" size="large">{{ lastInvocation.status }}</el-tag>
                    </div>
                    <el-descriptions :column="3" size="small" border>
                      <el-descriptions-item label="开始时间">{{ formatDateTime(lastInvocation.startedAt) }}</el-descriptions-item>
                      <el-descriptions-item label="结束时间">{{ formatDateTime(lastInvocation.finishedAt) }}</el-descriptions-item>
                      <el-descriptions-item label="耗时">{{ formatDurationMs(lastInvocation.startedAt, lastInvocation.finishedAt) }}</el-descriptions-item>
                    </el-descriptions>
                    <el-divider />
                    <template v-if="structuredColumns.length">
                      <el-table :data="structuredRows" size="small" border>
                        <el-table-column v-for="column in structuredColumns" :key="column" :prop="column" :label="column" />
                      </el-table>
                    </template>
                    <div v-else class="markdown-output" v-html="renderedOutput" />
                  </template>
                </el-card>
              </div>
            </template>
          </template>
        </el-tab-pane>
        <el-tab-pane label="调用历史" name="history">
          <div class="logs-toolbar">
            <div class="logs-filters-left">
              <el-radio-group v-model="logFilters.status" size="default" class="status-filter-group">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="success">成功</el-radio-button>
                <el-radio-button label="running">执行中</el-radio-button>
                <el-radio-button label="failed">失败</el-radio-button>
              </el-radio-group>
              <el-switch v-model="logFilters.onlySelected" size="default" active-text="仅当前智能体" />
            </div>
            <div class="logs-filters-right">
              <el-input
                v-model="logFilters.search"
                size="default"
                placeholder="搜索输入或输出"
                clearable
                :prefix-icon="Search"
                class="logs-search"
              />
              <el-button text type="primary" @click="agentStore.fetchLogs()" :loading="agentStore.loadingLogs">刷新</el-button>
            </div>
          </div>
          <el-empty v-if="!agentStore.loadingLogs && !filteredLogs.length" description="暂无符合条件的记录" />
          <el-table v-else :data="filteredLogs" :loading="agentStore.loadingLogs" border>
            <el-table-column prop="agentName" label="智能体" width="160" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.status === 'success' ? 'success' : row.status === 'running' ? 'warning' : 'danger'">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="耗时(ms)" width="120" />
            <el-table-column prop="createdAt" label="时间" width="190" />
            <el-table-column prop="input" label="输入" min-width="200" show-overflow-tooltip />
            <el-table-column prop="output" label="输出" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openLogDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="logs-pagination" v-if="agentStore.logs.length">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :total="logsPagination.total"
              :page-size="logsPagination.pageSize"
              :current-page="logsPagination.pageNum"
              :page-sizes="[10, 20, 50]"
              @current-change="handleLogsPageChange"
              @size-change="handleLogsPageSizeChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-drawer v-model="detailDrawerVisible" title="调用详情" size="60%">
      <template v-if="viewingLog">
        <el-descriptions :column="2" border class="log-detail-descriptions">
          <el-descriptions-item label="智能体">
            <el-tag size="large">{{ viewingLog.agentName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(viewingLog.status)" size="large">{{ viewingLog.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务 ID">
            <code class="task-id-code">{{ viewingLog.id || '--' }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">
            <span class="duration-text">{{ viewingLog.duration }} ms</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            <span class="time-text">{{ formatDateTime(viewingLog.createdAt) }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div class="log-content-section">
          <h3 class="section-title">输入内容</h3>
          <div class="log-content-box">
            <el-input
              :model-value="viewingLog.input"
              type="textarea"
              :rows="viewingLog.input.length > 200 ? 8 : 6"
              readonly
              class="log-content-input"
            />
          </div>
        </div>
        <el-divider />
        <div class="log-content-section">
          <div class="section-header">
            <h3 class="section-title">输出内容</h3>
            <el-button
              text
              type="primary"
              size="small"
              @click="
                navigator.clipboard?.writeText(viewingLog.output || '').then(() => {
                  ElMessage.success('已复制输出内容')
                })
              "
            >
              复制
            </el-button>
          </div>
          <div class="log-content-box">
            <template v-if="isStructuredLogOutput && structuredLogColumns.length">
              <el-table :data="structuredLogRows" size="small" border>
                <el-table-column v-for="column in structuredLogColumns" :key="column" :prop="column" :label="column" />
              </el-table>
            </template>
            <template v-else-if="isStructuredLogOutput">
              <el-input
                :model-value="renderedLogOutput"
                type="textarea"
                :rows="10"
                readonly
                class="log-content-input"
              />
            </template>
            <template v-else>
              <div class="markdown-output" v-html="renderedLogOutput" />
            </template>
          </div>
        </div>
      </template>
      <template #footer>
        <span class="drawer-footer">
          <el-button @click="detailDrawerVisible = false">关闭</el-button>
        </span>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.agents-shell {
  display: flex;
  flex-direction: column;
}

.agents-card {
  border-radius: 24px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.95) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 
    0 20px 60px rgba(15, 23, 42, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  position: relative;
  overflow: hidden;
}

.agents-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #ec4899);
  opacity: 0.8;
}

.catalog-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.catalog-filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
}

.catalog-header h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(135deg, #0f172a 0%, #475569 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.catalog-search {
  max-width: 400px;
  min-width: 200px;
  flex: 1;
}

.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.agent-tile {
  cursor: pointer;
  border: 1px solid rgba(99, 102, 241, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%);
  backdrop-filter: blur(10px);
  overflow: hidden;
}

.agent-tile::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 0;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: height 0.3s ease;
}

.agent-tile.active {
  border-color: rgba(99, 102, 241, 0.4);
  border-width: 2px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.08) 100%);
  box-shadow: 
    0 8px 24px rgba(99, 102, 241, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.3) inset;
}

.agent-tile.active::before {
  height: 3px;
}

.agent-tile:hover {
  transform: translateY(-6px) scale(1.02);
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 
    0 12px 32px rgba(99, 102, 241, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.3) inset;
}

.agent-tile:hover::before {
  height: 3px;
}

.agent-hot-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
  z-index: 1;
  animation: hot-badge-pulse 2s ease-in-out infinite;
}

@keyframes hot-badge-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 6px 16px rgba(239, 68, 68, 0.5);
  }
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 8px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.detail-title {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
  gap: 24px;
}

.detail-title h2 {
  margin: 0;
}

.detail-title p {
  margin: 6px 0 0;
  color: #475569;
}

.agent-meta {
  margin-bottom: 20px;
}

.detail-body {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.invoke-card,
.result-card {
  border-radius: 20px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.task-id {
  margin: 0;
  font-weight: 600;
}

.markdown-output {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  line-height: 1.6;
}

.logs-toolbar {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.95) 0%, rgba(241, 245, 249, 0.9) 100%);
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.1);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
  backdrop-filter: blur(10px);
}

.logs-filters-left {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.logs-filters-right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.status-filter-group {
  font-size: 15px;
}

.status-filter-group :deep(.el-radio-button__inner) {
  padding: 10px 20px;
  font-size: 15px;
  font-weight: 500;
}

.logs-search {
  max-width: 300px;
  min-width: 200px;
}

.logs-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.log-detail-descriptions {
  margin-bottom: 20px;
}

.task-id-code {
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  color: #6366f1;
  font-size: 13px;
}

.duration-text {
  font-weight: 600;
  color: #6366f1;
}

.time-text {
  color: #475569;
}

.log-content-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.log-content-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}

.log-content-input :deep(.el-textarea__inner) {
  background: transparent;
  border: none;
  padding: 0;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #0f172a;
}

.markdown-output {
  line-height: 1.6;
  color: #0f172a;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 移动端适配 - REQ-001 */
@media (max-width: 768px) {
  .agents-shell {
    padding: 0;
  }

  .agents-card {
    border-radius: 0;
    margin: 0 -12px;
  }

  .catalog-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;

    h2 {
      font-size: 20px;
    }
  }

  .catalog-filters {
    width: 100%;
    flex-direction: column;
    align-items: stretch;

    .catalog-search {
      width: 100%;
      max-width: none;
      min-width: 0;
    }
  }

  .agent-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .agent-tile {
    padding: 16px !important;

    .card-title {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
  }

  .detail-header {
    margin-bottom: 16px;
  }

  .detail-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .detail-body {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .invoke-card,
  .result-card {
    :deep(.el-card__header) {
      padding: 16px;
    }

    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  .agent-meta {
    :deep(.el-descriptions__table) {
      .el-descriptions__label {
        width: 80px;
      }
    }
  }

  .logs-filters {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .logs-search {
    width: 100%;
    max-width: none;
  }

  .logs-table {
    :deep(.el-table) {
      font-size: 0.9em;
    }

    :deep(.el-table th),
    :deep(.el-table td) {
      padding: 8px 4px;
    }
  }

  .logs-pagination {
    justify-content: center;

    :deep(.el-pagination) {
      font-size: 0.9em;
    }

    :deep(.el-pagination__sizes),
    :deep(.el-pagination__jump) {
      display: none;
    }
  }

  .log-detail-drawer {
    :deep(.el-drawer__body) {
      padding: 16px;
    }
  }

  .log-content-box {
    padding: 12px;
    font-size: 0.9em;
  }
}
</style>

