/**
 * BMI计算工具函数单元测试
 */

import { describe, it, expect } from 'vitest'
import { calculateBMI, getBMIStatus } from '../bmiCalculator'

describe('calculateBMI', () => {
  it('TC-COMP-REQ-001-01-06-02: 应该正确计算BMI值', () => {
    // 体重68.5kg, 身高175cm
    // BMI = 68.5 / (1.75²) = 68.5 / 3.0625 ≈ 22.36
    const result = calculateBMI(68.5, 175)
    expect(result).toBeCloseTo(22.36, 2)
  })

  it('TC-COMP-REQ-001-01-06-02: 应该保留2位小数', () => {
    const result = calculateBMI(70, 180)
    // BMI = 70 / (1.8²) = 70 / 3.24 ≈ 21.60
    expect(result).toBeCloseTo(21.60, 2)
  })

  it('TC-COMP-REQ-001-01-06-03: 应该处理身高未设置的情况', () => {
    const result = calculateBMI(68.5, undefined)
    expect(result).toBeNull()
  })

  it('TC-COMP-REQ-001-01-06-03: 应该处理身高为0的情况', () => {
    const result = calculateBMI(68.5, 0)
    expect(result).toBeNull()
  })

  it('TC-COMP-REQ-001-01-06-03: 应该处理体重为0的情况', () => {
    const result = calculateBMI(0, 175)
    expect(result).toBeNull()
  })

  it('应该处理边界值', () => {
    // 正常范围
    expect(calculateBMI(50, 160)).toBeCloseTo(19.53, 2)
    expect(calculateBMI(80, 180)).toBeCloseTo(24.69, 2)
  })
})

describe('getBMIStatus', () => {
  it('TC-COMP-REQ-001-01-06-02: 应该正确判断偏瘦', () => {
    expect(getBMIStatus(18.0)).toBe('偏瘦')
    expect(getBMIStatus(17.5)).toBe('偏瘦')
  })

  it('TC-COMP-REQ-001-01-06-02: 应该正确判断正常', () => {
    expect(getBMIStatus(20.0)).toBe('正常')
    expect(getBMIStatus(22.5)).toBe('正常')
    expect(getBMIStatus(24.9)).toBe('正常')
  })

  it('TC-COMP-REQ-001-01-06-02: 应该正确判断偏胖', () => {
    expect(getBMIStatus(25.0)).toBe('偏胖')
    expect(getBMIStatus(27.5)).toBe('偏胖')
    expect(getBMIStatus(29.9)).toBe('偏胖')
  })

  it('TC-COMP-REQ-001-01-06-02: 应该正确判断肥胖', () => {
    expect(getBMIStatus(30.0)).toBe('肥胖')
    expect(getBMIStatus(35.0)).toBe('肥胖')
  })

  it('应该处理边界值', () => {
    expect(getBMIStatus(18.5)).toBe('正常')
    expect(getBMIStatus(18.4)).toBe('偏瘦')
    expect(getBMIStatus(24.9)).toBe('正常')
    expect(getBMIStatus(25.0)).toBe('偏胖')
    expect(getBMIStatus(29.9)).toBe('偏胖')
    expect(getBMIStatus(30.0)).toBe('肥胖')
  })
})

