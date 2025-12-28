<template>
  <div class="image-view">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="title">图片管理</span>
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
          border
          stripe
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
                <el-image
                  :src="getImageUrl(row.thumbnailUrl || row.fileUrl)"
                  :preview-src-list="[getImageUrl(row.fileUrl)]"
                  fit="cover"
                  class="thumbnail-image"
                  lazy
                  loading="lazy"
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
          <el-table-column prop="createdAt" label="上传时间" width="180" />
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
            <el-image
              :src="getImageUrl(image.thumbnailUrl || image.fileUrl)"
              :preview-src-list="imageList.map((img) => getImageUrl(img.fileUrl))"
              fit="cover"
              class="grid-thumbnail"
              lazy
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
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
            <el-image
              :src="getImageUrl(image.thumbnailUrl || image.fileUrl)"
              :preview-src-list="imageList.map((img) => getImageUrl(img.fileUrl))"
              fit="cover"
              class="card-thumbnail"
              lazy
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
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
      width="500px"
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
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
import { imageApi } from '@/services/imageApi'
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
import ImageUploadDialog from './components/ImageUploadDialog.vue'
import ImageDetailDialog from './components/ImageDetailDialog.vue'
import ImageStatisticsDialog from './components/ImageStatisticsDialog.vue'

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
    console.error(error)
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
      console.error('Failed to load query from local storage', e)
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
    console.error(error)
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
const handleView = (image: Image) => {
  currentImage.value = image
  showDetailDialog.value = true
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
    console.error(error)
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
      console.error(error)
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
      console.error(error)
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
      console.error(error)
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
      console.error(error)
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
  .main-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title {
        font-size: 18px;
        font-weight: 600;
      }

      .header-actions {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        align-items: center;

        @media (max-width: 768px) {
          .button-text {
            display: none;
          }

          .view-mode-switch {
            margin-left: 0 !important;
            margin-top: 8px;
            width: 100%;
          }
        }
      }
    }
  }

      .filter-toolbar {
    margin-bottom: 16px;

    .filter-form {
      margin: 0;

      @media (max-width: 768px) {
        :deep(.el-form-item) {
          margin-bottom: 12px;
        }
      }
    }
  }

  .image-list-container {
    .thumbnail-cell {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 80px;

      .thumbnail-image {
        width: 80px;
        height: 80px;
        border-radius: 4px;
        cursor: pointer;
      }

      .image-placeholder {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 80px;
        height: 80px;
        background-color: #f5f7fa;
        color: #c0c4cc;
      }

      .image-error {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 80px;
        height: 80px;
        background-color: #f5f7fa;
        color: #909399;
      }
    }

    .text-muted {
      color: #909399;
    }

    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }

    // 网格视图
    .image-grid-view {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 16px;

      @media (max-width: 768px) {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 12px;
      }

      @media (max-width: 480px) {
        grid-template-columns: repeat(2, 1fr);
        gap: 8px;
      }

      .grid-item {
        position: relative;
        border: 1px solid #dcdfe6;
        border-radius: 8px;
        overflow: hidden;
        cursor: pointer;
        transition: all 0.3s;
        background: #fff;

        &:hover {
          border-color: #409eff;
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
          transform: translateY(-2px);

          .grid-actions {
            opacity: 1;
          }
        }

        &.selected {
          border-color: #409eff;
          background: #ecf5ff;
        }

        .grid-checkbox {
          position: absolute;
          top: 8px;
          left: 8px;
          z-index: 10;
          background: rgba(255, 255, 255, 0.9);
          border-radius: 4px;
          padding: 4px;
        }

        .grid-thumbnail {
          width: 100%;
          height: 200px;
        }

        .image-error {
          display: flex;
          justify-content: center;
          align-items: center;
          width: 100%;
          height: 200px;
          background-color: #f5f7fa;
          color: #909399;
        }

        .grid-info {
          padding: 12px;

          .grid-filename {
            font-size: 14px;
            font-weight: 500;
            margin-bottom: 8px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .grid-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 12px;
            color: #909399;

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
          background: rgba(255, 255, 255, 0.9);
          padding: 4px;
          border-radius: 4px;
        }
      }
    }

    // 卡片视图
    .image-card-view {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 16px;

      @media (max-width: 768px) {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 16px;
      }

      @media (max-width: 480px) {
        grid-template-columns: 1fr;
        gap: 12px;
      }

      .card-item {
        position: relative;
        border: 1px solid #dcdfe6;
        border-radius: 8px;
        overflow: hidden;
        cursor: pointer;
        transition: all 0.3s;
        background: #fff;

        &:hover {
          border-color: #409eff;
          box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
          transform: translateY(-4px);

          .card-overlay {
            opacity: 1;
          }
        }

        &.selected {
          border-color: #409eff;
          box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
        }

        .card-checkbox {
          position: absolute;
          top: 12px;
          left: 12px;
          z-index: 10;
          background: rgba(255, 255, 255, 0.95);
          border-radius: 50%;
          padding: 4px;
        }

        .card-thumbnail {
          width: 100%;
          height: 250px;
        }

        .image-error {
          display: flex;
          justify-content: center;
          align-items: center;
          width: 100%;
          height: 250px;
          background-color: #f5f7fa;
          color: #909399;
        }

        .card-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          justify-content: center;
          align-items: center;
          opacity: 0;
          transition: opacity 0.3s;

          .card-actions {
            display: flex;
            gap: 12px;

            .el-button {
              background: rgba(255, 255, 255, 0.9);
            }
          }
        }

        .card-info {
          padding: 16px;

          .card-filename {
            font-size: 15px;
            font-weight: 500;
            margin-bottom: 12px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .card-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 13px;
            color: #909399;

            .card-size {
              margin-left: 8px;
            }
          }
        }
      }
    }
  }
}
</style>

