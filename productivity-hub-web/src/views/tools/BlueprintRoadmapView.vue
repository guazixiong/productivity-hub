<script setup lang="ts">
import { ref } from 'vue'

const iframeLoaded = ref(false)
const iframeKey = ref(0)
const iframeSrc = '/blueprint-roadmap.html'

const handleIframeLoad = () => {
  iframeLoaded.value = true
}

const refreshBlueprint = () => {
  iframeLoaded.value = false
  iframeKey.value += 1
}

const openInNewTab = () => {
  window.open(iframeSrc, '_blank', 'noopener,noreferrer')
}
</script>

<template>
  <div class="blueprint-tool-page">
    <div class="header">
      <div class="header-text">
        <h2>AI架构师成长蓝图</h2>
        <p>完整覆盖基础与卓越能力的路线图，支持节点状态保存与快速跳转。</p>
      </div>
      <div class="header-actions">
        <el-button round @click="refreshBlueprint" :disabled="!iframeLoaded">
          刷新
        </el-button>
        <el-button round type="primary" @click="openInNewTab">
          新窗口打开
        </el-button>
      </div>
    </div>

    <el-alert
      class="usage-tip"
      type="info"
      show-icon
      title="节点状态保存在浏览器本地（localStorage），可随时刷新或单独在新标签页中查看。"
    />

    <div class="iframe-wrapper">
      <el-skeleton v-if="!iframeLoaded" class="iframe-skeleton" :rows="8" animated />
      <iframe
        v-show="iframeLoaded"
        :key="iframeKey"
        :src="iframeSrc"
        title="AI Blueprint"
        frameborder="0"
        @load="handleIframeLoad"
      />
    </div>
  </div>
</template>

<style scoped>
.blueprint-tool-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.header-text h2 {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 600;
  color: #0f172a;
}

.header-text p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.usage-tip {
  border: none;
}

.iframe-wrapper {
  position: relative;
  flex: 1;
  min-height: 720px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(99, 102, 241, 0.2);
  background: #f8fafc;
}

.iframe-wrapper iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.iframe-skeleton {
  position: absolute;
  inset: 0;
  padding: 24px;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>

