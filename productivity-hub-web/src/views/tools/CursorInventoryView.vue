<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowLeft, Refresh, Shop, Clock, Goods, Connection } from '@element-plus/icons-vue'
import { cursorShopApi, ldxpShopApi } from '@/services/api'
import type { CursorCommodity } from '@/types/cursorShop'
import type { LdxpGoodsItem } from '@/types/ldxpShop'

type ShopId = 'cursor' | 'ldxp'

const router = useRouter()
const activeShop = ref<ShopId | null>(null)
const isOverview = ref(true)

const loadingAll = ref(false)
const cursorLoading = ref(false)
const ldxpLoading = ref(false)

const cursorGoods = ref<CursorCommodity[]>([])
const ldxpGoods = ref<LdxpGoodsItem[]>([])
const lastUpdated = ref<Partial<Record<ShopId, string>>>({})

const timestampText = () =>
  new Date().toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })

const cursorStats = computed(() => {
  const total = cursorGoods.value.length
  const inStock = cursorGoods.value.filter((item) => item.stockState === 2 || item.stockState === 3 || item.stockState === 4).length
  const lowStock = cursorGoods.value.filter((item) => item.stockState === 1).length
  const outOfStock = cursorGoods.value.filter((item) => item.stockState === 0).length
  const totalStock = cursorGoods.value.reduce((sum, item) => sum + (item.stock ?? 0), 0)
  const totalSold = cursorGoods.value.reduce((sum, item) => sum + (item.orderSold ?? 0), 0)
  return {
    total,
    inStock,
    lowStock,
    outOfStock,
    totalStock,
    totalSold,
  }
})

const ldxpStats = computed(() => {
  const total = ldxpGoods.value.length
  const inStock = ldxpGoods.value.filter((item) => (item.extend?.stock_count ?? 0) > 5).length
  const lowStock = ldxpGoods.value.filter((item) => {
    const count = item.extend?.stock_count ?? 0
    return count > 0 && count <= 5
  }).length
  const outOfStock = ldxpGoods.value.filter((item) => (item.extend?.stock_count ?? 0) === 0).length
  const totalStock = ldxpGoods.value.reduce((sum, item) => sum + (item.extend?.stock_count ?? 0), 0)
  return {
    total,
    inStock,
    lowStock,
    outOfStock,
    totalStock,
  }
})

const shopCards = computed(() => [
  {
    id: 'cursor' as ShopId,
    name: 'Cursor 邮箱自助小店',
    description: '查看官方邮箱自助小店的库存与销量',
    stats: cursorStats.value,
    loading: cursorLoading.value,
    lastUpdated: lastUpdated.value.cursor,
    hasData: cursorGoods.value.length > 0,
    buyUrl: 'https://tuhjk.asia',
  },
  {
    id: 'ldxp' as ShopId,
    name: '链动小铺',
    description: '查看链动小铺的库存与限购信息',
    stats: ldxpStats.value,
    loading: ldxpLoading.value,
    lastUpdated: lastUpdated.value.ldxp,
    hasData: ldxpGoods.value.length > 0,
    buyUrl: 'https://pay.ldxp.cn/shop/WCT9GRNG',
  },
])

const currentLoading = computed(() => {
  if (activeShop.value === 'cursor') return cursorLoading.value
  if (activeShop.value === 'ldxp') return ldxpLoading.value
  return loadingAll.value
})

const currentStats = computed(() => {
  if (activeShop.value === 'cursor') return cursorStats.value
  if (activeShop.value === 'ldxp') return ldxpStats.value
  return null
})

const formatCursorPrice = (price: CursorCommodity['price']) => {
  if (price === null || price === undefined) return '-'
  return Number(price).toString()
}

const formatLdxpPrice = (price: number | null | undefined) => {
  if (price === null || price === undefined) return '-'
  return Number(price).toString()
}

const cursorStockTag = (state: CursorCommodity['stockState']) => {
  switch (state) {
    case 2:
    case 3:
    case 4:
      return 'success'
    case 1:
      return 'warning'
    case 0:
      return 'danger'
    default:
      return undefined
  }
}

const cursorStockText = (state: CursorCommodity['stockState']) => {
  switch (state) {
    case 2:
    case 3:
    case 4:
      return '库存充足'
    case 1:
      return '库存不足'
    case 0:
      return '缺货'
    default:
      return '-'
  }
}

const cursorRowClass = ({ stockState }: CursorCommodity) => {
  switch (stockState) {
    case 2:
    case 3:
    case 4:
      return 'row-success'
    case 1:
      return 'row-warning'
    case 0:
      return 'row-danger'
    default:
      return ''
  }
}

const cursorStockClass = (stockState: CursorCommodity['stockState']) => {
  switch (stockState) {
    case 2:
    case 3:
    case 4:
      return 'stock-success'
    case 1:
      return 'stock-warning'
    case 0:
      return 'stock-danger'
    default:
      return ''
  }
}

const getLdxpRowClassName = (item: LdxpGoodsItem) => {
  const count = item.extend?.stock_count ?? 0
  if (count === 0) return 'row-danger'
  if (count <= 5) return 'row-warning'
  return 'row-success'
}

const getLdxpStockTagType = (count?: number) => {
  if (count === undefined || count === null) return undefined
  if (count === 0) return 'danger'
  if (count <= 5) return 'warning'
  return 'success'
}

const getLdxpStockStateText = (count?: number) => {
  if (count === undefined || count === null) return '-'
  if (count === 0) return '缺货'
  if (count <= 5) return '库存紧张'
  return '库存充足'
}

const fetchCursorShop = async () => {
  cursorLoading.value = true
  try {
    cursorGoods.value = await cursorShopApi.commodities()
    lastUpdated.value = { ...lastUpdated.value, cursor: timestampText() }
  } catch (error) {
    ElMessage.error((error as Error).message || '获取 Cursor 小店库存失败')
  } finally {
    cursorLoading.value = false
  }
}

const fetchLdxpShop = async () => {
  ldxpLoading.value = true
  try {
    ldxpGoods.value = await ldxpShopApi.goodsList()
    lastUpdated.value = { ...lastUpdated.value, ldxp: timestampText() }
  } catch (error) {
    ElMessage.error((error as Error).message || '获取链动小铺库存失败')
  } finally {
    ldxpLoading.value = false
  }
}

const refreshAll = async () => {
  loadingAll.value = true
  try {
    await Promise.all([fetchCursorShop(), fetchLdxpShop()])
  } finally {
    loadingAll.value = false
  }
}

const refreshCurrent = async () => {
  if (activeShop.value === 'cursor') {
    await fetchCursorShop()
  } else if (activeShop.value === 'ldxp') {
    await fetchLdxpShop()
  } else {
    await refreshAll()
  }
}

const enterShop = (shop: ShopId) => {
  activeShop.value = shop
  isOverview.value = false
}

const goToBuy = (url: string, event: Event) => {
  event.stopPropagation()
  window.open(url, '_blank')
}

const backToOverview = () => {
  isOverview.value = true
}

onMounted(() => {
  refreshAll()
})
</script>

<template>
  <div class="cursor-inventory-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
      <div class="header-actions">
        <el-button type="primary" :icon="Refresh" :loading="loadingAll" @click="refreshAll">一键刷新全部</el-button>
      </div>
    </div>

    <div class="header-section">
      <div class="title-wrapper">
        <el-icon class="title-icon"><Shop /></el-icon>
        <div>
          <h2 class="page-title">Cursor 库存</h2>
          <p class="page-subtitle">在一个界面内切换查看 Cursor 邮箱小店与链动小铺的库存</p>
        </div>
      </div>
    </div>

    <template v-if="isOverview">
      <div class="shop-grid">
        <el-card
          v-for="shop in shopCards"
          :key="shop.id"
          class="shop-card"
          shadow="hover"
          @click="enterShop(shop.id)"
        >
          <div class="shop-card__header">
            <div class="shop-card__title">
              <el-icon class="shop-card__icon"><Goods /></el-icon>
              <div>
                <div class="shop-card__name">{{ shop.name }}</div>
                <div class="shop-card__desc">{{ shop.description }}</div>
              </div>
            </div>
            <el-tag :type="shop.hasData ? 'success' : 'info'" effect="light">
              {{ shop.hasData ? '有数据' : '暂无数据' }}
            </el-tag>
          </div>
          <div class="shop-card__stats">
            <div class="stat-block">
              <div class="stat-label">商品数</div>
              <div class="stat-value">{{ shop.stats.total }}</div>
            </div>
            <div class="stat-block">
              <div class="stat-label">库存充足</div>
              <div class="stat-value success">{{ shop.stats.inStock }}</div>
            </div>
            <div class="stat-block">
              <div class="stat-label">库存紧张/缺货</div>
              <div class="stat-value warning">{{ shop.stats.lowStock + shop.stats.outOfStock }}</div>
            </div>
            <div class="stat-block">
              <div class="stat-label">总库存</div>
              <div class="stat-value">{{ shop.stats.totalStock ?? '-' }}</div>
            </div>
            <div class="stat-block">
              <div class="stat-label">总销量</div>
              <div class="stat-value">{{ shop.stats.totalSold ?? '-' }}</div>
            </div>
          </div>
          <div class="shop-card__footer">
            <div class="update-info">
              <el-icon><Clock /></el-icon>
              <span>{{ shop.lastUpdated || '尚未刷新' }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </template>

    <template v-else>
      <div class="detail-header">
        <div class="detail-actions">
          <el-button text type="primary" :icon="ArrowLeft" @click="backToOverview">返回总览</el-button>
          <el-radio-group v-model="activeShop" size="large">
            <el-radio-button label="cursor">Cursor 小店</el-radio-button>
            <el-radio-button label="ldxp">链动小铺</el-radio-button>
          </el-radio-group>
        </div>
        <div class="detail-actions">
          <el-button type="primary" :icon="Refresh" :loading="currentLoading" @click="refreshCurrent">
            刷新当前
          </el-button>
          <el-button
            v-if="activeShop"
            type="success"
            :icon="Shop"
            @click="goToBuy(activeShop === 'cursor' ? 'https://tuhjk.asia' : 'https://pay.ldxp.cn/shop/WCT9GRNG', $event)"
          >
            去购买
          </el-button>
          <div v-if="activeShop && lastUpdated[activeShop]" class="update-time">
            <el-icon><Clock /></el-icon>
            <span>最近更新：{{ lastUpdated[activeShop] }}</span>
          </div>
        </div>
      </div>

      <div v-if="activeShop === 'cursor'">
        <div v-if="cursorStats.total > 0" class="stats-cards">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ cursorStats.total }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </el-card>
          <el-card class="stat-card stat-success" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ cursorStats.inStock }}</div>
              <div class="stat-label">有库存</div>
            </div>
          </el-card>
          <el-card class="stat-card stat-warning" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ cursorStats.lowStock }}</div>
              <div class="stat-label">库存不足</div>
            </div>
          </el-card>
          <el-card class="stat-card stat-danger" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ cursorStats.outOfStock }}</div>
              <div class="stat-label">缺货</div>
            </div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ cursorStats.totalStock }}</div>
              <div class="stat-label">总库存量</div>
            </div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ cursorStats.totalSold }}</div>
              <div class="stat-label">总销量</div>
            </div>
          </el-card>
        </div>

        <el-card class="table-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">Cursor 小店商品列表</span>
              <span v-if="cursorStats.total > 0" class="card-count">共 {{ cursorStats.total }} 件商品</span>
            </div>
          </template>
          <el-empty v-if="!currentLoading && cursorStats.total === 0" description="暂无库存数据">
            <el-button type="primary" @click="refreshCurrent">刷新数据</el-button>
          </el-empty>
          <el-table
            v-else
            :data="cursorGoods"
            v-loading="currentLoading"
            stripe
            :border="false"
            size="default"
            class="commodity-table"
            :row-class-name="({ row }) => cursorRowClass(row)"
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
                <span class="price-text">{{ formatCursorPrice(row.price) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="120" align="right">
              <template #default="{ row }">
                <span class="stock-text" :class="cursorStockClass(row.stockState)">
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
                <el-tag :type="cursorStockTag(row.stockState)" effect="light" size="large">
                  {{ cursorStockText(row.stockState) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <div v-else-if="activeShop === 'ldxp'">
        <div v-if="ldxpStats.total > 0" class="stats-cards">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ ldxpStats.total }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </el-card>
          <el-card class="stat-card stat-success" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ ldxpStats.inStock }}</div>
              <div class="stat-label">库存充足</div>
            </div>
          </el-card>
          <el-card class="stat-card stat-warning" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ ldxpStats.lowStock }}</div>
              <div class="stat-label">库存紧张</div>
            </div>
          </el-card>
          <el-card class="stat-card stat-danger" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ ldxpStats.outOfStock }}</div>
              <div class="stat-label">缺货</div>
            </div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ ldxpStats.totalStock }}</div>
              <div class="stat-label">总库存量</div>
            </div>
          </el-card>
        </div>

        <el-card class="table-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">链动小铺商品列表</span>
              <span v-if="ldxpStats.total > 0" class="card-count">共 {{ ldxpStats.total }} 件商品</span>
            </div>
          </template>

          <el-empty v-if="!currentLoading && ldxpStats.total === 0" description="暂无库存数据">
            <el-button type="primary" @click="refreshCurrent">刷新数据</el-button>
          </el-empty>

          <el-table
            v-else
            :data="ldxpGoods"
            v-loading="currentLoading"
            stripe
            :border="false"
            size="default"
            class="commodity-table"
            :row-class-name="({ row }) => getLdxpRowClassName(row)"
          >
            <el-table-column label="商品" min-width="260" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="commodity-name">
                  <el-icon class="name-icon"><Goods /></el-icon>
                  <div class="name-info">
                    <div class="name-row">
                      <span class="name-text">{{ row.name }}</span>
                      <el-tag v-if="row.category?.name" size="small" effect="light">{{ row.category.name }}</el-tag>
                    </div>
                    <el-link
                      v-if="row.link"
                      :href="row.link"
                      target="_blank"
                      type="primary"
                      :underline="false"
                      class="link-entry"
                    >
                      <el-icon><Connection /></el-icon>
                      <span>前往购买</span>
                    </el-link>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="140" align="right">
              <template #default="{ row }">
                <span class="price-text">￥{{ formatLdxpPrice(row.price) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="extend.stock_count" label="库存" width="120" align="right">
              <template #default="{ row }">
                <span class="stock-text" :class="getLdxpRowClassName(row)">
                  {{ row.extend?.stock_count ?? '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="extend.limit_count" label="限购" width="100" align="center">
              <template #default="{ row }">
                <span class="limit-text">{{ row.extend?.limit_count ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="库存状态" width="150" align="center">
              <template #default="{ row }">
                <el-tag :type="getLdxpStockTagType(row.extend?.stock_count)" effect="light" size="large">
                  {{ getLdxpStockStateText(row.extend?.stock_count) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="coupon_status" label="优惠" width="120" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.coupon_status" type="success" effect="light" size="small">可用优惠</el-tag>
                <span v-else class="muted-text">-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cursor-inventory-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 优化总览界面的按钮样式 */
.page-header .el-button {
  border-radius: 10px;
  transition: all 0.2s ease;
}

.page-header .el-button--text {
  padding: 8px 16px;
}

.page-header .el-button--text:hover {
  background: rgba(99, 102, 241, 0.08);
  transform: translateX(-2px);
}

.page-header .el-button--primary {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
  transition: all 0.2s ease;
}

.page-header .el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
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

.overview-tips {
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(99, 102, 241, 0.06);
  color: #475569;
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.shop-card {
  border-radius: 14px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 320px;
}

.shop-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.12);
  border-color: #6366f1;
}

.shop-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 20px;
  min-height: 0;
}

.shop-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.shop-card__title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-card__icon {
  font-size: 20px;
  color: #6366f1;
}

.shop-card__name {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.shop-card__desc {
  font-size: 13px;
  color: #64748b;
}

.shop-card__stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin: 12px 0;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.stat-block {
  background: rgba(99, 102, 241, 0.04);
  padding: 12px;
  border-radius: 10px;
  border: 1px solid rgba(99, 102, 241, 0.08);
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin-top: 4px;
}

.stat-value.success {
  color: #22c55e;
}

.stat-value.warning {
  color: #f59e0b;
}

.shop-card__footer {
  display: flex;
  align-items: center;
  margin-top: auto;
  padding-top: 12px;
  flex-shrink: 0;
}

.update-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 优化返回总览按钮样式 */
.detail-actions .el-button--text {
  border-radius: 10px;
  padding: 8px 16px;
  transition: all 0.2s ease;
}

.detail-actions .el-button--text:hover {
  background: rgba(99, 102, 241, 0.08);
  transform: translateX(-2px);
}

/* 优化标签页样式 - 圆角设计 */
.detail-actions :deep(.el-radio-group) {
  border-radius: 12px;
  padding: 4px;
  background: rgba(99, 102, 241, 0.06);
  border: 1px solid rgba(99, 102, 241, 0.12);
}

.detail-actions :deep(.el-radio-button) {
  border-radius: 10px;
  overflow: hidden;
  margin: 0;
  border: none;
}

.detail-actions :deep(.el-radio-button__inner) {
  border-radius: 10px;
  border: none;
  padding: 10px 20px;
  background: transparent;
  color: #64748b;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: none;
}

.detail-actions :deep(.el-radio-button__inner:hover) {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.detail-actions :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
  border: none;
}

.detail-actions :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-left: none;
}

.detail-actions :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-right: none;
}

/* 优化详情页面的其他按钮 */
.detail-actions .el-button--primary,
.detail-actions .el-button--success {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-actions .el-button--primary:hover,
.detail-actions .el-button--success:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.detail-actions .el-button--primary {
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}

.detail-actions .el-button--primary:hover {
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.detail-actions .el-button--success {
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.2);
}

.detail-actions .el-button--success:hover {
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.update-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
  padding: 8px 12px;
  background: rgba(99, 102, 241, 0.08);
  border-radius: 10px;
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

.commodity-name {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.name-icon {
  font-size: 20px;
  color: #6366f1;
  margin-top: 2px;
}

.name-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 600;
  color: #0f172a;
}

.link-entry {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.price-text {
  font-weight: 600;
  color: #0f172a;
}

.stock-text {
  font-weight: 700;
}

.limit-text {
  color: #0f172a;
}

.muted-text {
  color: #94a3b8;
}

.row-success {
  background: rgba(34, 197, 94, 0.05);
}

.row-warning {
  background: rgba(245, 158, 11, 0.06);
}

.row-danger {
  background: rgba(239, 68, 68, 0.06);
}

.row-success .stock-text {
  color: #16a34a;
}

.row-warning .stock-text {
  color: #d97706;
}

.row-danger .stock-text {
  color: #dc2626;
}

.commodity-table :deep(.el-table__header) {
  background: rgba(99, 102, 241, 0.04);
}

.commodity-table :deep(.el-table__header th) {
  background: transparent;
  color: #0f172a;
  font-weight: 700;
}

.commodity-table :deep(.el-table__body tr:hover > td) {
  background-color: rgba(99, 102, 241, 0.05) !important;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .header-actions {
    width: 100%;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

