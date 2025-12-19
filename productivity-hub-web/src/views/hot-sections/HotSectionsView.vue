<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import { scheduleApi } from '@/services/api'
import type { HotSection } from '@/types/hotSections'

// 顶级标签页
const MAIN_TABS = ['生活', '技术'] as const
type MainTab = typeof MAIN_TABS[number]

// 生活类热点标签列表
const LIFE_HOT_SECTION_NAMES = [
  '综合热榜',
  '知乎热搜',
  '哔哩哔哩热榜',
  '虎扑热帖',
  '抖音热榜',
  '小红书热帖',
  '百度贴吧热帖'
]

// 技术类热点标签列表
const TECH_HOT_SECTION_NAMES: string[] = [
  'GitHub',
  '技术期刊',
  '掘金',
  'V2EX',
  'infoQ',
  '吾爱破解',
  'CSDN'
]

// 二级标签专属图标（基于平台/类型）
const SECTION_ICONS: Record<string, string> = {
  // 生活
  综合热榜: '🔥',
  知乎热搜: '💬',
  哔哩哔哩热榜: '📺',
  虎扑热帖: '🏀',
  抖音热榜: '🎵',
  小红书热帖: '📸',
  百度贴吧热帖: '🧩',
  // 技术
  GitHub: '🐙',
  技术期刊: '📚',
  掘金: '💎',
  V2EX: '💻',
  infoQ: '📰',
  吾爱破解: '🧠',
  CSDN: '📘'
}

const getSectionIcon = (sectionName: string) => {
  return SECTION_ICONS[sectionName] ?? '📌'
}

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

// 下拉刷新相关状态（简化版，只保留刷新状态）
const pullRefreshState = ref<Map<string, {
  isRefreshing: boolean
}>>(new Map())

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

// 获取指定标签页的下拉刷新状态
const getPullRefreshState = (sectionName: string) => {
  return pullRefreshState.value.get(sectionName) || {
    isRefreshing: false
  }
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
  
  // 仅在首次访问时加载当前子标签数据
  const sectionData = sectionDataMap.value.get(activeTab.value)
  if (sectionData && sectionData.items.length === 0 && !sectionData.loading) {
    hotSectionsLoading.value = true
    try {
      await loadHotSection(activeTab.value, DEFAULT_LIMIT, false)
    } finally {
      hotSectionsLoading.value = false
    }
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

// 刷新当前标签页数据
const refreshCurrentSection = async () => {
  if (!activeTab.value) return
  
  const sectionData = sectionDataMap.value.get(activeTab.value)
  if (!sectionData || sectionData.loading) return
  
  // 初始化刷新状态
  if (!pullRefreshState.value.has(activeTab.value)) {
    pullRefreshState.value.set(activeTab.value, { isRefreshing: false })
  }
  const refreshState = pullRefreshState.value.get(activeTab.value)!
  
  // 如果正在刷新，不重复触发
  if (refreshState.isRefreshing) return
  
  refreshState.isRefreshing = true
  
  try {
    // 重置limit并重新加载
    sectionData.limit = DEFAULT_LIMIT
    sectionData.hasMore = true
    await loadHotSection(activeTab.value, DEFAULT_LIMIT, false)
    ElMessage.success('刷新成功')
  } catch (error) {
    ElMessage.error('刷新失败，请重试')
  } finally {
    // 延迟重置状态，让用户看到刷新完成的反馈
    setTimeout(() => {
      refreshState.isRefreshing = false
    }, 300)
  }
}

// 滚动处理（使用节流优化性能）
const handleScroll = (event: Event) => {
  if (!activeTab.value) return
  
  const container = event.target as HTMLElement
  if (!container) return
  
  // 验证是否是当前激活标签的滚动容器
  const currentContainer = scrollContainerRefs.value.get(activeTab.value)
  if (container !== currentContainer) return
  
  // 节流处理，避免频繁触发
  if (scrollTimer) {
    clearTimeout(scrollTimer)
  }
  
  scrollTimer = window.setTimeout(() => {
    if (!activeTab.value) return
    
    const sectionData = sectionDataMap.value.get(activeTab.value)
    if (!sectionData) return
    
    const scrollTop = container.scrollTop
    const scrollHeight = container.scrollHeight
    const clientHeight = container.clientHeight
    
    // 距离底部小于阈值时加载更多
    // 使用 <= 而不是 < 以确保在到达底部时也能触发
    if (!loadingMore.value && sectionData.hasMore && !sectionData.loading) {
      const distanceToBottom = scrollHeight - scrollTop - clientHeight
      if (distanceToBottom <= scrollThreshold) {
        loadMore()
      }
    }
  }, 100) // 100ms节流
}

// 滚轮事件处理（用于检测滚动到顶部时继续向上滚动触发刷新）
const handleWheel = (event: WheelEvent, sectionName: string) => {
  const container = scrollContainerRefs.value.get(sectionName)
  if (!container) return
  
  const currentScrollTop = container.scrollTop
  
  // 如果滚动到顶部（scrollTop === 0）且滚轮向上滚动（deltaY < 0）
  // 说明用户在顶部继续向上滚动，触发刷新当前标签数据
  if (currentScrollTop === 0 && event.deltaY < 0) {
    // 确保当前标签页是激活的
    if (activeTab.value === sectionName) {
      const sectionData = sectionDataMap.value.get(sectionName)
      const refreshState = pullRefreshState.value.get(sectionName)

      // 不在加载中且不在刷新中时触发刷新
      if (
        sectionData &&
        !sectionData.loading &&
        !loadingMore.value &&
        (!refreshState || !refreshState.isRefreshing)
      ) {
        refreshCurrentSection()
      }
    }
  }
}

// 存储滚轮事件处理函数，以便后续清理
const wheelHandlers = new Map<string, (e: WheelEvent) => void>()

// 设置滚动容器引用
const setScrollContainerRef = (el: HTMLElement | null, sectionName: string) => {
  if (el) {
    scrollContainerRefs.value.set(sectionName, el)
    
    // 初始化刷新状态
    if (!pullRefreshState.value.has(sectionName)) {
      pullRefreshState.value.set(sectionName, { isRefreshing: false })
    }
    
    // 创建滚轮事件处理函数
    const wheelHandler = (e: WheelEvent) => handleWheel(e, sectionName)
    el.addEventListener('wheel', wheelHandler, { passive: true })
    
    // 保存处理函数以便后续清理
    wheelHandlers.set(sectionName, wheelHandler)
  } else {
    // 清理事件监听器
    const oldEl = scrollContainerRefs.value.get(sectionName)
    const wheelHandler = wheelHandlers.get(sectionName)
    if (oldEl && wheelHandler) {
      oldEl.removeEventListener('wheel', wheelHandler)
    }
    
    scrollContainerRefs.value.delete(sectionName)
    pullRefreshState.value.delete(sectionName)
    wheelHandlers.delete(sectionName)
  }
}

// 格式化当前标签数据为微信分享格式
const formatForWeChat = (sectionName: string): string => {
  const sectionData = sectionDataMap.value.get(sectionName)
  if (!sectionData || sectionData.items.length === 0) {
    return ''
  }

  const items = sectionData.items
  const lines: string[] = []
  
  // 标题
  lines.push(`🔥 ${sectionName}`)
  lines.push('')
  
  // 内容列表
  items.forEach((item, index) => {
    // 序号和标题
    lines.push(`${index + 1}. ${item.title}`)
    
    // 链接（微信会自动识别链接）
    lines.push(item.link)
    
    // 描述（如果有，放在链接下方）
    if (item.desc) {
      lines.push(`💬 ${item.desc}`)
    }
    
    // 热度（如果有，放在描述下方）
    if (item.heat) {
      lines.push(`📊 ${item.heat}`)
    }
    
    // 条目之间空一行，保持清晰分段
    if (index < items.length - 1) {
      lines.push('')
    }
  })
  
  return lines.join('\n')
}

// 复制当前标签内容到剪贴板
const copyCurrentSection = async () => {
  if (!activeTab.value) {
    ElMessage.warning('请先选择一个标签')
    return
  }
  
  const sectionData = sectionDataMap.value.get(activeTab.value)
  if (!sectionData || sectionData.items.length === 0) {
    ElMessage.warning('当前标签暂无数据')
    return
  }
  
  try {
    const formattedText = formatForWeChat(activeTab.value)
    
    // 使用 Clipboard API
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(formattedText)
      ElMessage.success('已复制到剪贴板，可直接粘贴到微信')
    } else {
      // 降级方案：使用传统方法
      const textArea = document.createElement('textarea')
      textArea.value = formattedText
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      document.body.appendChild(textArea)
      textArea.select()
      document.execCommand('copy')
      document.body.removeChild(textArea)
      ElMessage.success('已复制到剪贴板，可直接粘贴到微信')
    }
  } catch (error) {
    ElMessage.error('复制失败，请重试')
    console.error('复制失败:', error)
  }
}


// 初始化
onMounted(async () => {
  initSectionData()
  
  // 默认加载生活类的第一个子标签，其他子标签按需加载
  if (LIFE_HOT_SECTION_NAMES.length > 0) {
    activeTab.value = LIFE_HOT_SECTION_NAMES[0]
    hotSectionsLoading.value = true
    try {
      await loadHotSection(activeTab.value, DEFAULT_LIMIT, false)
    } finally {
      hotSectionsLoading.value = false
    }
  }
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
                <div v-if="getCurrentHotSectionNames.length > 0" class="hot-tabs-wrapper">
                  <!-- 二级标签上的一键复制按钮（绝对定位，不占用内容区域高度） -->
                  <div class="hot-tabs-header">
                    <el-button
                      type="primary"
                      :icon="DocumentCopy"
                      size="small"
                      :disabled="!activeTab || !getSectionData(activeTab) || getSectionData(activeTab)?.items.length === 0"
                      @click="copyCurrentSection"
                      class="copy-button-header"
                    >
                      一键复制
                    </el-button>
                  </div>
                  <el-tabs
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
                          <span class="sub-tab-icon-wrap">
                            <span class="sub-tab-icon">
                              {{ getSectionIcon(sectionName) }}
                            </span>
                          </span>
                          <span class="sub-tab-text">
                            {{ sectionName }}
                          </span>
                        </div>
                      </template>
                      <div
                        v-if="activeTab === sectionName"
                        class="hot-section-content"
                      >
                        <div
                          :ref="(el) => setScrollContainerRef(el as HTMLElement | null, sectionName)"
                          class="hot-items-container"
                          @scroll="handleScroll"
                        >
                          <!-- 下拉刷新指示器 -->
                          <div
                            v-if="getPullRefreshState(sectionName).isRefreshing"
                            class="pull-refresh-indicator"
                          >
                            <div class="pull-refresh-content">
                              <el-icon class="is-loading"><TrendCharts /></el-icon>
                              <span>刷新中...</span>
                            </div>
                          </div>

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
                            <div
                              v-if="loadingMore || getSectionData(sectionName)?.loading"
                              class="loading-more"
                            >
                              <el-icon class="is-loading"><TrendCharts /></el-icon>
                              <span>加载中...</span>
                            </div>
                            <div
                              v-else-if="!getSectionData(sectionName)?.hasMore && (getSectionData(sectionName)?.items.length || 0) > 0"
                              class="no-more"
                            >
                              <span>没有更多数据了</span>
                            </div>
                            <div
                              v-else-if="(getSectionData(sectionName)?.items.length || 0) === 0 && !getSectionData(sectionName)?.loading"
                              class="hot-sections-empty"
                            >
                              <el-icon><TrendCharts /></el-icon>
                              <span>暂无热点数据</span>
                            </div>
                          </template>
                        </div>
                      </div>
                  </el-tab-pane>
                  </el-tabs>
                </div>
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

.hot-tabs-wrapper {
  position: relative;
  width: 100%;
  background: #f8fafc;
  border-radius: 16px;
  min-height: 420px;
}

.hot-tabs-header {
  position: absolute;
  /* 按钮与左侧热点列表顶部对齐，并与右侧二级标签宽度保持一致 */
  top: 8px;
  right: 0;
  width: 220px;
  z-index: 10;
  pointer-events: none;
}

.hot-tabs-header .copy-button-header {
  pointer-events: auto;
}

.copy-button-header {
  width: 100%;
  position: relative;
  border-radius: 999px;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.5px;
  padding: 9px 14px;
  color: #0f172a;
  box-shadow:
    0 2px 0 #60a5fa,
    0 8px 18px rgba(15, 23, 42, 0.25),
    0 0 0 1px rgba(59, 130, 246, 0.75);
  background: linear-gradient(180deg, #eff6ff 0%, #bfdbfe 40%, #60a5fa 100%);
  border: 1px solid #93c5fd;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.7);
  transition: transform 0.1s ease-out, box-shadow 0.15s ease, filter 0.15s ease;
  transform-origin: center;
  filter: saturate(1.05);
  overflow: hidden;
}

.copy-button-header::before {
  content: '';
  position: absolute;
  left: 2px;
  right: 2px;
  top: 2px;
  height: 50%;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0));
  opacity: 0.9;
  pointer-events: none;
}

.copy-button-header:hover {
  box-shadow:
    0 3px 0 #60a5fa,
    0 14px 26px rgba(15, 23, 42, 0.28),
    0 0 0 1px rgba(59, 130, 246, 0.85);
  transform: translateY(-1px) scale(1.01);
  filter: saturate(1.1) brightness(1.02);
}

.copy-button-header:active {
  box-shadow:
    0 1px 0 #3b82f6,
    0 6px 14px rgba(15, 23, 42, 0.28),
    0 0 0 1px rgba(59, 130, 246, 0.9);
  transform: translateY(1px) scale(0.99);
  filter: saturate(1);
}

.copy-button-header:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  background: linear-gradient(135deg, #e5e7eb, #cbd5f5);
  box-shadow: none;
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
  /* 让右侧二级标签整体下移，不影响左侧热点内容高度 */
  padding: 0;
  margin-top: 44px;
  border-bottom: none;
  border-right: none;
  border-left: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 0.96);
  border-radius: 0 16px 16px 0;
  display: flex;
  align-items: stretch;
  justify-content: flex-start;
  flex: 0 0 220px;
  position: relative;
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
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.hot-section-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  flex: 1;
}

.section-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 8px 4px 12px;
  margin-bottom: 4px;
}

.copy-button {
  border-radius: 8px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
  transition: all 0.2s ease;
}

.copy-button:hover {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}

.copy-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.copy-button-in-tab {
  border-radius: 6px;
  font-weight: 500;
  font-size: 12px;
  padding: 4px 8px;
  height: auto;
  margin-left: auto;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(59, 130, 246, 0.2);
  transition: all 0.2s ease;
}

.copy-button-in-tab:hover {
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}

.copy-button-in-tab:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.sub-tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 999px;
  width: 100%;
  cursor: pointer;
  color: inherit;
  transition: background 0.15s ease, transform 0.15s ease;
  position: relative;
}

.sub-tab-label:hover {
  background: rgba(226, 232, 240, 0.9);
}

.sub-tab-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sub-tab-icon-wrap {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(148, 163, 184, 0.5);
}

.sub-tab-icon {
  font-size: 14px;
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
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 8px 4px 0;
  position: relative;
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

.pull-refresh-indicator {
  position: absolute;
  top: 0;
  left: 0;
  right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  z-index: 10;
  pointer-events: none;
  background: linear-gradient(to bottom, rgba(248, 250, 252, 0.95), transparent);
}

.pull-refresh-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--primary-color);
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  padding-bottom: 8px;
  pointer-events: none;
}

.pull-refresh-content .el-icon {
  transition: transform 0.3s ease;
}

.pull-refresh-content .el-icon.is-loading {
  animation: rotate 1s linear infinite;
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

