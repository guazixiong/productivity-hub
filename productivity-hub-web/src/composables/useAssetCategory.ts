import { ref, computed } from 'vue'
import { assetCategoryApi } from '@/services/assetApi'
import type { AssetCategory } from '@/types/asset'

// 缓存时间：5分钟
const CACHE_TTL = 5 * 60 * 1000

// 全局缓存状态
const categoriesCache = ref<AssetCategory[]>([])
const lastFetched = ref<number>(0)
const isLoading = ref(false)

/**
 * 资产分类缓存 composable
 * 提供分类数据的缓存机制，避免重复请求
 */
export function useAssetCategory() {
  const isStale = computed(() => Date.now() - lastFetched.value > CACHE_TTL)

  /**
   * 获取可选择的分类列表（带缓存）
   */
  const getSelectableCategories = async (force = false): Promise<AssetCategory[]> => {
    // 如果缓存有效且不是强制刷新，直接返回缓存
    if (!force && categoriesCache.value.length > 0 && !isStale.value) {
      return categoriesCache.value
    }

    // 如果正在加载，等待加载完成
    if (isLoading.value) {
      return new Promise((resolve) => {
        const checkInterval = setInterval(() => {
          if (!isLoading.value) {
            clearInterval(checkInterval)
            resolve(categoriesCache.value)
          }
        }, 50)
      })
    }

    isLoading.value = true
    try {
      const data = await assetCategoryApi.getSelectableCategories()
      categoriesCache.value = data || []
      lastFetched.value = Date.now()
      return categoriesCache.value
    } catch (error) {
      console.error('加载资产分类失败:', error)
      // 如果请求失败，返回缓存数据（如果有）
      return categoriesCache.value
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 根据分类ID查找分类名称
   */
  const findCategoryName = (categoryId: string, categories?: AssetCategory[]): string => {
    const list = categories || categoriesCache.value
    for (const category of list) {
      if (category.id === categoryId) {
        return category.name
      }
      if (category.children) {
        for (const child of category.children) {
          if (child.id === categoryId) {
            return `${category.name} / ${child.name}`
          }
        }
      }
    }
    return ''
  }

  /**
   * 清除缓存（用于分类更新后刷新）
   */
  const clearCache = () => {
    categoriesCache.value = []
    lastFetched.value = 0
  }

  /**
   * 刷新缓存
   */
  const refreshCache = async () => {
    return await getSelectableCategories(true)
  }

  return {
    getSelectableCategories,
    findCategoryName,
    clearCache,
    refreshCache,
    categories: computed(() => categoriesCache.value),
    isLoading: computed(() => isLoading.value),
  }
}

