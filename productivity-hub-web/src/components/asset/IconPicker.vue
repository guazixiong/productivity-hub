<template>
  <div class="icon-picker">
    <el-input
      v-model="searchText"
      placeholder="搜索图标..."
      clearable
      class="icon-search"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <div class="icon-list">
      <div
        v-for="icon in filteredIcons"
        :key="icon.name"
        class="icon-item"
        :class="{ active: selectedIcon === icon.name }"
        @click="handleSelectIcon(icon.name)"
      >
        <el-icon :size="24">
          <component :is="icon.component" />
        </el-icon>
        <div class="icon-name">{{ icon.name }}</div>
      </div>
    </div>

    <div v-if="filteredIcons.length === 0" class="no-icons">
      未找到匹配的图标
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getCommonIcons, getAvailableIconNames, getIconComponent } from '@/utils/iconMapper'

interface Props {
  modelValue?: string
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'icon-selected', value: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const searchText = ref('')
const selectedIcon = ref(props.modelValue || '')

// 获取所有图标（优先显示常用图标，搜索时显示所有图标）
const allIcons = computed(() => {
  const commonIcons = getCommonIcons()
  
  // 如果有搜索文本，显示所有可用图标
  if (searchText.value) {
    const allIconNames = getAvailableIconNames()
    const iconMap = new Map<string, any>()
    
    // 先添加常用图标
    commonIcons.forEach((icon) => {
      iconMap.set(icon.name, icon.component)
    })
    
    // 再添加其他图标
    allIconNames.forEach((name) => {
      if (!iconMap.has(name)) {
        const component = getIconComponent(name)
        if (component) {
          iconMap.set(name, component)
        }
      }
    })
    
    return Array.from(iconMap.entries()).map(([name, component]) => ({
      name,
      component,
    }))
  }
  
  // 没有搜索文本时，只显示常用图标
  return commonIcons
})

// 过滤图标
const filteredIcons = computed(() => {
  if (!searchText.value) {
    return allIcons.value
  }
  
  const searchLower = searchText.value.toLowerCase()
  return allIcons.value.filter((icon) =>
    icon.name.toLowerCase().includes(searchLower)
  )
})

// 选择图标
const handleSelectIcon = (iconName: string) => {
  selectedIcon.value = iconName
  emit('update:modelValue', iconName)
  emit('icon-selected', iconName)
}

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  selectedIcon.value = newValue || ''
})
</script>

<style scoped lang="scss">
.icon-picker {
  .icon-search {
    margin-bottom: 16px;
  }

  .icon-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 12px;
    max-height: 400px;
    overflow-y: auto;
    padding: 8px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;

    .icon-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 12px 8px;
      border: 2px solid transparent;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;
      background: #f5f7fa;

      &:hover {
        background: #ecf5ff;
        border-color: #409eff;
      }

      &.active {
        background: #409eff;
        border-color: #409eff;
        color: white;
      }

      .icon-name {
        margin-top: 8px;
        font-size: 11px;
        text-align: center;
        word-break: break-all;
        line-height: 1.2;
        max-width: 100%;
      }
    }
  }

  .no-icons {
    text-align: center;
    padding: 40px;
    color: #909399;
  }
}
</style>

