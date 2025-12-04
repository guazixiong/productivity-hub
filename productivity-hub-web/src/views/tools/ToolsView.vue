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

const trackToolClick = async (toolId: string) => {
  try {
    toolStats.value = await toolApi.track(toolId)
  } catch (error) {
    console.warn('记录工具点击失败', error)
  }
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

    <el-skeleton v-if="isStatsLoading && !toolStats.length" :rows="3" animated />

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
  gap: 24px;
}

.tools-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tool-search {
  max-width: 360px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.tool-card {
  position: relative;
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.16);
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.tool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.15);
  border-color: #6366f1;
  background: rgba(255, 255, 255, 0.95);
}

.tool-hot-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: #ef4444;
  color: #fff;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.tool-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 8px;
}

.tool-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.tool-card:hover .tool-icon-wrapper {
  transform: scale(1.1);
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
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
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
