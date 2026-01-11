<template>
  <div class="ai-daily-container">
    <div class="ai-daily-content">
      <el-icon class="ai-daily-icon" :size="80">
        <Document />
      </el-icon>
      <h1 class="ai-daily-title">功能开发中</h1>
      <p class="ai-daily-description">敬请期待</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'
</script>

<style scoped>
.ai-daily-container {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.ai-daily-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.ai-daily-icon {
  color: var(--primary-color);
  opacity: 0.8;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.8;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

.ai-daily-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.ai-daily-description {
  font-size: 18px;
  color: var(--text-secondary);
  margin: 0;
}
</style>

