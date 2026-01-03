<template>
  <div class="exercise-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">运动记录</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              <span>新增记录</span>
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              <span>导出</span>
            </el-button>
            <el-button @click="showImportDialog = true">
              <el-icon><Upload /></el-icon>
              <span>导入</span>
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :xs="24" :sm="12" :md="8">
          <StatisticsCard
            title="本周运动时长"
            :value="stats?.totalDuration || 0"
            unit="分钟"
            color="#409EFF"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <StatisticsCard
            title="本周运动次数"
            :value="stats?.totalCount || 0"
            unit="次"
            color="#67C23A"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <StatisticsCard
            title="本周消耗卡路里"
            :value="stats?.totalCalories || 0"
            unit="千卡"
            color="#E6A23C"
          />
        </el-col>
      </el-row>

      <!-- 筛选工具栏 -->
      <div class="filter-toolbar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="运动类型">
            <el-select
              v-model="queryParams.exerciseType"
              placeholder="全部"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="type in exerciseTypes"
                :key="type"
                :label="type"
                :value="type"
              />
            </el-select>
          </el-form-item>
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
      <div class="table-wrapper">
        <el-table
          :data="records"
          v-loading="loading"
          :default-sort="{ prop: 'exerciseDate', order: 'descending' }"
          style="width: 100%"
        >
        <el-table-column prop="exerciseDate" label="日期" width="120" sortable />
        <el-table-column prop="exerciseType" label="运动类型" width="120" />
        <el-table-column prop="trainingPlanName" label="训练计划" width="150">
          <template #default="{ row }">
            {{ row.trainingPlanName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="durationMinutes" label="时长(分钟)" width="120" />
        <el-table-column prop="caloriesBurned" label="卡路里" width="100">
          <template #default="{ row }">
            {{ row.caloriesBurned || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="distanceKm" label="距离(km)" width="100">
          <template #default="{ row }">
            {{ row.distanceKm || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="heartRateAvg" label="平均心率" width="100">
          <template #default="{ row }">
            {{ row.heartRateAvg || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>

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
    <ExerciseRecordForm
      v-model="formVisible"
      :record="currentRecord"
      :training-plans="trainingPlans"
      @submit="handleFormSubmit"
    />

    <!-- 导入对话框 -->
    <ExerciseRecordImportDialog
      v-model="showImportDialog"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Upload } from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'
import { healthApi } from '@/services/healthApi'
import type {
  ExerciseRecordDTO,
  ExerciseRecordQuery,
  ExerciseStatistics,
  TrainingPlan,
} from '@/types/health'
import StatisticsCard from '@/components/health/StatisticsCard.vue'
import ExerciseRecordForm from '@/components/health/ExerciseRecordForm.vue'
import ExerciseRecordImportDialog from '@/components/health/ExerciseRecordImportDialog.vue'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const loading = ref(false)
const records = ref<ExerciseRecordDTO[]>([])
const stats = ref<ExerciseStatistics | null>(null)
const trainingPlans = ref<TrainingPlan[]>([])

const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const queryParams = reactive<ExerciseRecordQuery>({
  exerciseType: undefined,
  startDate: undefined,
  endDate: undefined,
})

const dateRange = ref<[string, string] | null>(null)
const formVisible = ref(false)
const showImportDialog = ref(false)
const currentRecord = ref<Partial<ExerciseRecordDTO> | undefined>(undefined)

const exerciseTypes = [
  '跑步', '游泳', '骑行', '力量训练', '瑜伽', '有氧运动', '球类运动', '其他'
]

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    const data = await healthApi.listExerciseRecords(params)
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
    const data = await healthApi.getExerciseStatistics({ period: 'week' })
    stats.value = data
  } catch (error: any) {
    console.error('加载统计失败:', error)
  }
}

const loadTrainingPlans = async () => {
  try {
    const data = await healthApi.listTrainingPlans()
    trainingPlans.value = data || []
  } catch (error: any) {
    console.error('加载训练计划失败:', error)
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
  queryParams.exerciseType = undefined
  queryParams.startDate = undefined
  queryParams.endDate = undefined
  dateRange.value = null
  pageNum.value = 1
  loadData()
}

const handleAdd = () => {
  currentRecord.value = undefined
  formVisible.value = true
}

const handleEdit = (record: ExerciseRecordDTO) => {
  currentRecord.value = { ...record }
  formVisible.value = true
}

const handleDelete = async (record: ExerciseRecordDTO) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      type: 'warning',
    })
    await healthApi.deleteExerciseRecord(record.id!)
    ElMessage.success('删除成功')
    await loadData()
    await loadStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleFormSubmit = async (data: ExerciseRecordDTO) => {
  try {
    if (currentRecord.value?.id) {
      await healthApi.updateExerciseRecord(currentRecord.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await healthApi.createExerciseRecord(data)
      ElMessage.success('创建成功')
    }
    await loadData()
    await loadStats()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleExport = async () => {
  try {
    loading.value = true
    const blob = await healthApi.exportExerciseRecordsExcel(queryParams)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `运动记录_${new Date().toISOString().slice(0, 10).replace(/-/g, '')}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  } finally {
    loading.value = false
  }
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

const handleImportSuccess = async () => {
  await loadData()
  await loadStats()
}

onMounted(() => {
  loadData()
  loadStats()
  loadTrainingPlans()
})
</script>

<style scoped>
.exercise-view {
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

/* 移动端适配 - REQ-001 */
@media (max-width: 768px) {
  .exercise-view {
    padding: 0;
    font-size: 0.9em;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .header-actions .el-button {
    flex: 1;
    min-width: 0;
  }

  .header-actions .el-button span {
    display: none;
  }

  .filter-toolbar {
    margin-bottom: 12px;
  }

  .filter-toolbar :deep(.el-form) {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .filter-toolbar :deep(.el-form-item) {
    margin: 0;
    width: 100%;
  }

  .filter-toolbar :deep(.el-select),
  .filter-toolbar :deep(.el-date-picker) {
    width: 100% !important;
  }

  .table-wrapper {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .table-wrapper :deep(.el-table) {
    font-size: 0.9em;
  }

  .table-wrapper :deep(.el-table th),
  .table-wrapper :deep(.el-table td) {
    padding: 8px 4px;
  }

  .pagination {
    justify-content: center;
  }

  .pagination :deep(.el-pagination) {
    font-size: 0.9em;
  }

  .pagination :deep(.el-pagination__sizes),
  .pagination :deep(.el-pagination__jump) {
    display: none;
  }
}
</style>

