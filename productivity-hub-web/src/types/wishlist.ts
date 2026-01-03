/**
 * 心愿单相关类型定义
 */

/**
 * 心愿单视图对象
 */
export interface Wishlist {
  id: string
  name: string
  price: number
  link?: string
  remark?: string
  achieved: boolean
  achievedAt?: string
  createdAt?: string
  syncedToAsset?: boolean // 是否已同步至资产
}

/**
 * 心愿单创建 DTO
 */
export interface WishlistCreateDTO {
  name: string
  price: number
  link?: string
  remark?: string
}

/**
 * 心愿单更新 DTO
 */
export interface WishlistUpdateDTO {
  id: string
  name: string
  price: number
  link?: string
  remark?: string
  achieved?: boolean
  achievedAt?: string
}

/**
 * 心愿单查询参数
 */
export interface WishlistQueryParams {
  achieved?: boolean
}

