<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useNavigationStore } from '@/stores/navigation'
import type { ToolStat } from '@/types/tools'
import { toolApi } from '@/services/api'
import { toolList, toolMetaMap, type ToolMeta } from '@/data/tools'

const router = useRouter()
const navigationStore = useNavigationStore()

const tools: ToolMeta[] = toolList

const searchKeyword = ref('')
const toolStats = ref<ToolStat[]>([])
const isStatsLoading = ref(false)

const toolStatMap = computed(() => {
  const map = new Map<string, ToolStat>()
  toolStats.value.forEach((stat) => map.set(stat.id, stat))
  return map
})

const getToolClicks = (toolId: string) => toolStatMap.value.get(toolId)?.clicks ?? 0

const sortedTools = computed(() => [...tools].sort((a, b) => getToolClicks(b.id) - getToolClicks(a.id)))

const filteredTools = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return sortedTools.value
  }
  return sortedTools.value.filter((tool) => {
    const haystack = [tool.name, tool.description, ...(tool.keywords ?? [])].join(' ').toLowerCase()
    return haystack.includes(keyword)
  })
})

const hotToolIdSet = computed(() => {
  const hotTools = sortedTools.value.filter((tool) => getToolClicks(tool.id) > 0).slice(0, 10)
  return new Set(hotTools.map((tool) => tool.id))
})

const loadToolStats = async () => {
  isStatsLoading.value = true
  try {
    toolStats.value = await toolApi.stats()
  } catch (error) {
    ElMessage.error((error as Error).message ?? '加载工具统计失败')
  } finally {
    isStatsLoading.value = false
  }
}

const trackToolClick = (toolId: string) => {
  // 使用 fire-and-forget 模式，不阻塞用户操作
  // 统计功能失败不应该影响用户体验
  toolApi
    .track(toolId)
    .then((stats) => {
      // 成功时更新统计数据
      toolStats.value = stats
    })
    .catch((error) => {
      // 静默失败，只记录日志，不显示错误消息
      console.debug('记录工具点击失败（已忽略）', error)
    })
}

const navigateToTool = (path: string, toolId: string) => {
  trackToolClick(toolId)
  // 记录来源页面为工具箱
  navigationStore.recordNavigation(path, '/tools')
  router.push(path)
}

const resetFilter = () => {
  searchKeyword.value = ''
}

onMounted(() => {
  loadToolStats()
})
</script>

<template>
  <div class="tools-container">
    <div class="tools-toolbar">
      <el-input
        v-model="searchKeyword"
        class="tool-search"
        placeholder="搜索工具名称、描述或关键字"
        clearable
        :prefix-icon="Search"
      />
      <el-button v-if="searchKeyword" text size="small" @click="resetFilter">清除</el-button>
    </div>

    <div v-if="filteredTools.length" class="tools-grid">
      <el-card
        v-for="tool in filteredTools"
        :key="tool.id"
        class="tool-card"
        shadow="hover"
        @click="navigateToTool(tool.path, tool.id)"
      >
        <div class="tool-hot-badge" v-if="hotToolIdSet.has(tool.id)">热</div>
        <div class="tool-content">
          <div class="tool-icon-wrapper">
            <el-icon class="tool-icon">
              <component :is="tool.icon" />
            </el-icon>
          </div>
          <div class="tool-info">
            <h3 class="tool-name">{{ tool.name }}</h3>
            <p class="tool-description">{{ tool.description }}</p>
          </div>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="未找到匹配的工具" />
  </div>
</template>

<style scoped>
.tools-container {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 8px 0;
}

.tools-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tool-search {
  max-width: 400px;
  flex: 1;
}

.tool-search :deep(.el-input__wrapper) {
  border-radius: 14px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.08);
  transition: all 0.3s ease;
}

.tool-search :deep(.el-input__wrapper:hover) {
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.15);
}

.tool-search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.tool-card {
  position: relative;
  border-radius: 20px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.85) 100%);
  backdrop-filter: blur(12px) saturate(180%);
  overflow: hidden;
}

.tool-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #ec4899);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.tool-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 
    0 20px 50px rgba(99, 102, 241, 0.2),
    0 0 0 1px rgba(99, 102, 241, 0.1) inset;
  border-color: rgba(99, 102, 241, 0.3);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.95) 100%);
}

.tool-card:hover::before {
  opacity: 1;
}

.tool-hot-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
  z-index: 1;
  animation: hot-badge-pulse 2s ease-in-out infinite;
}

@keyframes hot-badge-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 6px 16px rgba(239, 68, 68, 0.5);
  }
}

.tool-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 8px;
}

.tool-icon-wrapper {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
  position: relative;
  overflow: hidden;
}

.tool-icon-wrapper::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  transform: rotate(45deg);
  transition: transform 0.6s ease;
}

.tool-card:hover .tool-icon-wrapper {
  transform: scale(1.12) rotate(5deg);
  box-shadow: 0 12px 28px rgba(99, 102, 241, 0.4);
}

.tool-card:hover .tool-icon-wrapper::before {
  transform: rotate(225deg);
}

.tool-icon {
  font-size: 32px;
  color: white;
}

.tool-info {
  text-align: center;
  flex: 1;
}

.tool-name {
  margin: 0 0 10px 0;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #0f172a 0%, #475569 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.3px;
}

.tool-description {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .tools-container {
    gap: 16px;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }

  .tools-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .tool-search {
    max-width: 100%;
  }
}
</style>
