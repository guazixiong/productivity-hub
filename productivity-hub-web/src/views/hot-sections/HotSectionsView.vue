<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts } from '@element-plus/icons-vue'
import { scheduleApi } from '@/services/api'
import type { HotSection } from '@/types/hotSections'

// 固定的热点标签列表
const HOT_SECTION_NAMES = [
  '综合热榜',
  '知乎热搜',
  '微博热搜',
  '虎扑热帖',
  '小红书热帖',
  '哔哩哔哩热榜',
  '抖音热榜',
  '百度贴吧热帖'
]

// 每个标签页的数据状态
interface SectionData {
  items: HotSection['items']
  limit: number
  hasMore: boolean
  loading: boolean
}

const DEFAULT_LIMIT = 10
const LIMIT_STEP = 10
const sectionDataMap = ref<Map<string, SectionData>>(new Map())
const hotSectionsLoading = ref(false)
const activeTab = ref<string>('')
const loadingMore = ref(false)

// 滚动容器引用（使用Map存储每个标签页的容器）
const scrollContainerRefs = ref<Map<string, HTMLElement>>(new Map())
const scrollThreshold = 200 // 距离底部200px时触发加载
let scrollTimer: number | null = null // 滚动节流定时器

// 获取当前激活标签页的滚动容器
const getCurrentScrollContainer = () => {
  if (!activeTab.value) return null
  return scrollContainerRefs.value.get(activeTab.value) || null
}

// 初始化所有标签页的数据状态
const initSectionData = () => {
  HOT_SECTION_NAMES.forEach(name => {
    sectionDataMap.value.set(name, {
      items: [],
      limit: DEFAULT_LIMIT,
      hasMore: true,
      loading: false
    })
  })
}

// 获取指定标签页的数据
const getSectionData = (sectionName: string) => {
  return sectionDataMap.value.get(sectionName)
}

// 加载指定标签的热点数据
const loadHotSection = async (sectionName: string, limit?: number, append = false) => {
  const sectionData = sectionDataMap.value.get(sectionName)
  if (!sectionData) return
  
  if (sectionData.loading) return
  
  const targetLimit = limit ?? sectionData.limit
  sectionData.loading = true
  if (append) {
    loadingMore.value = true
  }
  
  try {
    const section = await scheduleApi.getHotSection(sectionName, targetLimit)
    
    if (append) {
      // 追加模式：后端返回的是前N条数据，需要只取新增的部分
      const currentCount = sectionData.items.length
      
      // 如果返回的数据量没有超过当前数量，说明没有更多数据了
      if (section.items.length <= currentCount) {
        sectionData.hasMore = false
      } else {
        // 只取超出当前数量的部分（即新增的数据）
        const newItems = section.items.slice(currentCount)
        
        // 进一步去重，防止重复数据（基于标题和链接）
        const existingKeys = new Set(
          sectionData.items.map(item => `${item.title}::${item.link}`)
        )
        const uniqueNewItems = newItems.filter(
          item => !existingKeys.has(`${item.title}::${item.link}`)
        )
        
        // 追加新数据到现有列表
        if (uniqueNewItems.length > 0) {
          sectionData.items.push(...uniqueNewItems)
        }
        
        // 如果返回的数据量少于请求的limit，或者没有新数据，说明没有更多了
        if (section.items.length < targetLimit || uniqueNewItems.length === 0) {
          sectionData.hasMore = false
        } else {
          sectionData.hasMore = true
        }
      }
    } else {
      // 初始加载或刷新
      sectionData.items = section.items
      
      // 判断是否还有更多数据
      // 如果返回的数据量少于请求的limit，说明没有更多了
      if (section.items.length < targetLimit) {
        sectionData.hasMore = false
      } else {
        sectionData.hasMore = true
      }
    }
    
    sectionData.limit = targetLimit
  } catch (error) {
    ElMessage.error((error as Error)?.message ?? `加载${sectionName}失败`)
    sectionData.hasMore = false
  } finally {
    sectionData.loading = false
    loadingMore.value = false
  }
}

// 加载所有标签页的初始数据
const loadAllHotSections = async () => {
  hotSectionsLoading.value = true
  try {
    // 并行加载所有标签页的初始数据
    const promises = HOT_SECTION_NAMES.map(name => loadHotSection(name, DEFAULT_LIMIT, false))
    await Promise.all(promises)
    
    // 设置默认激活的标签页
    if (!activeTab.value && HOT_SECTION_NAMES.length > 0) {
      activeTab.value = HOT_SECTION_NAMES[0]
    }
  } catch (error) {
    ElMessage.error((error as Error)?.message ?? '热点数据加载失败')
  } finally {
    hotSectionsLoading.value = false
  }
}

// 标签页切换处理
const handleTabChange = (name: string) => {
  // 滚动到顶部
  const oldContainer = getCurrentScrollContainer()
  if (oldContainer) {
    oldContainer.scrollTop = 0
  }
  
  activeTab.value = name
  
  // 如果当前标签页还没有数据，加载初始数据
  const sectionData = sectionDataMap.value.get(name)
  if (sectionData && sectionData.items.length === 0 && !sectionData.loading) {
    loadHotSection(name, DEFAULT_LIMIT, false)
  }
  
  // 等待DOM更新后滚动到顶部
  nextTick(() => {
    const newContainer = getCurrentScrollContainer()
    if (newContainer) {
      newContainer.scrollTop = 0
    }
  })
}

// 加载更多数据
const loadMore = async () => {
  if (!activeTab.value) return
  
  const sectionData = sectionDataMap.value.get(activeTab.value)
  if (!sectionData || loadingMore.value || !sectionData.hasMore || sectionData.loading) return
  
  const nextLimit = sectionData.limit + LIMIT_STEP
  await loadHotSection(activeTab.value, nextLimit, true)
}

// 滚动处理（使用节流优化性能）
const handleScroll = (event: Event) => {
  if (!activeTab.value) return
  
  const container = event.target as HTMLElement
  if (!container) return
  
  // 节流处理，避免频繁触发
  if (scrollTimer) {
    clearTimeout(scrollTimer)
  }
  
  scrollTimer = window.setTimeout(() => {
    const sectionData = sectionDataMap.value.get(activeTab.value!)
    if (!sectionData || loadingMore.value || !sectionData.hasMore || sectionData.loading) return
    
    const scrollTop = container.scrollTop
    const scrollHeight = container.scrollHeight
    const clientHeight = container.clientHeight
    
    // 距离底部小于阈值时加载更多
    const distanceToBottom = scrollHeight - scrollTop - clientHeight
    if (distanceToBottom < scrollThreshold) {
      loadMore()
    }
  }, 100) // 100ms节流
}

// 设置滚动容器引用
const setScrollContainerRef = (el: HTMLElement | null, sectionName: string) => {
  if (el) {
    scrollContainerRefs.value.set(sectionName, el)
  } else {
    scrollContainerRefs.value.delete(sectionName)
  }
}


// 初始化
onMounted(async () => {
  initSectionData()
  await loadAllHotSections()
})

onUnmounted(() => {
  // 清理定时器
  if (scrollTimer) {
    clearTimeout(scrollTimer)
    scrollTimer = null
  }
})
</script>

<template>
  <div class="hot-sections-container">
    <div class="page-header">
      <div class="header-content">
        <el-icon class="header-icon"><TrendCharts /></el-icon>
        <h1 class="page-title">热点速览</h1>
      </div>
    </div>

    <div class="content-wrapper">
      <el-card class="hot-sections-card" shadow="hover">
        <el-skeleton :loading="hotSectionsLoading" :rows="5" animated>
          <template #default>
            <el-tabs
              v-if="HOT_SECTION_NAMES.length > 0"
              v-model="activeTab"
              type="card"
              class="hot-tabs"
              @tab-change="handleTabChange"
            >
              <el-tab-pane
                v-for="sectionName in HOT_SECTION_NAMES"
                :key="sectionName"
                :name="sectionName"
              >
                <template #label>
                  <span class="tab-label">
                    <el-tag
                      effect="plain"
                      size="small"
                      class="tab-tag"
                    >
                      {{ sectionName }}
                    </el-tag>
                  </span>
                </template>
                <div
                  v-if="activeTab === sectionName"
                  :ref="(el) => setScrollContainerRef(el as HTMLElement | null, sectionName)"
                  class="hot-items-container"
                  @scroll="handleScroll"
                >
                  <div class="hot-items-list">
                    <a
                      v-for="(item, index) in getSectionData(sectionName)?.items || []"
                      :key="index"
                      :href="item.link"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="hot-item"
                    >
                      <div class="hot-item-content">
                        <div class="hot-item-title">
                          <span class="hot-item-index">{{ index + 1 }}</span>
                          <span class="hot-item-text">{{ item.title }}</span>
                        </div>
                        <div v-if="item.heat" class="hot-item-heat">{{ item.heat }}</div>
                      </div>
                      <div v-if="item.desc" class="hot-item-desc">{{ item.desc }}</div>
                    </a>
                  </div>
                  
                  <!-- 加载更多提示 -->
                  <template v-if="getSectionData(sectionName)">
                    <div v-if="loadingMore || getSectionData(sectionName)?.loading" class="loading-more">
                      <el-icon class="is-loading"><TrendCharts /></el-icon>
                      <span>加载中...</span>
                    </div>
                    <div v-else-if="!getSectionData(sectionName)?.hasMore && (getSectionData(sectionName)?.items.length || 0) > 0" class="no-more">
                      <span>没有更多数据了</span>
                    </div>
                    <div v-else-if="(getSectionData(sectionName)?.items.length || 0) === 0 && !getSectionData(sectionName)?.loading" class="hot-sections-empty">
                      <el-icon><TrendCharts /></el-icon>
                      <span>暂无热点数据</span>
                    </div>
                  </template>
                </div>
              </el-tab-pane>
            </el-tabs>
            <div v-else class="hot-sections-empty">
              <el-icon><TrendCharts /></el-icon>
              <span>暂无热点标签</span>
            </div>
          </template>
        </el-skeleton>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.hot-sections-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 60px);
}

.page-header {
  padding: 24px 32px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 28px;
  color: #6366f1;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
}

.content-wrapper {
  flex: 1;
  padding: 24px 32px;
  overflow: auto;
}

.hot-sections-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.hot-sections-card :deep(.el-card__body) {
  padding: 24px;
}

.hot-tabs {
  width: 100%;
}

.hot-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
  border-bottom: 2px solid rgba(148, 163, 184, 0.15);
}

.hot-tabs :deep(.el-tabs__item) {
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
  font-size: 14px;
  color: #64748b;
  border: none;
  transition: all 0.3s ease;
  position: relative;
}

.hot-tabs :deep(.el-tabs__item:hover) {
  color: #475569;
}

.hot-tabs :deep(.el-tabs__item.is-active) {
  color: #1e293b;
  font-weight: 600;
}

.hot-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 2px 2px 0 0;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  transition: all 0.2s ease;
  color: #64748b;
  background-color: #f1f5f9;
}

.tab-tag :deep(.el-tag__content) {
  color: inherit;
}

.hot-tabs :deep(.el-tabs__item.is-active .tab-tag) {
  background-color: #6366f1;
  color: #ffffff !important;
  border-color: #6366f1;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
}

.hot-tabs :deep(.el-tabs__item.is-active .tab-tag .el-tag__content) {
  color: #ffffff !important;
}

.hot-items-container {
  height: calc(100vh - 300px);
  min-height: 400px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
}

.hot-items-container::-webkit-scrollbar {
  width: 6px;
}

.hot-items-container::-webkit-scrollbar-track {
  background: rgba(241, 245, 249, 0.5);
  border-radius: 3px;
}

.hot-items-container::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 3px;
}

.hot-items-container::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.5);
}

.hot-items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  color: inherit;
  transition: all 0.2s ease;
  cursor: pointer;
}

.hot-item:hover {
  background: rgba(99, 102, 241, 0.08);
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.hot-item-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.hot-item-title {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.hot-item-index {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.hot-item-text {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: #1e293b;
  line-height: 1.5;
  word-break: break-word;
}

.hot-item-heat {
  flex-shrink: 0;
  padding: 4px 12px;
  border-radius: 12px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.hot-item-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin-top: 4px;
  padding-left: 36px;
}

.loading-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: #94a3b8;
  font-size: 14px;
}

.loading-more .el-icon {
  font-size: 16px;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.hot-sections-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #94a3b8;
  gap: 16px;
}

.hot-sections-empty .el-icon {
  font-size: 48px;
  color: #cbd5e1;
}

.hot-sections-empty span {
  font-size: 16px;
  color: #94a3b8;
}
</style>

