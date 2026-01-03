<template>
  <div class="bookmark-view">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="title">数据维护</span>
          <div class="header-actions">
            <el-button type="primary" @click="showImportDialog = true">
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button @click="handleDownloadTemplate">
              <el-icon><Download /></el-icon>
              下载模板
            </el-button>
            <el-button type="primary" @click="showUrlDialog = true">
              <el-icon><Plus /></el-icon>
              添加网址
            </el-button>
          </div>
        </div>
      </template>

      <div class="content-wrapper">
        <!-- 左侧：标签树 -->
        <div class="tag-sidebar">
          <div class="tag-header">
            <span>标签管理</span>
            <el-button type="text" size="small" @click="showTagDialog = true">
              <el-icon><Plus /></el-icon>
              添加标签
            </el-button>
          </div>
          <el-tree
            ref="tagTreeRef"
            :data="tagTree"
            :props="{ children: 'children', label: 'name' }"
            node-key="id"
            :default-expanded-keys="expandedKeys"
            :highlight-current="true"
            :current-node-key="selectedTagId"
            draggable
            @node-drop="handleTagDrop"
            @node-click="handleTagClick"
          >
            <template #default="{ node, data }">
              <div
                class="tree-node"
                :class="{
                  'is-selected': selectedTagId === data.id,
                  'is-level-2': data.level === 2,
                }"
              >
                <span class="node-label" @click.stop="handleTagClick(data)">{{ data.name }}</span>
                <span class="node-count">({{ data.urlCount }})</span>
                <div class="node-actions">
                  <el-button
                    type="text"
                    size="small"
                    @click.stop="handleEditTag(data)"
                    v-if="data.level === 1"
                  >
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click.stop="handleAddChildTag(data)"
                    v-if="data.level === 1"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click.stop="handleDeleteTag(data)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </template>
          </el-tree>
          <div class="tag-actions-footer">
            <el-button type="text" size="small" @click="handleClearFilter" v-if="selectedTagId">
              <el-icon><Close /></el-icon>
              清除筛选
            </el-button>
          </div>
        </div>

        <!-- 右侧：网址展示区 -->
        <div class="url-content">
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索网址..."
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <div v-if="searchMode" class="search-results">
            <div v-if="searchResults.length === 0" class="empty-state">
              <el-empty description="未找到相关网址" />
            </div>
            <div v-else class="url-grid">
              <div
                v-for="url in searchResults"
                :key="url.id"
                class="url-card"
                @click="handleUrlClick(url.url)"
              >
                <div class="url-icon">
                  <img v-if="url.iconUrl" :src="url.iconUrl" :alt="url.title" />
                  <el-icon v-else><Link /></el-icon>
                </div>
                <div class="url-info">
                  <div class="url-title">{{ url.title }}</div>
                  <div class="url-description">{{ url.description }}</div>
                  <div class="url-tags">
                    <el-tag
                      v-for="tag in url.tags"
                      :key="tag.id"
                      size="small"
                      class="url-tag"
                    >
                      {{ tag.name }}
                    </el-tag>
                  </div>
                </div>
                <div class="url-actions">
                  <el-button
                    type="text"
                    size="small"
                    @click.stop="handleEditUrl(url)"
                  >
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click.stop="handleDeleteUrl(url.id)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="url-groups">
            <div v-if="urlGroups.length === 0" class="empty-state">
              <el-empty description="暂无网址，点击上方按钮添加" />
            </div>
            <div v-else>
              <div
                v-for="group in filteredUrlGroups"
                :key="group.parentTag.id"
                class="url-group"
                v-show="shouldShowGroup(group)"
              >
                <div class="group-header">
                  <h3>{{ group.parentTag.name }}</h3>
                  <span class="group-count">共 {{ getGroupUrlCount(group) }} 个网址</span>
                </div>

                <!-- 二级标签分组 -->
                <div
                  v-for="subGroup in group.subGroups"
                  :key="subGroup.tag.id"
                  class="sub-group"
                >
                  <div class="sub-group-header">
                    <h4>{{ subGroup.tag.name }}</h4>
                    <span class="sub-group-count">({{ subGroup.urls.length }})</span>
                  </div>
                  <div class="url-grid">
                    <div
                      v-for="url in subGroup.urls"
                      :key="url.id"
                      class="url-card"
                      @click="handleUrlClick(url.url)"
                    >
                      <div class="url-icon">
                        <img v-if="url.iconUrl" :src="url.iconUrl" :alt="url.title" />
                        <el-icon v-else><Link /></el-icon>
                      </div>
                      <div class="url-info">
                        <div class="url-title">{{ url.title }}</div>
                        <div class="url-description">{{ url.description }}</div>
                        <div class="url-tags" v-if="url.tags && url.tags.length > 0">
                          <el-tag
                            v-for="tag in url.tags"
                            :key="tag.id"
                            size="small"
                            class="url-tag"
                            :effect="tag.id === selectedTagId ? 'dark' : 'plain'"
                            @click.stop="handleTagClickFromCard(tag)"
                          >
                            {{ tag.name }}
                          </el-tag>
                        </div>
                      </div>
                      <div class="url-actions">
                        <el-button
                          type="text"
                          size="small"
                          @click.stop="handleEditUrl(url)"
                        >
                          <el-icon><Edit /></el-icon>
                        </el-button>
                        <el-button
                          type="text"
                          size="small"
                          @click.stop="handleDeleteUrl(url.id)"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 未分类网址 -->
                <div v-if="group.uncategorizedUrls.length > 0" class="sub-group">
                  <div class="sub-group-header">
                    <h4>未分类</h4>
                    <span class="sub-group-count">({{ group.uncategorizedUrls.length }})</span>
                  </div>
                  <div class="url-grid">
                    <div
                      v-for="url in group.uncategorizedUrls"
                      :key="url.id"
                      class="url-card"
                      @click="handleUrlClick(url.url)"
                    >
                      <div class="url-icon">
                        <img v-if="url.iconUrl" :src="url.iconUrl" :alt="url.title" />
                        <el-icon v-else><Link /></el-icon>
                      </div>
                      <div class="url-info">
                        <div class="url-title">{{ url.title }}</div>
                        <div class="url-description">{{ url.description }}</div>
                        <div class="url-tags" v-if="url.tags && url.tags.length > 0">
                          <el-tag
                            v-for="tag in url.tags"
                            :key="tag.id"
                            size="small"
                            class="url-tag"
                            :effect="tag.id === selectedTagId ? 'dark' : 'plain'"
                            @click.stop="handleTagClickFromCard(tag)"
                          >
                            {{ tag.name }}
                          </el-tag>
                        </div>
                      </div>
                      <div class="url-actions">
                        <el-button
                          type="text"
                          size="small"
                          @click.stop="handleEditUrl(url)"
                        >
                          <el-icon><Edit /></el-icon>
                        </el-button>
                        <el-button
                          type="text"
                          size="small"
                          @click.stop="handleDeleteUrl(url.id)"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 标签对话框 -->
    <BookmarkTagDialog
      v-model="showTagDialog"
      :tag="editingTag"
      :parent-tag="parentTagForChild"
      @success="handleTagDialogSuccess"
    />

    <!-- 网址对话框 -->
    <BookmarkUrlDialog
      v-model="showUrlDialog"
      :url="editingUrl"
      @success="handleUrlDialogSuccess"
    />

    <!-- 导入对话框 -->
    <BookmarkImportDialog
      v-model="showImportDialog"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Search,
  Upload,
  Download,
  Link,
  Close,
} from '@element-plus/icons-vue'
import { bookmarkApi } from '@/services/bookmarkApi'
import type {
  BookmarkTag,
  BookmarkUrl,
  BookmarkGroup,
} from '@/types/bookmark'
import BookmarkTagDialog from './components/BookmarkTagDialog.vue'
import BookmarkUrlDialog from './components/BookmarkUrlDialog.vue'
import BookmarkImportDialog from './components/BookmarkImportDialog.vue'

const tagTree = ref<BookmarkTag[]>([])
const urlGroups = ref<BookmarkGroup[]>([])
const searchKeyword = ref('')
const searchResults = ref<BookmarkUrl[]>([])
const searchMode = computed(() => searchKeyword.value.trim().length > 0)
const selectedTagId = ref<string | null>(null)
const expandedKeys = ref<string[]>([])

const showTagDialog = ref(false)
const showUrlDialog = ref(false)
const showImportDialog = ref(false)
const editingTag = ref<BookmarkTag | null>(null)
const editingUrl = ref<BookmarkUrl | null>(null)
const parentTagForChild = ref<BookmarkTag | null>(null)

const tagTreeRef = ref()

// 加载标签树
const loadTagTree = async () => {
  try {
    tagTree.value = await bookmarkApi.getTagTree()
    // 初始化展开所有一级标签
    expandedKeys.value = tagTree.value.map(tag => tag.id)
  } catch (error) {
    ElMessage.error('加载标签树失败')
  }
}

// 加载网址分组
const loadUrlGroups = async () => {
  try {
    urlGroups.value = await bookmarkApi.getUrlGroups()
    // 初始化展开所有一级标签
    expandedKeys.value = tagTree.value.map(tag => tag.id)
  } catch (error) {
    ElMessage.error('加载网址分组失败')
  }
}

// 搜索网址
const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResults.value = []
    return
  }
  try {
    searchResults.value = await bookmarkApi.searchUrls(keyword)
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

// 点击标签树节点
const handleTagClick = (tag: BookmarkTag) => {
  selectedTagId.value = tag.id
  // 清空搜索关键词
  searchKeyword.value = ''
  searchResults.value = []
  // 选中标签树节点
  if (tagTreeRef.value) {
    tagTreeRef.value.setCurrentKey(tag.id)
  }
}

// 从卡片点击标签
const handleTagClickFromCard = (tag: BookmarkTag) => {
  selectedTagId.value = tag.id
  // 清空搜索关键词
  searchKeyword.value = ''
  searchResults.value = []
  // 展开并选中标签树中的对应节点
  if (tagTreeRef.value) {
    tagTreeRef.value.setCurrentKey(tag.id)
    // 如果是一级标签，确保展开
    const parentTag = tagTree.value.find(t => t.id === tag.id || t.children?.some(c => c.id === tag.id))
    if (parentTag && parentTag.id !== tag.id) {
      expandedKeys.value = [...new Set([...expandedKeys.value, parentTag.id])]
    }
  }
}

// 清除筛选
const handleClearFilter = () => {
  selectedTagId.value = null
  searchKeyword.value = ''
  searchResults.value = []
  if (tagTreeRef.value) {
    tagTreeRef.value.setCurrentKey(null)
  }
}

// 获取分组网址总数
const getGroupUrlCount = (group: BookmarkGroup) => {
  let count = group.uncategorizedUrls.length
  group.subGroups.forEach((subGroup) => {
    count += subGroup.urls.length
  })
  return count
}

// 过滤后的网址分组（根据选中的标签）
const filteredUrlGroups = computed(() => {
  if (!selectedTagId.value) {
    return urlGroups.value
  }
  
  return urlGroups.value.map(group => {
    // 如果选中的是一级标签，只显示该分组
    if (group.parentTag.id === selectedTagId.value) {
      return group
    }
    
    // 如果选中的是二级标签，只显示包含该标签的子分组
    const filteredSubGroups = group.subGroups.filter(subGroup => 
      subGroup.tag.id === selectedTagId.value
    )
    
    // 检查未分类网址是否包含该标签
    const filteredUncategorized = group.uncategorizedUrls.filter(url =>
      url.tags?.some(tag => tag.id === selectedTagId.value)
    )
    
    // 如果该分组有匹配的内容，返回过滤后的分组
    if (filteredSubGroups.length > 0 || filteredUncategorized.length > 0) {
      return {
        ...group,
        subGroups: filteredSubGroups,
        uncategorizedUrls: filteredUncategorized
      }
    }
    
    return null
  }).filter(group => group !== null) as BookmarkGroup[]
})

// 判断是否应该显示该分组
const shouldShowGroup = (group: BookmarkGroup) => {
  if (!selectedTagId.value) {
    return true
  }
  
  // 如果是一级标签匹配
  if (group.parentTag.id === selectedTagId.value) {
    return true
  }
  
  // 如果有匹配的子分组或未分类网址
  return group.subGroups.length > 0 || group.uncategorizedUrls.length > 0
}

// 点击网址
const handleUrlClick = (url: string) => {
  window.open(url, '_blank')
}

// 标签拖拽
const handleTagDrop = async () => {
  // 获取拖拽后的标签顺序
  const treeData = tagTreeRef.value?.data || []
  const tagIds: string[] = []
  
  const collectTagIds = (tags: BookmarkTag[]) => {
    tags.forEach((tag) => {
      tagIds.push(tag.id)
      if (tag.children) {
        collectTagIds(tag.children)
      }
    })
  }
  
  collectTagIds(treeData)
  
  try {
    await bookmarkApi.updateTagSort({ tagIds })
    ElMessage.success('排序更新成功')
    await loadTagTree()
  } catch (error) {
    ElMessage.error('排序更新失败')
  }
}

// 编辑标签
const handleEditTag = (tag: BookmarkTag) => {
  editingTag.value = tag
  parentTagForChild.value = null
  showTagDialog.value = true
}

// 添加子标签
const handleAddChildTag = (parentTag: BookmarkTag) => {
  editingTag.value = null
  parentTagForChild.value = parentTag
  showTagDialog.value = true
}

// 删除标签
const handleDeleteTag = async (tag: BookmarkTag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签"${tag.name}"吗？`,
      '确认删除',
      {
        type: 'warning',
      }
    )
    await bookmarkApi.deleteTag(tag.id)
    ElMessage.success('删除成功')
    await loadTagTree()
    await loadUrlGroups()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 编辑网址
const handleEditUrl = (url: BookmarkUrl) => {
  editingUrl.value = url
  showUrlDialog.value = true
}

// 删除网址
const handleDeleteUrl = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除该网址吗？', '确认删除', {
      type: 'warning',
    })
    await bookmarkApi.deleteUrl(id)
    ElMessage.success('删除成功')
    // 删除网址后需要同步刷新标签树中的统计数量
    await loadTagTree()
    await loadUrlGroups()
    if (searchMode.value) {
      await handleSearch()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 标签对话框成功
const handleTagDialogSuccess = async () => {
  await loadTagTree()
  await loadUrlGroups()
}

// 网址对话框成功
const handleUrlDialogSuccess = async () => {
  await loadUrlGroups()
  editingUrl.value = null
}

// 导入成功
const handleImportSuccess = async () => {
  await loadTagTree()
  await loadUrlGroups()
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

onMounted(() => {
  loadTagTree()
  loadUrlGroups()
})
</script>

<style scoped>
.bookmark-view {
  display: flex;
  flex-direction: column;
  padding: 0;
}

.main-card {
  min-height: calc(100vh - 128px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 
    0 20px 60px rgba(15, 23, 42, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset,
    0 1px 0 rgba(255, 255, 255, 0.9) inset;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.95) 100%);
  backdrop-filter: blur(40px) saturate(180%);
  position: relative;
  overflow: hidden;
}

.main-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(99, 102, 241, 0.3) 20%, 
    rgba(139, 92, 246, 0.3) 50%, 
    rgba(236, 72, 153, 0.3) 80%, 
    transparent 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 40%, #ec4899 80%, #f59e0b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% 200%;
  animation: gradientShift 8s ease infinite;
  position: relative;
  padding-left: 12px;
}

.title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, #6366f1, #ec4899);
  border-radius: 2px;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.header-actions {
  display: flex;
  gap: 10px;
}

.header-actions :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 10px 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.header-actions :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
}

.header-actions :deep(.el-button:not(.el-button--primary)) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.2);
  backdrop-filter: blur(10px);
}

.header-actions :deep(.el-button:not(.el-button--primary):hover) {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.content-wrapper {
  display: flex;
  gap: 24px;
  min-height: 600px;
}

.tag-sidebar {
  width: 320px;
  border-right: 1px solid rgba(148, 163, 184, 0.15);
  padding-right: 28px;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.5) 0%, transparent 100%);
  padding-top: 8px;
}

.tag-sidebar::-webkit-scrollbar {
  width: 8px;
}

.tag-sidebar::-webkit-scrollbar-track {
  background: rgba(241, 245, 249, 0.5);
  border-radius: 4px;
}

.tag-sidebar::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  border-radius: 4px;
  border: 2px solid transparent;
  background-clip: padding-box;
}

.tag-sidebar::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, rgba(99, 102, 241, 0.5), rgba(139, 92, 246, 0.5));
  background-clip: padding-box;
}

.tag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: 700;
  font-size: 17px;
  color: #1e293b;
  padding-bottom: 16px;
  border-bottom: 2px solid;
  border-image: linear-gradient(90deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.1), transparent) 1;
  position: relative;
}

.tag-header::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 1px;
}

.tag-header :deep(.el-button) {
  padding: 6px 12px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.tag-header :deep(.el-button:hover) {
  background: rgba(99, 102, 241, 0.1);
  transform: scale(1.05);
}

.tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 10px 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 3px 0;
  position: relative;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid transparent;
}

/* 一级标签：增加底部间距，避免与下一个一级标签重叠 */
.tree-node:not(.is-level-2) {
  margin-bottom: 20px !important;
}

/* 二级标签：增加行间距，避免堆叠在一起 */
.tree-node.is-level-2 {
  margin: 12px 0 !important;
  padding-top: 14px;
  padding-bottom: 14px;
}

/* 通过深度选择器覆盖 Element Plus Tree 内部节点样式 */
.tag-sidebar :deep(.el-tree-node__children) {
  padding-left: 0;
  margin-top: 4px;
  margin-bottom: 24px; /* 增加底部间距，避免一级标签的最后一个二级标签与下一个一级标签重叠 */
}

/* 二级标签节点之间的间距 */
.tag-sidebar :deep(.el-tree-node__children .el-tree-node) {
  margin-bottom: 12px;
}

/* 最后一个二级标签的底部间距由父容器控制，这里重置为0 */
.tag-sidebar :deep(.el-tree-node__children .el-tree-node:last-child) {
  margin-bottom: 0;
}

/* 确保二级标签节点容器本身也有间距 */
.tag-sidebar :deep(.el-tree-node__children .el-tree-node__content) {
  margin-bottom: 0;
}

/* 针对二级标签节点，增加额外的上下间距 */
.tag-sidebar :deep(.el-tree-node__children .el-tree-node .tree-node.is-level-2) {
  margin-top: 16px;
  margin-bottom: 16px;
}

/* 一级标签节点（根级节点）之间增加间距，避免重叠 */
.tag-sidebar :deep(.el-tree > .el-tree-node) {
  margin-bottom: 24px;
}

.tree-node::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  border-radius: 0 2px 2px 0;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.tree-node:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08), rgba(139, 92, 246, 0.05));
  border-color: rgba(99, 102, 241, 0.2);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
}

.tree-node:hover::before {
  transform: translateY(-50%) scaleY(1);
  height: 60%;
}

.tree-node.is-selected {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.12));
  border-color: rgba(99, 102, 241, 0.3);
  font-weight: 600;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.15);
  transform: translateX(4px);
}

.tree-node.is-selected::before {
  transform: translateY(-50%) scaleY(1);
  height: 80%;
  width: 4px;
}

.node-label {
  flex: 1;
  color: #1e293b;
  font-size: 14px;
  user-select: none;
  font-weight: 500;
  letter-spacing: 0.2px;
}

.tree-node.is-selected .node-label {
  color: #6366f1;
  font-weight: 700;
}

.node-count {
  color: #94a3b8;
  font-size: 12px;
  margin-left: 10px;
  font-weight: 600;
  padding: 2px 8px;
  background: rgba(148, 163, 184, 0.1);
  border-radius: 10px;
  min-width: 32px;
  text-align: center;
}

.tree-node.is-selected .node-count {
  background: rgba(99, 102, 241, 0.15);
  color: #6366f1;
}

.node-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
  margin-left: 8px;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.tag-actions-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid;
  border-image: linear-gradient(90deg, rgba(99, 102, 241, 0.2), transparent) 1;
}

.tag-actions-footer :deep(.el-button) {
  width: 100%;
  justify-content: center;
  padding: 10px;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.08));
  border: 1px solid rgba(99, 102, 241, 0.2);
  color: #6366f1;
}

.tag-actions-footer :deep(.el-button:hover) {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.15));
  border-color: rgba(99, 102, 241, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
}

.url-content {
  flex: 1;
  min-width: 0;
}

.search-bar {
  margin-bottom: 32px;
  position: relative;
}

.search-bar::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 16px;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 50%;
  opacity: 0.1;
  z-index: 0;
  pointer-events: none;
}

.search-bar :deep(.el-input__wrapper) {
  border-radius: 16px;
  box-shadow: 
    0 2px 8px rgba(15, 23, 42, 0.06),
    0 0 0 1px rgba(148, 163, 184, 0.1) inset;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 12px 16px;
}

.search-bar :deep(.el-input__wrapper:hover) {
  box-shadow: 
    0 4px 16px rgba(99, 102, 241, 0.12),
    0 0 0 1px rgba(99, 102, 241, 0.2) inset;
  background: rgba(255, 255, 255, 1);
}

.search-bar :deep(.el-input__wrapper.is-focus) {
  box-shadow: 
    0 6px 24px rgba(99, 102, 241, 0.2),
    0 0 0 2px rgba(99, 102, 241, 0.3) inset;
  background: rgba(255, 255, 255, 1);
}

.search-bar :deep(.el-input__inner) {
  font-size: 15px;
  color: #1e293b;
  font-weight: 500;
}

.search-bar :deep(.el-input__prefix) {
  color: #6366f1;
}

.search-results,
.url-groups {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.02), rgba(139, 92, 246, 0.02));
  border-radius: 20px;
  border: 2px dashed rgba(99, 102, 241, 0.2);
  margin: 20px 0;
}

.empty-state :deep(.el-empty) {
  padding: 40px 20px;
}

.empty-state :deep(.el-empty__description) {
  color: #64748b;
  font-size: 15px;
  font-weight: 500;
}

.url-group {
  margin-bottom: 56px;
  padding: 32px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0.2));
  border-radius: 24px;
  border: 1px solid rgba(148, 163, 184, 0.1);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.url-group::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(99, 102, 241, 0.3) 20%, 
    rgba(139, 92, 246, 0.3) 50%, 
    rgba(236, 72, 153, 0.3) 80%, 
    transparent 100%);
  opacity: 0.5;
}

.url-group:last-child {
  margin-bottom: 0;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 2px solid;
  border-image: linear-gradient(90deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.1), transparent) 1;
  position: relative;
}

.group-header::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 80px;
  height: 2px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 1px;
}

.group-header h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.3px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  padding-left: 16px;
}

.group-header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  border-radius: 2px;
}

.group-count {
  color: #6366f1;
  font-size: 13px;
  font-weight: 700;
  padding: 6px 14px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12), rgba(139, 92, 246, 0.08));
  border-radius: 20px;
  border: 1px solid rgba(99, 102, 241, 0.2);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
  letter-spacing: 0.3px;
}

.sub-group {
  margin-bottom: 36px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.08);
  transition: all 0.3s ease;
}

.sub-group:hover {
  background: rgba(255, 255, 255, 0.7);
  border-color: rgba(99, 102, 241, 0.15);
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.08);
}

.sub-group:last-child {
  margin-bottom: 0;
}

.sub-group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 8px 0 8px 16px;
  border-left: 3px solid;
  border-image: linear-gradient(180deg, #6366f1, #8b5cf6) 1;
  position: relative;
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.05), transparent);
  border-radius: 0 8px 8px 0;
}

.sub-group-header h4 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #475569;
  letter-spacing: 0.2px;
}

.sub-group-count {
  color: #94a3b8;
  font-size: 13px;
  font-weight: 600;
  padding: 3px 10px;
  background: rgba(148, 163, 184, 0.1);
  border-radius: 12px;
}

.url-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

@media (max-width: 1600px) {
  .url-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 1400px) {
  .url-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 18px;
  }
  
  .url-card {
    padding: 20px;
  }
}

@media (max-width: 1200px) {
  .bookmark-view {
    padding: 24px;
  }
  
  .content-wrapper {
    flex-direction: column;
  }
  
  .tag-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 2px solid;
    border-image: linear-gradient(90deg, rgba(99, 102, 241, 0.2), transparent) 1;
    padding-right: 0;
    padding-bottom: 28px;
    margin-bottom: 32px;
    position: static;
    max-height: 400px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.6), transparent);
  }
  
  .url-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 16px;
  }
  
  .title {
    font-size: 24px;
  }
  
  .header-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .bookmark-view {
    padding: 16px;
  }
  
  .main-card {
    border-radius: 20px;
  }
  
  .url-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .header-actions :deep(.el-button) {
    padding: 8px 16px;
    font-size: 13px;
  }
  
  .group-header h3 {
    font-size: 18px;
  }
  
  .sub-group-header h4 {
    font-size: 15px;
  }
}

.url-card {
  display: flex;
  flex-direction: column;
  padding: 24px;
  border: 1.5px solid rgba(148, 163, 184, 0.15);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  overflow: hidden;
  box-shadow: 
    0 4px 16px rgba(15, 23, 42, 0.06),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
}

.url-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #ec4899, #f59e0b);
  background-size: 200% 100%;
  opacity: 0;
  transition: all 0.4s ease;
  animation: gradientFlow 3s linear infinite;
}

@keyframes gradientFlow {
  0% { background-position: 0% 50%; }
  100% { background-position: 200% 50%; }
}

.url-card::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.1) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.url-card:hover {
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 
    0 12px 40px rgba(99, 102, 241, 0.25),
    0 0 0 1px rgba(99, 102, 241, 0.2) inset,
    0 0 60px rgba(99, 102, 241, 0.1);
  transform: translateY(-6px) scale(1.02);
  background: linear-gradient(135deg, rgba(255, 255, 255, 1) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.url-card:hover::before {
  opacity: 1;
}

.url-card:hover::after {
  opacity: 1;
}

.url-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: linear-gradient(135deg, 
    rgba(99, 102, 241, 0.12) 0%, 
    rgba(139, 92, 246, 0.1) 50%,
    rgba(236, 72, 153, 0.08) 100%);
  border: 2px solid;
  border-image: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.15)) 1;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 4px 12px rgba(99, 102, 241, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.url-icon::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, 
    transparent 30%, 
    rgba(255, 255, 255, 0.3) 50%, 
    transparent 70%);
  transform: rotate(45deg);
  transition: all 0.6s ease;
  opacity: 0;
}

.url-card:hover .url-icon {
  background: linear-gradient(135deg, 
    rgba(99, 102, 241, 0.2) 0%, 
    rgba(139, 92, 246, 0.18) 50%,
    rgba(236, 72, 153, 0.15) 100%);
  border-image: linear-gradient(135deg, rgba(99, 102, 241, 0.4), rgba(139, 92, 246, 0.3)) 1;
  transform: scale(1.15) rotate(5deg);
  box-shadow: 
    0 8px 24px rgba(99, 102, 241, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.url-card:hover .url-icon::before {
  opacity: 1;
  transform: rotate(45deg) translate(100%, 100%);
}

.url-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 14px;
  padding: 6px;
  position: relative;
  z-index: 1;
  transition: transform 0.4s ease;
}

.url-card:hover .url-icon img {
  transform: scale(1.1);
}

.url-icon .el-icon {
  font-size: 32px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  z-index: 1;
  transition: transform 0.4s ease;
}

.url-card:hover .url-icon .el-icon {
  transform: scale(1.15);
}

.url-info {
  flex: 1;
  min-width: 0;
}

.url-title {
  font-size: 17px;
  font-weight: 800;
  margin-bottom: 10px;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.5;
  letter-spacing: -0.2px;
  transition: color 0.3s ease;
}

.url-card:hover .url-title {
  color: #6366f1;
}

.url-description {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.7;
  min-height: 44px;
  font-weight: 400;
  letter-spacing: 0.1px;
}

.url-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.url-tag {
  font-size: 12px;
  padding: 6px 14px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  user-select: none;
  letter-spacing: 0.2px;
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;
}

.url-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.url-tag:hover {
  transform: translateY(-2px) scale(1.08);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  border-color: rgba(99, 102, 241, 0.3);
}

.url-tag:hover::before {
  left: 100%;
}

.url-tag :deep(.el-tag__content) {
  position: relative;
  z-index: 1;
}

.url-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.9));
  padding: 6px;
  border-radius: 12px;
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  transform: translateY(-4px) scale(0.9);
}

.url-card:hover .url-actions {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.url-actions .el-button {
  padding: 8px;
  color: #64748b;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.url-actions .el-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.2);
  transform: translate(-50%, -50%);
  transition: width 0.3s ease, height 0.3s ease;
}

.url-actions .el-button:hover {
  color: #6366f1;
  background: rgba(99, 102, 241, 0.1);
  transform: scale(1.15) rotate(5deg);
}

.url-actions .el-button:hover::before {
  width: 100%;
  height: 100%;
}
</style>

