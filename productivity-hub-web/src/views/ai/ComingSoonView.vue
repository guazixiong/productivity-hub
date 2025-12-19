<template>
  <div class="coming-soon-container">
    <div class="coming-soon-content">
      <el-icon class="coming-soon-icon" :size="80">
        <Tools />
      </el-icon>
      <h1 class="coming-soon-title">功能开发中</h1>
      <p class="coming-soon-description">敬请期待</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Tools } from '@element-plus/icons-vue'
</script>

<style scoped>
.coming-soon-container {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.coming-soon-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.coming-soon-icon {
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

.coming-soon-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.coming-soon-description {
  font-size: 18px;
  color: var(--text-secondary);
  margin: 0;
}
</style>

