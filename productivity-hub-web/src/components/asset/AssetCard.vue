<template>
  <div class="asset-card">
    <el-card shadow="hover" class="card">
      <div class="asset-content">
        <div class="asset-image">
          <img v-if="asset.image" :src="asset.image" :alt="asset.name" />
          <div v-else class="no-image">
            <el-icon :size="48"><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
        </div>
        <div class="asset-info">
          <div class="asset-name">{{ asset.name }}</div>
          <div class="asset-category">
            <el-icon><Folder /></el-icon>
            {{ asset.categoryName }}
          </div>
          <div class="asset-price">{{ formatCurrency(asset.price) }}</div>
          <div class="asset-meta">
            <div class="meta-item">
              <span class="meta-label">总价值：</span>
              <span class="meta-value">{{ formatCurrency(asset.totalValue) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">日均价格：</span>
              <span class="meta-value">{{ formatCurrency(asset.dailyAveragePrice) }}</span>
            </div>
          </div>
          <div class="asset-status">
            <el-tag :type="getStatusType(asset.status)" size="large">
              {{ formatStatus(asset.status) }}
            </el-tag>
          </div>
        </div>
        <div class="asset-actions">
          <el-button type="primary" size="small" @click="handleView">
            <el-icon><View /></el-icon>
            查看
          </el-button>
          <el-button size="small" @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Picture, Folder, View, Edit, Delete } from '@element-plus/icons-vue'
import type { AssetListItem, AssetStatus } from '@/types/asset'
import { formatCurrency } from '@/utils/format'

const props = defineProps<{
  asset: AssetListItem
}>()

const emit = defineEmits<{
  view: [asset: AssetListItem]
  edit: [asset: AssetListItem]
  delete: [asset: AssetListItem]
}>()

const getStatusType = (status: AssetStatus): 'success' | 'warning' | 'info' => {
  const statusMap: Record<AssetStatus, 'success' | 'warning' | 'info'> = {
    IN_SERVICE: 'success',
    RETIRED: 'warning',
    SOLD: 'info',
  }
  return statusMap[status] || 'info'
}

const formatStatus = (status: AssetStatus): string => {
  const statusMap: Record<AssetStatus, string> = {
    IN_SERVICE: '正在服役',
    RETIRED: '已退役',
    SOLD: '已卖出',
  }
  return statusMap[status] || status
}

const handleView = () => {
  emit('view', props.asset)
}

const handleEdit = () => {
  emit('edit', props.asset)
}

const handleDelete = () => {
  emit('delete', props.asset)
}
</script>

<style scoped lang="scss">
.asset-card {
  .card {
    margin-bottom: 16px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
  }

  .asset-content {
    display: flex;
    gap: 16px;

    .asset-image {
      width: 120px;
      height: 120px;
      flex-shrink: 0;
      border-radius: 8px;
      overflow: hidden;
      background: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .no-image {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #c0c4cc;
        font-size: 12px;
        gap: 8px;
      }
    }

    .asset-info {
      flex: 1;
      min-width: 0;

      .asset-name {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .asset-category {
        font-size: 14px;
        color: #909399;
        margin-bottom: 12px;
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .asset-price {
        font-size: 20px;
        font-weight: 600;
        color: #409eff;
        margin-bottom: 12px;
      }

      .asset-meta {
        display: flex;
        flex-direction: column;
        gap: 4px;
        margin-bottom: 12px;
        font-size: 13px;

        .meta-item {
          display: flex;
          align-items: center;

          .meta-label {
            color: #909399;
            margin-right: 4px;
          }

          .meta-value {
            color: #606266;
            font-weight: 500;
          }
        }
      }

      .asset-status {
        margin-top: 8px;
      }
    }

    .asset-actions {
      display: flex;
      flex-direction: column;
      gap: 8px;
      flex-shrink: 0;
      justify-content: flex-start;
    }
  }
}
</style>

