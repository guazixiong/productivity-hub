<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { useAgentStore } from '@/stores/agents'

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
              <div>
                <h2>智能体目录</h2>
                <p>按需筛选智能体，点击卡片进入执行界面</p>
              </div>
              <el-input v-model="searchKeyword" placeholder="搜索智能体" clearable :prefix-icon="Search" />
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
            <el-radio-group v-model="logFilters.status" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="success">成功</el-radio-button>
              <el-radio-button label="running">执行中</el-radio-button>
              <el-radio-button label="failed">失败</el-radio-button>
            </el-radio-group>
            <el-switch v-model="logFilters.onlySelected" size="small" active-text="仅当前智能体" />
            <el-input
              v-model="logFilters.search"
              size="small"
              placeholder="搜索输入或输出"
              clearable
              :prefix-icon="Search"
              class="logs-search"
            />
            <el-button text type="primary" @click="agentStore.fetchLogs()" :loading="agentStore.loadingLogs">刷新</el-button>
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
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.agents-shell {
  display: flex;
  flex-direction: column;
}

.agents-card {
  border-radius: 24px;
  border: 1px solid rgba(99, 102, 241, 0.16);
}

.catalog-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-end;
  margin-bottom: 16px;
}

.catalog-header p {
  margin: 4px 0 0;
  color: #475569;
}

.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.agent-tile {
  cursor: pointer;
  border: 1px solid transparent;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.agent-tile.active {
  border-color: #6366f1;
}

.agent-tile:hover {
  transform: translateY(-4px);
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
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
  align-items: center;
}

.logs-search {
  max-width: 260px;
}

@media (max-width: 768px) {
  .catalog-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-title {
    flex-direction: column;
  }

  .detail-body {
    grid-template-columns: 1fr;
  }

  .logs-search {
    width: 100%;
    max-width: none;
  }
}
</style>

