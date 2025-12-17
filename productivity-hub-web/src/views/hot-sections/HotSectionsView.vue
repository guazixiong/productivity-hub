<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { scheduleApi } from '@/services/api'
import type { HotSection } from '@/types/hotSections'

// 顶级标签页
const MAIN_TABS = ['生活', '技术'] as const
type MainTab = typeof MAIN_TABS[number]

// 生活类热点标签列表
const LIFE_HOT_SECTION_NAMES = [
  '综合热榜',
  '知乎热搜',
  '微博热搜',
  '虎扑热帖',
  '小红书热帖',
  '哔哩哔哩热榜',
  '抖音热榜',
  '百度贴吧热帖'
]

// 技术类热点标签列表（预留，后续扩展）
const TECH_HOT_SECTION_NAMES: string[] = [
  // 后续添加技术类热点标签
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
const mainActiveTab = ref<MainTab>('生活')
const activeTab = ref<string>('')
const loadingMore = ref(false)

// 滚动容器引用（使用Map存储每个标签页的容器）
const scrollContainerRefs = ref<Map<string, HTMLElement>>(new Map())
const scrollThreshold = 200 // 距离底部200px时触发加载
let scrollTimer: number | null = null // 滚动节流定时器

// 根据主标签页获取对应的热点标签列表
const getCurrentHotSectionNames = computed(() => {
  if (mainActiveTab.value === '生活') {
    return LIFE_HOT_SECTION_NAMES
  } else if (mainActiveTab.value === '技术') {
    return TECH_HOT_SECTION_NAMES
  }
  return []
})

// 获取当前激活标签页的滚动容器
const getCurrentScrollContainer = () => {
  if (!activeTab.value) return null
  return scrollContainerRefs.value.get(activeTab.value) || null
}

// 初始化所有标签页的数据状态
const initSectionData = () => {
  // 初始化生活类标签数据
  LIFE_HOT_SECTION_NAMES.forEach(name => {
    sectionDataMap.value.set(name, {
      items: [],
      limit: DEFAULT_LIMIT,
      hasMore: true,
      loading: false
    })
  })
  
  // 初始化技术类标签数据
  TECH_HOT_SECTION_NAMES.forEach(name => {
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

// 加载当前主标签页的所有子标签数据
const loadAllHotSections = async () => {
  const currentSections = getCurrentHotSectionNames.value
  if (currentSections.length === 0) {
    return
  }
  
  hotSectionsLoading.value = true
  try {
    // 并行加载所有标签页的初始数据
    const promises = currentSections.map(name => loadHotSection(name, DEFAULT_LIMIT, false))
    await Promise.all(promises)
    
    // 设置默认激活的子标签页
    if (!activeTab.value && currentSections.length > 0) {
      activeTab.value = currentSections[0]
    }
  } catch (error) {
    ElMessage.error((error as Error)?.message ?? '热点数据加载失败')
  } finally {
    hotSectionsLoading.value = false
  }
}

// 主标签页切换处理
const handleMainTabChange = async (name: MainTab) => {
  mainActiveTab.value = name
  
  const currentSections = getCurrentHotSectionNames.value
  if (currentSections.length === 0) {
    activeTab.value = ''
    return
  }
  
  // 设置当前主标签页的第一个子标签为激活状态
  activeTab.value = currentSections[0]
  
  // 如果当前主标签页下的数据未加载，则加载
  const needLoad = currentSections.some(name => {
    const sectionData = sectionDataMap.value.get(name)
    return !sectionData || sectionData.items.length === 0
  })
  
  if (needLoad) {
    await loadAllHotSections()
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
    <div class="content-wrapper">
      <el-card class="hot-sections-card" shadow="hover">
        <!-- 主标签页：生活 / 技术 -->
        <el-tabs
          v-model="mainActiveTab"
          type="border-card"
          class="main-tabs"
          @tab-change="handleMainTabChange"
        >
          <el-tab-pane
            v-for="mainTab in MAIN_TABS"
            :key="mainTab"
            :name="mainTab"
          >
            <template #label>
              <span class="main-tab-label">{{ mainTab }}</span>
            </template>
            
            <!-- 子标签页内容 -->
            <el-skeleton :loading="hotSectionsLoading" :rows="5" animated>
              <template #default>
                <!-- 生活/技术分类下的子标签 -->
                <el-tabs
                  v-if="getCurrentHotSectionNames.length > 0"
                  v-model="activeTab"
                  type="card"
                  tab-position="left"
                  class="hot-tabs"
                  @tab-change="handleTabChange"
                >
                  <el-tab-pane
                    v-for="sectionName in getCurrentHotSectionNames"
                    :key="sectionName"
                    :name="sectionName"
                  >
                    <template #label>
                      <div class="sub-tab-label" :title="sectionName">
                        <span class="sub-tab-dot"></span>
                        <span class="sub-tab-text">
                          {{ sectionName }}
                        </span>
                      </div>
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
                <!-- 技术标签为空时的占位提示 -->
                <div v-else class="hot-sections-empty">
                  <el-icon><TrendCharts /></el-icon>
                  <span>{{ mainActiveTab === '技术' ? '技术类热点标签即将上线，敬请期待...' : '暂无热点标签' }}</span>
                </div>
              </template>
            </el-skeleton>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.hot-sections-container {
  display: flex;
  flex-direction: column;
  padding: 0;
}

.content-wrapper {
  flex: 1;
  padding: 0;
}

.hot-sections-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 20px;
  border: 1px solid var(--ph-border-subtle);
  box-shadow: var(--surface-shadow);
  background: var(--surface-color);
  position: relative;
  overflow: hidden;
}

.hot-sections-card :deep(.el-card__body) {
  padding: 0;
}

/* 主标签页样式 */
.main-tabs {
  width: 100%;
  border: none;
  box-shadow: none;
}

.main-tabs :deep(.el-tabs__header) {
  margin: 0;
  border: none;
  background: rgba(248, 250, 252, 0.95);
  padding: 12px 18px 0;
}

.main-tabs :deep(.el-tabs__nav) {
  border: none;
}

.main-tabs :deep(.el-tabs__item) {
  border: none;
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  background: transparent;
  transition: color 0.2s ease, background-color 0.2s ease;
  border-radius: 999px;
  margin-right: 6px;
}

.main-tabs :deep(.el-tabs__item:hover) {
  color: var(--primary-color);
  background: rgba(191, 219, 254, 0.7);
}

.main-tabs :deep(.el-tabs__item.is-active) {
  color: #0f172a;
  background: #e0ecff;
  border: none;
}

.main-tab-label {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.main-tabs :deep(.el-tabs__content) {
  padding: 14px 16px 16px;
  background: transparent;
}

.hot-tabs {
  width: 100%;
  background: #f8fafc;
  border-radius: 16px;
  border: none;
  box-shadow: none;
  min-height: 420px;
  display: flex;
  flex-direction: row-reverse;
  overflow: hidden;
}

.hot-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0;
  border-bottom: none;
  border-right: none;
  border-left: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 0.96);
  border-radius: 0 16px 16px 0;
  display: flex;
  align-items: stretch;
  justify-content: flex-start;
  flex: 0 0 220px;
}

.hot-tabs :deep(.el-tabs__nav-wrap) {
  width: 100%;
}

.hot-tabs :deep(.el-tabs__nav-scroll) {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
  padding: 12px 10px;
}

.hot-tabs :deep(.el-tabs__nav) {
  border: none;
}

.hot-tabs :deep(.el-tabs__item) {
  padding: 0;
  height: auto;
  line-height: normal;
  font-size: 14px;
  color: var(--text-tertiary);
  border: none;
  transition: color 0.2s ease;
  position: relative;
  margin: 2px 0;
  border-radius: 0;
}

.hot-tabs :deep(.el-tabs__item:hover) {
  color: #e5e7eb;
}

.hot-tabs :deep(.el-tabs__item.is-active) {
  color: #e5e7eb;
  font-weight: 600;
}

.hot-tabs :deep(.el-tabs__active-bar) {
  display: none;
}

.hot-tabs :deep(.el-tabs__content) {
  padding: 14px 14px 16px;
  background: #f8fafc;
  border-radius: 16px 0 0 16px;
  flex: 1;
}

.sub-tab-label {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 999px;
  width: 100%;
  cursor: pointer;
  color: inherit;
  transition: background 0.15s ease, transform 0.15s ease;
}

.sub-tab-label:hover {
  background: rgba(226, 232, 240, 0.9);
}

.sub-tab-dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: #22c55e;
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.6);
  flex-shrink: 0;
}

.sub-tab-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hot-tabs :deep(.el-tabs__item.is-active .sub-tab-label) {
  background: rgba(59, 130, 246, 0.16);
  transform: translateX(1px);
  color: #0f172a;
}

.hot-tabs :deep(.el-tabs__item.is-active .sub-tab-dot) {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.hot-items-container {
  height: 420px;
  max-height: 520px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 8px 4px 0;
}

.hot-items-container::-webkit-scrollbar {
  width: 6px;
}

.hot-items-container::-webkit-scrollbar-track {
  background: rgba(241, 245, 249, 0.9);
  border-radius: 3px;
}

.hot-items-container::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.8);
  border-radius: 3px;
}

.hot-items-container::-webkit-scrollbar-thumb:hover {
  background: rgba(100, 116, 139, 0.9);
}

.hot-items-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hot-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid rgba(203, 213, 225, 0.9);
  background: rgba(248, 250, 252, 0.98);
  text-decoration: none;
  color: var(--text-secondary);
  transition: background-color 0.15s ease, border-color 0.15s ease, transform 0.08s ease-out, box-shadow 0.15s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  min-height: 70px;
}

.hot-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #2563eb, #22c55e);
  opacity: 0.85;
}

.hot-item:hover {
  background: #eff6ff;
  border-color: rgba(59, 130, 246, 0.6);
  transform: translateY(-1px);
  box-shadow: 0 8px 26px rgba(15, 23, 42, 0.12);
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
  gap: 8px;
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
  background: #e0ecff;
  border: 1px solid rgba(148, 163, 184, 0.7);
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
  box-shadow: none;
  transition: border-color 0.15s ease, background-color 0.15s ease, transform 0.08s ease-out;
}

.hot-item:hover .hot-item-index {
  transform: translateY(-1px);
  background: rgba(59, 130, 246, 0.12);
  border-color: rgba(59, 130, 246, 0.6);
}

.hot-item-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.5;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-item-heat {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 999px;
  background: #fff7ed;
  color: #c2410c;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  box-shadow: none;
  border: 1px solid rgba(251, 191, 36, 0.6);
}

.hot-item-desc {
  font-size: 13px;
  color: var(--text-tertiary);
  line-height: 1.5;
  margin-top: 4px;
  padding-left: 32px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.loading-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: var(--text-tertiary);
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
  padding: 64px 20px;
  color: var(--text-tertiary);
  gap: 16px;
}

.hot-sections-empty .el-icon {
  font-size: 48px;
  color: rgba(148, 163, 184, 0.8);
}

.hot-sections-empty span {
  font-size: 16px;
  color: var(--text-tertiary);
}
</style>

