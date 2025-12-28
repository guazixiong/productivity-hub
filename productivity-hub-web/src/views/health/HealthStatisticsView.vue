<template>
  <div class="health-statistics-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">健康统计</span>
          <div class="header-actions">
            <el-select v-model="timeRange" @change="handleTimeRangeChange" style="width: 150px">
              <el-option label="最近7天" value="7" />
              <el-option label="最近30天" value="30" />
              <el-option label="最近90天" value="90" />
              <el-option label="最近一年" value="365" />
            </el-select>
          </div>
        </div>
      </template>

      <!-- 运动统计 -->
      <el-card shadow="never" style="margin-bottom: 20px;">
        <template #header>
          <span class="section-title">运动统计</span>
        </template>
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="总运动时长"
              :value="exerciseStats?.totalDuration || 0"
              unit="分钟"
              color="#409EFF"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="总运动次数"
              :value="exerciseStats?.totalCount || 0"
              unit="次"
              color="#67C23A"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="总消耗卡路里"
              :value="exerciseStats?.totalCalories || 0"
              unit="千卡"
              color="#E6A23C"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="平均每次时长"
              :value="exerciseStats?.averageDuration || 0"
              unit="分钟"
              color="#F56C6C"
            />
          </el-col>
        </el-row>
        <!-- 计划维度统计 -->
        <div v-if="exerciseStats?.planStatistics && exerciseStats.planStatistics.length > 0" style="margin-bottom: 20px;">
          <h4 style="margin-bottom: 15px;">训练计划统计</h4>
          <el-table :data="exerciseStats.planStatistics" border>
            <el-table-column prop="planName" label="计划名称" width="200" />
            <el-table-column prop="count" label="次数" width="100" />
            <el-table-column prop="totalDuration" label="总时长(分钟)" width="150" />
            <el-table-column prop="totalCalories" label="总卡路里" width="150" />
          </el-table>
        </div>
        <div class="chart-container">
          <div ref="exerciseChartRef" style="width: 100%; height: 400px;"></div>
        </div>
      </el-card>

      <!-- 饮水统计 -->
      <el-card shadow="never" style="margin-bottom: 20px;">
        <template #header>
          <span class="section-title">饮水统计</span>
        </template>
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="总饮水量"
              :value="waterStats?.totalIntakeMl || 0"
              unit="ml"
              color="#409EFF"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="平均每日"
              :value="waterStats?.averageIntakeMl || 0"
              unit="ml"
              color="#67C23A"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="完成天数"
              :value="waterStats?.achievementDays || 0"
              unit="天"
              color="#E6A23C"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="总次数"
              :value="waterStats?.totalCount || 0"
              unit="次"
              color="#F56C6C"
            />
          </el-col>
        </el-row>
        <div class="chart-container">
          <div ref="waterChartRef" style="width: 100%; height: 400px;"></div>
        </div>
      </el-card>

      <!-- 体重统计 -->
      <el-card shadow="never">
        <template #header>
          <span class="section-title">体重统计</span>
        </template>
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="当前体重"
              :value="weightStats?.latestWeight || '-'"
              unit="kg"
              color="#409EFF"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="目标体重"
              :value="weightStats?.targetWeight || '-'"
              unit="kg"
              color="#67C23A"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="体重变化"
              :value="weightStats?.weightChange || '-'"
              unit="kg"
              :color="weightStats?.weightChange && weightStats.weightChange < 0 ? '#67C23A' : '#F56C6C'"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <StatisticsCard
              title="平均体重"
              :value="weightStats?.averageWeight || '-'"
              unit="kg"
              color="#E6A23C"
            />
          </el-col>
        </el-row>
        <div class="chart-container">
          <div ref="weightChartRef" style="width: 100%; height: 400px;"></div>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { healthApi } from '@/services/healthApi'
import type {
  ExerciseStatistics,
  WaterStatistics,
  WeightStatistics,
  ExerciseTrend,
  WaterTrend,
  WeightTrend,
} from '@/types/health'
import StatisticsCard from '@/components/health/StatisticsCard.vue'
import * as echarts from 'echarts'

const timeRange = ref('30')
const exerciseStats = ref<ExerciseStatistics | null>(null)
const waterStats = ref<WaterStatistics | null>(null)
const weightStats = ref<WeightStatistics | null>(null)

const exerciseChartRef = ref<HTMLElement>()
const waterChartRef = ref<HTMLElement>()
const weightChartRef = ref<HTMLElement>()

let exerciseChart: echarts.ECharts | null = null
let waterChart: echarts.ECharts | null = null
let weightChart: echarts.ECharts | null = null

const loadExerciseStats = async () => {
  try {
    const days = parseInt(timeRange.value)
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    // 加载类型维度统计
    const typeData = await healthApi.getExerciseStatistics({ 
      period: 'custom',
      startDate,
      endDate,
      groupBy: 'type',
    })
    // 加载计划维度统计
    const planData = await healthApi.getExerciseStatistics({ 
      period: 'custom',
      startDate,
      endDate,
      groupBy: 'plan',
    })
    // 合并统计数据
    exerciseStats.value = {
      ...typeData,
      planStatistics: planData.planStatistics || [],
    }
    await loadExerciseTrend()
  } catch (error: any) {
    ElMessage.error(error.message || '加载运动统计失败')
  }
}

const loadExerciseTrend = async () => {
  try {
    const data = await healthApi.getExerciseTrend({ 
      type: 'duration',
      days: parseInt(timeRange.value) 
    })
    await nextTick()
    if (exerciseChartRef.value && data) {
      if (!exerciseChart) {
        exerciseChart = echarts.init(exerciseChartRef.value)
      }
      const option = {
        title: {
          text: '运动趋势',
          left: 'center',
        },
        tooltip: {
          trigger: 'axis',
        },
        xAxis: {
          type: 'category',
          data: data.data.map(item => item.date),
          boundaryGap: false,
        },
        yAxis: {
          type: 'value',
          name: '时长(分钟)',
        },
        series: [
          {
            name: '运动时长',
            type: 'line',
            data: data.data.map(item => item.value),
            smooth: true,
            areaStyle: {
              opacity: 0.3,
            },
            itemStyle: {
              color: '#409EFF',
            },
          },
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
        },
      }
      exerciseChart.setOption(option)
    }
  } catch (error: any) {
    console.error('加载运动趋势失败:', error)
  }
}

const loadWaterStats = async () => {
  try {
    const days = parseInt(timeRange.value)
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    const data = await healthApi.getWaterStatistics({ 
      period: 'custom',
      startDate,
      endDate,
    })
    waterStats.value = data
    await loadWaterTrend()
  } catch (error: any) {
    ElMessage.error(error.message || '加载饮水统计失败')
  }
}

const loadWaterTrend = async () => {
  try {
    const data = await healthApi.getWaterTrend({ days: parseInt(timeRange.value) })
    await nextTick()
    if (waterChartRef.value && data) {
      if (!waterChart) {
        waterChart = echarts.init(waterChartRef.value)
      }
      const option = {
        title: {
          text: '饮水趋势',
          left: 'center',
        },
        tooltip: {
          trigger: 'axis',
        },
        xAxis: {
          type: 'category',
          data: data.data.map(item => item.date),
        },
        yAxis: [
          {
            type: 'value',
            name: '饮水量(ml)',
            position: 'left',
          },
          {
            type: 'value',
            name: '完成度(%)',
            position: 'right',
            max: 100,
          },
        ],
        series: [
          {
            name: '饮水量',
            type: 'bar',
            data: data.data.map(item => item.totalIntakeMl),
            itemStyle: {
              color: (params: any) => {
                const item = data.data[params.dataIndex]
                return item.isAchieved ? '#67C23A' : '#409EFF'
              },
            },
          },
          {
            name: '完成度',
            type: 'line',
            yAxisIndex: 1,
            data: data.data.map(item => item.achievementPercent),
            itemStyle: {
              color: '#E6A23C',
            },
          },
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
        },
      }
      waterChart.setOption(option)
    }
  } catch (error: any) {
    console.error('加载饮水趋势失败:', error)
  }
}

const loadWeightStats = async () => {
  try {
    const days = parseInt(timeRange.value)
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    const data = await healthApi.getWeightStatistics({ 
      period: 'custom',
      startDate,
      endDate,
    })
    weightStats.value = data
    await loadWeightTrend()
  } catch (error: any) {
    ElMessage.error(error.message || '加载体重统计失败')
  }
}

const loadWeightTrend = async () => {
  try {
    const data = await healthApi.getWeightTrend({ days: parseInt(timeRange.value) })
    await nextTick()
    if (weightChartRef.value && data) {
      if (!weightChart) {
        weightChart = echarts.init(weightChartRef.value)
      }
      const option = {
        title: {
          text: '体重趋势',
          left: 'center',
        },
        tooltip: {
          trigger: 'axis',
        },
        xAxis: {
          type: 'category',
          data: data.data.map(item => item.date),
          boundaryGap: false,
        },
        yAxis: [
          {
            type: 'value',
            name: '体重(kg)',
            position: 'left',
          },
          {
            type: 'value',
            name: 'BMI',
            position: 'right',
          },
        ],
        series: [
          {
            name: '体重',
            type: 'line',
            data: data.data.map(item => item.weightKg),
            smooth: true,
            itemStyle: {
              color: '#E6A23C',
            },
            markLine: data.targetWeight ? {
              data: [
                {
                  yAxis: data.targetWeight,
                  name: '目标体重',
                  lineStyle: {
                    color: '#67C23A',
                    type: 'dashed',
                  },
                  label: {
                    formatter: '目标: ' + data.targetWeight + 'kg',
                  },
                },
              ],
            } : undefined,
          },
          {
            name: 'BMI',
            type: 'line',
            yAxisIndex: 1,
            data: data.data.map(item => item.bmi || 0),
            smooth: true,
            itemStyle: {
              color: '#409EFF',
            },
          },
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
        },
      }
      weightChart.setOption(option)
    }
  } catch (error: any) {
    console.error('加载体重趋势失败:', error)
  }
}

const handleTimeRangeChange = () => {
  loadAllData()
}

const loadAllData = async () => {
  await Promise.all([
    loadExerciseStats(),
    loadWaterStats(),
    loadWeightStats(),
  ])
  // 等待DOM更新后调整图表大小
  await nextTick()
  setTimeout(() => {
    exerciseChart?.resize()
    waterChart?.resize()
    weightChart?.resize()
  }, 100)
}

const handleResize = () => {
  exerciseChart?.resize()
  waterChart?.resize()
  weightChart?.resize()
}

onMounted(() => {
  loadAllData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  exerciseChart?.dispose()
  waterChart?.dispose()
  weightChart?.dispose()
})
</script>

<style scoped>
.health-statistics-view {
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

.section-title {
  font-size: 16px;
  font-weight: bold;
}

.chart-container {
  margin-top: 20px;
}
</style>

