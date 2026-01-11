<script setup lang="ts">
/**
 * 首页组件
 */
import { computed, markRaw, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useConfigStore } from '@/stores/config'
import { useNavigationStore } from '@/stores/navigation'
import { useDevice } from '@/composables/useDevice'
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

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

// 地理位置（默认郑州）
const location = ref<{ city?: string; province?: string; address?: string }>({
  city: '郑州',
  province: '河南省',
  address: '郑州市',
})
const loadingLocation = ref(false)
const userIp = ref<string | null>(null)

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

// 获取用户IP地址
const getUserIp = async (): Promise<string | null> => {
  if (userIp.value) {
    return userIp.value
  }
  
  try {
    // 尝试多个IP获取服务，提高成功率
    const ipServices = [
      'https://api.ipify.org?format=json',
      'https://api64.ipify.org?format=json',
      'https://ipapi.co/json/',
    ]
    
    for (const service of ipServices) {
      try {
        const controller = new AbortController()
        const timeoutId = setTimeout(() => controller.abort(), 3000)
        
        const response = await fetch(service, { 
          signal: controller.signal
        } as RequestInit)
        
        clearTimeout(timeoutId)
        
        if (response.ok) {
          const data = await response.json()
          const ip = data.ip || data.query
          if (ip) {
            userIp.value = ip
            return ip
          }
        }
      } catch (e) {
        // 继续尝试下一个服务
        continue
      }
    }
  } catch (error) {
    // 忽略获取IP错误
  }
  
  return null
}

// 获取位置和天气信息（统一使用后端接口）
// 优化：先快速获取数据（不等待定位），然后异步获取定位并刷新
const fetchWeather = async (forceRefresh = false, useLocation = false) => {
  loadingLocation.value = true
  loadingWeather.value = true
  try {
    let latitude: number | undefined
    let longitude: number | undefined
    let ip: string | null = null
    
    // 优先获取IP地址（用于天地图API）
    ip = await getUserIp()
    
    // 如果需要使用定位，尝试从浏览器获取当前位置的经纬度
    if (useLocation && navigator.geolocation) {
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
            { timeout: 3000, enableHighAccuracy: false }
          )
        })
      } catch {
        // 定位失败，使用默认值
      }
    }
    
    // 调用后端天气接口（后端会根据IP、经纬度自动获取位置和天气信息）
    // 优化：如果有具体的城市名（从之前的API调用中获取的真实数据），传递给后端，避免后端调用反向地理编码API
    const params: { latitude?: number; longitude?: number; cityName?: string; ip?: string } = {}
    
    // 优先使用IP地址（天地图API）
    if (ip) {
      params.ip = ip
    } else if (latitude !== undefined && longitude !== undefined) {
      // 其次使用经纬度
      params.latitude = latitude
      params.longitude = longitude
    } else {
      // 检查是否有真实的城市名（不是初始默认值）
      // 如果location是从API返回的真实数据，应该包含有效的city信息
      // 只有当city存在且不是默认的'郑州'时，才认为是真实数据
      const hasRealCity = location.value?.city && 
                         location.value.city.trim() !== '' && 
                         location.value.city !== '郑州'
      if (hasRealCity) {
        // 如果有已存储的具体城市名，传递给后端
        params.cityName = location.value.city
      }
      // 如果没有真实的城市名，不传cityName参数，让后端根据IP、经纬度或使用默认值处理
    }
    
    // 根据是否强制刷新选择不同的接口
    const weatherData = forceRefresh 
      ? await homeApi.refreshWeather(params)
      : await homeApi.getWeather(params)
    
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
    ElMessage.error('获取天气信息失败，请稍后重试')
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

// 刷新天气信息
const handleRefreshWeather = async () => {
  await fetchWeather(true, true) // 刷新时使用定位
  ElMessage.success('天气信息已刷新')
}

// 获取今天的日期字符串（格式：YYYY-MM-DD）
const getTodayDate = (): string => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 获取每日一签（使用后端接口，带缓存）
const fetchDailyFortune = async (forceRefresh = false) => {
  loadingFortune.value = true
  const cacheKey = 'phub:daily-quote'
  const cacheDateKey = 'phub:daily-quote:date'
  const today = getTodayDate()
  
  try {
    // 如果不是强制刷新，先检查本地缓存
    if (!forceRefresh) {
      const cachedDate = localStorage.getItem(cacheDateKey)
      const cachedData = localStorage.getItem(cacheKey)
      
      if (cachedDate === today && cachedData) {
        try {
          const dailyQuote = JSON.parse(cachedData)
          fortune.value = {
            name: '每日一言',
            description: dailyQuote.quote || '今日运势良好',
            advice: dailyQuote.from || '保持积极心态',
          }
          loadingFortune.value = false
          return
        } catch (e) {
          // 缓存解析失败，继续调用API
          console.warn('解析每日一签缓存失败', e)
        }
      }
    }
    
    // 根据是否强制刷新选择不同的接口
    const dailyQuote = forceRefresh 
      ? await homeApi.refreshDailyQuote()
      : await homeApi.getDailyQuote()
    
    if (dailyQuote) {
      // 保存到本地缓存
      localStorage.setItem(cacheKey, JSON.stringify(dailyQuote))
      localStorage.setItem(cacheDateKey, today)
      
      // 后端返回的是DailyQuote格式，需要转换为FortuneData格式
      fortune.value = {
        name: '每日一言',
        description: dailyQuote.quote || '今日运势良好',
        advice: dailyQuote.from || '保持积极心态',
      }
    } else {
      // 如果API失败，尝试使用缓存
      const cachedDate = localStorage.getItem(cacheDateKey)
      const cachedData = localStorage.getItem(cacheKey)
      
      if (cachedDate === today && cachedData) {
        try {
          const cachedQuote = JSON.parse(cachedData)
          fortune.value = {
            name: '每日一言',
            description: cachedQuote.quote || '今日运势良好',
            advice: cachedQuote.from || '保持积极心态',
          }
          return
        } catch (e) {
          console.warn('使用缓存失败', e)
        }
      }
      
      // 使用默认值
      fortune.value = {
        name: '每日一言',
        description: '今日运势良好',
        advice: '保持积极心态',
      }
    }
  } catch (error) {
    console.error('获取每日一签失败', error)
    
    // 如果API失败，尝试使用缓存
    const cachedDate = localStorage.getItem(cacheDateKey)
    const cachedData = localStorage.getItem(cacheKey)
    
    if (cachedDate === today && cachedData) {
      try {
        const cachedQuote = JSON.parse(cachedData)
        fortune.value = {
          name: '每日一言',
          description: cachedQuote.quote || '今日运势良好',
          advice: cachedQuote.from || '保持积极心态',
        }
        loadingFortune.value = false
        return
      } catch (e) {
        console.warn('使用缓存失败', e)
      }
    }
    
    // 如果缓存也不可用，显示错误并使用默认值
    ElMessage.error('获取每日一签失败，请稍后重试')
    fortune.value = {
      name: '每日一言',
      description: '今日运势良好',
      advice: '保持积极心态',
    }
  } finally {
    loadingFortune.value = false
  }
}

// 刷新每日一签
const handleRefreshFortune = async () => {
  await fetchDailyFortune(true)
  ElMessage.success('每日一签已刷新')
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
  // 立即初始化UI，不等待任何异步操作
  calculateCountdown()
  calculateLunchCountdown()
  updateSalaryDates()
  
  // 先设置默认工具，确保页面有内容显示
  const defaultTools = toolList.slice(0, MAX_QUICK_TOOLS).map(tool => ({
    ...tool,
    icon: markRaw(tool.icon)
  }))
  quickTools.value = ensureQuickToolPresence(defaultTools)
  
  // 并行加载所有数据，不阻塞渲染
  // 1. 配置加载（不阻塞）
  configStore.fetchConfigs().then(() => {
    getOffWorkTime()
    getLunchBreakTime()
    getSalaryPayDay()
  }).catch(() => {
    // 配置加载失败不影响页面显示
  })
  
  // 2. 并行加载天气和每日一签（先快速获取，不等待定位）
  Promise.all([
    fetchWeather(false, false), // 不等待定位，快速获取
    fetchDailyFortune()
  ]).catch(() => {
    // 错误已在各自函数中处理
  })
  
  // 3. 异步加载热门工具
  void loadHotToolStats()
  
  // 4. 异步获取定位并刷新天气（不阻塞）
  if (navigator.geolocation) {
    // 延迟一点再获取定位，避免影响首次渲染
    setTimeout(() => {
      void fetchWeather(false, true).catch(() => {
        // 定位失败不影响已显示的数据
      })
    }, 100)
  }
  
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
              <div class="card-header-left">
                <el-icon><Location /></el-icon>
                <span>当前位置 & 天气</span>
              </div>
              <el-button
                class="refresh-btn"
                :icon="RefreshRight"
                circle
                size="small"
                :loading="loadingWeather || loadingLocation"
                @click="handleRefreshWeather"
              />
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
  gap: 24px;
  padding: 0;
  background: transparent;
}

.info-cards {
  display: flex;
  gap: 20px;
  align-items: stretch;
  flex-wrap: nowrap;
}

.info-left-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 0 0 360px;
  min-width: 320px;
}

.combined-countdown-card {
  flex: 1 1 auto;
}

/* 平板端适配 - REQ-001-02 */
@media (max-width: 1200px) {
  .info-cards {
    flex-direction: column;
  }

  .info-left-column {
    flex: 1 1 auto;
  }
}

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .info-cards {
    flex-direction: column;
    gap: 16px; /* 移动端间距缩放 */
  }

  .info-left-column {
    flex: 1 1 auto;
    min-width: 0;
    gap: 16px; /* 移动端间距缩放 */
  }

  .combined-countdown-card {
    min-width: 0;
  }
}

.info-card {
  border-radius: 16px;
  border: 1px solid var(--ph-border-subtle);
  transition: border-color 0.15s ease, background-color 0.15s ease, transform 0.08s ease-out, box-shadow 0.15s ease;
  background: var(--surface-color);
  box-shadow: var(--surface-shadow);
  position: relative;
  overflow: hidden;
}

.info-card:hover {
  transform: translateY(-1px);
  box-shadow: var(--surface-shadow-hover);
  border-color: rgba(37, 99, 235, 0.7);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-weight: 600;
  color: var(--text-primary);
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
  color: var(--text-tertiary);
  transition: color 0.15s ease, background-color 0.15s ease, transform 0.08s ease-out;
}

.refresh-btn:hover {
  color: #bfdbfe;
  background: rgba(37, 99, 235, 0.16);
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
  color: var(--text-primary);
}

.location-sub {
  font-size: 16px;
  color: #475569;
}

.location-detail {
  font-size: 14px;
  color: var(--text-tertiary);
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

/* 移除了多余的内部背景层，使用统一的卡片背景 */

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
  color: var(--text-primary);
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 12px;
  line-height: 1.3;
}

.location-province {
  font-size: 15px;
  color: var(--text-tertiary);
  font-weight: 500;
}

.location-detail {
  font-size: 13px;
  color: var(--text-tertiary);
  line-height: 1.5;
  margin-top: 4px;
}

.sub-label {
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-tertiary);
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
  border-top: 1px solid rgba(226, 232, 240, 0.9);
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
}

.weather-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.weather-temp {
  font-size: 40px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: -1px;
}

.weather-desc {
  font-size: 15px;
  color: var(--text-secondary);
  font-weight: 500;
  margin-top: 2px;
}

.weather-details {
  display: flex;
  flex-direction: row;
  gap: 24px;
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid rgba(226, 232, 240, 0.9);
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
  color: var(--text-tertiary);
  font-weight: 500;
  font-size: 11px;
  letter-spacing: 0.5px;
}

.weather-detail-value {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
}

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .location-weather-content {
    gap: 20px; /* 移动端间距缩放 */
  }

  .location-main {
    font-size: 24px; /* 移动端字体缩放 */
  }

  .location-province {
    font-size: 13px; /* 移动端字体缩放 */
  }

  .location-detail {
    font-size: 12px; /* 移动端字体缩放 */
  }

  .weather-block {
    padding-top: 16px; /* 移动端间距缩放 */
  }

  .weather-icon {
    font-size: 24px; /* 移动端字体缩放 */
  }

  .weather-temp {
    font-size: 32px; /* 移动端字体缩放 */
  }

  .weather-desc {
    font-size: 14px; /* 移动端字体缩放 */
  }

  .weather-details {
    flex-direction: column;
    gap: 10px; /* 移动端间距缩放 */
    padding-top: 12px; /* 移动端间距缩放 */
  }

  .weather-detail-label {
    font-size: 10px; /* 移动端字体缩放 */
  }

  .weather-detail-value {
    font-size: 12px; /* 移动端字体缩放 */
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
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  position: relative;
}

.fortune-description {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-secondary);
  text-align: center;
  font-style: italic;
}

.fortune-advice {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-tertiary);
  text-align: center;
  padding-top: 8px;
  border-top: 1px solid rgba(226, 232, 240, 0.9);
}

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .fortune-content {
    gap: 10px; /* 移动端间距缩放 */
    min-height: 80px;
    padding-top: 2px; /* 移动端间距缩放 */
  }

  .fortune-name {
    font-size: 24px; /* 移动端字体缩放 */
    padding: 10px 0; /* 移动端间距缩放 */
  }

  .fortune-description {
    font-size: 14px; /* 移动端字体缩放 */
    line-height: 1.6;
  }

  .fortune-advice {
    font-size: 12px; /* 移动端字体缩放 */
    padding-top: 6px; /* 移动端间距缩放 */
  }
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
  font-weight: 700;
  font-family: 'SF Mono', 'Monaco', 'Courier New', monospace;
  letter-spacing: 2px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.countdown-label {
  font-size: 14px;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.off-work-time {
  color: var(--primary-color);
  font-weight: 600;
  font-size: 16px;
}

/* 简化午休卡片样式 */

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
  color: #facc15;
}

.lunch-block .off-work-time {
  color: #facc15;
}

.salary-bubble {
  position: absolute;
  background: #ecfdf5;
  color: #047857;
  border-radius: 8px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
  border: 1px solid rgba(34, 197, 94, 0.6);
  display: inline-flex;
  align-items: center;
  gap: 4px;
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
  background: #fffbeb;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #9a3412;
  border: 1px solid #fed7aa;
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
  color: var(--text-secondary);
}

.salary-dates-title .el-icon {
  font-size: 16px;
  color: #fde68a;
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
  background: #fffbeb;
  border-radius: 8px;
  border: 1px solid rgba(251, 191, 36, 0.6);
  transition: border-color 0.15s ease, background-color 0.15s ease, transform 0.08s ease-out, box-shadow 0.15s ease;
}

.salary-date-item:hover {
  background: #fef3c7;
  border-color: #f59e0b;
  transform: translateX(1px);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.15);
}

.salary-date-item.is-today {
  background: #fef3c7;
  border-color: #f59e0b;
  box-shadow: 0 12px 36px rgba(15, 23, 42, 0.18);
}

.salary-date-display {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.salary-date-item.is-today .salary-date-display {
  color: #92400e;
  font-weight: 600;
}

.salary-date-days {
  font-size: 13px;
  color: var(--text-tertiary);
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
  background: #f59e0b;
  color: #fff;
  box-shadow: 0 2px 10px rgba(245, 158, 11, 0.6);
}

.salary-tomorrow-badge {
  background: rgba(15, 23, 42, 0.96);
  color: #facc15;
  border: 1px solid rgba(252, 211, 77, 0.35);
}

.salary-days-number {
  color: #facc15;
  font-weight: 700;
  font-size: 16px;
}

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .combined-countdown-content {
    flex-direction: column;
    gap: 16px; /* 移动端间距缩放 */
  }

  .countdown-block {
    gap: 6px; /* 移动端间距缩放 */
  }

  .countdown-title {
    font-size: 12px; /* 移动端字体缩放 */
  }

  .countdown-time {
    font-size: clamp(20px, 5vw, 32px); /* 移动端字体缩放 */
  }

  .countdown-label {
    font-size: 12px; /* 移动端字体缩放 */
  }

  .off-work-time {
    font-size: 14px; /* 移动端字体缩放 */
  }

  .weekend-overtime-tip {
    padding: 10px 16px; /* 移动端间距缩放 */
    font-size: 14px; /* 移动端字体缩放 */
    margin: 10px 0; /* 移动端间距缩放 */
  }
  
  .salary-date-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    padding: 8px 10px; /* 移动端间距缩放 */
  }

  .salary-date-display {
    font-size: 12px; /* 移动端字体缩放 */
  }
  
  .salary-date-days {
    align-self: flex-end;
    font-size: 11px; /* 移动端字体缩放 */
  }

  .salary-today-badge,
  .salary-tomorrow-badge {
    padding: 3px 8px; /* 移动端间距缩放 */
    font-size: 11px; /* 移动端字体缩放 */
  }

  .salary-days-number {
    font-size: 14px; /* 移动端字体缩放 */
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
  /* 固定为 4 列，配合 MAX_QUICK_TOOLS = 8，自然形成“上四下四”两行布局 */
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  border-radius: 12px;
  border: 1px solid rgba(203, 213, 225, 0.9);
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease, transform 0.08s ease-out, box-shadow 0.15s ease;
  background: rgba(248, 250, 252, 0.98);
  position: relative;
  overflow: hidden;
  min-height: 64px;
  height: 64px;
  box-sizing: border-box;
}

.tool-item:hover {
  background: #eff6ff;
  border-color: rgba(37, 99, 235, 0.7);
  transform: translateY(-1px);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.12);
}

.tool-icon {
  font-size: 28px;
  color: #2563eb;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tool-name {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 移动端适配 - REQ-001-02, REQ-001-03 */
@media (max-width: 768px) {
  .tools-grid {
    /* 移动端同样保持 4 列，上四下四的布局视觉一致 */
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px; /* 移动端间距缩放 */
  }

  .tool-item {
    gap: 10px; /* 移动端间距缩放 */
    padding: 12px 14px; /* 移动端间距缩放 */
    border-radius: 10px;
    min-height: 56px;
    height: 56px;
    /* 移动端禁用hover效果，改为点击激活 - REQ-001-03 */
    &:active {
      background: #eff6ff;
      border-color: rgba(37, 99, 235, 0.7);
      transform: scale(0.98);
    }
  }

  .tool-item:hover {
    /* 移动端禁用hover效果 */
    background: rgba(248, 250, 252, 0.98);
    border-color: rgba(203, 213, 225, 0.9);
    transform: none;
    box-shadow: none;
  }

  .tool-icon {
    font-size: 24px; /* 移动端字体缩放 */
  }

  .tool-name {
    font-size: 12px; /* 移动端字体缩放 */
  }
}

.tool-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border-radius: 8px;
  border: 1px dashed rgba(51, 65, 85, 0.95);
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease, color 0.15s ease;
  color: var(--text-tertiary);
}

.tool-empty:hover {
  border-color: rgba(148, 163, 184, 0.9);
  color: var(--text-secondary);
}

.tool-empty .el-icon {
  font-size: 32px;
}

.tool-empty-hint {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
  text-align: center;
}

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .home-container {
    gap: 16px; /* 移动端间距缩放 */
    padding: 0; /* 移动端移除额外padding */
  }

  .info-cards {
    flex-direction: column;
    gap: 16px; /* 移动端间距缩放 */
  }

  .info-card {
    border-radius: 12px; /* 移动端圆角调整 */
  }

  .card-header {
    font-size: 14px; /* 移动端字体缩放 */
  }

  .card-header .el-icon {
    font-size: 16px; /* 移动端字体缩放 */
  }

  .sub-label {
    font-size: 10px; /* 移动端字体缩放 */
  }

  .quick-tools-section {
    gap: 10px; /* 移动端间距缩放 */
    margin-top: 6px; /* 移动端间距缩放 */
  }

  .quick-tools-header {
    font-size: 14px; /* 移动端字体缩放 */
  }

  .tool-empty {
    padding: 16px; /* 移动端间距缩放 */
    font-size: 13px; /* 移动端字体缩放 */
  }

  .tool-empty .el-icon {
    font-size: 28px; /* 移动端字体缩放 */
  }

  .tool-empty-hint {
    font-size: 11px; /* 移动端字体缩放 */
  }
}
</style>

