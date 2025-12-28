<template>
  <el-dialog
    v-model="dialogVisible"
    title="上传图片"
    width="600px"
    @close="handleClose"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="图片文件" required>
        <el-upload
          ref="uploadRef"
          v-model:file-list="fileList"
          :auto-upload="false"
          :limit="10"
          :on-exceed="handleExceed"
          :on-remove="handleRemove"
          :before-upload="beforeUpload"
          multiple
          drag
          accept="image/*"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 jpg、jpeg、png、gif、webp、bmp 格式，单张图片最大 1MB，最多上传 10 张
            </div>
          </template>
        </el-upload>

        <!-- 上传进度 -->
        <div v-if="uploadProgress.size > 0" class="upload-progress-list">
          <div
            v-for="[filename, progress] in uploadProgress"
            :key="filename"
            class="upload-progress-item"
          >
            <div class="progress-header">
              <span class="progress-filename">{{ filename }}</span>
              <el-tag
                :type="progress.status === 'success' ? 'success' : progress.status === 'error' ? 'danger' : 'info'"
                size="small"
              >
                {{ progress.status === 'success' ? '完成' : progress.status === 'error' ? '失败' : '上传中' }}
              </el-tag>
            </div>
            <el-progress
              :percentage="progress.progress"
              :status="progress.status === 'error' ? 'exception' : progress.status === 'success' ? 'success' : undefined"
              :stroke-width="8"
            />
            <div v-if="progress.error" class="progress-error">
              {{ progress.error }}
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="分类" required>
        <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
          <el-option label="头像" value="avatar" />
          <el-option label="书签" value="bookmark" />
          <el-option label="待办" value="todo" />
          <el-option label="健康" value="health" />
          <el-option label="文章" value="article" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>

      <el-form-item label="业务模块">
        <el-input v-model="form.businessModule" placeholder="可选，如：bookmark、todo等" />
      </el-form-item>

      <el-form-item label="业务ID">
        <el-input v-model="form.businessId" placeholder="可选，关联的业务数据ID" />
      </el-form-item>

      <el-form-item label="描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="可选，图片描述信息"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          上传
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, type UploadFile, type UploadFiles } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { imageApi } from '@/services/imageApi'
import type { ImageCategory, ImageUploadForm } from '@/types/image'

interface UploadProgress {
  file: File
  progress: number
  status: 'pending' | 'uploading' | 'success' | 'error'
  error?: string
}

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const dialogVisible = ref(false)
const uploadRef = ref()
const fileList = ref<UploadFile[]>([])
const uploading = ref(false)
const uploadProgress = ref<Map<string, UploadProgress>>(new Map())

const form = ref<ImageUploadForm>({
  category: 'other',
  businessModule: undefined,
  businessId: undefined,
  description: undefined,
})

watch(
  () => props.modelValue,
  (val) => {
    dialogVisible.value = val
    if (val) {
      resetForm()
    }
  }
)

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

const resetForm = () => {
  form.value = {
    category: 'other',
    businessModule: undefined,
    businessId: undefined,
    description: undefined,
  }
  fileList.value = []
  uploadProgress.value.clear()
}

const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传 10 张图片')
}

const handleRemove = (file: UploadFile) => {
  const index = fileList.value.findIndex((f) => f.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

const beforeUpload = (file: File) => {
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('不支持的文件类型，仅支持 jpg、jpeg、png、gif、webp、bmp 格式')
    return false
  }

  // 验证文件大小（1MB = 1024 * 1024 字节）
  const maxSize = 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning(`文件 ${file.name} 大小超过 1MB，将自动压缩`)
  }

  return true
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的图片')
    return
  }

  if (!form.value.category) {
    ElMessage.warning('请选择图片分类')
    return
  }

  uploading.value = true
  uploadProgress.value.clear()

  try {
    const files = fileList.value.map((f) => f.raw!).filter(Boolean)

    // 初始化上传进度
    files.forEach((file) => {
      uploadProgress.value.set(file.name, {
        file,
        progress: 0,
        status: 'pending',
      })
    })

    if (files.length === 1) {
      // 单张上传（带进度）
      const file = files[0]
      const progressKey = file.name
      
      uploadProgress.value.set(progressKey, {
        file,
        progress: 0,
        status: 'uploading',
      })

      const formData = new FormData()
      formData.append('file', file)
      formData.append('category', form.value.category)
      if (form.value.businessModule) {
        formData.append('business_module', form.value.businessModule)
      }
      if (form.value.businessId) {
        formData.append('business_id', form.value.businessId)
      }
      if (form.value.description) {
        formData.append('description', form.value.description)
      }

      // 模拟进度更新（实际应该使用axios的onUploadProgress）
      const progressInterval = setInterval(() => {
        const current = uploadProgress.value.get(progressKey)
        if (current && current.progress < 90) {
          current.progress += 10
          uploadProgress.value.set(progressKey, { ...current })
        }
      }, 200)

      try {
        await imageApi.upload(formData)
        uploadProgress.value.set(progressKey, {
          file,
          progress: 100,
          status: 'success',
        })
        clearInterval(progressInterval)
        ElMessage.success('上传成功')
      } catch (error: any) {
        uploadProgress.value.set(progressKey, {
          file,
          progress: 0,
          status: 'error',
          error: error.message || '上传失败',
        })
        clearInterval(progressInterval)
        throw error
      }
    } else {
      // 批量上传
      const formData = new FormData()
      files.forEach((file) => {
        formData.append('files', file)
      })
      formData.append('category', form.value.category)
      if (form.value.businessModule) {
        formData.append('business_module', form.value.businessModule)
      }
      if (form.value.businessId) {
        formData.append('business_id', form.value.businessId)
      }
      if (form.value.description) {
        formData.append('description', form.value.description)
      }

      // 批量上传进度模拟
      files.forEach((file) => {
        const progressKey = file.name
        uploadProgress.value.set(progressKey, {
          file,
          progress: 0,
          status: 'uploading',
        })
      })

      const progressInterval = setInterval(() => {
        uploadProgress.value.forEach((progress, key) => {
          if (progress.status === 'uploading' && progress.progress < 90) {
            progress.progress += 10
            uploadProgress.value.set(key, { ...progress })
          }
        })
      }, 200)

      try {
        const result = await imageApi.batchUpload(formData)
        
        clearInterval(progressInterval)

        // 更新成功项
        result.successList.forEach((item) => {
          const progress = uploadProgress.value.get(item.originalFilename)
          if (progress) {
            uploadProgress.value.set(item.originalFilename, {
              ...progress,
              progress: 100,
              status: 'success',
            })
          }
        })

        // 更新失败项
        result.failList.forEach((item) => {
          const progress = uploadProgress.value.get(item.originalFilename)
          if (progress) {
            uploadProgress.value.set(item.originalFilename, {
              ...progress,
              progress: 0,
              status: 'error',
              error: item.errorMessage,
            })
          }
        })
        
        if (result.failCount === 0) {
          ElMessage.success(`成功上传 ${result.successCount} 张图片`)
        } else {
          ElMessage.warning(
            `部分上传失败：成功 ${result.successCount} 张，失败 ${result.failCount} 张`
          )
        }
      } catch (error) {
        clearInterval(progressInterval)
        throw error
      }
    }

    // 等待一下让用户看到完成状态
    await new Promise((resolve) => setTimeout(resolve, 500))
    
    emit('success')
    handleClose()
  } catch (error) {
    ElMessage.error('上传失败')
    console.error(error)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped lang="scss">
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.upload-progress-list {
  margin-top: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;

  .upload-progress-item {
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .progress-filename {
        font-size: 13px;
        color: #606266;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-right: 12px;
      }
    }

    .progress-error {
      margin-top: 4px;
      font-size: 12px;
      color: var(--el-color-danger);
    }
  }
}
</style>

