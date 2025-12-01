<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useConfigStore } from '@/stores/config'
import type { ConfigItem } from '@/types/config'

const configStore = useConfigStore()
const searchKeyword = ref('')
const editDrawerVisible = ref(false)
const formRef = ref<FormInstance>()
const editingConfig = reactive({
  id: '',
  key: '',
  module: '',
  description: '',
  value: '',
  updatedBy: '',
})

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

const openEditDrawer = (item: ConfigItem) => {
  Object.assign(editingConfig, item)
  editDrawerVisible.value = true
}

const handleEditSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await configStore.updateConfig({
      id: editingConfig.id,
      value: editingConfig.value,
      description: editingConfig.description,
    })
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
      <div class="card-header">
        <div>
          <h2>全局参数配置</h2>
          <p>配置项通过 Pinia 缓存，编辑后自动刷新</p>
        </div>
        <div class="actions">
          <el-input v-model="searchKeyword" placeholder="搜索模块 / Key / 描述" clearable :prefix-icon="Search" />
          <el-button :loading="configStore.loading" @click="configStore.fetchConfigs(true)">刷新缓存</el-button>
        </div>
      </div>
    </template>

    <el-table
      :data="filteredConfigs"
      :loading="configStore.loading"
      stripe
      class="config-table"
      empty-text="暂无配置"
    >
      <el-table-column prop="module" label="模块" width="160" />
      <el-table-column prop="key" label="配置 Key" min-width="200" />
      <el-table-column prop="value" label="当前值" min-width="200" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="最近更新" width="220">
        <template #default="{ row }">
          <div class="update-meta">
            <strong>{{ row.updatedBy }}</strong>
            <span>{{ row.updatedAt }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDrawer(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-drawer v-model="editDrawerVisible" title="编辑配置" size="30%">
    <el-form ref="formRef" :model="editingConfig" label-position="top">
      <el-form-item label="模块">
        <el-input v-model="editingConfig.module" disabled />
      </el-form-item>
      <el-form-item label="配置 Key">
        <el-input v-model="editingConfig.key" disabled />
      </el-form-item>
      <el-form-item label="值" prop="value" :rules="[{ required: true, message: '请输入值' }]">
        <el-input v-model="editingConfig.value" type="textarea" rows="4" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="editingConfig.description" type="textarea" rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="drawer-footer">
        <el-button @click="editDrawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="configStore.loading" @click="handleEditSubmit">保存</el-button>
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

.card-header h2 {
  margin: 0;
}

.card-header p {
  margin: 4px 0 0;
  color: #475569;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.config-table {
  border-radius: 12px;
  overflow: hidden;
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

.update-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #475569;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

