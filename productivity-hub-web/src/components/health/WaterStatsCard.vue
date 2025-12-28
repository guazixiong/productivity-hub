<template>
  <el-card class="water-stats-card" shadow="hover">
    <template #header>
      <span class="card-title">饮水达标</span>
    </template>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div style="height: 150px;"></div>
      </template>
      <template #default>
        <div v-if="!data" class="empty-state">
          <el-empty description="暂无饮水数据" />
        </div>
        <div v-else class="stats-content">
          <div class="stats-text">
            <span class="label">今日</span>
            <span class="value">{{ todayIntake }}/{{ target }}ml</span>
            <span class="percentage">{{ Math.round((todayIntake / target) * 100) }}%</span>
          </div>
          <div ref="chartRef" :style="{ height: chartHeight, width: '100%' }"></div>
        </div>
      </template>
    </el-skeleton>
  </el-card>
</template>

<script setup lang="ts">
/**
 * 饮水达标卡片组件
 * 
 * 关联需求: REQ-007
 * 关联组件: COMP-REQ-002-01-04
 * 关联接口: API-REQ-002-07, API-REQ-002-10
 */

import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { WaterStatistics } from '@/types/health'

interface Props {
  data: WaterStatistics | null
  target: number
  todayIntake: number
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const isMobile = computed(() => window.innerWidth < 768)
const chartHeight = computed(() => isMobile.value ? '120px' : '150px')

const initChart = () => {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const percentage = Math.min(100, (props.todayIntake / props.target) * 100)

  const option = {
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          position: 'center',
          formatter: `${percentage}%`,
          fontSize: isMobile.value ? 16 : 20,
          fontWeight: 'bold',
        },
        data: [
          { value: percentage, name: '已完成', itemStyle: { color: '#52C41A' } },
          { value: 100 - percentage, name: '未完成', itemStyle: { color: '#E8E8E8' } },
        ],
      },
    ],
  }

  chartInstance.setOption(option)
}

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(() => [props.todayIntake, props.target], () => {
  nextTick(() => {
    initChart()
  })
})

watch(() => props.loading, (loading) => {
  if (!loading) {
    nextTick(() => {
      initChart()
    })
  }
})

onMounted(() => {
  window.addEventListener('resize', resizeChart)
  nextTick(() => {
    initChart()
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped lang="scss">
.water-stats-card {
  .card-title {
    font-size: 16px;
    font-weight: 600;
  }

  .stats-content {
    .stats-text {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      font-size: 14px;

      .label {
        color: #666;
      }

      .value {
        color: #333;
        font-weight: 500;
      }

      .percentage {
        color: #4A90E2;
        font-weight: 600;
      }
    }
  }

  .empty-state {
    padding: 40px 0;
  }
}
</style>

