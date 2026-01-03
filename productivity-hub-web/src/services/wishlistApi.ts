import { request } from './http'
import type {
  Wishlist,
  WishlistCreateDTO,
  WishlistUpdateDTO,
  WishlistQueryParams,
} from '@/types/wishlist'

/**
 * 心愿单API
 */
export const wishlistApi = {
  /**
   * 获取心愿单列表
   */
  getWishlistList: (params?: WishlistQueryParams) =>
    request<Wishlist[]>({
      url: '/api/wishlist',
      method: 'GET',
      params,
    }),

  /**
   * 根据ID获取心愿单
   */
  getWishlistById: (id: string) =>
    request<Wishlist>({
      url: `/api/wishlist/${id}`,
      method: 'GET',
    }),

  /**
   * 创建心愿单
   */
  createWishlist: (data: WishlistCreateDTO) =>
    request<Wishlist>({
      url: '/api/wishlist',
      method: 'POST',
      data,
    }),

  /**
   * 更新心愿单
   */
  updateWishlist: (data: WishlistUpdateDTO) =>
    request<Wishlist>({
      url: '/api/wishlist',
      method: 'PUT',
      data,
    }),

  /**
   * 删除心愿单
   */
  deleteWishlist: (id: string) =>
    request<void>({
      url: `/api/wishlist/${id}`,
      method: 'DELETE',
    }),
}

