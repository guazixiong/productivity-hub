<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, FullScreen, Refresh, Sunny } from '@element-plus/icons-vue'

const router = useRouter()

const now = ref(new Date())
const is24Hour = ref(true)
const showSeconds = ref(true)
const isDark = ref(false)
const colorPalette = [
  '#f87171',
  '#34d399',
  '#60a5fa',
  '#c084fc',
  '#facc15',
  '#fb923c',
  '#38bdf8',
]
const activeColor = ref(colorPalette[2])
const clockTimer = ref<number | null>(null)
const clockRef = ref<HTMLElement | null>(null)

const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

const formattedTime = computed(() => {
  const date = now.value
  let hours = date.getHours()
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  if (!is24Hour.value) {
    hours = hours % 12 || 12
  }

  const hourStr = String(hours).padStart(2, '0')
  return showSeconds.value ? `${hourStr}:${minutes}:${seconds}` : `${hourStr}:${minutes}`
})

const meridiem = computed(() => (now.value.getHours() >= 12 ? 'PM' : 'AM'))

const formattedDate = computed(() => {
  const date = now.value
  return `${date.getFullYear()}年${String(date.getMonth() + 1).padStart(2, '0')}月${String(
    date.getDate()
  ).padStart(2, '0')}日 ${weekdays[date.getDay()]}`
})

const startClock = () => {
  now.value = new Date()
  clockTimer.value = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
}

const stopClock = () => {
  if (clockTimer.value) {
    clearInterval(clockTimer.value)
    clockTimer.value = null
  }
}

const toggleFullscreen = async () => {
  try {
    if (!document.fullscreenElement) {
      await clockRef.value?.requestFullscreen?.()
    } else {
      await document.exitFullscreen()
    }
  } catch (error) {
    ElMessage.error('当前浏览器不支持全屏模式')
  }
}

const randomizeColor = () => {
  const nextColor = colorPalette[Math.floor(Math.random() * colorPalette.length)]
  activeColor.value = nextColor
}

onMounted(() => {
  startClock()
})

onUnmounted(() => {
  stopClock()
})
</script>

<template>
  <div class="clock-container" :class="{ 'clock-dark': isDark }">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
      <div class="clock-controls">
        <el-switch v-model="is24Hour" active-text="24H" inactive-text="12H" />
        <el-switch v-model="showSeconds" active-text="显示秒" />
        <el-switch v-model="isDark" active-text="夜间模式" :active-icon="Sunny" />
        <el-button circle :icon="Refresh" @click="randomizeColor" />
        <el-button circle :icon="FullScreen" @click="toggleFullscreen" />
      </div>
    </div>
    <div ref="clockRef" class="clock-display">
      <div class="time" :style="{ color: activeColor }">
        {{ formattedTime }}
        <span v-if="!is24Hour" class="meridiem">{{ meridiem }}</span>
      </div>
      <div class="date">{{ formattedDate }}</div>
      <div class="color-palette">
        <button
          v-for="color in colorPalette"
          :key="color"
          :style="{ backgroundColor: color }"
          class="color-dot"
          :class="{ active: color === activeColor }"
          @click="activeColor = color"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.clock-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 70vh;
}

.clock-dark {
  background: radial-gradient(circle at top, #0f172a, #020617);
  border-radius: 24px;
  padding: 24px;
  box-shadow: inset 0 0 120px rgba(15, 23, 42, 0.6);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.clock-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.clock-display {
  flex: 1;
  border-radius: 24px;
  padding: 48px 24px;
  background: rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.clock-dark .clock-display {
  background: transparent;
}

.time {
  font-size: clamp(64px, 10vw, 144px);
  font-weight: 700;
  letter-spacing: 4px;
  font-family: 'DIN Alternate', 'Courier New', monospace;
}

.meridiem {
  font-size: 32px;
  margin-left: 16px;
  vertical-align: baseline;
  color: rgba(255, 255, 255, 0.8);
}

.clock-dark .time {
  text-shadow: 0 10px 40px rgba(94, 234, 212, 0.25);
}

.date {
  margin-top: 16px;
  font-size: 24px;
  color: rgba(15, 23, 42, 0.7);
}

.clock-dark .date {
  color: rgba(248, 250, 252, 0.8);
}

.color-palette {
  margin-top: 32px;
  display: flex;
  gap: 12px;
}

.color-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid transparent;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.color-dot.active {
  border-color: white;
  transform: scale(1.1);
}

.clock-dark .color-dot.active {
  border-color: rgba(255, 255, 255, 0.8);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .clock-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>


