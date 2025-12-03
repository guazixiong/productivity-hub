<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
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
  Money
} from '@element-plus/icons-vue'
import { toolApi } from '@/services/api'
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
const weather = ref<{ temp?: number; type?: string; desc?: string }>({
  temp: 26,
  type: 'Clear',
  desc: '晴朗',
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
const salaryBubbleText = '薪资+1'

// 快捷工具列表（热门工具 Top5）
const quickTools = ref<ToolMeta[]>([])
const toolStats = ref<ToolStat[]>([])
const quickToolsLoading = ref(false)
const MAX_QUICK_TOOLS = 5
const REQUIRED_QUICK_TOOL_IDS = ['blueprint']

const ensureQuickToolPresence = (tools: ToolMeta[]) => {
  const ensured = [...tools]
  const seen = new Set(ensured.map((tool) => tool.id))
  for (const id of REQUIRED_QUICK_TOOL_IDS) {
    if (!seen.has(id)) {
      const meta = toolMetaMap.get(id)
      if (meta) {
        ensured.unshift(meta)
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

const updateQuickToolsFromStats = (stats: ToolStat[]) => {
  const next: ToolMeta[] = []
  for (const stat of stats) {
    const meta = toolMetaMap.get(stat.id)
    if (meta) {
      next.push(meta)
    }
    if (next.length >= MAX_QUICK_TOOLS) {
      break
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
    quickTools.value = ensureQuickToolPresence(toolList.slice(0, MAX_QUICK_TOOLS))
  } finally {
    quickToolsLoading.value = false
  }
}

// 获取地理位置
const fetchLocation = async () => {
  loadingLocation.value = true
  try {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const { latitude, longitude } = position.coords
          // 使用逆地理编码API获取地址（这里使用免费的API，实际项目中可能需要使用付费服务）
          try {
            const response = await fetch(
              `https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${latitude}&longitude=${longitude}&localityLanguage=zh`
            )
            if (response.ok) {
              const data = await response.json()
              location.value = {
                city: data.city || data.locality || data.principalSubdivision || location.value.city || '郑州',
                province: data.principalSubdivision || data.countryName || location.value.province || '河南省',
                address: data.localityInfo?.administrative?.[0]?.name || data.countryName || location.value.address || '郑州市',
              }
            } else {
              // 如果API失败，保持当前（默认郑州）地址
              location.value = {
                city: location.value.city || '郑州',
                province: location.value.province || '河南省',
                address: location.value.address || '郑州市',
              }
            }
          } catch (error) {
            // 如果API失败，保持当前（默认郑州）地址
            location.value = {
              city: location.value.city || '郑州',
              province: location.value.province || '河南省',
              address: location.value.address || '郑州市',
            }
          }
          loadingLocation.value = false
        },
        (error) => {
          // 定位失败，保留默认郑州地址
          location.value = {
            city: location.value.city || '郑州',
            province: location.value.province || '河南省',
            address: location.value.address || '郑州市',
          }
          loadingLocation.value = false
        },
        {
          timeout: 10000,
          enableHighAccuracy: false,
        }
      )
    } else {
      // 浏览器不支持定位，保留默认郑州地址
      location.value = {
        city: location.value.city || '郑州',
        province: location.value.province || '河南省',
        address: location.value.address || '郑州市',
      }
      loadingLocation.value = false
    }
  } catch (error) {
    // 发生异常时保留默认郑州地址
    location.value = {
      city: location.value.city || '郑州',
      province: location.value.province || '河南省',
      address: location.value.address || '郑州市',
    }
    loadingLocation.value = false
  }
}

// 通过IP获取位置（备用方案）
const fetchLocationByIP = async () => {
  try {
    const response = await fetch('https://api.ipapi.com/json/?access_key=free')
    if (response.ok) {
      const data = await response.json()
      location.value = {
        city: data.city || location.value.city || '郑州',
        province: data.region_name || data.country_name || location.value.province || '河南省',
        address: data.country_name || location.value.address || '郑州市',
      }
    } else {
      // 使用另一个免费IP定位API
      const response2 = await fetch('https://ipapi.co/json/')
      if (response2.ok) {
        const data = await response2.json()
        location.value = {
          city: data.city || location.value.city || '郑州',
          province: data.region || data.country_name || location.value.province || '河南省',
          address: data.country_name || location.value.address || '郑州市',
        }
      } else {
        // 保留默认郑州地址
        location.value = {
          city: location.value.city || '郑州',
          province: location.value.province || '河南省',
          address: location.value.address || '郑州市',
        }
      }
    }
  } catch {
    // 保留默认郑州地址
    location.value = {
      city: location.value.city || '郑州',
      province: location.value.province || '河南省',
      address: location.value.address || '郑州市',
    }
  } finally {
    loadingLocation.value = false
  }
}

// 获取天气信息
const fetchWeather = async () => {
  // 没有城市信息时默认使用郑州
  if (!location.value.city || location.value.city === '未知') {
    location.value.city = '郑州'
    location.value.province = location.value.province || '河南省'
  }
  loadingWeather.value = true
  try {
    // 这里使用免费的天气API，实际项目中可能需要使用付费服务
    const response = await fetch(
      `https://api.openweathermap.org/data/2.5/weather?q=${location.value.city}&appid=demo&units=metric&lang=zh_cn`
    )
    if (response.ok) {
      const data = await response.json()
      weather.value = {
        temp: Math.round(data.main.temp),
        type: data.weather[0].main,
        desc: data.weather[0].description,
      }
    } else {
      // 如果API失败，使用模拟数据
      weather.value = {
        temp: 36.5,
        type: 'Clear',
        desc: '晴朗',
      }
    }
  } catch {
    // 使用模拟数据
    weather.value = {
      temp: 36.5,
      type: 'Clear',
      desc: '晴朗',
    }
  } finally {
    loadingWeather.value = false
  }
}

// 获取每日一签（带缓存，每天一换）
const fetchDailyFortune = async () => {
  loadingFortune.value = true
  try {
    // 检查缓存
    const cacheKey = 'daily_fortune_cache'
    const cacheDateKey = 'daily_fortune_date'
    const today = new Date().toISOString().split('T')[0]
    const cachedDate = localStorage.getItem(cacheDateKey)
    const cachedFortune = localStorage.getItem(cacheKey)
    
    // 如果缓存存在且是今天的，直接使用
    if (cachedDate === today && cachedFortune) {
      try {
        fortune.value = JSON.parse(cachedFortune)
        loadingFortune.value = false
        return
      } catch {
        // 缓存解析失败，继续获取新的
      }
    }
    
    // 使用免费的API获取每日一签
    const dateStr = today.replace(/-/g, '')
    const response = await fetch(`https://api.vvhan.com/api/fortune?date=${dateStr}`)
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
      } else {
        // 备用卦签数据
        generateFallbackFortune()
      }
    } else {
      generateFallbackFortune()
    }
  } catch {
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

// 计算薪资发放日期（每月15号，遇节假日顺延）
const calculateSalaryDate = (year: number, month: number): Date => {
  // 创建当月15号的日期
  const salaryDate = new Date(year, month - 1, 15)
  
  // 如果15号不是工作日，顺延到下一个工作日
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
  await configStore.fetchConfigs()
  getOffWorkTime()
  getLunchBreakTime()
  void loadHotToolStats()
  await fetchLocation()
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
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-card>

      <!-- 每日一签卡片 -->
      <el-card class="info-card fortune-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><MagicStick /></el-icon>
            <span>每日一签</span>
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

      <!-- 午休 & 下班时间模块 -->
      <el-card class="info-card countdown-card combined-countdown-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Clock /></el-icon>
            <span>时间助手</span>
          </div>
        </template>
        <transition name="salary-bubble">
          <div v-if="salaryBubbleVisible" class="salary-bubble">
            {{ salaryBubbleText }}
          </div>
        </transition>
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
      </el-card>
    </div>

    <!-- 快捷工具入口 -->
    <el-card class="tools-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Tools /></el-icon>
          <span>快捷工具</span>
        </div>
      </template>
      <template v-if="quickToolsLoading">
        <el-skeleton :rows="2" animated />
      </template>
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
    </el-card>
  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 100vh;
  padding: 24px;
  /* 使用浅色渐变叠加背景图，让整体色调更贴近期望的全局浅色系 */
  background:
    linear-gradient(135deg, rgba(248, 250, 252, 0.95), rgba(236, 252, 203, 0.9)),
    url('@/assets/home-bg.jpg') center/cover fixed no-repeat;
}

.info-cards {
  display: flex;
  gap: 24px;
  align-items: stretch;
}

.info-card {
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(18px);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.12);
}

.info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(99, 102, 241, 0.22);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1e1b4b;
}

.card-header .el-icon {
  font-size: 18px;
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
  overflow: hidden;
  flex: 0 0 280px;
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
  /* 在玻璃上叠一层细腻高光，不破坏透视感 */
  background:
    radial-gradient(circle at top left, rgba(248, 250, 252, 0.65), transparent 60%),
    radial-gradient(circle at bottom right, rgba(191, 219, 254, 0.45), transparent 60%),
    rgba(255, 255, 255, 0.6);
}

.location-weather-content {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  justify-content: space-between;
  gap: 24px;
  padding-top: 4px;
}

.location-block {
  flex: 1.4;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.location-main {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
}

.location-province {
  font-size: 14px;
  color: #4b5563;
}

.location-detail {
  font-size: 14px;
  color: #6b7280;
}

.sub-label {
  font-size: 12px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #6b7280;
  margin-bottom: 4px;
}

.sub-label-right {
  text-align: right;
}

.weather-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 6px;
  padding-left: 16px;
  border-left: 1px dashed rgba(148, 163, 184, 0.6);
}

.weather-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.weather-icon {
  font-size: 24px;
  color: #f59e0b;
}

.weather-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.weather-temp {
  font-size: 32px;
  font-weight: 700;
  color: #6366f1;
  background: linear-gradient(135deg, #4f46e5 0%, #2563eb 40%, #f97316 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.weather-desc {
  font-size: 16px;
  color: #374151;
}

@media (max-width: 768px) {
  .location-weather-content {
    flex-direction: column;
    gap: 12px;
  }

  .weather-block {
    align-items: flex-start;
    border-left: none;
    border-top: 1px dashed rgba(148, 163, 184, 0.6);
    padding-left: 0;
    padding-top: 12px;
  }

  .sub-label-right {
    text-align: left;
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
  font-size: 24px;
  font-weight: 700;
  color: #4f46e5;
  text-align: center;
  padding: 8px 0;
  border-bottom: 2px solid rgba(129, 140, 248, 0.35);
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
  font-size: clamp(20px, 4vw, 32px);
  font-weight: 700;
  color: #6366f1;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
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
  top: 18px;
  right: 24px;
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
  transform: translateY(-10px) scale(0.95);
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

.tools-card {
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background:
    radial-gradient(circle at top left, rgba(56, 189, 248, 0.18), transparent 55%),
    radial-gradient(circle at bottom right, rgba(129, 140, 248, 0.16), transparent 55%),
    rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(18px);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.18);
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.tool-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  cursor: pointer;
  transition: all 0.2s ease;
  background: rgba(255, 255, 255, 0.8);
}

.tool-item:hover {
  background: rgba(99, 102, 241, 0.08);
  border-color: #6366f1;
  transform: translateY(-2px);
}

.tool-icon {
  font-size: 32px;
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
    grid-template-columns: 1fr;
  }

  .tools-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }
}
</style>

