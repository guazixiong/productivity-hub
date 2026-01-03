<template>
  <div class="asset-detail-view">
    <el-page-header @back="handleBack">
      <template #content>
        <span>资产详情</span>
      </template>
      <template #extra>
        <el-button type="primary" @click="handleEdit">编辑</el-button>
      </template>
    </el-page-header>

    <el-card class="mt-16" v-loading="loading">
      <el-row :gutter="24">
        <el-col :span="8">
          <div class="asset-image-wrapper" v-if="asset?.image">
            <el-image :src="asset.image" fit="contain" />
          </div>
          <div v-else class="asset-image-placeholder">
            无图片
          </div>
        </el-col>
        <el-col :span="16">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="资产名称">
              {{ asset?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="分类">
              {{ asset?.categoryName }}
            </el-descriptions-item>
            <el-descriptions-item label="价格">
              {{ formatCurrency(asset?.price) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag v-if="asset" :type="getStatusTagType(asset.status)">
                {{ formatStatus(asset.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="购买日期">
              {{ asset?.purchaseDate }}
            </el-descriptions-item>
            <el-descriptions-item label="退役日期">
              {{ asset?.retiredDate || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="总价值">
              {{ formatCurrency(asset?.totalValue) }}
            </el-descriptions-item>
            <el-descriptions-item label="日均价格">
              {{ formatCurrency(asset?.dailyAveragePrice) }}
            </el-descriptions-item>
            <el-descriptions-item label="是否在服役">
              {{ asset?.inService ? '是' : '否' }}
            </el-descriptions-item>
            <el-descriptions-item label="使用天数">
              {{ asset?.usageDays }}
            </el-descriptions-item>
          </el-descriptions>

          <el-card class="mt-16" shadow="never">
            <template #header>
              <span>保修信息 & 贬值策略</span>
            </template>
            <el-descriptions :column="2">
              <el-descriptions-item label="启用保修">
                {{ asset?.warrantyEnabled ? '是' : '否' }}
              </el-descriptions-item>
              <el-descriptions-item label="保修截止日期">
                {{ asset?.warrantyEndDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="按使用次数贬值">
                {{ asset?.depreciationByUsageCount ? '是' : '否' }}
              </el-descriptions-item>
              <el-descriptions-item label="预计使用次数">
                {{ asset?.expectedUsageCount || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="按使用日期贬值">
                {{ asset?.depreciationByUsageDate ? '是' : '否' }}
              </el-descriptions-item>
              <el-descriptions-item label="使用日期">
                {{ asset?.usageDate || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="mt-16" shadow="never">
            <template #header>
              <span>备注</span>
            </template>
            <div class="remark-content">
              {{ asset?.remark || '无' }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { AssetDetail, AssetStatus } from '@/types/asset'
import { assetApi } from '@/services/assetApi'
import { formatCurrency } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const asset = ref<AssetDetail | null>(null)

const getStatusTagType = (status: AssetStatus) => {
  const map: Record<AssetStatus, 'success' | 'warning' | 'info'> = {
    IN_SERVICE: 'success',
    RETIRED: 'warning',
    SOLD: 'info',
  }
  return map[status] || 'info'
}

const formatStatus = (status: AssetStatus) => {
  const map: Record<AssetStatus, string> = {
    IN_SERVICE: '正在服役',
    RETIRED: '已退役',
    SOLD: '已卖出',
  }
  return map[status] || status
}

const loadDetail = async () => {
  const id = route.params.id as string
  if (!id) return
  loading.value = true
  try {
    const res = await assetApi.getAssetById(id)
    console.log('资产详情数据:', res)
    asset.value = res || null
    if (!res) {
      console.warn('资产详情数据为空')
    }
  } catch (error) {
    console.error('加载资产详情失败:', error)
    ElMessage.error('加载资产详情失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.back()
}

const handleEdit = () => {
  if (!asset.value) return
  router.push(`/assets/edit/${asset.value.id}`)
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.asset-detail-view {
  padding: 16px;
}

.mt-16 {
  margin-top: 16px;
}

.asset-image-wrapper {
  width: 100%;
  padding: 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  text-align: center;
}

.asset-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  border: 1px dashed var(--el-border-color);
  color: var(--el-text-color-secondary);
}

.remark-content {
  white-space: pre-wrap;
  min-height: 60px;
}
</style>


