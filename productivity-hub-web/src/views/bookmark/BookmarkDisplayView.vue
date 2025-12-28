<template>
  <div class="bookmark-display-view">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="title">宝藏类网址</span>
          <div class="header-actions">
            <el-button type="primary" @click="goToManage">
              <el-icon><Setting /></el-icon>
              数据维护
            </el-button>
          </div>
        </div>
      </template>

      <!-- 顶部：大标签 -->
      <div class="top-tags">
        <div
          v-for="parentTag in parentTags"
          :key="parentTag.id"
          class="top-tag-item"
          :class="{ 'is-active': selectedParentTagId === parentTag.id }"
          @click="handleParentTagClick(parentTag)"
        >
          <span class="tag-name">{{ parentTag.name }}</span>
          <span class="tag-count">({{ parentTag.urlCount }})</span>
        </div>
      </div>

      <div class="content-wrapper">
        <!-- 左侧：小标签 -->
        <div class="child-tags-sidebar">
          <div
            v-for="childTag in childTags"
            :key="childTag.id"
            class="child-tag-item"
            :class="{ 'is-active': selectedChildTagId === childTag.id }"
            @click="handleChildTagClick(childTag)"
          >
            <span class="tag-name">{{ childTag.name }}</span>
            <span class="tag-count">({{ childTag.urlCount }})</span>
          </div>
          <div v-if="childTags.length === 0" class="empty-child-tags">
            <el-empty description="该分类下暂无子标签" :image-size="80" />
          </div>
        </div>

        <!-- 右侧：网址展示区 -->
        <div class="url-content">
          <div v-if="currentUrls.length === 0" class="empty-state">
            <el-empty description="该标签下暂无网址" />
          </div>
          <div v-else class="url-grid">
            <div
              v-for="url in currentUrls"
              :key="url.id"
              class="url-card"
              @click="handleUrlClick(url.url)"
            >
              <div class="url-card__glow" />
              <div class="url-card__body">
                <div class="url-icon">
                  <img v-if="url.iconUrl" :src="url.iconUrl" :alt="url.title" />
                  <el-icon v-else><Link /></el-icon>
                </div>
                <div class="url-info">
                  <div class="url-title-row">
                    <div class="url-title">{{ url.title }}</div>
                    <div class="url-badges">
                      <el-tag size="small" effect="plain" class="url-domain">
                        {{ getDomain(url.url) }}
                      </el-tag>
                    </div>
                  </div>
                  <div class="url-description">
                    {{ url.description || '暂无描述' }}
                  </div>
                  <div v-if="url.tags && url.tags.length" class="url-tags">
                    <el-tag
                      v-for="tag in url.tags"
                      :key="tag.id"
                      size="small"
                      effect="plain"
                      class="url-tag"
                    >
                      {{ tag.name }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Link, Setting } from '@element-plus/icons-vue'
import { bookmarkApi } from '@/services/bookmarkApi'
import type {
  BookmarkTag,
  BookmarkUrl,
} from '@/types/bookmark'

const router = useRouter()

// 跳转到维护界面
const goToManage = () => {
  router.push('/bookmark/manage')
}

const parentTags = ref<BookmarkTag[]>([])
const childTags = ref<BookmarkTag[]>([])
const urlsByTag = ref<Record<string, BookmarkUrl[]>>({})
const selectedParentTagId = ref<string | null>(null)
const selectedChildTagId = ref<string | null>(null)

// 当前显示的网址列表
const currentUrls = computed(() => {
  if (!selectedChildTagId.value) {
    return []
  }
  return urlsByTag.value[selectedChildTagId.value] || []
})

// 加载一级标签
const loadParentTags = async () => {
  try {
    parentTags.value = await bookmarkApi.getParentTags()
    // 默认选中第一个大标签
    if (parentTags.value.length > 0 && !selectedParentTagId.value) {
      selectedParentTagId.value = parentTags.value[0].id
      await loadChildTags(parentTags.value[0].id, true)
    }
  } catch (error) {
    ElMessage.error('加载标签失败')
  }
}

// 加载子标签
const loadChildTags = async (parentId: string, autoSelectFirst: boolean = true) => {
  try {
    const newChildTags = await bookmarkApi.getChildTags(parentId)
    // 清空当前一级标签下的旧数据，避免显示混乱
    const childTagIds = new Set(newChildTags.map(tag => tag.id))
    Object.keys(urlsByTag.value).forEach(tagId => {
      if (!childTagIds.has(tagId)) {
        delete urlsByTag.value[tagId]
      }
    })
    childTags.value = newChildTags
    // 等待响应式更新
    await nextTick()
    // 加载所有子标签的网址
    for (const childTag of childTags.value) {
      if (!urlsByTag.value[childTag.id]) {
        await loadUrlsByTag(childTag.id)
      }
    }
    // 如果启用自动选择，且当前没有选中的子标签，则选中第一个
    if (autoSelectFirst && childTags.value.length > 0 && !selectedChildTagId.value) {
      // 确保第一个子标签的网址已加载
      const firstChildTag = childTags.value[0]
      if (!urlsByTag.value[firstChildTag.id]) {
        await loadUrlsByTag(firstChildTag.id)
      }
      // 等待网址加载完成后再设置选中状态
      await nextTick()
      selectedChildTagId.value = firstChildTag.id
    }
  } catch (error) {
    ElMessage.error('加载子标签失败')
  }
}

// 根据标签ID加载网址
const loadUrlsByTag = async (tagId: string) => {
  try {
    const urls = await bookmarkApi.getUrlsByTagId(tagId)
    urlsByTag.value[tagId] = urls
  } catch (error) {
    ElMessage.error('加载网址失败')
  }
}

// 点击大标签
const handleParentTagClick = async (parentTag: BookmarkTag) => {
  // 如果点击的是当前已选中的标签，则不处理
  if (selectedParentTagId.value === parentTag.id) {
    return
  }
  selectedParentTagId.value = parentTag.id
  selectedChildTagId.value = null
  // 等待响应式更新
  await nextTick()
  await loadChildTags(parentTag.id, true)
}

// 点击小标签
const handleChildTagClick = async (childTag: BookmarkTag) => {
  selectedChildTagId.value = childTag.id
  // 如果该标签的网址还未加载，则加载
  if (!urlsByTag.value[childTag.id]) {
    await loadUrlsByTag(childTag.id)
  }
}

// 点击网址
const handleUrlClick = (url: string) => {
  window.open(url, '_blank')
}

const getDomain = (rawUrl: string) => {
  try {
    const { hostname } = new URL(rawUrl)
    return hostname.replace(/^www\./, '')
  } catch {
    return rawUrl
  }
}

onMounted(() => {
  loadParentTags()
})
</script>

<style scoped>
.bookmark-display-view {
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

/* 顶部大标签 */
.top-tags {
  display: flex;
  gap: 12px;
  padding: 20px 0;
  margin-bottom: 24px;
  border-bottom: 2px solid;
  border-image: linear-gradient(90deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.1), transparent) 1;
  overflow-x: auto;
  position: relative;
}

.top-tags::-webkit-scrollbar {
  height: 6px;
}

.top-tags::-webkit-scrollbar-track {
  background: rgba(241, 245, 249, 0.5);
  border-radius: 3px;
}

.top-tags::-webkit-scrollbar-thumb {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  border-radius: 3px;
}

.top-tag-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.6);
  border: 2px solid transparent;
  white-space: nowrap;
  font-weight: 600;
  color: #475569;
  position: relative;
  overflow: hidden;
}

.top-tag-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.top-tag-item:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.08));
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.top-tag-item:hover::before {
  left: 100%;
}

.top-tag-item.is-active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.15));
  border-color: rgba(99, 102, 241, 0.4);
  color: #6366f1;
  font-weight: 700;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.2);
}

.top-tag-item.is-active .tag-name {
  color: #6366f1;
}

.top-tag-item .tag-name {
  font-size: 16px;
  transition: color 0.3s ease;
}

.top-tag-item .tag-count {
  font-size: 13px;
  padding: 3px 10px;
  background: rgba(148, 163, 184, 0.15);
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.top-tag-item.is-active .tag-count {
  background: rgba(99, 102, 241, 0.2);
  color: #6366f1;
}

.content-wrapper {
  display: flex;
  gap: 24px;
  min-height: 600px;
}

/* 左侧小标签 */
.child-tags-sidebar {
  width: 240px;
  border-right: 1px solid rgba(148, 163, 184, 0.15);
  padding-right: 24px;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.5) 0%, transparent 100%);
}

.child-tags-sidebar::-webkit-scrollbar {
  width: 6px;
}

.child-tags-sidebar::-webkit-scrollbar-track {
  background: rgba(241, 245, 249, 0.5);
  border-radius: 3px;
}

.child-tags-sidebar::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  border-radius: 3px;
}

.child-tag-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  margin-bottom: 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.5);
  border: 2px solid transparent;
  position: relative;
}

.child-tag-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleX(0);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  border-radius: 0 2px 2px 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.child-tag-item:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08), rgba(139, 92, 246, 0.05));
  border-color: rgba(99, 102, 241, 0.2);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
}

.child-tag-item:hover::before {
  transform: translateY(-50%) scaleX(1);
  height: 60%;
}

.child-tag-item.is-active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.12));
  border-color: rgba(99, 102, 241, 0.3);
  font-weight: 600;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.15);
  transform: translateX(4px);
}

.child-tag-item.is-active::before {
  transform: translateY(-50%) scaleX(1);
  height: 80%;
  width: 4px;
}

.child-tag-item .tag-name {
  flex: 1;
  color: #1e293b;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.child-tag-item.is-active .tag-name {
  color: #6366f1;
  font-weight: 700;
}

.child-tag-item .tag-count {
  color: #94a3b8;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 8px;
  background: rgba(148, 163, 184, 0.1);
  border-radius: 10px;
  transition: all 0.3s ease;
}

.child-tag-item.is-active .tag-count {
  background: rgba(99, 102, 241, 0.15);
  color: #6366f1;
}

.empty-child-tags {
  padding: 40px 20px;
  text-align: center;
}

/* 右侧网址区域 */
.url-content {
  flex: 1;
  min-width: 0;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.02), rgba(139, 92, 246, 0.02));
  border-radius: 20px;
  border: 2px dashed rgba(99, 102, 241, 0.2);
}

.empty-state :deep(.el-empty) {
  padding: 40px 20px;
}

.empty-state :deep(.el-empty__description) {
  color: #64748b;
  font-size: 15px;
  font-weight: 500;
}

.url-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}

.url-card {
  position: relative;
  padding: 14px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.9));
  box-shadow:
    0 4px 14px rgba(15, 23, 42, 0.06),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.url-card__glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 10% 20%, rgba(99, 102, 241, 0.08), transparent 45%),
    radial-gradient(circle at 90% 80%, rgba(236, 72, 153, 0.08), transparent 40%);
  opacity: 0;
  transition: opacity 0.35s ease;
}

.url-card__body {
  position: relative;
  display: flex;
  gap: 14px;
  align-items: flex-start;
  z-index: 1;
}

.url-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(99, 102, 241, 0.08), rgba(255, 255, 255, 0));
  opacity: 0;
  transition: opacity 0.35s ease;
}

.url-card:hover {
  transform: translateY(-6px);
  border-color: rgba(99, 102, 241, 0.25);
  box-shadow:
    0 14px 38px rgba(99, 102, 241, 0.18),
    0 0 0 1px rgba(99, 102, 241, 0.18) inset;
  background: linear-gradient(145deg, rgba(255, 255, 255, 1), rgba(255, 255, 255, 0.96));
}

.url-card:hover::before {
  opacity: 1;
}

.url-card:hover .url-card__glow {
  opacity: 1;
}

.url-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(140deg,
    rgba(99, 102, 241, 0.14) 0%,
    rgba(139, 92, 246, 0.12) 50%,
    rgba(236, 72, 153, 0.1) 100%);
  border: 1.5px solid;
  border-image: linear-gradient(135deg, rgba(99, 102, 241, 0.22), rgba(139, 92, 246, 0.18)) 1;
  box-shadow:
    0 4px 12px rgba(99, 102, 241, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  transition: all 0.35s ease;
}

.url-card:hover .url-icon {
  transform: scale(1.08) rotate(2deg);
  border-image: linear-gradient(135deg, rgba(99, 102, 241, 0.4), rgba(139, 92, 246, 0.35)) 1;
}

.url-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 6px;
  border-radius: 12px;
  position: relative;
  z-index: 1;
  transition: transform 0.35s ease;
}

.url-card:hover .url-icon img {
  transform: scale(1.05);
}

.url-icon .el-icon {
  font-size: 30px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.url-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.url-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: space-between;
}

.url-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.2px;
  transition: color 0.3s ease;
}

.url-card:hover .url-title {
  color: #6366f1;
}

.url-badges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.url-domain {
  border-radius: 999px;
  border-color: rgba(99, 102, 241, 0.12);
  color: #475569;
  background: rgba(99, 102, 241, 0.06);
}

.url-description {
  font-size: 13px;
  color: #475569;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
  min-height: 40px;
}

.url-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.url-tag {
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 14px;
  border-color: rgba(99, 102, 241, 0.18);
  background: rgba(99, 102, 241, 0.06);
  color: #4f46e5;
}

.url-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.18);
}

@media (max-width: 1200px) {
  .bookmark-display-view {
    padding: 24px;
  }
  
  .content-wrapper {
    flex-direction: column;
  }
  
  .child-tags-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 2px solid;
    border-image: linear-gradient(90deg, rgba(99, 102, 241, 0.2), transparent) 1;
    padding-right: 0;
    padding-bottom: 24px;
    margin-bottom: 24px;
    position: static;
    max-height: 300px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .child-tag-item {
    flex: 0 0 auto;
    margin-bottom: 0;
  }
  
  .url-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 18px;
  }
  
  .title {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .bookmark-display-view {
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
  
  .top-tag-item {
    padding: 10px 18px;
    font-size: 14px;
  }
}
</style>

