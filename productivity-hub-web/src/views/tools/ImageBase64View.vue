<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, UploadFilled, Download, RefreshRight } from '@element-plus/icons-vue'

const router = useRouter()

// 标签页
const activeTab = ref('encode')

// 编码相关
const encodeBase64Output = ref('')
const encodeImagePreview = ref('')
const encodeFileName = ref('')
const encodeFileSize = ref(0)
const isEncoding = ref(false)

// 解码相关
const decodeBase64Input = ref('')
const decodeImagePreview = ref('')
const decodeFileName = ref('')
const decodeFileSize = ref(0)
const isDecoding = ref(false)
const decodedImageUrl = ref('')

// 编码：处理文件上传
const handleFileChange = (file: File) => {
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 检查文件大小（限制10MB）
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }

  encodeFileName.value = file.name
  encodeFileSize.value = file.size
  isEncoding.value = true

  const reader = new FileReader()
  reader.onload = (e) => {
    const result = e.target?.result as string
    encodeBase64Output.value = result
    encodeImagePreview.value = result
    isEncoding.value = false
    ElMessage.success('编码成功')
  }
  reader.onerror = () => {
    ElMessage.error('读取文件失败')
    isEncoding.value = false
  }
  reader.readAsDataURL(file)
}

// 编码：复制Base64
const copyEncodeBase64 = () => {
  if (!encodeBase64Output.value) {
    ElMessage.warning('没有可复制的内容')
    return
  }
  navigator.clipboard.writeText(encodeBase64Output.value)
  ElMessage.success('已复制到剪贴板')
}

// 编码：清空
const clearEncode = () => {
  encodeBase64Output.value = ''
  encodeImagePreview.value = ''
  encodeFileName.value = ''
  encodeFileSize.value = 0
}

// 解码：清空
const clearDecode = () => {
  // 清理URL对象，避免内存泄漏
  if (decodedImageUrl.value) {
    URL.revokeObjectURL(decodedImageUrl.value)
  }
  decodeBase64Input.value = ''
  decodeImagePreview.value = ''
  decodeFileName.value = ''
  decodeFileSize.value = 0
  decodedImageUrl.value = ''
}

// 解码：Base64解码并还原图片
const decodeBase64 = () => {
  if (!decodeBase64Input.value.trim()) {
    ElMessage.warning('请输入Base64编码')
    return
  }

  isDecoding.value = true
  let base64String = decodeBase64Input.value.trim()
  let detectedImageType = 'png'

  try {
    // 清理之前的URL（如果有）
    if (decodedImageUrl.value) {
      URL.revokeObjectURL(decodedImageUrl.value)
    }

    // 移除可能的数据URL前缀（如 data:image/png;base64,）
    const dataUrlMatch = base64String.match(/^data:image\/([a-zA-Z+]+);base64,(.+)$/)
    if (dataUrlMatch) {
      base64String = dataUrlMatch[2]
      detectedImageType = dataUrlMatch[1].toLowerCase()
      decodeFileName.value = `decoded-image.${detectedImageType}`
    } else {
      decodeFileName.value = 'decoded-image.png'
    }

    // 验证Base64格式（允许空格和换行）
    const cleanBase64 = base64String.replace(/\s/g, '')
    const base64Regex = /^[A-Za-z0-9+/]*={0,2}$/
    if (!base64Regex.test(cleanBase64)) {
      throw new Error('无效的Base64格式')
    }

    // 尝试解码
    const binaryString = atob(cleanBase64)
    const bytes = new Uint8Array(binaryString.length)
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i)
    }

    // 从文件头检测图片类型
    if (bytes.length >= 4) {
      const header = Array.from(bytes.slice(0, 12))
        .map(b => b.toString(16).padStart(2, '0'))
        .join('')
      
      if (header.startsWith('ffd8')) {
        detectedImageType = 'jpg'
        decodeFileName.value = 'decoded-image.jpg'
      } else if (header.startsWith('89504e47')) {
        detectedImageType = 'png'
        decodeFileName.value = 'decoded-image.png'
      } else if (header.startsWith('474946')) {
        detectedImageType = 'gif'
        decodeFileName.value = 'decoded-image.gif'
      } else if (header.startsWith('52494646') && header.includes('57454250')) {
        detectedImageType = 'webp'
        decodeFileName.value = 'decoded-image.webp'
      } else if (header.startsWith('424d')) {
        detectedImageType = 'bmp'
        decodeFileName.value = 'decoded-image.bmp'
      } else if (header.startsWith('25504446')) {
        detectedImageType = 'pdf'
        decodeFileName.value = 'decoded-image.pdf'
      }
    }

    // 创建Blob
    const mimeType = detectedImageType === 'jpg' ? 'image/jpeg' : `image/${detectedImageType}`
    const blob = new Blob([bytes], { type: mimeType })
    const url = URL.createObjectURL(blob)

    // 验证是否为有效图片
    const img = new Image()
    img.onload = () => {
      decodedImageUrl.value = url
      decodeImagePreview.value = url
      
      // 更新文件信息
      decodeFileSize.value = bytes.length
      
      isDecoding.value = false
      ElMessage.success('解码成功')
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      isDecoding.value = false
      ElMessage.error('Base64数据不是有效的图片格式，请确认输入的是图片的Base64编码')
    }
    img.src = url
  } catch (error) {
    isDecoding.value = false
    if (error instanceof Error && error.message.includes('Invalid character')) {
      ElMessage.error('Base64字符串包含无效字符，请检查格式')
    } else {
      ElMessage.error(error instanceof Error ? error.message : '解码失败，请检查Base64格式是否正确')
    }
  }
}

// 解码：下载还原的图片
const downloadImage = () => {
  if (!decodedImageUrl.value) {
    ElMessage.warning('没有可下载的图片')
    return
  }

  const link = document.createElement('a')
  link.href = decodedImageUrl.value
  link.download = decodeFileName.value || 'decoded-image.png'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('下载成功')
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}
</script>

<template>
  <div class="base64-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>

    <el-tabs v-model="activeTab" class="base64-tabs">
      <!-- 编码标签页 -->
      <el-tab-pane label="图片编码" name="encode">
        <div class="tab-content">
          <div class="encode-layout">
            <div class="upload-section">
              <h4>上传图片</h4>
              <el-upload
                class="upload-dragger"
                drag
                :auto-upload="false"
                :on-change="(file) => handleFileChange(file.raw as File)"
                :show-file-list="false"
                accept="image/*"
              >
                <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                <div class="el-upload__text">
                  将图片拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">支持 JPG、PNG、GIF、WEBP 等格式，最大 10MB</div>
                </template>
              </el-upload>

              <div v-if="encodeFileName" class="file-info">
                <div class="info-item">
                  <span class="info-label">文件名：</span>
                  <span class="info-value">{{ encodeFileName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">文件大小：</span>
                  <span class="info-value">{{ formatFileSize(encodeFileSize) }}</span>
                </div>
              </div>
            </div>

            <div class="preview-section">
              <h4>图片预览</h4>
              <div v-if="encodeImagePreview" class="image-preview">
                <img :src="encodeImagePreview" alt="预览" />
              </div>
              <el-empty v-else description="暂无图片" :image-size="100" />
            </div>
          </div>

          <div class="output-section">
            <div class="section-header">
              <h4>Base64 编码结果</h4>
              <div class="header-actions">
                <el-button size="small" @click="copyEncodeBase64" :disabled="!encodeBase64Output">复制</el-button>
                <el-button size="small" @click="clearEncode" :disabled="!encodeBase64Output">清空</el-button>
              </div>
            </div>
            <el-input
              v-model="encodeBase64Output"
              type="textarea"
              :rows="10"
              placeholder="Base64编码将显示在这里..."
              readonly
              class="base64-output"
            />
            <div v-if="encodeBase64Output" class="output-info">
              <span>编码长度：{{ encodeBase64Output.length }} 字符</span>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 解码标签页 -->
      <el-tab-pane label="Base64解码" name="decode">
        <div class="tab-content">
          <div class="decode-layout">
            <div class="input-section">
              <div class="section-header">
                <h4>输入 Base64 编码</h4>
                <div class="header-actions">
                  <el-button size="small" @click="clearDecode" :disabled="!decodeBase64Input">清空</el-button>
                </div>
              </div>
              <el-input
                v-model="decodeBase64Input"
                type="textarea"
                :rows="12"
                placeholder="粘贴Base64编码字符串，支持带或不带 data:image/...;base64, 前缀"
                class="base64-input"
              />
              <div class="decode-tip">
                <span>提示：可以粘贴完整的 data URL（如 data:image/png;base64,...）或纯Base64字符串</span>
              </div>
              <div class="decode-actions">
                <el-button 
                  type="primary" 
                  :icon="RefreshRight" 
                  @click="decodeBase64" 
                  :loading="isDecoding"
                  :disabled="!decodeBase64Input.trim()"
                >
                  解码还原
                </el-button>
              </div>
            </div>

            <div class="preview-section">
              <h4>图片预览</h4>
              <div v-if="decodeImagePreview" class="image-preview">
                <img :src="decodeImagePreview" alt="预览" />
                <div v-if="decodeFileName" class="file-info">
                  <div class="info-item">
                    <span class="info-label">文件名：</span>
                    <span class="info-value">{{ decodeFileName }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">文件大小：</span>
                    <span class="info-value">{{ formatFileSize(decodeFileSize) }}</span>
                  </div>
                </div>
                <div class="download-action">
                  <el-button 
                    type="primary" 
                    :icon="Download" 
                    @click="downloadImage" 
                    :disabled="!decodedImageUrl"
                  >
                    下载图片
                  </el-button>
                </div>
              </div>
              <el-empty v-else description="暂无图片，请输入Base64编码并点击解码" :image-size="100" />
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.base64-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  margin-bottom: 8px;
}

.base64-tabs {
  width: 100%;
}

:deep(.el-tabs__content) {
  padding-top: 20px;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 编码布局 */
.encode-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* 解码布局 */
.decode-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.upload-section,
.preview-section,
.input-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-section h4,
.preview-section h4,
.input-section h4 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
  font-weight: 600;
}

.upload-dragger {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  padding: 40px 20px;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: rgba(99, 102, 241, 0.05);
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.info-label {
  color: #64748b;
  font-weight: 500;
}

.info-value {
  color: #0f172a;
  margin-left: 8px;
}

.image-preview {
  width: 100%;
  min-height: 300px;
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.02);
  gap: 16px;
}

.image-preview img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  object-fit: contain;
}

.download-action {
  width: 100%;
  display: flex;
  justify-content: center;
}

.output-section,
.input-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-header h4 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.base64-output,
.base64-input {
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.output-info {
  font-size: 12px;
  color: #64748b;
  text-align: right;
}

.decode-tip {
  font-size: 12px;
  color: #64748b;
  padding: 8px 12px;
  background: rgba(99, 102, 241, 0.05);
  border-radius: 6px;
}

.decode-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .encode-layout,
  .decode-layout {
    grid-template-columns: 1fr;
  }
}
</style>

