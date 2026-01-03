<template>
  <div class="tools-grid">
    <div
      v-for="tool in tools"
      :key="tool.id"
      class="tool-item"
      @click="handleToolClick(tool)"
    >
      <div class="tool-icon">{{ tool.icon }}</div>
      <div class="tool-name">{{ tool.name }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 常用工具网格组件
 */

import { useRouter } from 'vue-router'

interface Tool {
  id: string
  name: string
  icon: string
  route: string
}

interface Props {
  tools: Tool[]
}

const props = defineProps<Props>()
const router = useRouter()

const handleToolClick = (tool: Tool) => {
  router.push(tool.route)
}
</script>

<style scoped lang="scss">
.tools-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  .tool-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 24px;
    border: 1px solid #E8E8E8;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    .tool-icon {
      font-size: 48px;
      margin-bottom: 8px;
    }

    .tool-name {
      font-size: 14px;
      color: #333;
    }

    &:hover {
      border-color: #4A90E2;
      box-shadow: 0 2px 8px rgba(74, 144, 226, 0.2);
      transform: translateY(-2px);
    }
  }
}

@media (min-width: 769px) {
  .tools-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>

