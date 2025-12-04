export interface PageResult<T> {
  items: T[]
  total: number
  pageNum: number
  pageSize: number
}

