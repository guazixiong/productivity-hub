export interface CursorCommodity {
  id: number
  name: string
  price: number | null
  stock: number | null
  orderSold: number | null
  stockState: number | null
  stockStateText: string
}

