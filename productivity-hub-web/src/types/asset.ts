/**
 * 资产 & 资产分类相关类型定义
 */

/**
 * 资产分类视图对象
 */
export interface AssetCategory {
  id: string
  name: string
  icon?: string
  parentId?: string
  level?: number
  children?: AssetCategory[]
  isDefault?: boolean
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
  assetCount?: number
}

/**
 * 资产分类创建DTO
 */
export interface AssetCategoryCreateDTO {
  name: string
  icon?: string
  parentId?: string
}

/**
 * 资产分类更新DTO
 */
export interface AssetCategoryUpdateDTO {
  id: string
  name: string
  icon?: string
  parentId?: string
}

/**
 * 资产列表项（对应后端 AssetListVO）
 */
export interface AssetListItem {
  id: string
  // 详情接口会返回下面这些字段，这里列表只用到部分字段
  name: string
  price: number
  image?: string
  categoryId: string
  categoryName: string
  status: AssetStatus
  purchaseDate: string
  totalValue: number
  dailyAveragePrice: number
  usageDays?: number
  additionalFeesTotal?: number // 列表接口返回的是合计金额
}

/**
 * 资产状态
 */
export type AssetStatus = 'IN_SERVICE' | 'RETIRED' | 'SOLD'

/**
 * 附加费用视图对象（对应后端 AssetAdditionalFeeVO）
 */
export interface AssetAdditionalFee {
  id?: string
  name: string
  amount: number
  feeDate: string
  remark?: string
}

/**
 * 附加费用创建 DTO（对应后端 AssetAdditionalFeeCreateDTO）
 */
export interface AssetAdditionalFeeCreateDTO {
  assetId: string
  name: string
  amount: number
  feeDate: string
  remark?: string
}

/**
 * 附加费用更新 DTO（对应后端 AssetAdditionalFeeUpdateDTO）
 */
export interface AssetAdditionalFeeUpdateDTO {
  id: string
  name: string
  amount: number
  feeDate: string
  remark?: string
}

/**
 * 资产分页结果（对应后端 AssetPageVO）
 */
export interface AssetPageResult {
  list: AssetListItem[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * 资产列表查询参数（对应后端 AssetQueryDTO）
 */
export interface AssetQueryParams {
  pageNum?: number
  pageSize?: number
  categoryId?: string
  status?: AssetStatus
}

/**
 * 资产详情（对应后端 AssetDetailVO）
 */
export interface AssetDetail {
  id: string
  name: string
  price: number
  image?: string
  remark?: string
  categoryId: string
  categoryName?: string
  purchaseDate: string
  warrantyEnabled?: boolean
  warrantyEndDate?: string
  depreciationByUsageCount?: boolean
  expectedUsageCount?: number
  depreciationByUsageDate?: boolean
  usageDate?: string
  inService?: boolean
  retiredDate?: string
  status: AssetStatus
  totalValue: number
  usageDays: number
  dailyAveragePrice: number
  additionalFees?: AssetAdditionalFee[] // 详情接口返回的是费用列表
}

/**
 * 资产创建 DTO（对应后端 AssetCreateDTO）
 */
export interface AssetCreateDTO {
  name: string
  price: number
  image?: string
  remark?: string
  categoryId: string
  purchaseDate: string
  warrantyEnabled?: boolean
  warrantyEndDate?: string
  depreciationByUsageCount?: boolean
  expectedUsageCount?: number
  depreciationByUsageDate?: boolean
  usageDate?: string
  inService?: boolean
  retiredDate?: string
  // 注意：创建/更新资产时，附加费用通过独立的API接口管理，不在此DTO中
}

/**
 * 资产更新 DTO（对应后端 AssetUpdateDTO）
 */
export interface AssetUpdateDTO extends AssetCreateDTO {
  id: string
  version: number
}


