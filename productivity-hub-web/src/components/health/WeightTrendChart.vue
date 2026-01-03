<template>
  <div class="weight-trend-chart">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div style="height: 250px;"></div>
      </template>
      <template #default>
        <div v-if="!data || !data.data || data.data.length === 0" class="empty-state">
          <el-empty description="暂无体重数据" />
        </div>
        <div
          v-else
          ref="chartRef"
          class="chart-container"
          :style="{ height: chartHeight, width: '100%' }"
        ></div>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup lang="ts">
/**
 * 体重趋势图组件
 */

import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { WeightTrend } from '@/types/health'

interface Props {
  data: WeightTrend | null
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const isMobile = computed(() => window.innerWidth < 768)
const chartHeight = computed(() => isMobile.value ? '250px' : '400px')

const initChart = () => {
  if (!chartRef.value) return

  // 检查 DOM 元素是否有有效的尺寸
  const rect = chartRef.value.getBoundingClientRect()
  if (rect.width === 0 || rect.height === 0) {
    // 如果尺寸为 0，延迟重试
    setTimeout(() => {
      initChart()
    }, 100)
    return
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  if (!props.data || !props.data.data || props.data.data.length === 0) {
    return
  }

  const option = {
    title: {
      text: `体重趋势 ${props.data.latestWeight}kg ${props.data.gapFromTarget ? (props.data.gapFromTarget > 0 ? '↑' : '↓') + Math.abs(props.data.gapFromTarget) + 'kg' : ''}`,
      left: 'center',
      textStyle: {
        fontSize: isMobile.value ? 14 : 16,
      },
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const param = params[0]
        return `${param.name}<br/>体重: ${param.value}kg`
      },
    },
    xAxis: {
      type: 'category',
      data: props.data.data.map(item => item.date),
      boundaryGap: false,
    },
    yAxis: {
      type: 'value',
      name: '体重(kg)',
    },
    series: [
      {
        name: '体重',
        type: 'line',
        data: props.data.data.map(item => item.weightKg),
        smooth: true,
        itemStyle: {
          color: '#4A90E2',
        },
        areaStyle: {
          opacity: 0.3,
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

  chartInstance.setOption(option)
}

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(() => props.data, () => {
  nextTick(() => {
    // 确保 DOM 更新后再初始化
    setTimeout(() => {
      initChart()
    }, 50)
  })
}, { deep: true })

watch(() => props.loading, (loading) => {
  if (!loading) {
    nextTick(() => {
      // 确保 DOM 更新后再初始化
      setTimeout(() => {
        initChart()
      }, 50)
    })
  }
})

onMounted(() => {
  window.addEventListener('resize', resizeChart)
  nextTick(() => {
    // 确保 DOM 完全渲染后再初始化
    setTimeout(() => {
      initChart()
    }, 100)
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
.weight-trend-chart {
  margin-bottom: 24px;

  .empty-state {
    height: 250px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .chart-container {
    width: 100%;
    min-height: 250px;
  }
}

@media (max-width: 768px) {
  .weight-trend-chart {
    margin-bottom: 16px;
  }
}
</style>

