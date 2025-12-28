<template>
  <div class="water-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">饮水记录</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              <span>新增记录</span>
            </el-button>
            <el-button @click="showTargetDialog = true">
              <el-icon><Setting /></el-icon>
              <span>设置目标</span>
            </el-button>
          </div>
        </div>
      </template>

      <!-- 今日进度卡片 -->
      <WaterProgressCard 
        v-if="todayProgress" 
        :key="`progress-${todayProgress.date}-${todayProgress.totalIntakeMl}-${todayProgress.intakeCount}`"
        :progress="todayProgress" 
      />

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :xs="24" :sm="12" :md="8">
          <StatisticsCard
            title="今日饮水量"
            :value="todayProgress?.totalIntakeMl ?? 0"
            unit="ml"
            color="#409EFF"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <StatisticsCard
            title="本周平均每日"
            :value="weekAverageDailyIntake"
            unit="ml"
            color="#67C23A"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <StatisticsCard
            title="本周完成天数"
            :value="stats?.achievementDays ?? 0"
            unit="天"
            color="#E6A23C"
          />
        </el-col>
      </el-row>

      <!-- 筛选工具栏 -->
      <div class="filter-toolbar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleDateRangeChange"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="records"
        v-loading="loading"
        :default-sort="{ prop: 'intakeDate', order: 'descending' }"
        style="width: 100%"
      >
        <el-table-column prop="intakeDate" label="日期" width="120" sortable />
        <el-table-column prop="intakeTime" label="时间" width="100" />
        <el-table-column prop="waterType" label="类型" width="120" />
        <el-table-column prop="volumeMl" label="饮水量(ml)" width="120" />
        <el-table-column prop="notes" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="formVisible"
      :title="currentRecord?.id ? '编辑饮水记录' : '新增饮水记录'"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="日期" prop="intakeDate">
          <el-date-picker
            v-model="form.intakeDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时间" prop="intakeTime">
          <el-time-picker
            v-model="form.intakeTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="类型" prop="waterType">
          <el-select v-model="form.waterType" placeholder="请选择" style="width: 100%">
            <el-option label="白开水" value="白开水" />
            <el-option label="矿泉水" value="矿泉水" />
            <el-option label="茶水" value="茶水" />
            <el-option label="咖啡" value="咖啡" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="饮水量(ml)" prop="volumeMl">
          <el-input-number
            v-model="form.volumeMl"
            :min="1"
            :max="5000"
            placeholder="请输入饮水量"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.notes"
            type="textarea"
            :rows="3"
            placeholder="可选"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFormSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 设置目标对话框 -->
    <el-dialog v-model="showTargetDialog" title="设置饮水目标" width="500px">
      <el-form ref="targetFormRef" :model="targetForm" :rules="targetRules" label-width="120px">
        <el-form-item label="每日目标(ml)" prop="dailyTargetMl">
          <el-input-number
            v-model="targetForm.dailyTargetMl"
            :min="500"
            :max="10000"
            placeholder="请输入每日目标饮水量"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="提醒时间">
          <div class="reminder-times">
            <el-tag
              v-for="(time, index) in targetForm.reminderIntervals"
              :key="index"
              closable
              @close="removeReminderTime(index)"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ time }}
            </el-tag>
            <el-time-picker
              v-model="reminderTimeInput"
              format="HH:mm"
              value-format="HH:mm"
              placeholder="选择提醒时间"
              @change="addReminderTime"
              style="width: 150px"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTargetDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTarget">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Setting } from '@element-plus/icons-vue'
import { healthApi } from '@/services/healthApi'
import type {
  WaterIntakeDTO,
  WaterIntakeQuery,
  WaterProgress,
  WaterTarget,
  WaterStatistics,
} from '@/types/health'
import StatisticsCard from '@/components/health/StatisticsCard.vue'
import WaterProgressCard from '@/components/health/WaterProgressCard.vue'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const records = ref<WaterIntakeDTO[]>([])
const stats = ref<WaterStatistics | null>(null)
const todayProgress = ref<WaterProgress | null>(null)
const waterTarget = ref<WaterTarget | null>(null)
const isMounted = ref(false)

const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const queryParams = reactive<WaterIntakeQuery>({
  startDate: undefined,
  endDate: undefined,
})

const dateRange = ref<[string, string] | null>(null)
const formVisible = ref(false)
const showTargetDialog = ref(false)
const formRef = ref<FormInstance>()
const targetFormRef = ref<FormInstance>()
const currentRecord = ref<Partial<WaterIntakeDTO> | undefined>(undefined)
const reminderTimeInput = ref<string>('')

const form = reactive<WaterIntakeDTO>({
  intakeDate: new Date().toISOString().split('T')[0],
  intakeTime: new Date().toTimeString().slice(0, 5),
  waterType: '白开水',
  volumeMl: 200,
  notes: '',
})

const targetForm = reactive<WaterTarget>({
  dailyTargetMl: 2000,
  reminderIntervals: [],
})

const rules: FormRules = {
  intakeDate: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  intakeTime: [
    { required: true, message: '请选择时间', trigger: 'change' }
  ],
  waterType: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  volumeMl: [
    { required: true, message: '请输入饮水量', trigger: 'blur' },
    { type: 'number', min: 1, max: 5000, message: '饮水量必须在1-5000ml之间', trigger: 'blur' }
  ],
}

const targetRules: FormRules = {
  dailyTargetMl: [
    { required: true, message: '请输入每日目标饮水量', trigger: 'blur' },
    { type: 'number', min: 500, max: 10000, message: '每日目标饮水量必须在500-10000ml之间', trigger: 'blur' }
  ],
}

// 计算本周平均每日饮水量（总饮水量/7天）
const weekAverageDailyIntake = computed(() => {
  if (!stats.value || !stats.value.totalIntakeMl) {
    return 0
  }
  // 本周是7天
  return Math.round(stats.value.totalIntakeMl / 7)
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    const data = await healthApi.listWaterIntakes(params)
    records.value = data.items || []
    total.value = data.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const data = await healthApi.getWaterStatistics({ period: 'week' })
    if (isMounted.value) {
      stats.value = data || {
        totalIntakeMl: 0,
        totalCount: 0,
        averageIntakeMl: 0,
        achievementDays: 0,
        typeStatistics: []
      }
    }
  } catch (error: any) {
    console.error('加载统计失败:', error)
    // 错误时设置为默认值，避免显示旧数据
    if (isMounted.value) {
      stats.value = {
        totalIntakeMl: 0,
        totalCount: 0,
        averageIntakeMl: 0,
        achievementDays: 0,
        typeStatistics: []
      }
    }
  }
}

const loadTodayProgress = async () => {
  try {
    const data = await healthApi.getWaterProgress()
    if (isMounted.value) {
      // 确保数据完整，如果后端返回的数据不完整，使用默认值
      if (data) {
        todayProgress.value = {
          date: data.date || new Date().toISOString().split('T')[0],
          dailyTargetMl: data.dailyTargetMl ?? (waterTarget.value?.dailyTargetMl || 2000),
          totalIntakeMl: data.totalIntakeMl ?? 0,
          remainingMl: data.remainingMl ?? (data.dailyTargetMl ?? (waterTarget.value?.dailyTargetMl || 2000)),
          progressPercent: data.progressPercent ?? 0,
          isAchieved: data.isAchieved ?? false,
          intakeCount: data.intakeCount ?? 0
        }
      } else {
        // 如果后端返回 null，使用默认值
        todayProgress.value = {
          date: new Date().toISOString().split('T')[0],
          dailyTargetMl: waterTarget.value?.dailyTargetMl || 2000,
          totalIntakeMl: 0,
          remainingMl: waterTarget.value?.dailyTargetMl || 2000,
          progressPercent: 0,
          isAchieved: false,
          intakeCount: 0
        }
      }
    }
  } catch (error: any) {
    console.error('加载今日进度失败:', error)
    // 错误时设置为默认值，但只在组件仍然挂载时
    if (isMounted.value) {
      todayProgress.value = {
        date: new Date().toISOString().split('T')[0],
        dailyTargetMl: waterTarget.value?.dailyTargetMl || 2000,
        totalIntakeMl: 0,
        remainingMl: waterTarget.value?.dailyTargetMl || 2000,
        progressPercent: 0,
        isAchieved: false,
        intakeCount: 0
      }
    }
  }
}

const loadWaterTarget = async () => {
  try {
    const data = await healthApi.getWaterTarget()
    if (isMounted.value) {
      waterTarget.value = data
      if (data) {
        targetForm.dailyTargetMl = data.dailyTargetMl
        targetForm.reminderIntervals = [...(data.reminderIntervals || [])]
      }
    }
  } catch (error: any) {
    console.error('加载饮水目标失败:', error)
  }
}

const handleDateRangeChange = (dates: [string, string] | null) => {
  if (dates) {
    queryParams.startDate = dates[0]
    queryParams.endDate = dates[1]
  } else {
    queryParams.startDate = undefined
    queryParams.endDate = undefined
  }
}

const handleReset = () => {
  queryParams.startDate = undefined
  queryParams.endDate = undefined
  dateRange.value = null
  pageNum.value = 1
  loadData()
}

const handleAdd = () => {
  currentRecord.value = undefined
  Object.assign(form, {
    intakeDate: new Date().toISOString().split('T')[0],
    intakeTime: new Date().toTimeString().slice(0, 5),
    waterType: '白开水',
    volumeMl: 200,
    notes: '',
  })
  formVisible.value = true
}

const handleEdit = (record: WaterIntakeDTO) => {
  currentRecord.value = { ...record }
  Object.assign(form, {
    intakeDate: record.intakeDate,
    intakeTime: record.intakeTime,
    waterType: record.waterType,
    volumeMl: record.volumeMl,
    notes: record.notes || '',
  })
  formVisible.value = true
}

const handleDelete = async (record: WaterIntakeDTO) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      type: 'warning',
    })
    await healthApi.deleteWaterIntake(record.id!)
    ElMessage.success('删除成功')
    // 刷新数据，确保统计数据更新
    await Promise.all([
      loadData(),
      loadTodayProgress(),
      loadStats()
    ])
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleFormSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (currentRecord.value?.id) {
          await healthApi.updateWaterIntake(currentRecord.value.id, form)
          ElMessage.success('更新成功')
        } else {
          await healthApi.createWaterIntake(form)
          ElMessage.success('创建成功')
        }
        formVisible.value = false
        // 刷新数据，确保统计数据更新
        await Promise.all([
          loadData(),
          loadTodayProgress(),
          loadStats()
        ])
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const addReminderTime = (time: string) => {
  if (time && !targetForm.reminderIntervals?.includes(time)) {
    if (!targetForm.reminderIntervals) {
      targetForm.reminderIntervals = []
    }
    targetForm.reminderIntervals.push(time)
    reminderTimeInput.value = ''
  }
}

const removeReminderTime = (index: number) => {
  targetForm.reminderIntervals?.splice(index, 1)
}

const handleSaveTarget = async () => {
  if (!targetFormRef.value) return
  await targetFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await healthApi.setWaterTarget(targetForm)
        ElMessage.success('保存成功')
        showTargetDialog.value = false
        await loadWaterTarget()
        await loadTodayProgress()
      } catch (error: any) {
        ElMessage.error(error.message || '保存失败')
      }
    }
  })
}

const handlePageChange = (page: number) => {
  pageNum.value = page
  loadData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  pageNum.value = 1
  loadData()
}

onMounted(async () => {
  isMounted.value = true
  // 先加载目标，因为进度需要目标数据
  await loadWaterTarget()
  // 并行加载其他数据
  await Promise.all([
    loadData(),
    loadStats(),
    loadTodayProgress()
  ])
})

onBeforeUnmount(() => {
  isMounted.value = false
  // 清理状态，防止在组件卸载后更新
  todayProgress.value = null
})
</script>

<style scoped>
.water-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-toolbar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.reminder-times {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
</style>

