<template>
  <el-dialog
    v-model="visible"
    title="批量导入网址"
    width="700px"
    @close="handleClose"
  >
    <div class="import-content">
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="上传文件" />
        <el-step title="预览数据" />
        <el-step title="导入结果" />
      </el-steps>

      <div class="step-content">
        <!-- 步骤1：上传文件 -->
        <div v-if="currentStep === 0" class="upload-step">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".xlsx,.xls"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 .xlsx 或 .xls 格式的Excel文件
              </div>
            </template>
          </el-upload>

          <div class="upload-actions">
            <el-button @click="handleDownloadTemplate">下载模板</el-button>
            <el-button
              type="primary"
              :disabled="!selectedFile"
              @click="handlePreview"
            >
              下一步：预览数据
            </el-button>
          </div>
        </div>

        <!-- 步骤2：预览数据（简化版，直接导入） -->
        <div v-if="currentStep === 1" class="preview-step">
          <div class="preview-info">
            <el-alert
              title="文件已选择，点击确认导入按钮开始导入"
              type="info"
              :closable="false"
              show-icon
            />
          </div>

          <div class="preview-actions">
            <el-button @click="currentStep = 0">上一步</el-button>
            <el-button type="primary" @click="handleImport" :loading="importing">
              确认导入
            </el-button>
          </div>
        </div>

        <!-- 步骤3：导入结果 -->
        <div v-if="currentStep === 2" class="result-step">
          <el-result
            :icon="importResult.failed === 0 ? 'success' : 'warning'"
            :title="importResult.failed === 0 ? '导入成功' : '导入完成'"
            :sub-title="`成功：${importResult.success}，失败：${importResult.failed}，跳过：${importResult.skipped}`"
          >
            <template #extra>
              <div v-if="importResult.errors.length > 0" class="error-list">
                <el-alert
                  title="错误信息"
                  type="error"
                  :closable="false"
                  show-icon
                >
                  <ul class="error-items">
                    <li v-for="(error, index) in importResult.errors" :key="index">
                      {{ error }}
                    </li>
                  </ul>
                </el-alert>
              </div>
              <el-button type="primary" @click="handleClose">完成</el-button>
            </template>
          </el-result>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { bookmarkApi } from '@/services/bookmarkApi'
import type { BookmarkImportResult } from '@/types/bookmark'

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const currentStep = ref(0)
const selectedFile = ref<File | null>(null)
const importResult = ref<BookmarkImportResult>({
  total: 0,
  success: 0,
  failed: 0,
  skipped: 0,
  errors: [],
})
const importing = ref(false)
const uploadRef = ref()

// 文件选择
const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
}

// 预览数据（简化版，直接进入下一步）
const handlePreview = () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  currentStep.value = 1
}

// 导入
const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  importing.value = true
  try {
    const result = await bookmarkApi.importFromExcel(selectedFile.value)
    importResult.value = result
    currentStep.value = 2

    if (result.failed === 0) {
      emit('success')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

// 下载模板
const handleDownloadTemplate = async () => {
  try {
    await bookmarkApi.downloadTemplate()
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

// 关闭
const handleClose = () => {
  visible.value = false
  currentStep.value = 0
  selectedFile.value = null
  importResult.value = {
    total: 0,
    success: 0,
    failed: 0,
    skipped: 0,
    errors: [],
  }
  uploadRef.value?.clearFiles()
}

watch(
  () => props.modelValue,
  (val) => {
    if (!val) {
      handleClose()
    }
  }
)
</script>

<style scoped>
.import-content {
  padding: 20px 0;
}

.step-content {
  margin-top: 30px;
  min-height: 300px;
}

.upload-step {
  text-align: center;
}

.upload-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.preview-step {
  padding: 0 20px;
}

.preview-info {
  margin-bottom: 20px;
}

.preview-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.result-step {
  padding: 20px;
}

.error-list {
  margin-top: 20px;
  max-width: 600px;
}

.error-items {
  margin: 10px 0 0 0;
  padding-left: 20px;
  text-align: left;
}

.error-items li {
  margin-bottom: 5px;
  font-size: 12px;
}
</style>

