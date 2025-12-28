<template>
  <el-dialog
    v-model="dialogVisible"
    title="图片统计信息"
    width="900px"
    @close="handleClose"
  >
    <div v-loading="loading" class="statistics-content">
      <el-row :gutter="20">
        <!-- 基础统计 -->
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>基础统计</span>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="图片总数">
                {{ statistics?.totalCount || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="总存储空间">
                {{ formatFileSize(statistics?.totalSize || 0) }}
              </el-descriptions-item>
              <el-descriptions-item label="平均文件大小">
                {{ formatFileSize(statistics?.averageSize || 0) }}
              </el-descriptions-item>
              <el-descriptions-item label="最大文件">
                {{ formatFileSize(statistics?.maxFileSize || 0) }}
              </el-descriptions-item>
              <el-descriptions-item label="最小文件">
                {{ formatFileSize(statistics?.minFileSize || 0) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <!-- 访问统计 -->
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>访问统计</span>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="总访问次数">
                {{ statistics?.accessStats.totalAccessCount || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="平均访问次数">
                {{ statistics?.accessStats.averageAccessCount || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="最高访问次数">
                {{ statistics?.accessStats.maxAccessCount || 0 }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分类统计 -->
      <el-card shadow="never" style="margin-top: 20px">
        <template #header>
          <span>分类统计</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="12">
            <v-chart
              :option="categoryChartOption"
              style="height: 300px"
              autoresize
            />
          </el-col>
          <el-col :span="12">
            <el-table :data="statistics?.categoryStats || []" border>
              <el-table-column prop="category" label="分类" width="120">
                <template #default="{ row }">
                  <el-tag :type="getCategoryTagType(row.category)">
                    {{ getCategoryLabel(row.category) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="count" label="数量" width="100" />
              <el-table-column prop="totalSize" label="存储空间">
                <template #default="{ row }">
                  {{ formatFileSize(row.totalSize) }}
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </el-card>

      <!-- 趋势图表 -->
      <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="12">
            <el-card v-if="statistics?.uploadTrend && statistics.uploadTrend.length > 0" shadow="never">
              <template #header>
                <div class="chart-header">
                  <span>上传趋势</span>
                  <el-button link size="small" @click="refreshStatistics">刷新</el-button>
                </div>
              </template>
              <v-chart
                :option="uploadTrendChartOption"
                style="height: 300px"
                autoresize
              />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card v-if="statistics?.accessTrend && statistics.accessTrend.length > 0" shadow="never">
              <template #header>
                <div class="chart-header">
                  <span>访问趋势</span>
                  <el-button link size="small" @click="refreshStatistics">刷新</el-button>
                </div>
              </template>
              <v-chart
                :option="accessTrendChartOption"
                style="height: 300px"
                autoresize
              />
            </el-card>
          </el-col>
      </el-row>

      <!-- 热门图片 -->
      <el-card v-if="statistics?.hotImages && statistics.hotImages.length > 0" shadow="never" style="margin-top: 20px">
        <template #header>
          <span>热门图片 Top 10</span>
        </template>
        <div class="hot-images">
          <div
            v-for="img in statistics.hotImages"
            :key="img.id"
            class="hot-image-item"
            @click="handleViewImage(img)"
          >
            <el-image
              :src="getImageUrl(img.thumbnailUrl || img.fileUrl)"
              fit="cover"
              class="hot-thumbnail"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="hot-info">
              <div class="hot-filename">{{ img.originalFilename }}</div>
              <div class="hot-count">访问 {{ img.accessCount }} 次</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'
import { imageApi } from '@/services/imageApi'
import type { ImageStatistics, ImageCategory } from '@/types/image'
import {
  getImageUrl,
  formatFileSize,
  getCategoryTagType,
  getCategoryLabel,
} from '@/utils/imageUtils'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
])

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const dialogVisible = ref(false)
const loading = ref(false)
const statistics = ref<ImageStatistics | null>(null)

// 图表配置
const categoryChartOption = ref({})
const uploadTrendChartOption = ref({})
const accessTrendChartOption = ref({})

watch(
  () => props.modelValue,
  (val) => {
    dialogVisible.value = val
    if (val) {
      loadStatistics()
    }
  }
)

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

const loadStatistics = async () => {
  loading.value = true
  try {
    statistics.value = await imageApi.statistics()
    updateCharts()
  } catch (error) {
    ElMessage.error('加载统计信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const updateCharts = () => {
  if (!statistics.value) return

  // 分类统计饼图
  categoryChartOption.value = {
    title: {
      text: '分类分布',
      left: 'center',
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
    },
    series: [
      {
        name: '图片数量',
        type: 'pie',
        radius: '50%',
        data: statistics.value.categoryStats.map((item) => ({
          value: item.count,
          name: getCategoryLabel(item.category),
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }

  // 上传趋势折线图
  if (statistics.value.uploadTrend && statistics.value.uploadTrend.length > 0) {
    uploadTrendChartOption.value = {
      title: {
        text: '上传趋势',
        left: 'center',
      },
      tooltip: {
        trigger: 'axis',
      },
      xAxis: {
        type: 'category',
        data: statistics.value.uploadTrend.map((item) => item.date),
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          name: '上传数量',
          type: 'line',
          data: statistics.value.uploadTrend.map((item) => item.count),
          smooth: true,
        },
      ],
    }
  }

  // 访问趋势折线图
  if (statistics.value.accessTrend && statistics.value.accessTrend.length > 0) {
    accessTrendChartOption.value = {
      title: {
        text: '访问趋势',
        left: 'center',
      },
      tooltip: {
        trigger: 'axis',
      },
      xAxis: {
        type: 'category',
        data: statistics.value.accessTrend.map((item) => item.date),
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          name: '访问次数',
          type: 'line',
          data: statistics.value.accessTrend.map((item) => item.count),
          smooth: true,
        },
      ],
    }
  }
}


const handleViewImage = (img: any) => {
  // 在新窗口打开图片
  window.open(getImageUrl(img.fileUrl), '_blank')
}

const handleClose = () => {
  dialogVisible.value = false
}

const refreshStatistics = () => {
  loadStatistics()
}
</script>

<style scoped lang="scss">
.statistics-content {
  .hot-images {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 16px;

    .hot-image-item {
      cursor: pointer;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      overflow: hidden;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      }

      .hot-thumbnail {
        width: 100%;
        height: 120px;
      }

      .image-error {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 120px;
        background-color: #f5f7fa;
        color: #909399;
      }

      .hot-info {
        padding: 8px;
        font-size: 12px;

        .hot-filename {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-bottom: 4px;
        }

        .hot-count {
          color: #909399;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

