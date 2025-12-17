<script setup lang="ts">
import { computed, ref, watch, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTabsStore } from '@/stores/tabs'
import { Close, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const tabsStore = useTabsStore()

// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const contextMenuTab = ref<string | null>(null)

// 标签页滚动相关
const tabsContainerRef = ref<HTMLElement>()
const scrollLeft = ref(0)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)

// 计算属性
const tabs = computed(() => tabsStore.tabs)
const activeTab = computed(() => tabsStore.activeTab)

// 检查是否可以滚动
const checkScrollability = () => {
  if (!tabsContainerRef.value) return
  
  const container = tabsContainerRef.value
  canScrollLeft.value = scrollLeft.value > 0
  canScrollRight.value = 
    scrollLeft.value < container.scrollWidth - container.clientWidth
}

// 滚动标签页
const scrollTabs = (direction: 'left' | 'right') => {
  if (!tabsContainerRef.value) return
  
  const container = tabsContainerRef.value
  const scrollAmount = 200
  
  if (direction === 'left') {
    scrollLeft.value = Math.max(0, scrollLeft.value - scrollAmount)
  } else {
    scrollLeft.value = Math.min(
      container.scrollWidth - container.clientWidth,
      scrollLeft.value + scrollAmount
    )
  }
  
  container.scrollTo({
    left: scrollLeft.value,
    behavior: 'smooth',
  })
  
  setTimeout(checkScrollability, 300)
}

// 点击标签页
const handleTabClick = (tabPath: string) => {
  tabsStore.setActiveTab(tabPath)
  router.push(tabPath)
}

// 关闭标签页
const handleCloseTab = (e: MouseEvent, tabPath: string) => {
  e.stopPropagation()
  
  // 如果关闭的是当前激活的标签页，需要导航到其他标签页
  if (tabPath === activeTab.value) {
    const currentIndex = tabs.value.findIndex(tab => tab.path === tabPath)
    const remainingTabs = tabs.value.filter(tab => tab.path !== tabPath)
    
    if (remainingTabs.length > 0) {
      // 优先切换到右侧的标签页，如果没有则切换到左侧
      const nextTab = remainingTabs[currentIndex] || remainingTabs[currentIndex - 1] || remainingTabs[0]
      tabsStore.removeTab(tabPath)
      router.push(nextTab.path)
    } else {
      // 如果没有其他标签页，导航到首页
      tabsStore.removeTab(tabPath)
      router.push('/home')
    }
  } else {
    tabsStore.removeTab(tabPath)
  }
}

// 右键菜单
const handleContextMenu = (e: MouseEvent, tabPath: string) => {
  e.preventDefault()
  e.stopPropagation()
  
  contextMenuPosition.value = { x: e.clientX, y: e.clientY }
  contextMenuTab.value = tabPath
  contextMenuVisible.value = true
}

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuTab.value = null
}

// 关闭其他标签页
const handleCloseOthers = () => {
  if (contextMenuTab.value) {
    tabsStore.closeOtherTabs(contextMenuTab.value)
    router.push(contextMenuTab.value)
  }
  closeContextMenu()
}

// 刷新标签页
const handleRefreshTab = async () => {
  const targetPath = contextMenuTab.value || activeTab.value
  if (!targetPath) {
    closeContextMenu()
    return
  }

  closeContextMenu()

  // 如果刷新的是非当前激活标签，先切换过去
  if (targetPath !== route.fullPath) {
    tabsStore.setActiveTab(targetPath)
    await router.push(targetPath)
  }

  tabsStore.refreshTab(targetPath)
}

// 关闭所有标签页
const handleCloseAll = () => {
  tabsStore.closeAllTabs()
  router.push('/home')
  closeContextMenu()
}

// 点击其他地方关闭右键菜单
const handleClickOutside = () => {
  if (contextMenuVisible.value) {
    closeContextMenu()
  }
}

// 监听滚动
const handleScroll = () => {
  if (tabsContainerRef.value) {
    scrollLeft.value = tabsContainerRef.value.scrollLeft
    checkScrollability()
  }
}

// 监听标签页变化，更新滚动状态
watch(
  () => tabsStore.tabs.length,
  () => {
    nextTick(() => {
      checkScrollability()
    })
  }
)

// 组件挂载后检查滚动
onMounted(() => {
  nextTick(() => {
    checkScrollability()
  })
})
</script>

<template>
  <div class="tabs-container" @click="handleClickOutside">
    <!-- 左滚动按钮 -->
    <el-button
      v-if="canScrollLeft"
      :icon="ArrowLeft"
      circle
      size="small"
      class="scroll-button scroll-button-left"
      @click="scrollTabs('left')"
    />
    
    <!-- 标签页列表 -->
    <div
      ref="tabsContainerRef"
      class="tabs-list"
      @scroll="handleScroll"
    >
      <div
        v-for="tab in tabs"
        :key="tab.path"
        :class="['tab-item', { active: tab.path === activeTab }]"
        @click="handleTabClick(tab.path)"
        @contextmenu="handleContextMenu($event, tab.path)"
      >
        <span class="tab-title">{{ tab.title }}</span>
        <el-icon
          class="tab-close"
          @click.stop="handleCloseTab($event, tab.path)"
        >
          <Close />
        </el-icon>
      </div>
    </div>
    
    <!-- 右滚动按钮 -->
    <el-button
      v-if="canScrollRight"
      :icon="ArrowRight"
      circle
      size="small"
      class="scroll-button scroll-button-right"
      @click="scrollTabs('right')"
    />
    
    <!-- 右键菜单 -->
    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="{
        left: `${contextMenuPosition.x}px`,
        top: `${contextMenuPosition.y}px`,
      }"
      @click.stop
    >
      <div class="context-menu-item" @click="handleCloseOthers">
        关闭其他标签页
      </div>
      <div class="context-menu-item" @click="handleRefreshTab">
        刷新当前标签页
      </div>
      <div class="context-menu-item" @click="handleCloseAll">
        关闭所有标签页
      </div>
    </div>
  </div>
</template>

<style scoped>
.tabs-container {
  position: relative;
  display: flex;
  align-items: center;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.95) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.1);
  padding: 0 12px;
  height: 52px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
}

.scroll-button {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border: 1px solid rgba(99, 102, 241, 0.15);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%);
  color: #6366f1;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
}

.scroll-button:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(139, 92, 246, 0.1) 100%);
  border-color: #6366f1;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
}

.scroll-button-left {
  margin-right: 4px;
}

.scroll-button-right {
  margin-left: 4px;
}

.tabs-list {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tabs-list::-webkit-scrollbar {
  display: none;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 18px;
  min-width: 130px;
  max-width: 220px;
  height: 40px;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.9) 0%, rgba(241, 245, 249, 0.85) 100%);
  border: 1px solid rgba(99, 102, 241, 0.12);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  user-select: none;
  position: relative;
  overflow: hidden;
}

.tab-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  border-radius: 0 0 3px 3px;
  transition: height 0.3s ease;
}

.tab-item:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.08) 100%);
  border-color: rgba(99, 102, 241, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.tab-item.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.12) 100%);
  border-color: rgba(99, 102, 241, 0.35);
  color: #312e81;
  font-weight: 600;
  box-shadow: 
    0 4px 16px rgba(99, 102, 241, 0.2),
    inset 0 0 0 1px rgba(255, 255, 255, 0.3);
}

.tab-item.active::before {
  height: 100%;
}

.tab-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: #334155;
  transition: color 0.2s ease;
}

.tab-item.active .tab-title {
  color: #312e81;
  font-weight: 600;
}

.tab-close {
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  padding: 2px;
  border-radius: 4px;
  transition: all 0.2s ease;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-close:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.context-menu {
  position: fixed;
  z-index: 9999;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.95) 100%);
  border: 1px solid rgba(99, 102, 241, 0.15);
  border-radius: 14px;
  box-shadow: 
    0 12px 32px rgba(15, 23, 42, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  padding: 6px;
  min-width: 180px;
  backdrop-filter: blur(20px) saturate(180%);
}

.context-menu-item {
  padding: 12px 18px;
  font-size: 14px;
  color: #334155;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.context-menu-item:hover {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.08) 100%);
  color: #312e81;
  transform: translateX(2px);
}
</style>

