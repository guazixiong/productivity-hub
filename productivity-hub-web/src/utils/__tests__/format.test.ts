/**
 * 格式化工具函数单元测试
 */

import { describe, it, expect } from 'vitest'
import { formatCurrency } from '../format'

describe('formatCurrency', () => {
  it('应该正确格式化货币', () => {
    expect(formatCurrency(12999.0)).toBe('¥12,999.00')
    expect(formatCurrency(1000)).toBe('¥1,000.00')
    expect(formatCurrency(0)).toBe('¥0.00')
  })

  it('应该处理小数', () => {
    expect(formatCurrency(12999.99)).toBe('¥12,999.99')
    expect(formatCurrency(0.5)).toBe('¥0.50')
  })

  it('应该处理负数', () => {
    expect(formatCurrency(-1000)).toBe('-¥1,000.00')
  })

  it('应该处理大数字', () => {
    expect(formatCurrency(1000000)).toBe('¥1,000,000.00')
  })
})

