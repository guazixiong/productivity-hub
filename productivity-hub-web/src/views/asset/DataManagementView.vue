<template>
  <div class="data-management-view">
    <el-card>
      <template #header>
        <span>数据管理</span>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="数据导入" name="import">
          <div class="import-section">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              accept=".xlsx,.xls,.csv"
              drag
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 Excel (.xlsx, .xls) 和 CSV (.csv) 格式文件
                </div>
              </template>
            </el-upload>

            <el-form :model="importForm" label-width="120px" style="margin-top: 24px; max-width: 500px">
              <el-form-item label="数据类型">
                <el-radio-group v-model="importForm.dataType">
                  <el-radio label="asset">资产数据</el-radio>
                  <el-radio label="wishlist">心愿单数据</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="导入模式">
                <el-radio-group v-model="importForm.incremental">
                  <el-radio :label="false">全量导入</el-radio>
                  <el-radio :label="true">增量导入</el-radio>
                </el-radio-group>
                <div class="form-item-desc">
                  全量导入：清空现有数据后导入；增量导入：在现有数据基础上追加
                </div>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="importing"
                  :disabled="!selectedFile"
                  @click="handleImport"
                >
                  开始导入
                </el-button>
                <el-button @click="handleResetImport">重置</el-button>
              </el-form-item>
            </el-form>

            <el-card v-if="importResult" style="margin-top: 24px">
              <template #header>
                <span>导入结果</span>
              </template>
              <div class="import-result">
                <el-statistic title="成功数量" :value="importResult.successCount">
                  <template #suffix>
                    <span style="color: #67c23a">条</span>
                  </template>
                </el-statistic>
                <el-statistic title="失败数量" :value="importResult.failCount">
                  <template #suffix>
                    <span style="color: #f56c6c">条</span>
                  </template>
                </el-statistic>
              </div>
              <el-table
                v-if="importResult.errors && importResult.errors.length > 0"
                :data="importResult.errors"
                border
                style="margin-top: 16px"
              >
                <el-table-column prop="row" label="行号" width="100" />
                <el-table-column prop="message" label="错误信息" />
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>

        <el-tab-pane label="数据导出" name="export">
          <div class="export-section">
            <el-form :model="exportForm" label-width="120px" style="max-width: 500px">
              <el-form-item label="数据类型">
                <el-radio-group v-model="exportForm.dataType">
                  <el-radio label="asset">资产数据</el-radio>
                  <el-radio label="wishlist">心愿单数据</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="导出格式">
                <el-radio-group v-model="exportForm.format">
                  <el-radio label="excel">Excel (.xlsx)</el-radio>
                  <el-radio label="csv">CSV (.csv)</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="exporting" @click="handleExport">
                  开始导出
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, type UploadInstance, type UploadFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { dataManagementApi } from '@/services/dataManagementApi'
import type { DataImportResult, ExportFormat } from '@/types/dataManagement'

const activeTab = ref('import')
const uploadRef = ref<UploadInstance>()
const importing = ref(false)
const exporting = ref(false)
const selectedFile = ref<File | null>(null)
const importResult = ref<DataImportResult | null>(null)

const importForm = reactive<{
  dataType: 'asset' | 'wishlist'
  incremental: boolean
}>({
  dataType: 'asset',
  incremental: false,
})

const exportForm = reactive<{
  dataType: 'asset' | 'wishlist'
  format: ExportFormat
}>({
  dataType: 'asset',
  format: 'excel',
})

const handleFileChange = (file: UploadFile) => {
  selectedFile.value = file.raw || null
  importResult.value = null
}

const handleFileRemove = () => {
  selectedFile.value = null
  importResult.value = null
}

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  importing.value = true
  try {
    const response = await dataManagementApi.importData(
      selectedFile.value,
      importForm.dataType,
      importForm.incremental,
    )
    importResult.value = response.data
    if (importResult.value.failCount === 0) {
      ElMessage.success(`导入成功，共导入 ${importResult.value.successCount} 条数据`)
    } else {
      ElMessage.warning(
        `导入完成，成功 ${importResult.value.successCount} 条，失败 ${importResult.value.failCount} 条`,
      )
    }
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

const handleResetImport = () => {
  uploadRef.value?.clearFiles()
  selectedFile.value = null
  importResult.value = null
  importForm.dataType = 'asset'
  importForm.incremental = false
}

const handleExport = async () => {
  exporting.value = true
  try {
    await dataManagementApi.exportData(exportForm.dataType, exportForm.format)
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.data-management-view {
  padding: 16px;
}

.import-section,
.export-section {
  padding: 16px 0;
}

.form-item-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.import-result {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
}
</style>

