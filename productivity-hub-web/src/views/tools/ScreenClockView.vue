<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, FullScreen } from '@element-plus/icons-vue'

const router = useRouter()

const now = ref(new Date())
const is24Hour = ref(true)
const showSeconds = ref(true)
const backgroundColor = ref<'white' | 'black'>('black')
const isFullscreen = ref(false)
const clockTimer = ref<number | null>(null)
const clockRef = ref<HTMLElement | null>(null)

const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

// 时间数字分解
const timeDigits = computed(() => {
  const date = now.value
  let hours = date.getHours()
  const minutes = date.getMinutes()
  const seconds = date.getSeconds()

  if (!is24Hour.value) {
    hours = hours % 12 || 12
  }

  const hourStr = String(hours).padStart(2, '0')
  const minuteStr = String(minutes).padStart(2, '0')
  const secondStr = String(seconds).padStart(2, '0')

  return {
    hours: [hourStr[0], hourStr[1]],
    minutes: [minuteStr[0], minuteStr[1]],
    seconds: [secondStr[0], secondStr[1]],
  }
})

const meridiem = computed(() => (now.value.getHours() >= 12 ? 'PM' : 'AM'))

const formattedDate = computed(() => {
  const date = now.value
  return `${date.getFullYear()} 年 ${date.getMonth() + 1}月 ${date.getDate()}日`
})

// 跟踪每个数字位的前一个值，用于触发翻页动画
const prevDigits = ref({
  hours: ['0', '0'],
  minutes: ['0', '0'],
  seconds: ['0', '0'],
})

// 跟踪哪些数字位需要翻页动画
const flipStates = ref({
  hours: [false, false],
  minutes: [false, false],
  seconds: [false, false],
})

// 存储每个数字位的旧值，用于翻页动画
const oldDigits = ref({
  hours: ['0', '0'],
  minutes: ['0', '0'],
  seconds: ['0', '0'],
})

const isInitialized = ref(false)

watch(
  timeDigits,
  (newDigits) => {
    // 首次初始化时不触发动画
    if (!isInitialized.value) {
      prevDigits.value = { ...newDigits }
      oldDigits.value = { ...newDigits }
      isInitialized.value = true
      return
    }

    // 检查小时
    if (newDigits.hours[0] !== prevDigits.value.hours[0]) {
      oldDigits.value.hours[0] = prevDigits.value.hours[0]
      flipStates.value.hours[0] = true
      setTimeout(() => {
        flipStates.value.hours[0] = false
        oldDigits.value.hours[0] = newDigits.hours[0]
      }, 600)
    }
    if (newDigits.hours[1] !== prevDigits.value.hours[1]) {
      oldDigits.value.hours[1] = prevDigits.value.hours[1]
      flipStates.value.hours[1] = true
      setTimeout(() => {
        flipStates.value.hours[1] = false
        oldDigits.value.hours[1] = newDigits.hours[1]
      }, 600)
    }
    // 检查分钟
    if (newDigits.minutes[0] !== prevDigits.value.minutes[0]) {
      oldDigits.value.minutes[0] = prevDigits.value.minutes[0]
      flipStates.value.minutes[0] = true
      setTimeout(() => {
        flipStates.value.minutes[0] = false
        oldDigits.value.minutes[0] = newDigits.minutes[0]
      }, 600)
    }
    if (newDigits.minutes[1] !== prevDigits.value.minutes[1]) {
      oldDigits.value.minutes[1] = prevDigits.value.minutes[1]
      flipStates.value.minutes[1] = true
      setTimeout(() => {
        flipStates.value.minutes[1] = false
        oldDigits.value.minutes[1] = newDigits.minutes[1]
      }, 600)
    }
    // 检查秒
    if (newDigits.seconds[0] !== prevDigits.value.seconds[0]) {
      oldDigits.value.seconds[0] = prevDigits.value.seconds[0]
      flipStates.value.seconds[0] = true
      setTimeout(() => {
        flipStates.value.seconds[0] = false
        oldDigits.value.seconds[0] = newDigits.seconds[0]
      }, 600)
    }
    if (newDigits.seconds[1] !== prevDigits.value.seconds[1]) {
      oldDigits.value.seconds[1] = prevDigits.value.seconds[1]
      flipStates.value.seconds[1] = true
      setTimeout(() => {
        flipStates.value.seconds[1] = false
        oldDigits.value.seconds[1] = newDigits.seconds[1]
      }, 600)
    }
    prevDigits.value = { ...newDigits }
  },
  { immediate: true }
)

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

const setBackground = (color: 'white' | 'black') => {
  backgroundColor.value = color
}

const setHourMode = (mode: '12' | '24') => {
  is24Hour.value = mode === '24'
}

const handleFullscreenChange = () => {
  isFullscreen.value = document.fullscreenElement === clockRef.value
}

onMounted(() => {
  startClock()
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  stopClock()
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<template>
  <div ref="clockRef" class="clock-container" :class="`bg-${backgroundColor}`">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>
    <div v-if="!isFullscreen" class="clock-controls">
      <div class="control-group">
        <span class="label">时制</span>
        <el-button-group>
          <el-button :type="is24Hour ? 'primary' : 'default'" @click="setHourMode('24')">24H</el-button>
          <el-button :type="!is24Hour ? 'primary' : 'default'" @click="setHourMode('12')">12H</el-button>
        </el-button-group>
      </div>
      <div class="control-group">
        <span class="label">底色</span>
        <el-button-group>
          <el-button :type="backgroundColor === 'black' ? 'primary' : 'default'" @click="setBackground('black')">
            黑底
          </el-button>
          <el-button :type="backgroundColor === 'white' ? 'primary' : 'default'" @click="setBackground('white')">
            白底
          </el-button>
        </el-button-group>
      </div>
      <div class="control-group">
        <span class="label">显示</span>
        <el-switch v-model="showSeconds" active-text="秒" />
        <el-button circle :type="isFullscreen ? 'primary' : 'default'" :icon="FullScreen" @click="toggleFullscreen" />
      </div>
    </div>
    <div class="clock-display">
      <div class="flip-clock">
        <!-- 小时 -->
        <div class="flip-group">
          <div class="flip-card" :class="[`theme-${backgroundColor}`, { flipping: flipStates.hours[0] }]">
            <div class="flip-card-top">
              <div class="flip-card-top-front">{{ timeDigits.hours[0] }}</div>
              <div class="flip-card-top-back">{{ timeDigits.hours[0] }}</div>
            </div>
            <div class="flip-card-bottom">
              <div class="flip-card-bottom-front">{{ timeDigits.hours[0] }}</div>
              <div class="flip-card-bottom-back">{{ timeDigits.hours[0] }}</div>
            </div>
          </div>
          <div class="flip-card" :class="[`theme-${backgroundColor}`, { flipping: flipStates.hours[1] }]">
            <div class="flip-card-top">
              <div class="flip-card-top-front">{{ timeDigits.hours[1] }}</div>
              <div class="flip-card-top-back">{{ timeDigits.hours[1] }}</div>
            </div>
            <div class="flip-card-bottom">
              <div class="flip-card-bottom-front">{{ timeDigits.hours[1] }}</div>
              <div class="flip-card-bottom-back">{{ timeDigits.hours[1] }}</div>
            </div>
          </div>
        </div>
        <div class="group-gap"></div>
        <!-- 分钟 -->
        <div class="flip-group">
          <div class="flip-card" :class="[`theme-${backgroundColor}`, { flipping: flipStates.minutes[0] }]">
            <div class="flip-card-top">
              <div class="flip-card-top-front">{{ timeDigits.minutes[0] }}</div>
              <div class="flip-card-top-back">{{ timeDigits.minutes[0] }}</div>
            </div>
            <div class="flip-card-bottom">
              <div class="flip-card-bottom-front">{{ timeDigits.minutes[0] }}</div>
              <div class="flip-card-bottom-back">{{ timeDigits.minutes[0] }}</div>
            </div>
          </div>
          <div class="flip-card" :class="[`theme-${backgroundColor}`, { flipping: flipStates.minutes[1] }]">
            <div class="flip-card-top">
              <div class="flip-card-top-front">{{ timeDigits.minutes[1] }}</div>
              <div class="flip-card-top-back">{{ timeDigits.minutes[1] }}</div>
            </div>
            <div class="flip-card-bottom">
              <div class="flip-card-bottom-front">{{ timeDigits.minutes[1] }}</div>
              <div class="flip-card-bottom-back">{{ timeDigits.minutes[1] }}</div>
            </div>
          </div>
        </div>
        <div v-if="showSeconds" class="group-gap"></div>
        <!-- 秒 -->
        <div v-if="showSeconds" class="flip-group">
          <div class="flip-card" :class="[`theme-${backgroundColor}`, { flipping: flipStates.seconds[0] }]">
            <div class="flip-card-top">
              <div class="flip-card-top-front">{{ timeDigits.seconds[0] }}</div>
              <div class="flip-card-top-back">{{ timeDigits.seconds[0] }}</div>
            </div>
            <div class="flip-card-bottom">
              <div class="flip-card-bottom-front">{{ timeDigits.seconds[0] }}</div>
              <div class="flip-card-bottom-back">{{ timeDigits.seconds[0] }}</div>
            </div>
          </div>
          <div class="flip-card" :class="[`theme-${backgroundColor}`, { flipping: flipStates.seconds[1] }]">
            <div class="flip-card-top">
              <div class="flip-card-top-front">{{ timeDigits.seconds[1] }}</div>
              <div class="flip-card-top-back">{{ timeDigits.seconds[1] }}</div>
            </div>
            <div class="flip-card-bottom">
              <div class="flip-card-bottom-front">{{ timeDigits.seconds[1] }}</div>
              <div class="flip-card-bottom-back">{{ timeDigits.seconds[1] }}</div>
            </div>
          </div>
        </div>
        <!-- 12小时制显示AM/PM -->
        <div v-if="!is24Hour" class="meridiem">{{ meridiem }}</div>
      </div>
      <div class="date" :key="formattedDate">{{ formattedDate }}</div>
    </div>
  </div>
</template>

<style scoped>
.clock-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 100vh;
  padding: 16px;
  border-radius: 24px;
  transition: background-color 0.3s ease;
}

.clock-container.bg-white {
  background: #ffffff;
}

.clock-container.bg-black {
  background: #000000;
}

.clock-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  padding: 16px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  margin: 0 0 0 auto;
  max-width: fit-content;
  backdrop-filter: blur(6px);
}

.bg-white .clock-controls {
  background: rgba(255, 255, 255, 0.5);
}

.clock-controls .label {
  font-size: 13px;
  margin-right: 6px;
  opacity: 0.9;
  font-weight: 500;
}

.bg-white .clock-controls .label {
  color: #333;
}

.bg-black .clock-controls .label {
  color: #fff;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.clock-display {
  flex: 1;
  width: 100%;
  border-radius: 16px;
  padding: 32px 16px 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.flip-clock {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.flip-group {
  display: flex;
  gap: 12px;
}

.group-gap {
  width: clamp(16px, 3vw, 40px);
  height: 1px;
}

.meridiem {
  font-size: clamp(24px, 4vw, 48px);
  font-weight: 600;
  margin-left: 16px;
  color: #666;
  line-height: 1;
}

.bg-black .meridiem {
  color: #ccc;
}

.date {
  margin-top: 32px;
  font-size: clamp(22px, 3.6vw, 36px);
  color: #d9d9d9;
  font-weight: 600;
  width: 100%;
  text-align: center;
  display: block;
}

/* 翻页卡片样式 */
.flip-card {
  position: relative;
  width: clamp(70px, 12vw, 140px);
  height: clamp(100px, 18vw, 180px);
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
  perspective: 1000px;
}

.flip-card-top,
.flip-card-bottom {
  position: absolute;
  left: 0;
  width: 100%;
  height: 50%;
  overflow: hidden;
  transform-style: preserve-3d;
  z-index: 1;
}

.flip-card-top {
  top: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px 10px 0 0;
  transform-origin: bottom center;
}

.flip-card-bottom {
  bottom: 0;
  border-radius: 0 0 10px 10px;
  transform-origin: top center;
  z-index: 2;
}

/* 仅保留一份时间显示，关闭翻页下半部分 */
.flip-card-top {
  height: 100%;
  border-bottom: 0;
}

.flip-card-bottom {
  display: none;
}

.flip-card-top-back,
.flip-card-bottom-front,
.flip-card-bottom-back {
  display: none !important;
}

.flip-card-top-front,
.flip-card-top-back,
.flip-card-bottom-front,
.flip-card-bottom-back {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: clamp(48px, 8vw, 96px);
  font-weight: 800;
  font-family: 'Roboto Mono', 'Courier New', monospace;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.flip-card-top-front,
.flip-card-top-back,
.flip-card-bottom-front,
.flip-card-bottom-back {
  align-items: center;
  padding: 0;
}

.flip-card-top-front {
  transform: rotateX(0deg);
}

.flip-card-top-back {
  transform: rotateX(180deg);
}

.flip-card-bottom-front {
  transform: rotateX(0deg);
}

.flip-card-bottom-back {
  transform: rotateX(180deg);
}

/* 翻页动画 */
.flip-card.flipping .flip-card-top {
  animation: flip-top 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.flip-card.flipping .flip-card-bottom {
  animation: flip-bottom 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.flip-card:not(.flipping) .flip-card-top {
  transform: rotateX(0deg);
}

.flip-card:not(.flipping) .flip-card-bottom {
  transform: rotateX(0deg);
}

/* 非翻页时只展示当前数字，避免上下两层同时可见 */
.flip-card:not(.flipping) .flip-card-top-back,
.flip-card:not(.flipping) .flip-card-bottom-back {
  display: none;
}

@keyframes flip-top {
  0% {
    transform: rotateX(0deg);
  }
  100% {
    transform: rotateX(-180deg);
  }
}

@keyframes flip-bottom {
  0% {
    transform: rotateX(0deg);
  }
  100% {
    transform: rotateX(180deg);
  }
}

/* 白底主题 */
.flip-card.theme-white {
  background: #ffffff;
}

.flip-card.theme-white .flip-card-top,
.flip-card.theme-white .flip-card-bottom {
  background: #ffffff;
  color: #1a1a1a;
}

.flip-card.theme-white .flip-card-top {
  border-bottom-color: rgba(0, 0, 0, 0.1);
}

/* 黑底主题 */
.flip-card.theme-black {
  background: #0f0f0f;
}

.flip-card.theme-black .flip-card-top,
.flip-card.theme-black .flip-card-bottom {
  background: #0f0f0f;
  color: #d9d9d9;
}

.flip-card.theme-black .flip-card-top {
  border-bottom-color: rgba(255, 255, 255, 0.08);
}

/* 翻页动画时的阴影效果 */
.flip-card.flipping {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .clock-controls {
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .flip-clock {
    gap: 6px;
  }

  .flip-group {
    gap: 6px;
  }

  .group-gap {
    width: clamp(10px, 2vw, 24px);
  }
}
</style>
