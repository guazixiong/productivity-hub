<template>
  <el-dialog
    v-model="dialogVisible"
    :title="image ? '图片详情' : ''"
    width="840px"
    class="ph-dialog"
    @close="handleClose"
  >
    <div v-if="image" class="image-detail">
      <!-- 图片预览 -->
      <div class="image-preview">
        <div class="preview-container">
          <el-image
            :src="getImageUrl(image.fileUrl)"
            :preview-src-list="[getImageUrl(image.fileUrl)]"
            fit="contain"
            class="preview-image"
            :zoom-rate="1.2"
            :max-scale="7"
            :min-scale="0.2"
            :preview-teleported="true"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>
          <div class="preview-actions">
            <el-button circle size="small" @click="downloadImage">
              <el-icon><Download /></el-icon>
            </el-button>
            <el-button circle size="small" @click="copyImageUrl">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
            <el-button circle size="small" @click="openInNewTab">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 图片信息 -->
      <div class="image-info">
        <el-descriptions :column="2" border class="info-descriptions">
          <el-descriptions-item label="文件名">
            {{ image.originalFilename }}
          </el-descriptions-item>
          <el-descriptions-item label="分类">
            <el-tag :type="getCategoryTagType(image.category)">
              {{ getCategoryLabel(image.category) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="文件大小">
            {{ formatFileSize(image.fileSize) }}
          </el-descriptions-item>
          <el-descriptions-item label="尺寸">
            <span v-if="image.width && image.height">
              {{ image.width }} × {{ image.height }} 像素
            </span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="文件类型">
            {{ image.fileType }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(image.status)">
              {{ getStatusLabel(image.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="访问次数">
            {{ image.accessCount }}
          </el-descriptions-item>
          <el-descriptions-item label="上传时间">
            {{ formatDateTime(image.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">
            {{ formatDateTime(image.updatedAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="image.businessModule" label="业务模块" :span="2">
            {{ image.businessModule }}
          </el-descriptions-item>
          <el-descriptions-item v-if="image.businessId" label="业务ID" :span="2">
            {{ image.businessId }}
          </el-descriptions-item>
          <el-descriptions-item v-if="image.description" label="描述" :span="2">
            {{ image.description }}
          </el-descriptions-item>
          <el-descriptions-item v-if="image.shareToken" label="分享链接" :span="2">
            <div class="share-link">
              <el-input
                :value="getShareUrl()"
                readonly
                style="flex: 1"
              >
                <template #append>
                  <el-button @click="copyShareUrl">复制</el-button>
                </template>
              </el-input>
            </div>
            <div v-if="image.shareExpiresAt" class="share-expires">
              过期时间：{{ formatDateTime(image.shareExpiresAt) }}
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 编辑表单 -->
        <el-form
          v-if="editing"
          :model="editForm"
          label-width="96px"
          class="edit-form"
        >
          <el-form-item label="描述">
            <el-input
              v-model="editForm.description"
              type="textarea"
              :rows="3"
              placeholder="图片描述"
            />
          </el-form-item>
          <el-form-item label="业务模块">
            <el-input v-model="editForm.businessModule" placeholder="业务模块标识" />
          </el-form-item>
          <el-form-item label="业务ID">
            <el-input v-model="editForm.businessId" placeholder="业务关联ID" />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="!editing" @click="handleClose">关闭</el-button>
        <template v-else>
          <el-button @click="handleCancelEdit">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存
          </el-button>
        </template>
        <el-button
          v-if="!editing && image"
          type="primary"
          @click="handleEdit"
        >
          编辑
        </el-button>
        <el-button
          v-if="image && !image.shareToken"
          @click="handleGenerateShare"
        >
          生成分享链接
        </el-button>
        <el-button
          v-if="image && image.shareToken"
          type="danger"
          @click="handleRevokeShare"
        >
          撤销分享
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Download, CopyDocument, FullScreen } from '@element-plus/icons-vue'
import { imageApi } from '@/services/imageApi'
import type { Image, ImageUpdateDTO, ImageCategory, ImageStatus } from '@/types/image'
import {
  getImageUrl,
  formatFileSize,
  getCategoryTagType,
  getCategoryLabel,
  getStatusTagType,
  getStatusLabel,
} from '@/utils/imageUtils'
import { formatDateTime } from '@/utils/format'

const props = defineProps<{
  modelValue: boolean
  image: Image | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  update: []
}>()

const dialogVisible = ref(false)
const editing = ref(false)
const saving = ref(false)

const editForm = ref<ImageUpdateDTO>({
  description: undefined,
  businessModule: undefined,
  businessId: undefined,
})

watch(
  () => props.modelValue,
  (val) => {
    dialogVisible.value = val
    if (val && props.image) {
      editing.value = false
      editForm.value = {
        description: props.image.description,
        businessModule: props.image.businessModule,
        businessId: props.image.businessId,
      }
    }
  }
)

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

const getShareUrl = () => {
  if (!props.image?.shareToken) return ''
  return `${window.location.origin}/api/images/share/${props.image.shareToken}`
}

const copyShareUrl = async () => {
  const url = getShareUrl()
  if (url) {
    await navigator.clipboard.writeText(url)
    ElMessage.success('分享链接已复制到剪贴板')
  }
}

const handleClose = () => {
  dialogVisible.value = false
  editing.value = false
}

const handleEdit = () => {
  editing.value = true
}

const handleCancelEdit = () => {
  editing.value = false
  if (props.image) {
    editForm.value = {
      description: props.image.description,
      businessModule: props.image.businessModule,
      businessId: props.image.businessId,
    }
  }
}

const handleSave = async () => {
  if (!props.image) return

  saving.value = true
  try {
    await imageApi.update(props.image.id, editForm.value)
    ElMessage.success('保存成功')
    editing.value = false
    emit('update')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleGenerateShare = async () => {
  if (!props.image) return

  try {
    const response = await imageApi.share(props.image.id)
    ElMessage.success('分享链接已生成')
    emit('update')
  } catch (error) {
    ElMessage.error('生成分享链接失败')
  }
}

const handleRevokeShare = async () => {
  if (!props.image) return

  try {
    await ElMessageBox.confirm('确定要撤销分享链接吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await imageApi.revokeShare(props.image.id)
    ElMessage.success('分享链接已撤销')
    emit('update')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('撤销分享链接失败')
    }
  }
}

// 下载图片
const downloadImage = async () => {
  if (!props.image) return
  try {
    const url = getImageUrl(props.image.fileUrl)
    const link = document.createElement('a')
    link.href = url
    link.download = props.image.originalFilename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('下载已开始')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 复制图片URL
const copyImageUrl = async () => {
  if (!props.image) return
  try {
    const url = getImageUrl(props.image.fileUrl)
    await navigator.clipboard.writeText(url)
    ElMessage.success('图片链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 在新标签页打开
const openInNewTab = () => {
  if (!props.image) return
  const url = getImageUrl(props.image.fileUrl)
  window.open(url, '_blank')
}
</script>

<style scoped lang="scss">
.image-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .image-preview {
    display: flex;
    justify-content: center;
    margin-bottom: 4px;

    .preview-container {
      position: relative;
      display: inline-block;
      padding: 10px;
      border-radius: 16px;
      background: var(--ph-bg-card);
      box-shadow: 0 18px 40px rgba(15, 23, 42, 0.1);
      border: 1px solid var(--ph-border-subtle);

      .preview-image {
        max-width: 100%;
        max-height: 420px;
        border-radius: 12px;
        display: block;
        background: #f8fafc;
      }

      .preview-actions {
        position: absolute;
        bottom: 18px;
        right: 18px;
        display: flex;
        gap: 8px;
        background: rgba(15, 23, 42, 0.76);
        padding: 8px 10px;
        border-radius: 999px;
        opacity: 0;
        transition: opacity 0.25s;

        .el-button {
          background: rgba(255, 255, 255, 0.96);
          border: none;

          &:hover {
            background: #ffffff;
          }
        }
      }

      &:hover .preview-actions {
        opacity: 1;
      }
    }

    .image-error {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      width: 420px;
      height: 320px;
      background-color: var(--ph-bg-card-subtle);
      color: var(--text-tertiary);
      gap: 8px;
      border-radius: 16px;
    }
  }

  .image-info {
    display: flex;
    flex-direction: column;
    gap: 14px;

    .info-descriptions {
      :deep(.el-descriptions__header) {
        margin-bottom: 8px;
      }

      :deep(.el-descriptions__body) {
        border-radius: 12px;
        overflow: hidden;
      }
    }

    .edit-form {
      margin-top: 6px;

      :deep(.el-form-item__label) {
        color: var(--text-secondary);
        font-weight: 600;
      }
    }

    .share-link {
      display: flex;
      gap: 8px;
    }

    .share-expires {
      margin-top: 8px;
      font-size: 12px;
      color: var(--text-tertiary);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

