<template>
  <div class="image-view">
    <el-card class="main-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <div class="title-icon">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="title-meta">
              <div class="title-row">
                <span class="title">图片管理</span>
                <el-tag v-if="total > 0" size="small" type="info" effect="plain" class="total-tag">
                  共 {{ total }} 张
                </el-tag>
              </div>
              <p class="subtitle">统一管理业务素材，支持批量操作与预览</p>
            </div>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="showUploadDialog = true">
              <el-icon><Upload /></el-icon>
              <span class="button-text">上传图片</span>
            </el-button>
            <el-button
              v-if="selectedImages.length > 0"
              type="danger"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              <span class="button-text">批量删除 ({{ selectedImages.length }})</span>
            </el-button>
            <el-button @click="showStatisticsDialog = true">
              <el-icon><DataAnalysis /></el-icon>
              <span class="button-text">统计信息</span>
            </el-button>
            <el-button
              v-if="selectedImages.length > 0"
              @click="showBatchEditDialog = true"
            >
              <span class="button-text">批量编辑 ({{ selectedImages.length }})</span>
            </el-button>
            <!-- 视图切换 -->
            <el-radio-group v-model="viewMode" size="small" class="view-mode-switch">
              <el-radio-button value="table">表格</el-radio-button>
              <el-radio-button value="grid">网格</el-radio-button>
              <el-radio-button value="card">卡片</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <!-- 筛选工具栏 -->
      <div class="filter-toolbar">
        <el-form :inline="true" :model="query" class="filter-form">
          <el-form-item label="分类">
            <el-select
              v-model="query.category"
              placeholder="全部分类"
              clearable
              style="width: 150px"
            >
              <el-option label="全部" value="" />
              <el-option label="头像" value="avatar" />
              <el-option label="书签" value="bookmark" />
              <el-option label="待办" value="todo" />
              <el-option label="健康" value="health" />
              <el-option label="文章" value="article" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="query.status"
              placeholder="全部状态"
              clearable
              style="width: 150px"
            >
              <el-option label="全部" value="" />
              <el-option label="正常" value="ACTIVE" />
              <el-option label="已删除" value="DELETED" />
              <el-option label="已归档" value="ARCHIVED" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="搜索文件名或描述"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 图片列表 -->
      <div class="image-list-container">
        <!-- 空状态 -->
        <el-empty
          v-if="!loading && imageList.length === 0"
          description="暂无图片"
          :image-size="200"
        >
          <el-button type="primary" @click="showUploadDialog = true">
            上传图片
          </el-button>
        </el-empty>

        <!-- 表格视图 -->
        <el-table
          v-else-if="viewMode === 'table'"
          v-loading="loading"
          :data="imageList"
          class="image-table"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55">
            <template #header>
              <el-checkbox
                :model-value="isAllSelected"
                :indeterminate="isIndeterminate"
                @change="handleSelectAll"
              />
            </template>
          </el-table-column>
          <el-table-column label="缩略图" width="120">
            <template #default="{ row }">
              <div class="thumbnail-cell">
                <template v-if="getThumbnailSrc(row)">
                  <el-image
                    :src="getThumbnailSrc(row)!"
                    fit="cover"
                    class="thumbnail-image"
                    lazy
                    loading="lazy"
                    @click.stop="handlePreview(row)"
                  >
                    <template #placeholder>
                      <div class="image-placeholder">
                        <el-icon class="is-loading"><Picture /></el-icon>
                      </div>
                    </template>
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </template>
                <div v-else class="image-placeholder">
                  <el-icon class="is-loading"><Picture /></el-icon>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="originalFilename" label="文件名" min-width="200" show-overflow-tooltip />
          <el-table-column label="分类" width="100">
            <template #default="{ row }">
              <el-tag :type="getCategoryTagType(row.category)">
                {{ getCategoryLabel(row.category) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="尺寸" width="120">
            <template #default="{ row }">
              <span v-if="row.width && row.height">
                {{ row.width }} × {{ row.height }}
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="文件大小" width="100">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column label="访问次数" width="100" sortable="custom" prop="accessCount">
            <template #default="{ row }">
              {{ row.accessCount }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上传时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button link type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-dropdown @command="(cmd) => handleActionCommand(cmd, row)">
                <el-button link type="primary" size="small">
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="share">
                      <el-icon><Share /></el-icon>
                      <span>生成分享链接</span>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="row.status === 'DELETED'" command="restore">
                      <el-icon><Refresh /></el-icon>
                      <span>恢复</span>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="row.status === 'ACTIVE'" command="archive">
                      <el-icon><FolderOpened /></el-icon>
                      <span>归档</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon>
                      <span style="color: var(--el-color-danger)">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <!-- 网格视图 -->
        <div v-else-if="viewMode === 'grid'" class="image-grid-view">
          <div
            v-for="image in imageList"
            :key="image.id"
            class="grid-item"
            :class="{ selected: isImageSelected(image) }"
            @click="toggleImageSelection(image)"
          >
            <div class="grid-checkbox">
              <el-checkbox
                :model-value="isImageSelected(image)"
                @change="toggleImageSelection(image)"
                @click.stop
              />
            </div>
            <template v-if="getThumbnailSrc(image)">
              <el-image
                :src="getThumbnailSrc(image)!"
                fit="cover"
                class="grid-thumbnail"
                lazy
                @click.stop="handlePreview(image)"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </template>
            <div v-else class="image-error">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="grid-info">
              <div class="grid-filename" :title="image.originalFilename">
                {{ image.originalFilename }}
              </div>
              <div class="grid-meta">
                <el-tag :type="getCategoryTagType(image.category)" size="small">
                  {{ getCategoryLabel(image.category) }}
                </el-tag>
                <span class="grid-size">{{ formatFileSize(image.fileSize) }}</span>
              </div>
            </div>
            <div class="grid-actions" @click.stop>
              <el-button link type="primary" size="small" @click="handleView(image)">
                查看
              </el-button>
              <el-dropdown @command="(cmd) => handleActionCommand(cmd, image)">
                <el-button link type="primary" size="small">
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="share">
                      <el-icon><Share /></el-icon>
                      <span>生成分享链接</span>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="image.status === 'DELETED'" command="restore">
                      <el-icon><Refresh /></el-icon>
                      <span>恢复</span>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="image.status === 'ACTIVE'" command="archive">
                      <el-icon><FolderOpened /></el-icon>
                      <span>归档</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon>
                      <span style="color: var(--el-color-danger)">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <el-empty v-if="!loading && imageList.length === 0" description="暂无图片" />
        </div>

        <!-- 卡片视图 -->
        <div v-else-if="viewMode === 'card'" class="image-card-view">
          <div
            v-for="image in imageList"
            :key="image.id"
            class="card-item"
            :class="{ selected: isImageSelected(image) }"
            @click="toggleImageSelection(image)"
          >
            <div class="card-checkbox">
              <el-checkbox
                :model-value="isImageSelected(image)"
                @change="toggleImageSelection(image)"
                @click.stop
              />
            </div>
            <template v-if="getThumbnailSrc(image)">
              <el-image
                :src="getThumbnailSrc(image)!"
                fit="cover"
                class="card-thumbnail"
                lazy
                @click.stop="handlePreview(image)"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </template>
            <div v-else class="image-error">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="card-overlay">
              <div class="card-actions">
                <el-button circle size="small" @click.stop="handleView(image)">
                  <el-icon><Picture /></el-icon>
                </el-button>
                <el-button circle size="small" @click.stop="handleEdit(image)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button circle size="small" type="danger" @click.stop="handleDelete(image)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="card-info">
              <div class="card-filename" :title="image.originalFilename">
                {{ image.originalFilename }}
              </div>
              <div class="card-meta">
                <el-tag :type="getCategoryTagType(image.category)" size="small">
                  {{ getCategoryLabel(image.category) }}
                </el-tag>
                <span class="card-size">{{ formatFileSize(image.fileSize) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="!loading && imageList.length === 0" description="暂无图片" />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePageSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 上传对话框 -->
    <ImageUploadDialog
      v-model="showUploadDialog"
      @success="handleUploadSuccess"
    />

    <!-- 详情/编辑对话框 -->
    <ImageDetailDialog
      v-model="showDetailDialog"
      :image="currentImage"
      @update="handleImageUpdate"
    />

    <!-- 统计对话框 -->
    <ImageStatisticsDialog
      v-model="showStatisticsDialog"
    />

    <!-- 批量编辑对话框 -->
    <el-dialog
      v-model="showBatchEditDialog"
      title="批量编辑"
      :width="isMobile ? '90%' : '500px'"
      :fullscreen="isMobile"
    >
      <el-form :model="batchEditForm" label-width="100px">
        <el-form-item label="分类">
          <el-select
            v-model="batchEditForm.category"
            placeholder="不修改"
            clearable
            style="width: 100%"
          >
            <el-option label="头像" value="avatar" />
            <el-option label="书签" value="bookmark" />
            <el-option label="待办" value="todo" />
            <el-option label="健康" value="health" />
            <el-option label="文章" value="article" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务模块">
          <el-input
            v-model="batchEditForm.businessModule"
            placeholder="不修改"
            clearable
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="batchEditForm.description"
            type="textarea"
            :rows="3"
            placeholder="不修改"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="batchEditing" @click="handleBatchEdit">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 图片预览弹窗：居中展示，不带底部列表 -->
    <el-dialog
      v-model="showPreviewDialog"
      :width="isMobile ? '92%' : '70%'"
      class="preview-dialog"
      align-center
      destroy-on-close
    >
      <div v-if="previewImage" class="preview-wrapper">
        <el-image
          :src="getImageUrl(previewImage.fileUrl)"
          fit="contain"
          class="preview-image"
          :preview-src-list="[]"
          @error="onPreviewError"
        >
          <template #error>
            <div class="preview-error">
              <el-icon><Picture /></el-icon>
              <span>图片加载失败</span>
            </div>
          </template>
        </el-image>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload,
  Delete,
  Search,
  Picture,
  Share,
  Refresh,
  FolderOpened,
  ArrowDown,
  DataAnalysis,
  Edit,
} from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'
import { imageApi } from '@/services/imageApi'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()
import type { Image, ImageListQuery, ImageStatus, ImageCategory } from '@/types/image'
import type { PageResult } from '@/types/common'
import {
  getImageUrl,
  formatFileSize,
  getCategoryTagType,
  getCategoryLabel,
  getStatusTagType,
  getStatusLabel,
} from '@/utils/imageUtils'
import { formatDateTime } from '@/utils/format'
import ImageUploadDialog from './components/ImageUploadDialog.vue'
import ImageDetailDialog from './components/ImageDetailDialog.vue'
import ImageStatisticsDialog from './components/ImageStatisticsDialog.vue'

// 安全获取缩略图/预览地址，避免空字符串导致透明
const getThumbnailSrc = (image: Image) => {
  const url = getImageUrl(image.thumbnailUrl || image.fileUrl)
  return url && url.trim() ? url : null
}

const loading = ref(false)
const imageList = ref<Image[]>([])
const total = ref(0)
const selectedImages = ref<Image[]>([])
const dateRange = ref<[string, string] | null>(null)

const query = ref<ImageListQuery>({
  pageNum: 1,
  pageSize: 20,
  category: undefined,
  status: 'ACTIVE',
  keyword: undefined,
  startTime: undefined,
  endTime: undefined,
  sortBy: 'createTime',
  sortOrder: 'desc',
})

const showUploadDialog = ref(false)
const showDetailDialog = ref(false)
const showStatisticsDialog = ref(false)
const showBatchEditDialog = ref(false)
const showPreviewDialog = ref(false)
const previewImage = ref<Image | null>(null)
const currentImage = ref<Image | null>(null)
const batchEditing = ref(false)

// 视图模式：table | grid | card
const viewMode = ref<'table' | 'grid' | 'card'>(
  (localStorage.getItem('image-view-mode') as 'table' | 'grid' | 'card') || 'table'
)

// 批量编辑表单
const batchEditForm = ref({
  category: undefined as ImageCategory | undefined,
  businessModule: undefined as string | undefined,
  description: undefined as string | undefined,
})

// 监听视图模式变化，保存到本地存储
watch(viewMode, (val) => {
  localStorage.setItem('image-view-mode', val)
})

// 加载图片列表
const loadImageList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
      query.value.startTime = dateRange.value[0]
      query.value.endTime = dateRange.value[1]
    } else {
      query.value.startTime = undefined
      query.value.endTime = undefined
    }

    const response = await imageApi.list(query.value)
    
    // 适配后端返回格式：{ total, pageNum, pageSize, pages, list }
    const data = response as any
    if (data.list) {
      imageList.value = data.list
      total.value = data.total || 0
      query.value.pageNum = data.pageNum || 1
      query.value.pageSize = data.pageSize || 20
    } else if (data.items) {
      // 兼容前端格式
      imageList.value = data.items
      total.value = data.total || 0
      query.value.pageNum = data.pageNum || 1
      query.value.pageSize = data.pageSize || 20
    } else {
      imageList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('加载图片列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  query.value.pageNum = 1
  saveQueryToLocal()
  loadImageList()
}

// 重置
const handleReset = () => {
  query.value = {
    pageNum: 1,
    pageSize: 20,
    category: undefined,
    status: 'ACTIVE',
    keyword: undefined,
    startTime: undefined,
    endTime: undefined,
    sortBy: 'createTime',
    sortOrder: 'desc',
  }
  dateRange.value = null
  // 清除本地存储的筛选条件
  localStorage.removeItem('image-query-filters')
  loadImageList()
}

// 保存筛选条件到本地存储
const saveQueryToLocal = () => {
  const queryData = {
    category: query.value.category,
    status: query.value.status,
    keyword: query.value.keyword,
    dateRange: dateRange.value,
  }
  localStorage.setItem('image-query-filters', JSON.stringify(queryData))
}

// 从本地存储加载筛选条件
const loadQueryFromLocal = () => {
  const saved = localStorage.getItem('image-query-filters')
  if (saved) {
    try {
      const queryData = JSON.parse(saved)
      if (queryData.category) query.value.category = queryData.category
      if (queryData.status) query.value.status = queryData.status
      if (queryData.keyword) query.value.keyword = queryData.keyword
      if (queryData.dateRange) dateRange.value = queryData.dateRange
    } catch (e) {
      // 忽略加载查询参数错误
    }
  }
}

// 批量编辑
const handleBatchEdit = async () => {
  if (selectedImages.value.length === 0) {
    ElMessage.warning('请选择要编辑的图片')
    return
  }

  const hasChanges = batchEditForm.value.category ||
    batchEditForm.value.businessModule ||
    batchEditForm.value.description

  if (!hasChanges) {
    ElMessage.warning('请至少修改一个字段')
    return
  }

  batchEditing.value = true
  try {
    const updateData: any = {}
    if (batchEditForm.value.category) {
      updateData.category = batchEditForm.value.category
    }
    if (batchEditForm.value.businessModule !== undefined) {
      updateData.businessModule = batchEditForm.value.businessModule
    }
    if (batchEditForm.value.description !== undefined) {
      updateData.description = batchEditForm.value.description
    }

    // 批量更新
    const promises = selectedImages.value.map((img) =>
      imageApi.update(img.id, updateData)
    )
    await Promise.all(promises)

    ElMessage.success(`成功更新 ${selectedImages.value.length} 张图片`)
    selectedImages.value = []
    batchEditForm.value = {
      category: undefined,
      businessModule: undefined,
      description: undefined,
    }
    showBatchEditDialog.value = false
    loadImageList()
  } catch (error) {
    ElMessage.error('批量编辑失败')
  } finally {
    batchEditing.value = false
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  query.value.pageNum = page
  loadImageList()
}

const handlePageSizeChange = (size: number) => {
  query.value.pageSize = size
  query.value.pageNum = 1
  loadImageList()
}

// 选择变化
const handleSelectionChange = (selection: Image[]) => {
  selectedImages.value = selection
}

// 判断图片是否被选中
const isImageSelected = (image: Image): boolean => {
  return selectedImages.value.some((img) => img.id === image.id)
}

// 切换图片选择状态
const toggleImageSelection = (image: Image) => {
  const index = selectedImages.value.findIndex((img) => img.id === image.id)
  if (index > -1) {
    selectedImages.value.splice(index, 1)
  } else {
    selectedImages.value.push(image)
  }
}

// 全选/取消全选
const isAllSelected = computed(() => {
  return imageList.value.length > 0 && selectedImages.value.length === imageList.value.length
})

const isIndeterminate = computed(() => {
  return selectedImages.value.length > 0 && selectedImages.value.length < imageList.value.length
})

const handleSelectAll = (checked: boolean) => {
  if (checked) {
    selectedImages.value = [...imageList.value]
  } else {
    selectedImages.value = []
  }
}

// 查看详情
const handleView = async (image: Image) => {
  try {
    // 调用访问接口，增加访问统计并获取最新数据
    const updatedImage = await imageApi.access(image.id)
    currentImage.value = updatedImage
    showDetailDialog.value = true
    // 更新列表中的图片数据
    const index = imageList.value.findIndex(img => img.id === image.id)
    if (index !== -1) {
      imageList.value[index] = updatedImage
    }
  } catch (error) {
    // 如果访问接口失败，仍然显示详情（使用原有数据）
    currentImage.value = image
    showDetailDialog.value = true
  }
}

// 仅预览大图（不展示下方列表）
const handlePreview = (image: Image) => {
  previewImage.value = image
  showPreviewDialog.value = true
}

const onPreviewError = () => {
  ElMessage.error('图片加载失败')
}

// 编辑
const handleEdit = (image: Image) => {
  currentImage.value = image
  showDetailDialog.value = true
}

// 操作命令
const handleActionCommand = async (command: string, image: Image) => {
  switch (command) {
    case 'share':
      await handleShare(image)
      break
    case 'restore':
      await handleRestore(image)
      break
    case 'archive':
      await handleArchive(image)
      break
    case 'delete':
      await handleDelete(image)
      break
  }
}

// 生成分享链接
const handleShare = async (image: Image) => {
  try {
    const response = await imageApi.share(image.id)
    const shareUrl = `${window.location.origin}${response.shareUrl}`
    
    await ElMessageBox.alert(
      `分享链接已生成：\n${shareUrl}\n\n过期时间：${response.expiresAt || '永久有效'}`,
      '分享链接',
      {
        confirmButtonText: '复制链接',
        callback: () => {
          navigator.clipboard.writeText(shareUrl)
          ElMessage.success('链接已复制到剪贴板')
        },
      }
    )
  } catch (error) {
    ElMessage.error('生成分享链接失败')
  }
}

// 恢复图片
const handleRestore = async (image: Image) => {
  try {
    await ElMessageBox.confirm('确定要恢复这张图片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await imageApi.restore(image.id)
    ElMessage.success('恢复成功')
    loadImageList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('恢复失败')
    }
  }
}

// 归档图片
const handleArchive = async (image: Image) => {
  try {
    await ElMessageBox.confirm('确定要归档这张图片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await imageApi.archive(image.id)
    ElMessage.success('归档成功')
    loadImageList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('归档失败')
    }
  }
}

// 删除图片
const handleDelete = async (image: Image) => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await imageApi.delete(image.id)
    ElMessage.success('删除成功')
    loadImageList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedImages.value.length === 0) {
    ElMessage.warning('请选择要删除的图片')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedImages.value.length} 张图片吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false,
        distinguishCancelAndClose: true,
      }
    )
    const ids = selectedImages.value.map((img) => img.id)
    await imageApi.batchDelete({ ids })
    ElMessage.success(`成功删除 ${selectedImages.value.length} 张图片`)
    selectedImages.value = []
    loadImageList()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 上传成功回调
const handleUploadSuccess = () => {
  loadImageList()
}

// 图片更新回调
const handleImageUpdate = () => {
  loadImageList()
}

// 键盘快捷键
const handleKeydown = (e: KeyboardEvent) => {
  // Ctrl/Cmd + F: 聚焦搜索框
  if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
    e.preventDefault()
    const searchInput = document.querySelector('.filter-form input[type="text"]') as HTMLInputElement
    if (searchInput) {
      searchInput.focus()
    }
  }
  
  // Delete: 删除选中的图片
  if (e.key === 'Delete' && selectedImages.value.length > 0) {
    e.preventDefault()
    handleBatchDelete()
  }
  
  // Ctrl/Cmd + A: 全选
  if ((e.ctrlKey || e.metaKey) && e.key === 'a') {
    e.preventDefault()
    if (imageList.value.length > 0) {
      handleSelectAll(true)
    }
  }
  
  // Escape: 取消选择
  if (e.key === 'Escape') {
    selectedImages.value = []
  }
}

onMounted(() => {
  loadQueryFromLocal()
  loadImageList()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
.image-view {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 16px;

  .main-card {
    border-radius: 18px;
    border: 1px solid var(--surface-border);
    background: var(--surface-color);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      padding: 8px 6px;
      background: var(--brand-gradient-subtle);
      border-bottom: 1px solid var(--surface-border);

      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .title-icon {
        width: 44px;
        height: 44px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        background: linear-gradient(145deg, rgba(37, 99, 235, 0.14), rgba(34, 211, 238, 0.12));
        color: var(--primary-color);
        box-shadow: 0 10px 24px rgba(37, 99, 235, 0.14);
      }

      .title-meta {
        display: flex;
        flex-direction: column;
        gap: 2px;
      }

      .title-row {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .title {
        font-size: 20px;
        font-weight: 700;
        color: var(--text-primary);
        letter-spacing: 0.2px;
      }

      .subtitle {
        margin: 0;
        color: var(--text-tertiary);
        font-size: 13px;
      }

      .total-tag {
        background: #fff;
        border-color: var(--surface-border);
        color: var(--text-secondary);
      }

      .view-mode-switch {
        background: #fff;
        border-radius: 12px;
        padding: 4px;
        box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6), 0 10px 28px rgba(15, 23, 42, 0.08);
      }

      .header-actions {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        align-items: center;
        justify-content: flex-end;
        padding: 8px 10px;
        background: var(--ph-bg-card);
        border-radius: 12px;
        border: 1px solid var(--ph-border-subtle);
        box-shadow: 0 16px 36px rgba(15, 23, 42, 0.08);

        .el-button {
          border-radius: 10px;
        }
      }
    }
  }

  .filter-toolbar {
    margin: 16px 0 10px;
    padding: 14px 16px;
    background: var(--ph-bg-card);
    border: 1px solid var(--ph-border-subtle);
    border-radius: 14px;
    box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);

    .filter-form {
      display: flex;
      flex-wrap: wrap;
      align-items: flex-end;
      gap: 12px 16px;
      margin: 0;

      :deep(.el-form-item) {
        margin-bottom: 0;
      }

      :deep(.el-form-item__label) {
        color: var(--text-secondary);
        font-weight: 600;
      }
    }
  }

  .image-list-container {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .image-table {
      border-radius: 12px;
      overflow: hidden;
      border: 1px solid var(--surface-border);

      :deep(.el-table__inner-wrapper::before) {
        background-color: transparent;
      }

      :deep(.el-table__header-wrapper th) {
        background-color: var(--ph-bg-card-subtle);
        color: var(--text-secondary);
        font-weight: 600;
      }

      :deep(.el-table__row) {
        transition: background-color 0.2s ease, box-shadow 0.2s ease;
      }

      :deep(.el-table__body tr:hover > td) {
        background-color: var(--brand-soft);
      }
    }

    .thumbnail-cell {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 80px;

      .thumbnail-image {
        width: 80px;
        height: 80px;
        display: block;
        border-radius: 4px;
        cursor: pointer;
        border: 1px solid var(--surface-border);
        box-shadow: 0 12px 22px rgba(15, 23, 42, 0.06);

        // 确保内部图片不会按照原始尺寸撑开单元格
        :deep(.el-image__inner) {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
        }
      }

      .image-placeholder {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 80px;
        height: 80px;
        background-color: var(--ph-bg-card-subtle);
        color: var(--text-tertiary);
        border: 1px dashed var(--ph-border-subtle);
        border-radius: 6px;
      }

      .image-error {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 80px;
        height: 80px;
        background-color: var(--ph-bg-card-subtle);
        color: var(--text-tertiary);
        border-radius: 6px;
      }
    }

    .text-muted {
      color: var(--text-tertiary);
    }

    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 4px;
      border-top: 1px solid var(--surface-border);
    }
  }

  .image-grid-view {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 16px;

    .grid-item {
      position: relative;
      border: 1px solid var(--ph-border-subtle);
      border-radius: 14px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.25s ease;
      background: var(--ph-bg-card);
      box-shadow: 0 16px 36px rgba(15, 23, 42, 0.08);

      &:hover {
        border-color: var(--primary-color);
        box-shadow: var(--surface-shadow-hover);
        transform: translateY(-2px);

        .grid-actions {
          opacity: 1;
        }
      }

      &.selected {
        border-color: var(--primary-color);
        background: rgba(37, 99, 235, 0.05);
        box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.12);
      }

      .grid-checkbox {
        position: absolute;
        top: 10px;
        left: 10px;
        z-index: 10;
        background: rgba(255, 255, 255, 0.95);
        border-radius: 6px;
        padding: 4px;
        box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
      }

      .grid-thumbnail {
        width: 100%;
        height: 200px;
        background: #f8fafc;

        :deep(.el-image__inner) {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
        }
      }

      .image-error {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 200px;
        background-color: var(--ph-bg-card-subtle);
        color: var(--text-tertiary);
      }

      .grid-info {
        padding: 12px;

        .grid-filename {
          font-size: 14px;
          font-weight: 600;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          color: var(--text-primary);
        }

        .grid-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 12px;
          color: var(--text-tertiary);

          .grid-size {
            margin-left: 8px;
          }
        }
      }

      .grid-actions {
        position: absolute;
        bottom: 12px;
        right: 12px;
        opacity: 0;
        transition: opacity 0.3s;
        display: flex;
        gap: 4px;
        background: rgba(255, 255, 255, 0.95);
        padding: 6px;
        border-radius: 8px;
        box-shadow: 0 14px 28px rgba(15, 23, 42, 0.14);
      }
    }
  }

  .image-card-view {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 18px;

    .card-item {
      position: relative;
      border: 1px solid var(--ph-border-subtle);
      border-radius: 14px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.25s ease;
      background: var(--ph-bg-card);
      box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);

      &:hover {
        border-color: var(--primary-color);
        box-shadow: var(--surface-shadow-hover);
        transform: translateY(-4px);

        .card-overlay {
          opacity: 1;
        }
      }

      &.selected {
        border-color: var(--primary-color);
        box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.12);
      }

      .card-checkbox {
        position: absolute;
        top: 12px;
        left: 12px;
        z-index: 10;
        background: rgba(255, 255, 255, 0.95);
        border-radius: 50%;
        padding: 4px;
        box-shadow: 0 12px 22px rgba(15, 23, 42, 0.16);
      }

      .card-thumbnail {
        width: 100%;
        height: 240px;
        background: #f8fafc;

        :deep(.el-image__inner) {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
        }
      }

      .image-error {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 240px;
        background-color: var(--ph-bg-card-subtle);
        color: var(--text-tertiary);
      }

      .card-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(180deg, rgba(15, 23, 42, 0.15), rgba(15, 23, 42, 0.55));
        display: flex;
        justify-content: center;
        align-items: center;
        opacity: 0;
        transition: opacity 0.25s ease;

        .card-actions {
          display: flex;
          gap: 12px;

          .el-button {
            background: rgba(255, 255, 255, 0.92);
            border-color: rgba(255, 255, 255, 0.8);
          }
        }
      }

      .card-info {
        padding: 14px;

        .card-filename {
          font-size: 15px;
          font-weight: 600;
          margin-bottom: 10px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          color: var(--text-primary);
        }

        .card-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 13px;
          color: var(--text-tertiary);

          .card-size {
            margin-left: 8px;
          }
        }
      }
    }
  }
}

  .preview-dialog {
    :deep(.el-dialog__body) {
      padding: 12px 16px 18px;
    }
  }

  .preview-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    max-height: 72vh;
  }

  .preview-image {
    max-width: 100%;
    max-height: 70vh;
    display: block;
    margin: 0 auto;
    background: #f8fafc;
    border-radius: 12px;
    box-shadow: 0 12px 30px rgba(15, 23, 42, 0.18);
  }

  .preview-error {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    width: 100%;
    min-height: 320px;
    color: var(--text-tertiary);
    background: var(--ph-bg-card-subtle);
    border-radius: 12px;
    gap: 6px;
  }

/* 移动端适配 - REQ-001 */
@media (max-width: 768px) {
  .image-view {
    padding: 0;
    font-size: 0.9em;

    .main-card {
      border-radius: 0;
      margin: 0 -12px;

      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
        padding: 12px;

        .title {
          font-size: 16px;
        }

        .header-actions {
          width: 100%;
          flex-wrap: wrap;

          .el-button {
            flex: 1;
            min-width: 0;

            .button-text {
              display: none;
            }
          }

          .view-mode-switch {
            width: 100%;
            margin-top: 8px;
          }
        }
      }
    }

    .filter-toolbar {
      margin-bottom: 12px;
      padding: 0 12px;

      .filter-form {
        :deep(.el-form) {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        :deep(.el-form-item) {
          margin: 0;
          width: 100%;
        }

        :deep(.el-select),
        :deep(.el-date-picker),
        :deep(.el-input) {
          width: 100% !important;
        }
      }
    }

    .image-list-container {
      padding: 0 12px;

      :deep(.el-table) {
        font-size: 0.9em;
      }

      :deep(.el-table th),
      :deep(.el-table td) {
        padding: 8px 4px;
      }

      .pagination-wrapper {
        justify-content: center;

        :deep(.el-pagination) {
          font-size: 0.9em;
        }

        :deep(.el-pagination__sizes),
        :deep(.el-pagination__jump) {
          display: none;
        }
      }
    }

    .image-grid-view {
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 12px;
    }

    .image-card-view {
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 14px;

      .card-item .card-thumbnail {
        height: 200px;
      }
    }
  }
}
</style>

