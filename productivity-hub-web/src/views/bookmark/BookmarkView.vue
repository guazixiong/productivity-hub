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
              <div class="tree-node" :class="{ 'is-selected': selectedTagId === data.id }">
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
            <div v-else class="main-content-layout">
              <!-- 左侧：网址卡片网格 -->
              <div class="url-content-area">
                <div v-if="currentGroup" class="group-header">
                  <h3>{{ currentGroup.parentTag.name }}</h3>
                  <span class="group-count">共 {{ getGroupUrlCount(currentGroup) }} 个网址</span>
                </div>
                <div v-if="!currentGroup" class="empty-state">
                  <el-empty description="请从左侧选择一个标签查看网址" />
                </div>
                <div v-else class="url-grid">
                  <div
                    v-for="url in getDisplayUrls()"
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

              <!-- 右侧：二级标签列表 -->
              <div
                class="subtags-sidebar"
                v-if="currentGroup && (currentGroup.subGroups.length > 0 || currentGroup.uncategorizedUrls.length > 0)"
              >
                <div class="subtags-header">二级标签</div>
                <div class="subtags-list">
                  <div
                    v-for="subGroup in currentGroup.subGroups"
                    :key="subGroup.tag.id"
                    class="sub-tag-item"
                    :class="{ 'is-selected': selectedTagId === subGroup.tag.id }"
                    @click="handleSubTagClick(subGroup.tag)"
                  >
                    <span class="sub-tag-name">{{ subGroup.tag.name }}</span>
                    <span class="sub-tag-count">{{ subGroup.urls.length }}</span>
                  </div>
                  <div
                    v-if="currentGroup.uncategorizedUrls.length > 0"
                    class="sub-tag-item uncategorized"
                    :class="{ 'is-selected': selectedSubTagId === 'uncategorized' }"
                    @click="handleUncategorizedClick"
                  >
                    <span class="sub-tag-name">未分类</span>
                    <span class="sub-tag-count">{{ currentGroup.uncategorizedUrls.length }}</span>
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
const selectedSubTagId = ref<string | 'uncategorized' | null>(null)
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

// 获取当前选中的一级标签对应的分组
const currentGroup = computed(() => {
  if (!selectedTagId.value) {
    return null
  }
  
  // 检查选中的是否是一级标签
  const parentTag = tagTree.value.find(t => t.id === selectedTagId.value)
  if (parentTag && parentTag.level === 1) {
    return urlGroups.value.find(g => g.parentTag.id === selectedTagId.value) || null
  }
  
  // 如果是二级标签，找到其父标签对应的分组
  const parentTagForSub = tagTree.value.find(t => 
    t.children?.some(c => c.id === selectedTagId.value)
  )
  if (parentTagForSub) {
    return urlGroups.value.find(g => g.parentTag.id === parentTagForSub.id) || null
  }
  
  return null
})

// 获取要显示的网址列表
const getDisplayUrls = (): BookmarkUrl[] => {
  if (!currentGroup.value) {
    return []
  }
  
  // 如果选中了二级标签，只显示该二级标签的网址
  if (selectedSubTagId.value === 'uncategorized') {
    return currentGroup.value.uncategorizedUrls
  }
  
  if (selectedSubTagId.value) {
    const subGroup = currentGroup.value.subGroups.find(
      sg => sg.tag.id === selectedSubTagId.value
    )
    return subGroup ? subGroup.urls : []
  }
  
  // 如果只选中了一级标签，显示该一级标签下的所有网址
  return getAllUrlsInGroup(currentGroup.value)
}

// 点击标签树节点
const handleTagClick = (tag: BookmarkTag) => {
  // 判断是一级标签还是二级标签
  const isLevel1 = tag.level === 1
  
  if (isLevel1) {
    // 一级标签：选中该标签，清空二级标签选择
    selectedTagId.value = tag.id
    selectedSubTagId.value = null
  } else {
    // 二级标签：找到父标签，并选中该二级标签
    const parentTag = tagTree.value.find(t => 
      t.children?.some(c => c.id === tag.id)
    )
    if (parentTag) {
      selectedTagId.value = parentTag.id
      selectedSubTagId.value = tag.id
    } else {
      selectedTagId.value = tag.id
      selectedSubTagId.value = null
    }
  }
  
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
  // 判断是一级标签还是二级标签
  const isLevel1 = tag.level === 1
  
  if (isLevel1) {
    selectedTagId.value = tag.id
    selectedSubTagId.value = null
  } else {
    // 二级标签：找到父标签
    const parentTag = tagTree.value.find(t => 
      t.children?.some(c => c.id === tag.id)
    )
    if (parentTag) {
      selectedTagId.value = parentTag.id
      selectedSubTagId.value = tag.id
    } else {
      selectedTagId.value = tag.id
      selectedSubTagId.value = null
    }
  }
  
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
  selectedSubTagId.value = null
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

// 获取某个一级标签下的所有网址（用于左侧内容区域）
const getAllUrlsInGroup = (group: BookmarkGroup) => {
  const urls: BookmarkUrl[] = []
  group.subGroups.forEach((subGroup) => {
    urls.push(...subGroup.urls)
  })
  urls.push(...group.uncategorizedUrls)
  return urls
}

// 点击右侧二级标签条目
const handleSubTagClick = (tag: BookmarkTag) => {
  if (currentGroup.value) {
    selectedSubTagId.value = tag.id
    // 清空搜索关键词
    searchKeyword.value = ''
    searchResults.value = []
    // 同步标签树选中状态
    if (tagTreeRef.value) {
      tagTreeRef.value.setCurrentKey(tag.id)
    }
  }
}

// 点击未分类
const handleUncategorizedClick = () => {
  if (currentGroup.value) {
    selectedSubTagId.value = 'uncategorized'
    // 清空搜索关键词
    searchKeyword.value = ''
    searchResults.value = []
  }
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
  max-width: 1200px;
  margin: 0 auto;
  min-height: 520px;
  border-radius: 20px;
  border: 1px solid var(--ph-border-subtle);
  box-shadow: var(--surface-shadow);
  background: var(--surface-color);
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
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(99, 102, 241, 0.25) 20%,
    rgba(139, 92, 246, 0.25) 50%,
    rgba(236, 72, 153, 0.25) 80%,
    transparent 100%
  );
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
  border-radius: 999px;
  font-weight: 500;
  padding: 8px 18px;
  transition: all 0.2s ease;
  box-shadow: none;
}

.header-actions :deep(.el-button--primary) {
  background: var(--primary-color);
  border: none;
}

.header-actions :deep(.el-button--primary:hover) {
  transform: translateY(-1px);
}

.header-actions :deep(.el-button:not(.el-button--primary)) {
  background: #f9fafb;
  border: 1px solid rgba(148, 163, 184, 0.4);
}

.header-actions :deep(.el-button:not(.el-button--primary):hover) {
  background: #ffffff;
  border-color: rgba(37, 99, 235, 0.6);
  transform: translateY(-1px);
}

.content-wrapper {
  display: flex;
  gap: 24px;
  min-height: 480px;
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

.main-content-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.url-content-area {
  flex: 1;
  min-width: 0;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid;
  border-image: linear-gradient(90deg, rgba(99, 102, 241, 0.2), transparent) 1;
}

.group-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
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

.subtags-sidebar {
  width: 220px;
  flex-shrink: 0;
  border-left: 1px solid rgba(148, 163, 184, 0.2);
  padding-left: 24px;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.subtags-sidebar::-webkit-scrollbar {
  width: 6px;
}

.subtags-sidebar::-webkit-scrollbar-track {
  background: rgba(241, 245, 249, 0.5);
  border-radius: 3px;
}

.subtags-sidebar::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  border-radius: 3px;
}

.subtags-header {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid;
  border-image: linear-gradient(90deg, rgba(99, 102, 241, 0.2), transparent) 1;
}

.subtags-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sub-tag-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid rgba(226, 232, 240, 0.9);
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sub-tag-item:hover {
  background: #f9fafb;
  border-color: rgba(148, 163, 184, 0.9);
  transform: translateX(2px);
}

.sub-tag-item.is-selected {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.14), rgba(139, 92, 246, 0.12));
  border-color: rgba(99, 102, 241, 0.6);
  color: #1e293b;
}

.sub-tag-item.uncategorized {
  opacity: 0.8;
}

.sub-tag-name {
  font-weight: 600;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sub-tag-count {
  font-weight: 600;
  color: #6366f1;
}

.url-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

@media (max-width: 1600px) {
  .url-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 14px;
  }
}

@media (max-width: 1400px) {
  .url-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 12px;
  }
  
  .url-card {
    padding: 16px;
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
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
  }
  
  .title {
    font-size: 24px;
  }
  
  .header-actions {
    flex-wrap: wrap;
  }

  .main-content-layout {
    flex-direction: column;
  }

  .subtags-sidebar {
    width: 100%;
    border-left: none;
    border-top: 1px solid rgba(148, 163, 184, 0.2);
    padding-left: 0;
    padding-top: 20px;
    margin-top: 24px;
    position: static;
    max-height: none;
  }
}

@media (max-width: 768px) {
  .bookmark-view {
    padding: 16px;
  }
  
  .main-card {
    border-radius: 18px;
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
  flex-direction: row;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 16px 14px;
  border: 1.5px solid rgba(148, 163, 184, 0.15);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  overflow: hidden;
  box-shadow: 
    0 3px 12px rgba(15, 23, 42, 0.05),
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
    0 10px 32px rgba(99, 102, 241, 0.2),
    0 0 0 1px rgba(99, 102, 241, 0.2) inset,
    0 0 60px rgba(99, 102, 241, 0.1);
  transform: translateY(-4px) scale(1.01);
  background: linear-gradient(135deg, rgba(255, 255, 255, 1) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.url-card:hover::before {
  opacity: 1;
}

.url-card:hover::after {
  opacity: 1;
}

.url-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, 
    rgba(99, 102, 241, 0.12) 0%, 
    rgba(139, 92, 246, 0.1) 50%,
    rgba(236, 72, 153, 0.08) 100%);
  border: 1.5px solid;
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
  width: auto;
  height: 100%;
  max-width: 100%;
  object-fit: contain;
  border-radius: 14px;
  padding: 5px 6px;
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
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.url-title {
  font-size: 15px;
  font-weight: 700;
  margin: 0;
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
  font-size: 13px;
  color: #475569;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  line-height: 1.6;
  min-height: 0;
  font-weight: 500;
  letter-spacing: 0;
}

.url-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}

.url-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 14px;
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
  top: 10px;
  right: 10px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.9));
  padding: 4px 6px;
  border-radius: 10px;
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
  padding: 6px;
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

