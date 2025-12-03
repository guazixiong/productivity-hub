<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

type CalendarEntry = {
  raw: string
  display: string
  type: 'workday' | 'weekend' | 'holiday'
  reason?: string
}

const calculationMode = ref<'range' | 'duration'>('range')
const workdayStartDate = ref('')
const workdayEndDate = ref('')
const workdayDuration = ref(5)
const workdayResult = ref<{
  totalDays: number
  workdays: number
  workdayList: string[]
  calendar: CalendarEntry[]
  skippedHolidays: CalendarEntry[]
  endDate: string
  endDateDisplay: string
} | null>(null)

const holidays = [
  '2024-01-01', '2024-02-10', '2024-02-11', '2024-02-12', '2024-02-13', '2024-02-14', '2024-02-15', '2024-02-16', '2024-02-17',
  '2024-04-04', '2024-04-05', '2024-04-06',
  '2024-05-01', '2024-05-02', '2024-05-03', '2024-05-04', '2024-05-05',
  '2024-06-10',
  '2024-09-15', '2024-09-16', '2024-09-17',
  '2024-10-01', '2024-10-02', '2024-10-03', '2024-10-04', '2024-10-05', '2024-10-06', '2024-10-07',
]

const weekdaysCN = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const highlightMap: Record<CalendarEntry['type'], { label: string; tag: 'success' | 'danger' | 'info' }> = {
  workday: { label: '工作日', tag: 'success' },
  weekend: { label: '周末', tag: 'info' },
  holiday: { label: '节假日', tag: 'danger' },
}

const isWeekend = (date: Date): boolean => {
  const day = date.getDay()
  return day === 0 || day === 6
}

const formatDateStr = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatDisplayDate = (dateStr: string): string => {
  const [year, month, day] = dateStr.split('-').map((item) => Number(item))
  const date = new Date(year, month - 1, day)
  return `${year}年${String(month).padStart(2, '0')}月${String(day).padStart(2, '0')}日 ${weekdaysCN[date.getDay()]}`
}

const calendarTimeline = computed(() => workdayResult.value?.calendar ?? [])
const skippedHolidayList = computed(() => workdayResult.value?.skippedHolidays ?? [])

const classifyDate = (date: Date): CalendarEntry => {
  const raw = formatDateStr(date)
  const weekend = isWeekend(date)
  const holiday = holidays.includes(raw)

  return {
    raw,
    display: formatDisplayDate(raw),
    type: holiday ? 'holiday' : weekend ? 'weekend' : 'workday',
    reason: holiday ? '法定节假日' : weekend ? '周末自动跳过' : undefined,
  }
}

const calculateWorkdays = () => {
  if (!workdayStartDate.value) {
    ElMessage.warning('请选择开始日期')
    return
  }

  const start = new Date(workdayStartDate.value)
  const calendar: CalendarEntry[] = []
  const workdayList: string[] = []
  const skippedHolidays: CalendarEntry[] = []
  let end = new Date(start)

  if (calculationMode.value === 'range') {
    if (!workdayEndDate.value) {
      ElMessage.warning('请选择结束日期')
      return
    }

    end = new Date(workdayEndDate.value)

    if (start > end) {
      ElMessage.error('开始日期不能晚于结束日期')
      return
    }

    const current = new Date(start)
    while (current <= end) {
      const entry = classifyDate(current)
      calendar.push(entry)
      if (entry.type === 'workday') {
        workdayList.push(entry.raw)
      } else if (entry.type === 'holiday') {
        skippedHolidays.push(entry)
      }
      current.setDate(current.getDate() + 1)
    }
  } else {
    if (!workdayDuration.value || workdayDuration.value <= 0) {
      ElMessage.warning('请输入大于0的工作日天数')
      return
    }

    const target = Math.floor(workdayDuration.value)
    const current = new Date(start)

    while (workdayList.length < target) {
      const entry = classifyDate(current)
      calendar.push(entry)

      if (entry.type === 'workday') {
        workdayList.push(entry.raw)
      } else if (entry.type === 'holiday') {
        skippedHolidays.push(entry)
      }

      if (workdayList.length === target) {
        end = new Date(current)
        break
      }

      current.setDate(current.getDate() + 1)
    }

    workdayEndDate.value = formatDateStr(end)
  }

  workdayResult.value = {
    totalDays: calendar.length,
    workdays: workdayList.length,
    workdayList,
    calendar,
    skippedHolidays,
    endDate: formatDateStr(end),
    endDateDisplay: formatDisplayDate(formatDateStr(end)),
  }

  ElMessage.success('计算完成')
}
</script>

<template>
  <div class="workday-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>
    <el-card class="workday-inputs" shadow="hover">
      <div class="mode-switch">
        <span>计算方式</span>
        <el-radio-group v-model="calculationMode" class="mode-radio-group">
          <el-radio-button label="range">按日期区间</el-radio-button>
          <el-radio-button label="duration">按工作日天数</el-radio-button>
        </el-radio-group>
      </div>
      <div class="workday-input-group">
        <label>起始日期</label>
        <el-date-picker
          v-model="workdayStartDate"
          type="date"
          placeholder="请选择开始日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </div>
      <div v-if="calculationMode === 'range'" class="workday-input-group">
        <label>结束日期</label>
        <el-date-picker
          v-model="workdayEndDate"
          type="date"
          placeholder="请选择结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </div>
      <div v-else class="workday-input-group">
        <label>工作日天数</label>
        <el-input-number v-model="workdayDuration" :min="1" :max="365" />
      </div>
      <el-button type="primary" size="large" @click="calculateWorkdays" class="calculate-button">
        计算工作日
      </el-button>
    </el-card>
    <el-card v-if="workdayResult" class="workday-result-card" shadow="hover">
      <div class="workday-result">
        <div class="result-grid">
          <div class="result-panel summary-card">
            <div class="stat-group">
              <div class="stat">
                <div class="stat-label">总天数</div>
                <div class="stat-value">{{ workdayResult.totalDays }} 天</div>
              </div>
              <div class="stat">
                <div class="stat-label">工作日</div>
                <div class="stat-value primary">{{ workdayResult.workdays }} 天</div>
              </div>
            </div>
            <el-divider />
            <div class="stat-details">
              <div>
                <span>结束日期</span>
                <strong>{{ workdayResult.endDateDisplay }}</strong>
              </div>
              <div>
                <span>跳过节假日</span>
                <strong>{{ workdayResult.skippedHolidays.length }} 天</strong>
              </div>
            </div>
            <p class="stat-tip">已排除周末与法定节假日，并展示每个跳过的日期。</p>
          </div>
          <div class="result-panel list-card">
            <div class="workday-list-header">
              <h4>日期进度</h4>
              <span>{{ calendarTimeline.length }} 天</span>
            </div>
            <el-scrollbar class="workday-scroll">
              <el-timeline>
                <el-timeline-item
                  v-for="item in calendarTimeline"
                  :key="item.raw"
                  :timestamp="item.display"
                  :type="highlightMap[item.type].tag"
                >
                  <div class="calendar-badge" :class="`type-${item.type}`">
                    {{ highlightMap[item.type].label }}
                    <span v-if="item.reason"> · {{ item.reason }}</span>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </el-scrollbar>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.workday-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  margin-bottom: 8px;
}

.workday-inputs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  padding: 24px;
}

.mode-switch {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  margin-bottom: 8px;
}

.mode-radio-group {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.mode-radio-group :deep(.el-radio-button) {
  flex: 0 0 auto;
}

.mode-radio-group :deep(.el-radio-button__inner) {
  min-width: 120px;
  justify-content: center;
  padding: 6px 14px;
  font-size: 13px;
  border-radius: 999px;
}

.mode-switch span {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.workday-input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.workday-input-group label {
  font-size: 14px;
  color: #0f172a;
  font-weight: 600;
}

.calculate-button {
  width: 100%;
  margin-top: auto;
}

.workday-result-card {
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.workday-result-card :deep(.el-card__body) {
  padding: 24px;
}

.workday-result {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.result-panel {
  border: 1px solid rgba(15, 23, 42, 0.04);
  border-radius: 12px;
  background: #f8fafc;
  padding: 20px;
  height: 100%;
}

.summary-card,
.list-card {
  height: 100%;
}

.stat-group {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.stat-details {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.stat-details span {
  display: block;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.stat-details strong {
  font-size: 16px;
  color: #0f172a;
}

.stat {
  flex: 1;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
}

.stat-value.primary {
  color: #6366f1;
}

.stat-tip {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.workday-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.workday-scroll {
  max-height: 320px;
}

.calendar-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.calendar-badge span {
  font-weight: 400;
}

.calendar-badge.type-workday {
  background: rgba(34, 197, 94, 0.15);
  color: #15803d;
}

.calendar-badge.type-weekend {
  background: rgba(59, 130, 246, 0.15);
  color: #1d4ed8;
}

.calendar-badge.type-holiday {
  background: rgba(248, 113, 113, 0.18);
  color: #b91c1c;
}

.holiday-section {
  padding: 20px;
}

.holiday-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 768px) {
  .stat-group {
    flex-direction: column;
  }

  .stat-details {
    flex-direction: column;
  }
}
</style>

