<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowLeft, Refresh, Shop, Clock, Goods } from '@element-plus/icons-vue'
import { cursorShopApi } from '@/services/api'
import type { CursorCommodity } from '@/types/cursorShop'

const router = useRouter()
const loading = ref(false)
const commodities = ref<CursorCommodity[]>([])
const lastUpdated = ref('')

const hasData = computed(() => commodities.value.length > 0)

const stats = computed(() => {
  const total = commodities.value.length
  const inStock = commodities.value.filter((item) => item.stockState === 2 || item.stockState === 3).length
  const lowStock = commodities.value.filter((item) => item.stockState === 1).length
  const outOfStock = commodities.value.filter((item) => item.stockState === 0).length
  const totalStock = commodities.value.reduce((sum, item) => sum + (item.stock ?? 0), 0)
  const totalSold = commodities.value.reduce((sum, item) => sum + (item.orderSold ?? 0), 0)
  return {
    total,
    inStock,
    lowStock,
    outOfStock,
    totalStock,
    totalSold,
  }
})

const formatPrice = (price: CursorCommodity['price']) => {
  if (price === null || price === undefined) return '-'
  return Number(price).toString()
}

const stockTagType = (state: CursorCommodity['stockState']) => {
  switch (state) {
    case 2:
    case 3:
      return 'success'
    case 1:
      return 'warning'
    case 0:
      return 'danger'
    default:
      return undefined
  }
}

const loadCommodities = async () => {
  loading.value = true
  try {
    commodities.value = await cursorShopApi.commodities()
    lastUpdated.value = new Date().toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })
  } catch (error) {
    ElMessage.error((error as Error).message || '获取库存失败')
  } finally {
    loading.value = false
  }
}

const getRowClassName = ({ stockState }: CursorCommodity) => {
  switch (stockState) {
    case 2:
    case 3:
      return 'row-success'
    case 1:
      return 'row-warning'
    case 0:
      return 'row-danger'
    default:
      return ''
  }
}

const getStockClass = (stockState: CursorCommodity['stockState']) => {
  switch (stockState) {
    case 2:
    case 3:
      return 'stock-success'
    case 1:
      return 'stock-warning'
    case 0:
      return 'stock-danger'
    default:
      return ''
  }
}

onMounted(() => {
  loadCommodities()
})
</script>

<template>
  <div class="cursor-shop-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>

    <div class="header-section">
      <div class="title-wrapper">
        <el-icon class="title-icon"><Shop /></el-icon>
        <div>
          <h2 class="page-title">Cursor 邮箱自助小店库存</h2>
          <p class="page-subtitle">实时查看商品库存状态和销售情况</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          type="primary"
          :icon="Refresh"
          :loading="loading"
          @click="loadCommodities"
        >
          刷新库存
        </el-button>
        <div v-if="lastUpdated" class="update-time">
          <el-icon><Clock /></el-icon>
          <span>最近更新：{{ lastUpdated }}</span>
        </div>
      </div>
    </div>

    <div v-if="hasData" class="stats-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">商品总数</div>
        </div>
      </el-card>
      <el-card class="stat-card stat-success" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ stats.inStock }}</div>
          <div class="stat-label">有库存</div>
        </div>
      </el-card>
      <el-card class="stat-card stat-warning" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ stats.lowStock }}</div>
          <div class="stat-label">库存不足</div>
        </div>
      </el-card>
      <el-card class="stat-card stat-danger" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ stats.outOfStock }}</div>
          <div class="stat-label">缺货</div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalStock }}</div>
          <div class="stat-label">总库存量</div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalSold }}</div>
          <div class="stat-label">总销量</div>
        </div>
      </el-card>
    </div>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">商品列表</span>
          <span v-if="hasData" class="card-count">共 {{ stats.total }} 件商品</span>
        </div>
      </template>

      <el-empty v-if="!loading && !hasData" description="暂无库存数据">
        <el-button type="primary" @click="loadCommodities">刷新数据</el-button>
      </el-empty>

      <el-table
        v-else
        :data="commodities"
        v-loading="loading"
        stripe
        :border="false"
        size="default"
        class="commodity-table"
        :row-class-name="({ row }) => getRowClassName(row)"
      >
        <el-table-column prop="name" label="商品名称" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="commodity-name">
              <el-icon class="name-icon"><Goods /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="140" align="right">
          <template #default="{ row }">
            <span class="price-text">{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="120" align="right">
          <template #default="{ row }">
            <span class="stock-text" :class="getStockClass(row.stockState)">
              {{ row.stock ?? '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="orderSold" label="销量" width="120" align="right">
          <template #default="{ row }">
            <span class="sold-text">{{ row.orderSold ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stockStateText" label="库存状态" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="stockTagType(row.stockState)" effect="light" size="large">
              {{ row.stockStateText ?? '-' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.cursor-shop-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 24px;
}

.page-header {
  margin-bottom: 8px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  flex-wrap: wrap;
}

.title-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.title-icon {
  font-size: 32px;
  color: #6366f1;
  margin-top: 4px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #0f172a;
  line-height: 1.2;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.update-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
  padding: 8px 12px;
  background: rgba(99, 102, 241, 0.08);
  border-radius: 8px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.12);
  border-color: #6366f1;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.stat-success .stat-value {
  color: #22c55e;
}

.stat-warning .stat-value {
  color: #f59e0b;
}

.stat-danger .stat-value {
  color: #ef4444;
}

.table-card {
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.card-count {
  font-size: 13px;
  color: #64748b;
}

.commodity-table {
  margin-top: 8px;
}

.commodity-table :deep(.el-table__header) {
  background: rgba(99, 102, 241, 0.04);
}

.commodity-table :deep(.el-table__header th) {
  background: transparent;
  color: #475569;
  font-weight: 600;
  font-size: 13px;
  border-bottom: 2px solid rgba(99, 102, 241, 0.12);
}

.commodity-table :deep(.el-table__row) {
  transition: all 0.2s ease;
}

.commodity-table :deep(.el-table__row:hover) {
  background: rgba(99, 102, 241, 0.06);
}

.commodity-table :deep(.el-table__row.row-success) {
  background: rgba(34, 197, 94, 0.04);
}

.commodity-table :deep(.el-table__row.row-warning) {
  background: rgba(245, 158, 11, 0.04);
}

.commodity-table :deep(.el-table__row.row-danger) {
  background: rgba(239, 68, 68, 0.04);
}

.commodity-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #0f172a;
}

.name-icon {
  color: #6366f1;
  font-size: 16px;
}

.price-text {
  font-weight: 600;
  color: #0f172a;
  font-size: 14px;
}

.stock-text {
  font-weight: 600;
  font-size: 14px;
}

.stock-text.stock-success {
  color: #22c55e;
}

.stock-text.stock-warning {
  color: #f59e0b;
}

.stock-text.stock-danger {
  color: #ef4444;
}

.sold-text {
  color: #64748b;
  font-size: 14px;
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .commodity-table :deep(.el-table) {
    font-size: 12px;
  }

  .commodity-table :deep(.el-table__cell) {
    padding: 8px 4px;
  }
}
</style>
