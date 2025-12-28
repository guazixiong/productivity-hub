<template>
  <div class="training-plan-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">运动计划</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              <span>新增计划</span>
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :xs="24" :sm="12" :md="6">
          <StatisticsCard
            title="总计划数"
            :value="plans.length"
            unit="个"
            color="#409EFF"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <StatisticsCard
            title="进行中"
            :value="activePlansCount"
            unit="个"
            color="#67C23A"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <StatisticsCard
            title="已完成"
            :value="completedPlansCount"
            unit="个"
            color="#E6A23C"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <StatisticsCard
            title="已暂停"
            :value="pausedPlansCount"
            unit="个"
            color="#909399"
          />
        </el-col>
      </el-row>

      <!-- 筛选工具栏 -->
      <div class="filter-toolbar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="计划状态">
            <el-select
              v-model="queryParams.status"
              placeholder="全部"
              clearable
              style="width: 150px"
            >
              <el-option label="进行中" value="ACTIVE" />
              <el-option label="已暂停" value="PAUSED" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="计划类型">
            <el-select
              v-model="queryParams.planType"
              placeholder="全部"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="type in planTypes"
                :key="type"
                :label="type"
                :value="type"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="filteredPlans"
        v-loading="loading"
        :default-sort="{ prop: 'createdAt', order: 'descending' }"
        style="width: 100%"
      >
        <el-table-column prop="planName" label="计划名称" width="200" />
        <el-table-column prop="planType" label="计划类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120">
          <template #default="{ row }">
            {{ row.startDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="120">
          <template #default="{ row }">
            {{ row.endDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="targetDurationDays" label="目标天数" width="100">
          <template #default="{ row }">
            {{ row.targetDurationDays || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="targetCaloriesPerDay" label="每日目标(千卡)" width="130">
          <template #default="{ row }">
            {{ row.targetCaloriesPerDay || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="exerciseRecordCount" label="运动次数" width="100">
          <template #default="{ row }">
            {{ row.exerciseRecordCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCalories" label="总卡路里" width="100">
          <template #default="{ row }">
            {{ row.totalCalories || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              link
              type="warning"
              @click="handlePause(row)"
            >
              暂停
            </el-button>
            <el-button
              v-if="row.status === 'PAUSED'"
              link
              type="success"
              @click="handleResume(row)"
            >
              恢复
            </el-button>
            <el-button
              v-if="row.status === 'ACTIVE' || row.status === 'PAUSED'"
              link
              type="info"
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <TrainingPlanForm
      v-model="formVisible"
      :plan="currentPlan"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { healthApi } from '@/services/healthApi'
import type { TrainingPlan, TrainingPlanDTO, PlanType, PlanStatus } from '@/types/health'
import StatisticsCard from '@/components/health/StatisticsCard.vue'
import TrainingPlanForm from '@/components/health/TrainingPlanForm.vue'

const loading = ref(false)
const plans = ref<TrainingPlan[]>([])

const queryParams = reactive<{
  status?: string
  planType?: string
}>({
  status: undefined,
  planType: undefined,
})

const formVisible = ref(false)
const currentPlan = ref<Partial<TrainingPlan> | undefined>(undefined)

const planTypes: PlanType[] = ['减脂', '增肌', '塑形', '耐力提升', '康复训练', '其他']

const filteredPlans = computed(() => {
  let result = plans.value
  if (queryParams.status) {
    result = result.filter(p => p.status === queryParams.status)
  }
  if (queryParams.planType) {
    result = result.filter(p => p.planType === queryParams.planType)
  }
  return result
})

const activePlansCount = computed(() => {
  return plans.value.filter(p => p.status === 'ACTIVE').length
})

const completedPlansCount = computed(() => {
  return plans.value.filter(p => p.status === 'COMPLETED').length
})

const pausedPlansCount = computed(() => {
  return plans.value.filter(p => p.status === 'PAUSED').length
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await healthApi.listTrainingPlans(queryParams)
    plans.value = data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.status = undefined
  queryParams.planType = undefined
  loadData()
}

const handleAdd = () => {
  currentPlan.value = undefined
  formVisible.value = true
}

const handleEdit = (plan: TrainingPlan) => {
  currentPlan.value = { ...plan }
  formVisible.value = true
}

const handlePause = async (plan: TrainingPlan) => {
  try {
    await ElMessageBox.confirm('确定要暂停该训练计划吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await healthApi.pauseTrainingPlan(plan.id)
    ElMessage.success('暂停成功')
    await loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '暂停失败')
    }
  }
}

const handleResume = async (plan: TrainingPlan) => {
  try {
    await ElMessageBox.confirm('确定要恢复该训练计划吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    await healthApi.resumeTrainingPlan(plan.id)
    ElMessage.success('恢复成功')
    await loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '恢复失败')
    }
  }
}

const handleComplete = async (plan: TrainingPlan) => {
  try {
    await ElMessageBox.confirm('确定要完成该训练计划吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
    })
    await healthApi.completeTrainingPlan(plan.id)
    ElMessage.success('完成成功')
    await loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '完成失败')
    }
  }
}

const handleDelete = async (plan: TrainingPlan) => {
  try {
    await ElMessageBox.confirm(
      plan.exerciseRecordCount && plan.exerciseRecordCount > 0
        ? '该计划有关联的运动记录，确定要删除吗？'
        : '确定要删除该训练计划吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    await healthApi.deleteTrainingPlan(plan.id, false)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleFormSubmit = async (data: TrainingPlanDTO) => {
  try {
    if (currentPlan.value?.id) {
      await healthApi.updateTrainingPlan(currentPlan.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await healthApi.createTrainingPlan(data)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    await loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const getStatusTagType = (status: PlanStatus) => {
  const map: Record<PlanStatus, string> = {
    ACTIVE: 'success',
    PAUSED: 'warning',
    COMPLETED: 'info',
    CANCELLED: 'danger',
  }
  return map[status] || ''
}

const getStatusText = (status: PlanStatus) => {
  const map: Record<PlanStatus, string> = {
    ACTIVE: '进行中',
    PAUSED: '已暂停',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return map[status] || status
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.training-plan-view {
  padding: 0;
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
</style>

