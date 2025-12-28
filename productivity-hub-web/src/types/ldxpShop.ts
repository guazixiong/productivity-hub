export interface LdxpGoodsCategory {
  id: number
  name: string
}

export interface LdxpGoodsUser {
  link: string
  nickname: string
  avatar: string
  token: string
}

export interface LdxpGoodsExtend {
  stock_count: number
  show_stock_type: number
  send_order: number
  limit_count: number
}

export interface LdxpGoodsItem {
  link: string
  goods_type: string
  goods_key: string
  name: string
  price: number
  market_price: number
  description: string
  create_time: number
  image: string
  coupon_status: number
  category: LdxpGoodsCategory
  user: LdxpGoodsUser
  discount: unknown
  multipleoffers: unknown
  fullgift: unknown
  extend: LdxpGoodsExtend
}

export interface LdxpGoodsQuery {
  token: string
  keywords?: string
  category_id?: number
  goods_type?: string
  current?: number
  pageSize?: number
}

export interface LdxpGoodsResponse {
  code: number
  msg: string
  time: number
  data: {
    total: number
    list: LdxpGoodsItem[]
  }
}

