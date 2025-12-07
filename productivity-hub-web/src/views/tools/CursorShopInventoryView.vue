<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { cursorShopApi } from '@/services/api'
import type { CursorCommodity } from '@/types/cursorShop'

const loading = ref(false)
const commodities = ref<CursorCommodity[]>([])
const lastUpdated = ref('')

const hasData = computed(() => commodities.value.length > 0)

const formatPrice = (price: CursorCommodity['price']) => {
  if (price === null || price === undefined) return '-'
  return Number(price).toString()
}

const stockTagType = (state: CursorCommodity['stockState']) => {
  switch (state) {
    case 2:
      return 'success'
    case 1:
      return 'warning'
    case 0:
      return 'danger'
    default:
      return ''
  }
}

const loadCommodities = async () => {
  loading.value = true
  try {
    commodities.value = await cursorShopApi.commodities()
    lastUpdated.value = new Date().toLocaleString()
  } catch (error) {
    ElMessage.error((error as Error).message || '获取库存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCommodities()
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>Cursor 邮箱自助小店库存</h2>
      </div>
      <div class="actions">
        <el-button type="primary" :loading="loading" @click="loadCommodities">刷新库存</el-button>
        <span v-if="lastUpdated" class="last-updated">最近更新：{{ lastUpdated }}</span>
      </div>
    </div>

    <el-card>
      <el-empty v-if="!loading && !hasData" description="暂无库存数据" />
      <el-table
        v-else
        :data="commodities"
        v-loading="loading"
        stripe
        border
        size="small"
      >
        <el-table-column prop="name" label="商品" min-width="200" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            {{ formatPrice(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="120">
          <template #default="{ row }">
            {{ row.stock ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="orderSold" label="销量" width="120">
          <template #default="{ row }">
            {{ row.orderSold ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="stockStateText" label="库存状态" width="140">
          <template #default="{ row }">
            <el-tag :type="stockTagType(row.stockState)">{{ row.stockStateText ?? '-' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.subtitle {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.last-updated {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>

