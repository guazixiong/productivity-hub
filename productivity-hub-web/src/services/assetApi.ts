import { request } from './http'
import type {
  AssetCategory,
  AssetCategoryCreateDTO,
  AssetCategoryUpdateDTO,
  AssetListItem,
  AssetDetail,
  AssetPageResult,
  AssetQueryParams,
  AssetCreateDTO,
  AssetUpdateDTO,
  AssetAdditionalFee,
  AssetAdditionalFeeCreateDTO,
  AssetAdditionalFeeUpdateDTO,
} from '@/types/asset'

/**
 * 资产分类API
 */
export const assetCategoryApi = {
  /**
   * 获取所有分类（仅管理员）
   */
  getAllCategories: () =>
    request<AssetCategory[]>({
      url: '/api/asset/categories',
      method: 'GET',
    }),

  /**
   * 获取所有分类（用于选择，所有用户可访问）
   */
  getSelectableCategories: () =>
    request<AssetCategory[]>({
      url: '/api/asset/categories/selectable',
      method: 'GET',
    }),

  /**
   * 根据ID获取分类
   */
  getCategoryById: (id: string) =>
    request<AssetCategory>({
      url: `/api/asset/categories/${id}`,
      method: 'GET',
    }),

  /**
   * 创建分类
   */
  createCategory: (data: AssetCategoryCreateDTO) =>
    request<AssetCategory>({
      url: '/api/asset/categories',
      method: 'POST',
      data,
    }),

  /**
   * 更新分类
   */
  updateCategory: (data: AssetCategoryUpdateDTO) =>
    request<AssetCategory>({
      url: '/api/asset/categories',
      method: 'PUT',
      data,
    }),

  /**
   * 删除分类
   */
  deleteCategory: (id: string) =>
    request<void>({
      url: `/api/asset/categories/${id}`,
      method: 'DELETE',
    }),
}

/**
 * 资产管理 API
 */
export const assetApi = {
  /**
   * 获取资产分页列表
   */
  getAssetPage: (params: AssetQueryParams) =>
    request<AssetPageResult>({
      url: '/api/asset/assets',
      method: 'GET',
      params,
    }),

  /**
   * 根据ID获取资产详情
   */
  getAssetById: (id: string) =>
    request<AssetDetail>({
      url: `/api/asset/assets/${id}`,
      method: 'GET',
    }),

  /**
   * 创建资产
   */
  createAsset: (data: AssetCreateDTO) =>
    request<AssetDetail>({
      url: '/api/asset/assets',
      method: 'POST',
      data,
    }),

  /**
   * 更新资产
   */
  updateAsset: (data: AssetUpdateDTO) =>
    request<AssetDetail>({
      url: '/api/asset/assets',
      method: 'PUT',
      data,
    }),

  /**
   * 删除资产
   */
  deleteAsset: (id: string) =>
    request<void>({
      url: `/api/asset/assets/${id}`,
      method: 'DELETE',
    }),

  /**
   * 批量删除资产
   */
  batchDeleteAssets: (ids: string[]) =>
    request<void>({
      url: '/api/asset/assets/batch',
      method: 'DELETE',
      data: ids,
    }),
}

/**
 * 附加费用管理 API
 */
export const assetAdditionalFeeApi = {
  /**
   * 根据资产ID获取附加费用列表
   */
  getFeesByAssetId: (assetId: string) =>
    request<AssetAdditionalFee[]>({
      url: `/api/asset/assets/${assetId}/fees`,
      method: 'GET',
    }),

  /**
   * 创建附加费用
   */
  createFee: (data: AssetAdditionalFeeCreateDTO) =>
    request<AssetAdditionalFee>({
      url: '/api/asset/fees',
      method: 'POST',
      data,
    }),

  /**
   * 更新附加费用
   */
  updateFee: (data: AssetAdditionalFeeUpdateDTO) =>
    request<AssetAdditionalFee>({
      url: '/api/asset/fees',
      method: 'PUT',
      data,
    }),

  /**
   * 删除附加费用
   */
  deleteFee: (id: string) =>
    request<void>({
      url: `/api/asset/fees/${id}`,
      method: 'DELETE',
    }),
}

