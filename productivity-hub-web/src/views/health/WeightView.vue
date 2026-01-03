<template>
  <div class="weight-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">体重记录</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              <span>新增记录</span>
            </el-button>
            <el-button @click="showImportDialog = true">
              <el-icon><Upload /></el-icon>
              <span>导入</span>
            </el-button>
            <el-button @click="handleOpenBodyInfoDialog">
              <el-icon><User /></el-icon>
              <span>身体信息</span>
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :xs="24" :sm="12" :md="6">
          <StatisticsCard
            title="当前体重"
            :value="currentWeight || '-'"
            unit="kg"
            color="#409EFF"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <StatisticsCard
            title="BMI"
            :value="currentBmi || '-'"
            unit=""
            color="#67C23A"
          >
            <template #extra>
              <el-tag 
                v-if="currentHealthStatus" 
                :type="getHealthStatusTagType(currentHealthStatus)"
                size="small"
              >
                {{ currentHealthStatus }}
              </el-tag>
            </template>
          </StatisticsCard>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <StatisticsCard
            title="目标体重"
            :value="bodyInfo?.targetWeightKg || '-'"
            unit="kg"
            color="#E6A23C"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <StatisticsCard
            title="推荐体重"
            :value="recommendedWeightRange || '-'"
            unit="kg"
            color="#909399"
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
      <div class="table-wrapper">
        <el-table
          :data="records"
          v-loading="loading"
          :default-sort="{ prop: 'recordDate', order: 'descending' }"
          style="width: 100%"
        >
        <el-table-column prop="recordDate" label="日期" width="120" sortable />
        <el-table-column prop="recordTime" label="时间" width="100" />
        <el-table-column prop="weightKg" label="体重(kg)" width="120" />
        <el-table-column prop="bmi" label="BMI" width="150">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>{{ row.bmi ? row.bmi.toFixed(2) : '-' }}</span>
              <el-tag 
                v-if="row.healthStatus" 
                :type="getHealthStatusTagType(row.healthStatus)"
                size="small"
              >
                {{ row.healthStatus }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="bodyFatPercentage" label="体脂率(%)" width="120">
          <template #default="{ row }">
            {{ row.bodyFatPercentage ? row.bodyFatPercentage.toFixed(1) : '-' }}
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
    <el-dialog
      v-model="formVisible"
      :title="currentRecord?.id ? '编辑体重记录' : '新增体重记录'"
      :width="isMobile ? '90%' : '500px'"
      :fullscreen="isMobile"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="日期" prop="recordDate">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时间" prop="recordTime">
          <el-time-picker
            v-model="form.recordTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="体重" prop="weightKg">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-input-number
              v-model="form.weightKg"
              :min="weightUnit === 'kg' ? 20 : 40"
              :max="weightUnit === 'kg' ? 300 : 600"
              :precision="2"
              :placeholder="`请输入体重(${weightUnit === 'kg' ? '公斤' : '斤'})`"
              style="flex: 1"
            />
            <el-select v-model="weightUnit" style="width: 100px" @change="handleWeightUnitChange">
              <el-option label="公斤" value="kg" />
              <el-option label="斤" value="jin" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="体脂率(%)">
          <el-input-number
            v-model="form.bodyFatPercentage"
            :min="5"
            :max="50"
            :precision="1"
            placeholder="可选"
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

    <!-- 导入对话框 -->
    <WeightRecordImportDialog
      v-model="showImportDialog"
      @success="handleImportSuccess"
    />

    <!-- 身体信息对话框 -->
    <el-dialog 
      v-model="showBodyInfoDialog" 
      title="身体信息" 
      :width="isMobile ? '90%' : '500px'"
      :fullscreen="isMobile"
    >
      <el-form ref="bodyInfoFormRef" :model="bodyInfoForm" :rules="bodyInfoRules" label-width="120px">
        <el-form-item label="身高(cm)">
          <el-input-number
            v-model="bodyInfoForm.heightCm"
            :min="100"
            :max="250"
            :precision="2"
            placeholder="请输入身高"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="bodyInfoForm.birthDate"
            type="date"
            placeholder="选择出生日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="bodyInfoForm.gender" placeholder="请选择" style="width: 100%">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标体重" prop="targetWeightKg">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-input-number
              v-model="bodyInfoForm.targetWeightKg"
              :min="targetWeightUnit === 'kg' ? 20 : 40"
              :max="targetWeightUnit === 'kg' ? 300 : 600"
              :precision="2"
              :placeholder="`请输入目标体重(${targetWeightUnit === 'kg' ? '公斤' : '斤'})`"
              style="flex: 1"
            />
            <el-select v-model="targetWeightUnit" style="width: 100px" @change="handleTargetWeightUnitChange">
              <el-option label="公斤" value="kg" />
              <el-option label="斤" value="jin" />
            </el-select>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBodyInfoDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBodyInfo">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Upload } from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'
import { healthApi } from '@/services/healthApi'
import type {
  WeightRecord,
  WeightRecordDTO,
  WeightRecordQuery,
  UserBodyInfo,
  UserBodyInfoDTO,
} from '@/types/health'
import StatisticsCard from '@/components/health/StatisticsCard.vue'
import WeightRecordImportDialog from '@/components/health/WeightRecordImportDialog.vue'
import type { FormInstance, FormRules } from 'element-plus'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const loading = ref(false)
const records = ref<WeightRecord[]>([])
const bodyInfo = ref<UserBodyInfo | null>(null)

const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const queryParams = reactive<WeightRecordQuery>({
  startDate: undefined,
  endDate: undefined,
})

const dateRange = ref<[string, string] | null>(null)
const formVisible = ref(false)
const showBodyInfoDialog = ref(false)
const showImportDialog = ref(false)
const formRef = ref<FormInstance>()
const bodyInfoFormRef = ref<FormInstance>()
const currentRecord = ref<Partial<WeightRecord> | undefined>(undefined)
const weightUnit = ref<'kg' | 'jin'>('kg')
const targetWeightUnit = ref<'kg' | 'jin'>('kg')

const form = reactive<WeightRecordDTO>({
  recordDate: new Date().toISOString().split('T')[0],
  recordTime: new Date().toTimeString().slice(0, 5),
  weightKg: 0,
  bodyFatPercentage: undefined,
  notes: '',
})

const bodyInfoForm = reactive<UserBodyInfoDTO>({
  heightCm: undefined,
  birthDate: undefined,
  gender: undefined,
  targetWeightKg: undefined,
})

const currentWeight = computed(() => {
  if (records.value.length > 0) {
    const latest = records.value.sort((a, b) => 
      new Date(b.recordDate).getTime() - new Date(a.recordDate).getTime()
    )[0]
    return latest.weightKg.toFixed(2)
  }
  return null
})

const currentBmi = computed(() => {
  if (bodyInfo.value?.heightCm && currentWeight.value) {
    const heightM = bodyInfo.value.heightCm / 100
    const bmi = parseFloat(currentWeight.value) / (heightM * heightM)
    return bmi.toFixed(2)
  }
  return null
})

const currentHealthStatus = computed(() => {
  if (!currentBmi.value || currentBmi.value === '-') {
    return null
  }
  const bmi = parseFloat(currentBmi.value)
  if (bmi < 18.5) {
    return '偏瘦'
  } else if (bmi < 24) {
    return '正常'
  } else if (bmi < 28) {
    return '偏胖'
  } else {
    return '肥胖'
  }
})

// 推荐体重范围（基于 BMI 正常范围 18.5-24.9）
const recommendedWeightRange = computed(() => {
  if (bodyInfo.value?.heightCm) {
    const heightM = bodyInfo.value.heightCm / 100
    const minWeight = heightM * heightM * 18.5
    const maxWeight = heightM * heightM * 24.9
    return `${minWeight.toFixed(1)}-${maxWeight.toFixed(1)}`
  }
  return null
})

const rules: FormRules = {
  recordDate: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  weightKg: [
    { required: true, message: '请输入体重', trigger: 'blur' },
    { 
      validator: (rule: any, value: number, callback: any) => {
        if (value === undefined || value === null || value === 0) {
          callback(new Error('请输入体重'))
          return
        }
        if (weightUnit.value === 'kg') {
          if (value < 20 || value > 300) {
            callback(new Error('体重必须在20.00-300.00公斤之间'))
            return
          }
        } else {
          if (value < 40 || value > 600) {
            callback(new Error('体重必须在40.00-600.00斤之间'))
            return
          }
        }
        callback()
      },
      trigger: 'blur' 
    }
  ],
  bodyFatPercentage: [
    { type: 'number', min: 5, max: 50, message: '体脂率必须在5.00-50.00%之间', trigger: 'blur' }
  ],
}

const bodyInfoRules: FormRules = {
  heightCm: [
    { type: 'number', min: 100, max: 250, message: '身高必须在100.00-250.00cm之间', trigger: 'blur' }
  ],
  targetWeightKg: [
    { 
      validator: (rule: any, value: number, callback: any) => {
        if (value === undefined || value === null) {
          callback()
          return
        }
        if (targetWeightUnit.value === 'kg') {
          if (value < 20 || value > 300) {
            callback(new Error('目标体重必须在20.00-300.00公斤之间'))
            return
          }
        } else {
          if (value < 40 || value > 600) {
            callback(new Error('目标体重必须在40.00-600.00斤之间'))
            return
          }
        }
        callback()
      },
      trigger: 'blur' 
    }
  ],
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    const data = await healthApi.listWeightRecords(params)
    records.value = data.items || []
    total.value = data.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadBodyInfo = async () => {
  try {
    const data = await healthApi.getBodyInfo()
    bodyInfo.value = data
    if (data) {
      // 重置单位选择为公斤（因为数据库存储的是公斤）
      targetWeightUnit.value = 'kg'
      Object.assign(bodyInfoForm, {
        heightCm: data.heightCm,
        birthDate: data.birthDate,
        gender: data.gender,
        targetWeightKg: data.targetWeightKg,
      })
    } else {
      // 如果没有数据，重置表单
      targetWeightUnit.value = 'kg'
      Object.assign(bodyInfoForm, {
        heightCm: undefined,
        birthDate: undefined,
        gender: undefined,
        targetWeightKg: undefined,
      })
    }
  } catch (error: any) {
    console.error('加载身体信息失败:', error)
  }
}

const handleOpenBodyInfoDialog = async () => {
  // 打开对话框前重新加载数据，确保显示最新值
  await loadBodyInfo()
  showBodyInfoDialog.value = true
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

const handleWeightUnitChange = () => {
  // 单位切换时，转换体重值
  if (form.weightKg && form.weightKg > 0) {
    if (weightUnit.value === 'jin') {
      // 从公斤转为斤
      form.weightKg = form.weightKg * 2
    } else {
      // 从斤转为公斤
      form.weightKg = form.weightKg / 2
    }
  }
}

const handleTargetWeightUnitChange = () => {
  // 单位切换时，转换目标体重值
  if (bodyInfoForm.targetWeightKg && bodyInfoForm.targetWeightKg > 0) {
    if (targetWeightUnit.value === 'jin') {
      // 从公斤转为斤
      bodyInfoForm.targetWeightKg = bodyInfoForm.targetWeightKg * 2
    } else {
      // 从斤转为公斤
      bodyInfoForm.targetWeightKg = bodyInfoForm.targetWeightKg / 2
    }
  }
}

const handleAdd = () => {
  currentRecord.value = undefined
  weightUnit.value = 'kg'
  Object.assign(form, {
    recordDate: new Date().toISOString().split('T')[0],
    recordTime: new Date().toTimeString().slice(0, 5),
    weightKg: 0,
    bodyFatPercentage: undefined,
    notes: '',
  })
  formVisible.value = true
}

const handleEdit = (record: WeightRecord) => {
  currentRecord.value = { ...record }
  weightUnit.value = 'kg' // 编辑时默认使用公斤
  Object.assign(form, {
    recordDate: record.recordDate,
    recordTime: record.recordTime,
    weightKg: record.weightKg,
    bodyFatPercentage: record.bodyFatPercentage,
    notes: record.notes || '',
  })
  formVisible.value = true
}

const handleDelete = async (record: WeightRecord) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      type: 'warning',
    })
    await healthApi.deleteWeightRecord(record.id!)
    ElMessage.success('删除成功')
    await loadData()
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
        // 如果单位是斤，转换为公斤
        let weightKg = form.weightKg
        if (weightUnit.value === 'jin') {
          weightKg = weightKg / 2
        }
        
        // 计算BMI
        let bmi: number | undefined = undefined
        if (bodyInfo.value?.heightCm && weightKg) {
          const heightM = bodyInfo.value.heightCm / 100
          bmi = weightKg / (heightM * heightM)
        }
        
        const submitData = { ...form, weightKg, bmi }
        
        if (currentRecord.value?.id) {
          await healthApi.updateWeightRecord(currentRecord.value.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await healthApi.createWeightRecord(submitData)
          ElMessage.success('创建成功')
        }
        formVisible.value = false
        await loadData()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const handleSaveBodyInfo = async () => {
  if (!bodyInfoFormRef.value) return
  await bodyInfoFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 如果单位是斤，转换为公斤
        const submitData = { ...bodyInfoForm }
        if (targetWeightUnit.value === 'jin' && submitData.targetWeightKg) {
          submitData.targetWeightKg = submitData.targetWeightKg / 2
        }
        
        await healthApi.setBodyInfo(submitData)
        ElMessage.success('保存成功')
        showBodyInfoDialog.value = false
        await loadBodyInfo()
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

const handleImportSuccess = async () => {
  await loadData()
  await loadBodyInfo()
}

const getHealthStatusTagType = (healthStatus: string): 'info' | 'success' | 'warning' | 'danger' => {
  switch (healthStatus) {
    case '偏瘦':
      return 'info'
    case '正常':
      return 'success'
    case '偏胖':
      return 'warning'
    case '肥胖':
      return 'danger'
    default:
      return 'info'
  }
}

onMounted(() => {
  loadData()
  loadBodyInfo()
})
</script>

<style scoped>
.weight-view {
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
  .weight-view {
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

