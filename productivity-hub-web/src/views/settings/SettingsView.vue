<script setup lang="ts">
/**
 * 设置页面组件
 */
import { computed, reactive, ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useDevice } from '@/composables/useDevice'
import { useConfigStore } from '@/stores/config'
import type { ConfigItem } from '@/types/config'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const configStore = useConfigStore()
const searchKeyword = ref('')
const editDrawerVisible = ref(false)
const detailDrawerVisible = ref(false)
const formRef = ref<FormInstance>()
const editingModule = ref('')
const editingModuleDescription = ref('')
const editingConfigs = reactive<Record<string, { value: string }>>({})
const viewingModule = ref<string>('')

interface ModuleInfo {
  module: string
  description: string
  createdAt: string
  updatedAt: string
  configCount: number
}

const filteredConfigs = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return configStore.configs
  return configStore.configs.filter(
    (item) =>
      item.key.toLowerCase().includes(keyword) ||
      item.module.toLowerCase().includes(keyword) ||
      item.description.toLowerCase().includes(keyword),
  )
})

const moduleList = computed(() => {
  const moduleMap = new Map<string, ConfigItem[]>()
  
  filteredConfigs.value.forEach((item) => {
    if (!moduleMap.has(item.module)) {
      moduleMap.set(item.module, [])
    }
    moduleMap.get(item.module)!.push(item)
  })

  const modules: ModuleInfo[] = []
  moduleMap.forEach((configs, module) => {
    const dates = configs.map((c) => parseDate(c.updatedAt).getTime())
    const createdDates = configs.map((c) => parseDate(c.createdAt).getTime())
    const latestUpdate = new Date(Math.max(...dates))
    const earliestCreate = new Date(Math.min(...createdDates))
    
    // 获取模块描述，与编辑模块信息中的保持一致
    const description = configStore.getModuleDescription(module) || ''
    
    modules.push({
      module,
      description,
      createdAt: formatDate(earliestCreate),
      updatedAt: formatDate(latestUpdate),
      configCount: configs.length,
    })
  })

  return modules.sort((a, b) => a.module.localeCompare(b.module))
})

const parseDate = (dateStr: string): Date => {
  const normalized = dateStr.replace(' ', 'T')
  const date = new Date(normalized)
  return isNaN(date.getTime()) ? new Date() : date
}

const formatDate = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const moduleConfigs = computed(() => {
  if (!editingModule.value) return []
  return configStore.configs.filter((item) => item.module === editingModule.value)
})

const viewingModuleConfigs = computed(() => {
  if (!viewingModule.value) return []
  return configStore.configs.filter((item) => item.module === viewingModule.value)
})

const openEditDrawer = (module: string) => {
  editingModule.value = module
  editingModuleDescription.value = configStore.getModuleDescription(module) || ''
  Object.keys(editingConfigs).forEach((key) => delete editingConfigs[key])
  moduleConfigs.value.forEach((item) => {
    editingConfigs[item.id] = {
      value: item.value,
    }
  })
  editDrawerVisible.value = true
}

const openDetailDrawer = (module: string) => {
  viewingModule.value = module
  detailDrawerVisible.value = true
}

const handleEditSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    // 保存模块描述（作为配置项保存）
    if (editingModule.value) {
      await configStore.updateModuleDescription(editingModule.value, editingModuleDescription.value)
    }
    const updates = Object.entries(editingConfigs).map(([id, data]) => ({
      id,
      value: data.value,
    }))
    for (const update of updates) {
      await configStore.updateConfig(update)
    }
    ElMessage.success('配置已更新')
    editDrawerVisible.value = false
  } catch (error) {
    ElMessage.error((error as string) ?? '更新失败')
  }
}

onMounted(() => {
  configStore.fetchConfigs()
})
</script>

<template>
  <el-card class="config-card">
    <template #header>
      <div class="card-header" :class="{ 'mobile-layout': isMobile }">
        <div>
          <h2>全局参数配置</h2>
          <p>配置项通过 Pinia 缓存，编辑后自动刷新</p>
        </div>
        <div class="actions" :class="{ 'mobile-actions': isMobile }">
          <el-input v-model="searchKeyword" placeholder="搜索模块 / Key / 描述" clearable :prefix-icon="Search" />
          <el-button :loading="configStore.loading" @click="configStore.fetchConfigs(true)">刷新缓存</el-button>
        </div>
      </div>
    </template>

    <div class="table-wrapper">
      <el-table
        :data="moduleList"
        :loading="configStore.loading"
        stripe
        class="config-table"
        empty-text="暂无配置"
      >
        <el-table-column prop="module" label="模块" :width="isMobile ? 120 : 160">
          <template #default="{ row }">
            <el-tag type="primary" :size="isMobile ? 'default' : 'large'">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" :min-width="isMobile ? 120 : 150" />
        <el-table-column v-if="!isMobile" prop="createdAt" label="创建时间" width="180" />
        <el-table-column v-if="!isMobile" prop="updatedAt" label="最近更新时间" width="180" />
        <el-table-column label="操作" :width="isMobile ? 120 : 200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons" :class="{ 'mobile-actions': isMobile }">
              <el-button link type="primary" :size="isMobile ? 'small' : 'default'" @click="openDetailDrawer(row.module)">详情</el-button>
              <el-button link type="primary" :size="isMobile ? 'small' : 'default'" @click="openEditDrawer(row.module)">编辑</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-card>

  <el-drawer v-model="editDrawerVisible" :title="`编辑模块配置 - ${editingModule}`" :size="isMobile ? '100%' : '60%'">
    <div class="edit-drawer-content">
      <el-card class="module-description-card" shadow="never">
        <template #header>
          <div class="card-header-title">模块描述</div>
        </template>
        <el-input
          v-model="editingModuleDescription"
          type="textarea"
          :rows="2"
          placeholder="请输入模块描述，用于在列表中显示该模块的含义"
          maxlength="200"
          show-word-limit
        />
      </el-card>

      <el-card class="configs-card" shadow="never">
        <template #header>
          <div class="configs-card-header">
            <span class="card-header-title">配置项列表 ({{ moduleConfigs.length }} 项)</span>
          </div>
        </template>
        <el-form ref="formRef" :model="editingConfigs" label-position="top" :validate-on-rule-change="false">
          <div class="config-list">
            <div
              v-for="config in moduleConfigs"
              :key="config.id"
              class="config-item"
            >
              <div class="config-item-header">
                <div class="config-item-title-row">
                  <span class="config-item-title">{{ config.description || config.key }}</span>
                  <code class="config-key-code-inline">{{ config.key }}</code>
                </div>
                <div class="config-item-meta">
                  <span class="meta-text">{{ config.updatedAt }}</span>
                  <span class="meta-divider">·</span>
                  <span class="meta-text">{{ config.updatedBy }}</span>
                </div>
              </div>
              <el-form-item
                :prop="`${config.id}.value`"
                :rules="[{ required: true, message: '请输入配置值' }]"
                class="config-value-item"
              >
                <el-input
                  v-model="editingConfigs[config.id].value"
                  type="textarea"
                  :rows="config.value.length > 100 ? 4 : 2"
                  placeholder="请输入配置值"
                  class="config-value-input"
                  maxlength="2000"
                  show-word-limit
                />
              </el-form-item>
            </div>
          </div>
        </el-form>
      </el-card>
    </div>
    <template #footer>
      <span class="drawer-footer">
        <el-button @click="editDrawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="configStore.loading" @click="handleEditSubmit" v-button-lock>保存全部</el-button>
      </span>
    </template>
  </el-drawer>

  <el-drawer v-model="detailDrawerVisible" :title="`模块详情 - ${viewingModule}`" :size="isMobile ? '100%' : '50%'">
    <template v-if="viewingModuleConfigs.length > 0">
      <el-descriptions :column="1" border class="detail-descriptions" style="margin-bottom: 24px">
        <el-descriptions-item label="模块">
          <el-tag type="primary" size="large">{{ viewingModule }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="配置项数量">
          {{ viewingModuleConfigs.length }} 个
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(new Date(Math.min(...viewingModuleConfigs.map(c => parseDate(c.createdAt).getTime())))) }}
        </el-descriptions-item>
        <el-descriptions-item label="最近更新时间">
          {{ formatDate(new Date(Math.max(...viewingModuleConfigs.map(c => parseDate(c.updatedAt).getTime())))) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider>配置项列表</el-divider>

      <div class="table-wrapper">
        <el-table :data="viewingModuleConfigs" border :size="isMobile ? 'small' : 'small'" class="module-configs-table">
          <el-table-column prop="key" label="配置 Key" :min-width="isMobile ? 150 : 200">
            <template #default="{ row }">
              <code class="config-key-code">{{ row.key }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" :min-width="isMobile ? 120 : 200" />
          <el-table-column prop="value" label="当前值" :min-width="isMobile ? 150 : 200" show-overflow-tooltip />
          <el-table-column v-if="!isMobile" label="更新时间" width="160">
            <template #default="{ row }">
              <div class="update-info">
                <span>{{ row.updatedAt }}</span>
                <span style="color: #94a3b8; font-size: 12px">{{ row.updatedBy }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
    <template #footer>
      <span class="drawer-footer">
        <el-button @click="detailDrawerVisible = false">关闭</el-button>
        <el-button type="primary" @click="viewingModule && openEditDrawer(viewingModule)">
          编辑模块配置
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>

<style scoped>
.config-card {
  background: var(--surface-color);
  border: 1px solid rgba(99, 102, 241, 0.16);
  color: #0f172a;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.08);
  border-radius: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.card-header.mobile-layout {
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.card-header p {
  margin: 4px 0 0;
  color: #475569;
  font-size: 13px;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.actions.mobile-actions {
  flex-direction: column;
  width: 100%;
}

.actions.mobile-actions .el-input {
  width: 100%;
}

.actions.mobile-actions .el-button {
  width: 100%;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons.mobile-actions {
  flex-direction: column;
  gap: 4px;
}

.action-buttons.mobile-actions .el-button {
  width: 100%;
  text-align: left;
  padding: 4px 0;
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

.config-table {
  border-radius: 12px;
  overflow: hidden;
  min-width: 600px;
}

.config-table :deep(.el-table__header-wrapper th) {
  background: #eef2ff;
  color: #312e81;
  font-weight: 600;
}

.config-table :deep(.el-table__cell) {
  color: #0f172a;
}

.config-table :deep(.el-table__body tr:nth-child(2n + 1) > td) {
  background: #f8fafc;
}

.config-table :deep(.el-table__body tr:hover > td) {
  background: #eef2ff !important;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.detail-descriptions {
  margin-top: 12px;
}

.config-key-code {
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  color: #6366f1;
}

.update-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.module-configs-table {
  margin-top: 16px;
  min-width: 600px;
}

.module-configs-table :deep(.el-table__cell) {
  padding: 12px;
}

@media (max-width: 768px) {
  .config-card {
    border-radius: 12px;
    margin: 0 -12px;
  }

  .config-card :deep(.el-card__header) {
    padding: 12px;
  }

  .config-card :deep(.el-card__body) {
    padding: 12px;
  }

  .card-header h2 {
    font-size: 18px;
  }

  .card-header p {
    font-size: 12px;
  }

  .config-table :deep(.el-table__header-wrapper th) {
    padding: 8px 4px;
    font-size: 12px;
  }

  .config-table :deep(.el-table__body-wrapper td) {
    padding: 8px 4px;
    font-size: 12px;
  }

  .module-configs-table :deep(.el-table__cell) {
    padding: 8px 4px;
    font-size: 12px;
  }

  .config-item {
    padding: 12px;
  }

  .config-item-title {
    font-size: 13px;
  }

  .config-key-code-inline {
    font-size: 10px;
    padding: 2px 6px;
  }

  .config-item-meta {
    font-size: 11px;
  }

  .config-value-input :deep(.el-textarea__inner) {
    font-size: 12px;
    min-height: 50px;
  }

  .drawer-footer {
    flex-direction: column-reverse;
    width: 100%;
  }

  .drawer-footer .el-button {
    width: 100%;
    margin: 0;
  }
}

.edit-drawer-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 20px;
  height: 100%;
  overflow: hidden;
}

.module-description-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  flex-shrink: 0;
}

.module-description-card :deep(.el-card__header) {
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.module-description-card :deep(.el-card__body) {
  padding: 16px;
}

.card-header-title {
  font-weight: 600;
  color: #0f172a;
  font-size: 14px;
}

.configs-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.configs-card :deep(.el-card__header) {
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.configs-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.configs-card :deep(.el-card__body) {
  padding: 16px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.configs-card :deep(.el-card__body)::-webkit-scrollbar {
  width: 6px;
}

.configs-card :deep(.el-card__body)::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.configs-card :deep(.el-card__body)::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.configs-card :deep(.el-card__body)::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  padding: 14px 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.config-item:hover {
  border-color: #c7d2fe;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.08);
}

.config-item-header {
  margin-bottom: 10px;
}

.config-item-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.config-item-title {
  font-size: 14px;
  color: #0f172a;
  font-weight: 500;
  flex: 1;
}

.config-key-code-inline {
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 11px;
  color: #6366f1;
  font-weight: normal;
  white-space: nowrap;
}

.config-item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.meta-text {
  color: #94a3b8;
}

.meta-divider {
  color: #cbd5e1;
}

.config-value-item {
  margin-bottom: 0;
}

.config-value-item :deep(.el-form-item__label) {
  display: none;
}

.config-value-input {
  width: 100%;
}

.config-value-input :deep(.el-textarea__inner) {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  resize: vertical;
  min-height: 60px;
}
</style>

