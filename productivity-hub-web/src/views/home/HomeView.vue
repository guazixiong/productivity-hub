<script setup lang="ts">
import { computed, markRaw, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useConfigStore } from '@/stores/config'
import { useNavigationStore } from '@/stores/navigation'
import { ElMessage } from 'element-plus'
import { 
  Location, 
  Sunny, 
  Clock,
  Tools,
  Setting,
  MagicStick,
  Money,
  RefreshRight
} from '@element-plus/icons-vue'
import { toolApi, homeApi } from '@/services/api'
import type { ToolStat } from '@/types/tools'
import { toolList, toolMetaMap, type ToolMeta } from '@/data/tools'

const router = useRouter()
const configStore = useConfigStore()

// 地理位置（默认郑州）
const location = ref<{ city?: string; province?: string; address?: string }>({
  city: '郑州',
  province: '河南省',
  address: '郑州市',
})
const loadingLocation = ref(false)

// 天气信息（默认郑州）
const weather = ref<{ temp?: number; type?: string; desc?: string; wind?: string; humidity?: string }>({
  temp: 26,
  type: 'Clear',
  desc: '晴朗',
  wind: '未知',
  humidity: '未知',
})
const loadingWeather = ref(false)

// 每日一签
interface FortuneData {
  name: string
  description: string
  advice: string
}
const fortune = ref<FortuneData>({ name: '', description: '', advice: '' })
const loadingFortune = ref(false)

// 下班倒计时
const offWorkTime = ref('18:00')
const countdown = ref('')
const countdownInterval = ref<number | null>(null)
const isOvertime = ref(false)

// 午休倒计时
const lunchBreakTime = ref('11:30')
const lunchCountdown = ref('')
const showLunchCountdown = ref(true)

// 薪资发放工作日
interface SalaryDate {
  date: Date
  display: string
  daysLeft: number
  isToday: boolean
}
const salaryDates = ref<SalaryDate[]>([])
const salaryPayDay = ref<number>(15) // 薪资发放日（每月的第几天，默认15号）
const weekdaysCN = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// 中国法定节假日（2024-2025年）
const holidays = [
  // 2024年
  '2024-01-01', '2024-02-10', '2024-02-11', '2024-02-12', '2024-02-13', '2024-02-14', '2024-02-15', '2024-02-16', '2024-02-17',
  '2024-04-04', '2024-04-05', '2024-04-06',
  '2024-05-01', '2024-05-02', '2024-05-03', '2024-05-04', '2024-05-05',
  '2024-06-10',
  '2024-09-15', '2024-09-16', '2024-09-17',
  '2024-10-01', '2024-10-02', '2024-10-03', '2024-10-04', '2024-10-05', '2024-10-06', '2024-10-07',
  // 2025年
  '2025-01-01', '2025-01-28', '2025-01-29', '2025-01-30', '2025-01-31', '2025-02-01', '2025-02-02', '2025-02-03',
  '2025-04-04', '2025-04-05', '2025-04-06', '2025-04-07',
  '2025-05-01', '2025-05-02', '2025-05-03', '2025-05-04', '2025-05-05',
  '2025-05-31',
  '2025-10-01', '2025-10-02', '2025-10-03', '2025-10-04', '2025-10-05', '2025-10-06', '2025-10-07', '2025-10-08',
]

// 时间变化提示
const SALARY_BUBBLE_DISPLAY_DURATION = 1500
const SALARY_BUBBLE_FADE_DURATION = 320

const salaryBubbleVisible = ref(false)
const salaryBubbleTimer = ref<number | null>(null)
const salaryBubbleCooldownTimer = ref<number | null>(null)
const salaryBubbleLocked = ref(false)
const salaryBubbleText = '薪资 +1'

// 快捷工具列表（热门工具 Top8）
const MAX_QUICK_TOOLS = 8
const REQUIRED_QUICK_TOOL_IDS = ['blueprint']
const quickTools = ref<ToolMeta[]>([])
const toolStats = ref<ToolStat[]>([])
const quickToolsLoading = ref(false)

// 基于字符串动态生成颜色（确保相同名称总是得到相同颜色）
const getTagColor = (name: string): string => {
  // 使用柔和的灰色系 palette，更统一协调
  const colorPalette = [
    '#f1f5f9', // slate-100
    '#e2e8f0', // slate-200
    '#cbd5e1', // slate-300
    '#f8fafc', // slate-50
    '#e0e7ff', // indigo-100
    '#dbeafe', // blue-100
    '#e0f2fe', // cyan-100
    '#f0f9ff', // sky-100
    '#f5f3ff', // violet-100
    '#faf5ff', // purple-100
    '#fdf4ff', // fuchsia-100
    '#fef2f2', // red-100
  ]
  
  // 简单的字符串哈希函数
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  
  // 使用哈希值选择颜色
  const index = Math.abs(hash) % colorPalette.length
  return colorPalette[index]
}

const ensureQuickToolPresence = (tools: ToolMeta[]) => {
  const ensured = [...tools]
  const seen = new Set(ensured.map((tool) => tool.id))
  for (const id of REQUIRED_QUICK_TOOL_IDS) {
    if (!seen.has(id)) {
      const meta = toolMetaMap.get(id)
      if (meta) {
        // 标记图标组件为 raw，避免响应式警告
        ensured.unshift({ ...meta, icon: markRaw(meta.icon) })
        seen.add(id)
      }
    }
  }
  return ensured.slice(0, MAX_QUICK_TOOLS)
}

// 获取下班时间配置
const getOffWorkTime = () => {
  const config = configStore.configs.find(
    (c) => c.module === 'home' && c.key === 'offWorkTime'
  )
  if (config) {
    offWorkTime.value = config.value || '18:00'
  }
}

// 获取午休时间配置
const getLunchBreakTime = () => {
  const config = configStore.configs.find(
    (c) => c.module === 'home' && c.key === 'lunchBreakTime'
  )
  if (config) {
    lunchBreakTime.value = config.value || '12:00'
  }
}

// 获取薪资发放日配置
const getSalaryPayDay = () => {
  const config = configStore.configs.find(
    (c) => c.module === 'home' && c.key === 'salaryPayDay'
  )
  if (config) {
    const day = parseInt(config.value, 10)
    if (!isNaN(day) && day >= 1 && day <= 31) {
      salaryPayDay.value = day
    }
  }
  // 配置更新后重新计算薪资发放日期
  updateSalaryDates()
}

const updateQuickToolsFromStats = (stats: ToolStat[]) => {
  // 过滤出热门工具（点击量>0），并按点击量降序排序
  const hotStats = stats
    .filter((stat) => stat.clicks > 0)
    .sort((a, b) => b.clicks - a.clicks)
  
  const next: ToolMeta[] = []
  const usedIds = new Set<string>()
  
  // 先添加热门工具
  for (const stat of hotStats) {
    if (next.length >= MAX_QUICK_TOOLS) {
      break
    }
    const meta = toolMetaMap.get(stat.id)
    if (meta && !usedIds.has(meta.id)) {
      // 标记图标组件为 raw，避免响应式警告
      next.push({ ...meta, icon: markRaw(meta.icon) })
      usedIds.add(meta.id)
    }
  }
  
  // 如果热门工具不足，用默认工具补充
  if (next.length < MAX_QUICK_TOOLS) {
    for (const tool of toolList) {
    if (next.length >= MAX_QUICK_TOOLS) {
      break
    }
      if (!usedIds.has(tool.id)) {
        // 标记图标组件为 raw，避免响应式警告
        next.push({ ...tool, icon: markRaw(tool.icon) })
        usedIds.add(tool.id)
  }
    }
  }
  
  quickTools.value = ensureQuickToolPresence(next)
}

const loadHotToolStats = async () => {
  quickToolsLoading.value = true
  try {
    const stats = await toolApi.stats()
    toolStats.value = stats
    updateQuickToolsFromStats(stats)
  } catch (error) {
    ElMessage.error((error as Error)?.message ?? '热门工具加载失败')
    const fallbackTools = toolList.slice(0, MAX_QUICK_TOOLS).map(tool => ({
      ...tool,
      icon: markRaw(tool.icon)
    }))
    quickTools.value = ensureQuickToolPresence(fallbackTools)
  } finally {
    quickToolsLoading.value = false
  }
}

// 获取位置和天气信息（统一使用后端接口）
const fetchWeather = async () => {
  loadingLocation.value = true
  loadingWeather.value = true
  try {
    // 尝试从浏览器获取当前位置的经纬度
    let latitude: number | undefined
    let longitude: number | undefined
    
    if (navigator.geolocation) {
      try {
        await new Promise<void>((resolve) => {
          navigator.geolocation.getCurrentPosition(
            (position) => {
              latitude = position.coords.latitude
              longitude = position.coords.longitude
              resolve()
            },
            () => {
              // 定位失败，使用默认值（郑州）
              resolve()
            },
            { timeout: 5000, enableHighAccuracy: false }
          )
        })
      } catch {
        // 定位失败，使用默认值
      }
    }
    
    // 调用后端天气接口（后端会根据经纬度自动获取位置和天气信息）
    // 只传递有效的经纬度值，避免传递undefined
    const params: { latitude?: number; longitude?: number } = {}
    if (latitude !== undefined && longitude !== undefined) {
      params.latitude = latitude
      params.longitude = longitude
    }
    const weatherData = await homeApi.getWeather(params)
    
    if (weatherData) {
      // 更新位置信息
      location.value = {
        city: weatherData.city || '郑州',
        province: weatherData.province || '河南省',
        address: weatherData.address || weatherData.city || '郑州市',
      }
      
      // 更新天气信息
      weather.value = {
        temp: parseFloat(weatherData.temp) || 26,
        type: weatherData.weather || '未知',
        desc: weatherData.weather || '晴朗',
        wind: weatherData.wind || '未知',
        humidity: weatherData.humidity || '未知',
      }
    } else {
      // 如果API失败，使用默认值
      location.value = {
        city: '郑州',
        province: '河南省',
        address: '郑州市',
      }
      weather.value = {
        temp: 26,
        type: 'Clear',
        desc: '晴朗',
        wind: '未知',
        humidity: '未知',
      }
    }
  } catch (error) {
    console.error('获取位置和天气信息失败:', error)
    // 使用默认值
    location.value = {
      city: '郑州',
      province: '河南省',
      address: '郑州市',
    }
    weather.value = {
      temp: 26,
      type: 'Clear',
      desc: '晴朗',
      wind: '未知',
      humidity: '未知',
    }
  } finally {
    loadingLocation.value = false
    loadingWeather.value = false
  }
}

// 获取每日一签（带缓存，每天一换）
const fetchDailyFortune = async (forceRefresh = false) => {
  loadingFortune.value = true
  try {
    // 检查缓存
    const cacheKey = 'daily_fortune_cache'
    const cacheDateKey = 'daily_fortune_date'
    const today = new Date().toISOString().split('T')[0]
    const cachedDate = localStorage.getItem(cacheDateKey)
    const cachedFortune = localStorage.getItem(cacheKey)
    
    // 如果不强制刷新，且缓存存在且是今天的，直接使用
    if (!forceRefresh && cachedDate === today && cachedFortune) {
      try {
        fortune.value = JSON.parse(cachedFortune)
        loadingFortune.value = false
        return
      } catch {
        // 缓存解析失败，继续获取新的
      }
    }
    
    // 使用免费的API获取每日一签（带超时处理）
    const dateStr = today.replace(/-/g, '')
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000) // 5秒超时
    
    try {
      const response = await fetch(`https://api.vvhan.com/api/fortune?date=${dateStr}`, {
        signal: controller.signal,
      })
      clearTimeout(timeoutId)
      
      if (response.ok) {
        const data = await response.json()
        if (data.success && data.data) {
          const fortuneData = {
            name: data.data.name || '未知',
            description: data.data.description || '今日运势良好',
            advice: data.data.advice || '保持积极心态',
          }
          fortune.value = fortuneData
          // 保存到缓存
          localStorage.setItem(cacheKey, JSON.stringify(fortuneData))
          localStorage.setItem(cacheDateKey, today)
          return
        }
      }
    } catch (error) {
      clearTimeout(timeoutId)
      // 静默处理错误，不输出到控制台
      // 网络错误、超时等都会在这里被捕获
    }
    
    // API 失败时使用备用方案
    generateFallbackFortune()
  } catch (error) {
    // 静默处理所有错误，使用备用方案
    generateFallbackFortune()
  } finally {
    loadingFortune.value = false
  }
}

// 生成备用卦签（基于日期生成固定随机数，确保每天相同）
const generateFallbackFortune = () => {
  const fortunes = [
    { name: '乾卦', description: '天行健，君子以自强不息', advice: '今日宜积极进取，保持努力' },
    { name: '坤卦', description: '地势坤，君子以厚德载物', advice: '今日宜包容谦逊，以德服人' },
    { name: '震卦', description: '雷声震，君子以恐惧修省', advice: '今日宜谨慎行事，反省自身' },
    { name: '巽卦', description: '随风巽，君子以申命行事', advice: '今日宜顺势而为，灵活变通' },
    { name: '坎卦', description: '水洊至，君子以常德行', advice: '今日宜持之以恒，保持德行' },
    { name: '离卦', description: '明两作，君子以继明照于四方', advice: '今日宜光明正大，照亮他人' },
    { name: '艮卦', description: '兼山艮，君子以思不出其位', advice: '今日宜专注本职，脚踏实地' },
    { name: '兑卦', description: '丽泽兑，君子以朋友讲习', advice: '今日宜交流学习，共同进步' },
  ]
  // 基于日期生成固定随机数，确保每天相同
  const today = new Date().toISOString().split('T')[0]
  const dateHash = today.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
  const index = dateHash % fortunes.length
  const fortuneData = fortunes[index]
  fortune.value = fortuneData
  // 保存到缓存
  const cacheKey = 'daily_fortune_cache'
  const cacheDateKey = 'daily_fortune_date'
  localStorage.setItem(cacheKey, JSON.stringify(fortuneData))
  localStorage.setItem(cacheDateKey, today)
}

// 刷新每日一签
const handleRefreshFortune = async () => {
  await fetchDailyFortune(true)
}

// 格式化倒计时显示
const formatCountdown = (diff: number): string => {
  const hoursLeft = Math.floor(diff / (1000 * 60 * 60))
  const minutesLeft = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const secondsLeft = Math.floor((diff % (1000 * 60)) / 1000)

  const parts: string[] = []
  if (hoursLeft > 0) {
    parts.push(`${hoursLeft}时`)
  }
  if (minutesLeft > 0 || hoursLeft > 0) {
    parts.push(`${minutesLeft}分`)
  }
  parts.push(`${secondsLeft}秒`)
  
  return parts.join('')
}

// 计算下班倒计时（18:00 之后显示加班时长）
const calculateCountdown = () => {
  const now = new Date()
  const [hours, minutes] = offWorkTime.value.split(':').map(Number)
  const offWork = new Date()
  offWork.setHours(hours, minutes, 0, 0)

  if (now <= offWork) {
    // 正常下班倒计时
    isOvertime.value = false
    const diff = offWork.getTime() - now.getTime()
    countdown.value = formatCountdown(diff)
  } else {
    // 已经过下班时间，显示加班时长
    isOvertime.value = true
    const diff = now.getTime() - offWork.getTime()
    countdown.value = formatCountdown(diff)
  }
}

// 计算午休倒计时（超过午休时间后不再显示）
const calculateLunchCountdown = () => {
  const now = new Date()
  const [hours, minutes] = lunchBreakTime.value.split(':').map(Number)
  const lunchBreak = new Date()
  lunchBreak.setHours(hours, minutes, 0, 0)

  // 如果午休时间已过，当天不再显示
  if (now >= lunchBreak) {
    showLunchCountdown.value = false
    lunchCountdown.value = ''
    return
  }

  showLunchCountdown.value = true
  const diff = lunchBreak.getTime() - now.getTime()
  lunchCountdown.value = formatCountdown(diff)
}

// 格式化日期字符串
const formatDateStr = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 判断是否为周末
const isWeekend = (date: Date): boolean => {
  const day = date.getDay()
  return day === 0 || day === 6
}

// 判断今天是否是周末
const isTodayWeekend = computed(() => {
  return isWeekend(new Date())
})

// 判断是否为节假日
const isHoliday = (date: Date): boolean => {
  return holidays.includes(formatDateStr(date))
}

// 判断是否为工作日
const isWorkday = (date: Date): boolean => {
  return !isWeekend(date) && !isHoliday(date)
}

// 获取下一个工作日
const getNextWorkday = (date: Date): Date => {
  const next = new Date(date)
  next.setDate(next.getDate() + 1)
  while (!isWorkday(next)) {
    next.setDate(next.getDate() + 1)
  }
  return next
}

// 计算薪资发放日期（使用配置的日期，遇节假日顺延）
const calculateSalaryDate = (year: number, month: number): Date => {
  // 创建当月配置日期的日期
  const day = salaryPayDay.value
  const salaryDate = new Date(year, month - 1, day)
  
  // 如果配置日期不是工作日，顺延到下一个工作日
  if (!isWorkday(salaryDate)) {
    return getNextWorkday(salaryDate)
  }
  
  return salaryDate
}

// 格式化薪资发放日期显示
const formatSalaryDateDisplay = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const weekday = weekdaysCN[date.getDay()]
  return `${year}年${month}月${day}日 ${weekday}`
}

// 计算距离薪资发放日的天数
const calculateDaysUntilSalary = (salaryDate: Date): number => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(salaryDate.getFullYear(), salaryDate.getMonth(), salaryDate.getDate())
  const diff = target.getTime() - today.getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

// 更新薪资发放日期列表
const updateSalaryDates = () => {
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth() + 1
  
  const dates: SalaryDate[] = []
  
  // 计算当前月份和接下来2个月的薪资发放日期
  for (let i = 0; i < 3; i++) {
    let year = currentYear
    let month = currentMonth + i
    
    // 处理跨年
    if (month > 12) {
      year += Math.floor((month - 1) / 12)
      month = ((month - 1) % 12) + 1
    }
    
    const salaryDate = calculateSalaryDate(year, month)
    const daysLeft = calculateDaysUntilSalary(salaryDate)
    const today = new Date()
    const isToday = formatDateStr(today) === formatDateStr(salaryDate)
    
    // 只显示未来或今天的薪资发放日期
    if (daysLeft >= 0 || isToday) {
      dates.push({
        date: salaryDate,
        display: formatSalaryDateDisplay(salaryDate),
        daysLeft,
        isToday,
      })
      
      // 只显示最近的两个
      if (dates.length >= 2) {
        break
      }
    }
  }
  
  salaryDates.value = dates
}

// 时间变化提示
const triggerSalaryBubble = async () => {
  if (salaryBubbleLocked.value) return

  salaryBubbleLocked.value = true
  salaryBubbleVisible.value = false

  if (salaryBubbleTimer.value) {
    clearTimeout(salaryBubbleTimer.value)
    salaryBubbleTimer.value = null
  }
  if (salaryBubbleCooldownTimer.value) {
    clearTimeout(salaryBubbleCooldownTimer.value)
    salaryBubbleCooldownTimer.value = null
  }

  await nextTick()
  salaryBubbleVisible.value = true

  salaryBubbleTimer.value = window.setTimeout(() => {
    salaryBubbleVisible.value = false
    salaryBubbleTimer.value = null
    salaryBubbleCooldownTimer.value = window.setTimeout(() => {
      salaryBubbleLocked.value = false
      salaryBubbleCooldownTimer.value = null
    }, SALARY_BUBBLE_FADE_DURATION)
  }, SALARY_BUBBLE_DISPLAY_DURATION)
}

watch(countdown, (newVal, oldVal) => {
  if (!newVal || newVal === oldVal) return
  void triggerSalaryBubble()
})

watch(lunchCountdown, (newVal, oldVal) => {
  if (!newVal || newVal === oldVal) return
  void triggerSalaryBubble()
})

// 监听配置变化，更新薪资发放日
watch(() => configStore.configs, () => {
  getSalaryPayDay()
}, { deep: true })

// 天气图标（Element Plus可能没有专门的天气图标，统一使用Sunny）
const weatherIcon = computed(() => Sunny)

// 跳转到工具
const navigateToTool = (path: string) => {
  // 记录来源页面为首页
  const navigationStore = useNavigationStore()
  navigationStore.recordNavigation(path, '/home')
  router.push(path)
}

onMounted(async () => {
  // 配置加载失败不影响页面显示
  await configStore.fetchConfigs()
  getOffWorkTime()
  getLunchBreakTime()
  getSalaryPayDay()
  // 先设置默认工具，确保页面有内容显示
  const defaultTools = toolList.slice(0, MAX_QUICK_TOOLS).map(tool => ({
    ...tool,
    icon: markRaw(tool.icon)
  }))
  quickTools.value = ensureQuickToolPresence(defaultTools)
  // 然后加载热门工具并更新
  void loadHotToolStats()
  await fetchWeather()
  await fetchDailyFortune()
  calculateCountdown()
  calculateLunchCountdown()
  updateSalaryDates()
  
  // 每秒更新倒计时
  countdownInterval.value = window.setInterval(() => {
    calculateCountdown()
    calculateLunchCountdown()
  }, 1000)
  
  // 每天更新一次薪资发放日期
  const updateSalaryDatesInterval = window.setInterval(() => {
    updateSalaryDates()
  }, 24 * 60 * 60 * 1000) // 24小时更新一次
})

onUnmounted(() => {
  if (countdownInterval.value) {
    clearInterval(countdownInterval.value)
  }
  if (salaryBubbleTimer.value) {
    clearTimeout(salaryBubbleTimer.value)
  }
  if (salaryBubbleCooldownTimer.value) {
    clearTimeout(salaryBubbleCooldownTimer.value)
  }
})
</script>

<template>
  <div class="home-container">
    <!-- 顶部信息卡片 -->
    <div class="info-cards">
      <div class="info-left-column">
        <!-- 当前位置 & 天气综合卡片 -->
        <el-card class="info-card location-weather-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Location /></el-icon>
              <span>当前位置 & 天气</span>
            </div>
          </template>
          <el-skeleton :loading="loadingLocation || loadingWeather" animated>
            <template #default>
              <div class="location-weather-content">
                <!-- 左侧：位置 -->
                <div class="location-block">
                  <div class="sub-label">当前城市</div>
                  <div class="location-main">
                    {{ location.city || '郑州' }}
                    <span class="location-province">{{ location.province || '河南省' }}</span>
                  </div>
                  <div class="location-detail">{{ location.address || '郑州市' }}</div>
                </div>
                <!-- 右侧：天气 -->
                <div class="weather-block">
                  <div class="sub-label sub-label-right">实时天气</div>
                  <div class="weather-top">
                    <el-icon class="weather-icon">
                      <component :is="weatherIcon" />
                    </el-icon>
                    <div class="weather-temp">{{ weather.temp ?? 26 }}°C</div>
                  </div>
                  <div class="weather-desc">{{ weather.desc || '晴朗' }}</div>
                  <div class="weather-details">
                    <div class="weather-detail-item">
                      <span class="weather-detail-label">风力</span>
                      <span class="weather-detail-value">{{ weather.wind || '未知' }}</span>
                    </div>
                    <div class="weather-detail-item">
                      <span class="weather-detail-label">湿度</span>
                      <span class="weather-detail-value">{{ weather.humidity || '未知' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </el-skeleton>
        </el-card>

        <!-- 每日一签卡片 -->
        <el-card class="info-card fortune-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon><MagicStick /></el-icon>
                <span>每日一签</span>
              </div>
              <el-button
                class="refresh-btn"
                :icon="RefreshRight"
                circle
                size="small"
                :loading="loadingFortune"
                @click="handleRefreshFortune"
              />
            </div>
          </template>
          <el-skeleton :loading="loadingFortune" animated>
            <template #default>
              <div class="fortune-content">
                <div class="fortune-name">{{ fortune.name || '加载中...' }}</div>
                <div class="fortune-description">{{ fortune.description || '' }}</div>
                <div class="fortune-advice">{{ fortune.advice || '' }}</div>
              </div>
            </template>
          </el-skeleton>
        </el-card>
      </div>

      <!-- 午休 & 下班时间模块 -->
      <el-card class="info-card countdown-card combined-countdown-card" shadow="hover">
        <template #header>
          <div class="card-header countdown-header">
            <div class="card-header-left">
              <el-icon><Clock /></el-icon>
              <span>时间助手</span>
            </div>
            <transition name="salary-bubble">
              <div v-if="salaryBubbleVisible" class="salary-bubble">
                {{ salaryBubbleText }}
              </div>
            </transition>
          </div>
        </template>
        <!-- 周末加班提示 -->
        <div v-if="isTodayWeekend" class="weekend-overtime-tip">
          加班辛苦了 💪
        </div>
        <div class="combined-countdown-content">
          <!-- 午休倒计时（超过午休时间后不再显示） -->
          <div v-if="showLunchCountdown" class="countdown-block lunch-block">
            <div class="countdown-title">午休倒计时</div>
            <div class="countdown-content">
              <div class="countdown-time">{{ lunchCountdown || '计算中...' }}</div>
              <div class="countdown-label">
                距离 <span class="off-work-time">{{ lunchBreakTime }}</span> 还有
              </div>
            </div>
          </div>

          <!-- 下班倒计时 / 加班时长 -->
          <div class="countdown-block offwork-block">
            <div class="countdown-title">下班{{ isOvertime ? '加班' : '倒计时' }}</div>
            <div class="countdown-content">
              <div class="countdown-time">{{ countdown || '计算中...' }}</div>
              <div class="countdown-label">
                <template v-if="!isOvertime">
                  距离 <span class="off-work-time">{{ offWorkTime }}</span> 还有
                </template>
                <template v-else>
                  已加班 <span class="off-work-time">{{ countdown }}</span>
                </template>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 薪资发放日期 -->
        <div v-if="salaryDates.length > 0" class="salary-dates-section">
          <el-divider />
          <div class="salary-dates-content">
            <div class="salary-dates-title">
              <el-icon><Money /></el-icon>
              <span>薪资发放日</span>
            </div>
            <div class="salary-dates-list">
              <div
                v-for="(item, index) in salaryDates"
                :key="index"
                class="salary-date-item"
                :class="{ 'is-today': item.isToday }"
              >
                <div class="salary-date-display">{{ item.display }}</div>
                <div class="salary-date-days">
                  <template v-if="item.isToday">
                    <span class="salary-today-badge">今天</span>
                  </template>
                  <template v-else-if="item.daysLeft === 1">
                    <span class="salary-tomorrow-badge">明天</span>
                  </template>
                  <template v-else>
                    还有 <span class="salary-days-number">{{ item.daysLeft }}</span> 天
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷工具入口（迁移到时间助手卡片内） -->
        <div class="quick-tools-section">
          <el-divider />
          <div class="quick-tools-header">
            <div class="card-header-left">
              <el-icon><Tools /></el-icon>
              <span>快捷工具</span>
            </div>
          </div>
          <div v-if="quickToolsLoading">
            <el-skeleton :rows="2" animated />
          </div>
          <div v-else-if="quickTools.length" class="tools-grid">
            <div
              v-for="tool in quickTools"
              :key="tool.path"
              class="tool-item"
              @click="navigateToTool(tool.path)"
            >
              <el-icon class="tool-icon">
                <component :is="tool.icon" />
              </el-icon>
              <span class="tool-name">{{ tool.name }}</span>
            </div>
          </div>
          <div
            v-else
            class="tool-empty"
            @click="router.push('/tools')"
          >
            <el-icon><Setting /></el-icon>
            <span>暂无热门工具，前往工具广场看看</span>
            <div class="tool-empty-hint">当前暂无热门数据，请稍后再试</div>
          </div>
        </div>
      </el-card>
    </div>

  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  gap: 28px;
  min-height: 100vh;
  padding: 32px;
  /* 使用更优雅的渐变背景 */
  background: 
    radial-gradient(circle at 20% 50%, rgba(139, 92, 246, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(99, 102, 241, 0.06) 0%, transparent 50%),
    radial-gradient(circle at 40% 20%, rgba(236, 72, 153, 0.05) 0%, transparent 50%),
    linear-gradient(135deg, #f8fafc 0%, #f1f5f9 50%, #e0e7ff 100%);
  background-attachment: fixed;
}

.info-cards {
  display: flex;
  gap: 24px;
  align-items: stretch;
  flex-wrap: nowrap;
}

.info-left-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 0 0 360px;
  min-width: 320px;
}

.combined-countdown-card {
  flex: 1 1 auto;
}

@media (max-width: 1200px) {
  .info-cards {
    flex-direction: column;
  }

  .info-left-column {
    flex: 1 1 auto;
  }
}

.info-card {
  border-radius: 24px;
  border: 1px solid rgba(148, 163, 184, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 
    0 20px 50px rgba(15, 23, 42, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  position: relative;
  overflow: hidden;
}

.info-card::before {
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

.info-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 
    0 28px 60px rgba(99, 102, 241, 0.2),
    0 0 0 1px rgba(99, 102, 241, 0.1) inset;
  border-color: rgba(99, 102, 241, 0.3);
}

.info-card:hover::before {
  opacity: 1;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-weight: 600;
  color: #1e1b4b;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header .el-icon {
  font-size: 18px;
}

.refresh-btn {
  padding: 4px;
  border: none;
  background: transparent;
  color: #64748b;
  transition: all 0.2s ease;
}

.refresh-btn:hover {
  color: #6366f1;
  background: rgba(99, 102, 241, 0.1);
}

.refresh-btn:active {
  transform: rotate(180deg);
}

.location-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.location-main {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.location-sub {
  font-size: 16px;
  color: #475569;
}

.location-detail {
  font-size: 14px;
  color: #64748b;
}

.location-weather-card {
  position: relative;
  overflow: visible;
  flex: 0 0 auto;
  min-width: 0;
}

.fortune-card {
  flex: 0 0 280px;
  min-width: 0;
}

.combined-countdown-card {
  flex: 1 1 auto;
  min-width: 400px;
}

.location-weather-card,
.fortune-card,
.combined-countdown-card {
  /* 更精致的玻璃态效果 */
  background:
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.8), transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(224, 231, 255, 0.6), transparent 50%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.85) 100%);
}

.location-weather-content {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 28px;
  padding-top: 4px;
}

.location-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  padding-bottom: 4px;
}

.location-main {
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 12px;
  line-height: 1.3;
}

.location-province {
  font-size: 15px;
  color: #64748b;
  font-weight: 500;
}

.location-detail {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.5;
  margin-top: 4px;
}

.sub-label {
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #94a3b8;
  font-weight: 600;
  margin-bottom: 6px;
}

.sub-label-right {
  text-align: left;
}

.weather-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid rgba(148, 163, 184, 0.25);
  width: 100%;
}

.weather-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 2px;
}

.weather-icon {
  font-size: 28px;
  color: #f59e0b;
  filter: drop-shadow(0 2px 4px rgba(245, 158, 11, 0.2));
}

.weather-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.weather-temp {
  font-size: 40px;
  font-weight: 800;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 30%, #ec4899 60%, #f97316 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  letter-spacing: -1px;
  filter: drop-shadow(0 2px 4px rgba(99, 102, 241, 0.2));
}

.weather-desc {
  font-size: 15px;
  color: #475569;
  font-weight: 500;
  margin-top: 2px;
}

.weather-details {
  display: flex;
  flex-direction: row;
  gap: 24px;
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  width: 100%;
}

.weather-detail-item {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding: 2px 0;
  flex: 1;
  min-width: 0;
}

.weather-detail-label {
  color: #94a3b8;
  font-weight: 500;
  font-size: 11px;
  letter-spacing: 0.5px;
}

.weather-detail-value {
  color: #475569;
  font-weight: 600;
  font-size: 13px;
}

@media (max-width: 768px) {
  .location-weather-content {
    gap: 24px;
  }

  .location-main {
    font-size: 28px;
  }

  .weather-block {
    padding-top: 20px;
  }

  .weather-details {
    flex-direction: column;
    gap: 12px;
  }
}

.fortune-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 100px;
  padding-top: 4px;
}

.fortune-name {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-align: center;
  padding: 12px 0;
  border-bottom: 2px solid;
  border-image: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.4), transparent) 1;
  position: relative;
}

.fortune-name::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 2px;
}

.fortune-description {
  font-size: 16px;
  line-height: 1.8;
  color: #0f172a;
  text-align: center;
  font-style: italic;
}

.fortune-advice {
  font-size: 14px;
  line-height: 1.6;
  color: #64748b;
  text-align: center;
  padding-top: 8px;
  border-top: 1px solid rgba(99, 102, 241, 0.1);
}

.countdown-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  padding: 8px 0;
  min-width: 0;
  width: 100%;
}

.countdown-time {
  font-size: clamp(24px, 4.5vw, 40px);
  font-weight: 800;
  font-family: 'SF Mono', 'Monaco', 'Courier New', monospace;
  letter-spacing: 3px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 4px 12px rgba(99, 102, 241, 0.3));
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  animation: countdown-pulse 2s ease-in-out infinite;
}

@keyframes countdown-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.95;
    transform: scale(1.02);
  }
}

.countdown-label {
  font-size: 14px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.off-work-time {
  color: #6366f1;
  font-weight: 600;
  font-size: 16px;
}

.lunch-countdown-card {
  border-color: rgba(251, 146, 60, 0.2);
}

.lunch-countdown-card:hover {
  box-shadow: 0 12px 32px rgba(251, 146, 60, 0.15);
}

.lunch-countdown-card .countdown-time {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.lunch-countdown-card .off-work-time {
  color: #fb923c;
}

.combined-countdown-card {
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: visible;
  padding-top: 16px;
}

.combined-countdown-content {
  display: flex;
  flex-direction: row;
  gap: 24px;
  align-items: stretch;
  justify-content: space-between;
  min-width: 0;
}

.countdown-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.countdown-title {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.countdown-header {
  position: relative;
  min-height: 32px;
}

.countdown-header .salary-bubble {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

.lunch-block .countdown-time {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.lunch-block .off-work-time {
  color: #fb923c;
}

.salary-bubble {
  position: absolute;
  background: rgba(99, 102, 241, 0.18);
  color: #312e81;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 10px 25px rgba(79, 70, 229, 0.2);
  border: 1px solid rgba(99, 102, 241, 0.2);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.salary-bubble::after {
  content: '';
  position: absolute;
  bottom: -8px;
  right: 20px;
  width: 12px;
  height: 12px;
  background: inherit;
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-left: none;
  border-top: none;
  transform: rotate(45deg);
  box-shadow: inherit;
}

.salary-bubble-enter-active,
.salary-bubble-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.salary-bubble-enter-from,
.salary-bubble-leave-to {
  opacity: 0;
  transform: translateY(calc(-50% - 10px)) scale(0.95);
}

.weekend-overtime-tip {
  text-align: center;
  padding: 12px 20px;
  margin: 12px 0;
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.15) 0%, rgba(249, 115, 22, 0.12) 100%);
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #ea580c;
  border: 1px solid rgba(251, 146, 60, 0.25);
  box-shadow: 0 4px 12px rgba(251, 146, 60, 0.15);
  animation: weekend-tip-pulse 2s ease-in-out infinite;
}

@keyframes weekend-tip-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.9;
    transform: scale(1.02);
  }
}

.salary-dates-section {
  margin-top: 8px;
  padding-top: 8px;
}

.salary-dates-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.salary-dates-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.salary-dates-title .el-icon {
  font-size: 16px;
  color: #f59e0b;
}

.salary-dates-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.salary-date-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: rgba(245, 158, 11, 0.08);
  border-radius: 8px;
  border: 1px solid rgba(245, 158, 11, 0.2);
  transition: all 0.2s ease;
}

.salary-date-item:hover {
  background: rgba(245, 158, 11, 0.12);
  border-color: rgba(245, 158, 11, 0.3);
  transform: translateX(2px);
}

.salary-date-item.is-today {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.15), rgba(251, 146, 60, 0.12));
  border-color: rgba(245, 158, 11, 0.4);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.15);
}

.salary-date-display {
  font-size: 14px;
  color: #0f172a;
  font-weight: 500;
}

.salary-date-item.is-today .salary-date-display {
  color: #f59e0b;
  font-weight: 600;
}

.salary-date-days {
  font-size: 13px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.salary-today-badge,
.salary-tomorrow-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.salary-today-badge {
  background: linear-gradient(135deg, #f59e0b, #fb923c);
  color: #fff;
  box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
}

.salary-tomorrow-badge {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.salary-days-number {
  color: #f59e0b;
  font-weight: 700;
  font-size: 16px;
}

@media (max-width: 768px) {
  .combined-countdown-content {
    flex-direction: column;
  }
  
  .salary-date-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
  
  .salary-date-days {
    align-self: flex-end;
  }
}

.quick-tools-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}

.quick-tools-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.15);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.85) 100%);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.tool-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.1), transparent);
  transition: left 0.5s ease;
}

.tool-item:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(139, 92, 246, 0.1) 100%);
  border-color: #6366f1;
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.2);
}

.tool-item:hover::before {
  left: 100%;
}

.tool-icon {
  font-size: 28px;
  color: #6366f1;
}

.tool-name {
  font-size: 14px;
  color: #0f172a;
  font-weight: 500;
}

.tool-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border-radius: 12px;
  border: 1px dashed rgba(99, 102, 241, 0.3);
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
}

.tool-empty:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.tool-empty .el-icon {
  font-size: 32px;
}

.tool-empty-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  text-align: center;
}

@media (max-width: 768px) {
  .info-cards {
    flex-direction: column;
  }
}
</style>

