/**
 * 卡路里计算工具函数单元测试
 * 
 * 关联测试用例: TC-COMP-REQ-001-01-04-01, TC-COMP-REQ-001-01-04-02
 * 关联需求: REQ-004
 * 关联组件: COMP-REQ-001-01-04
 */

import { describe, it, expect } from 'vitest'
import { calculateCalories, type ExerciseType } from '../caloriesCalculator'

describe('calculateCalories', () => {
  it('TC-COMP-REQ-001-01-04-01: 应该正确计算跑步的卡路里', () => {
    const result = calculateCalories('跑步', 30)
    expect(result).toBe(330) // 11 * 30
  })

  it('TC-COMP-REQ-001-01-04-01: 应该正确计算游泳的卡路里', () => {
    const result = calculateCalories('游泳', 30)
    expect(result).toBe(270) // 9 * 30
  })

  it('TC-COMP-REQ-001-01-04-01: 应该正确计算骑行的卡路里', () => {
    const result = calculateCalories('骑行', 30)
    expect(result).toBe(210) // 7 * 30
  })

  it('TC-COMP-REQ-001-01-04-01: 应该正确计算力量训练的卡路里', () => {
    const result = calculateCalories('力量训练', 30)
    expect(result).toBe(180) // 6 * 30
  })

  it('TC-COMP-REQ-001-01-04-01: 应该正确计算瑜伽的卡路里', () => {
    const result = calculateCalories('瑜伽', 30)
    expect(result).toBe(105) // 3.5 * 30
  })

  it('TC-COMP-REQ-001-01-04-01: 应该正确计算有氧运动的卡路里', () => {
    const result = calculateCalories('有氧运动', 30)
    expect(result).toBe(240) // 8 * 30
  })

  it('TC-COMP-REQ-001-01-04-01: 应该正确计算球类运动的卡路里', () => {
    const result = calculateCalories('球类运动', 30)
    expect(result).toBe(270) // 9 * 30
  })

  it('TC-COMP-REQ-001-01-04-01: 应该正确计算其他的卡路里', () => {
    const result = calculateCalories('其他', 30)
    expect(result).toBe(150) // 5 * 30
  })

  it('TC-COMP-REQ-001-01-04-02: 应该处理不同时长的计算', () => {
    expect(calculateCalories('跑步', 10)).toBe(110) // 11 * 10
    expect(calculateCalories('跑步', 60)).toBe(660) // 11 * 60
    expect(calculateCalories('跑步', 90)).toBe(990) // 11 * 90
  })

  it('TC-COMP-REQ-001-01-04-02: 应该处理边界值', () => {
    expect(calculateCalories('跑步', 0)).toBe(0)
    expect(calculateCalories('跑步', 1)).toBe(11)
    expect(calculateCalories('跑步', -10)).toBe(0)
  })

  it('TC-COMP-REQ-001-01-04-02: 应该四舍五入结果', () => {
    // 瑜伽 3.5 * 10 = 35
    expect(calculateCalories('瑜伽', 10)).toBe(35)
    // 瑜伽 3.5 * 11 = 38.5, 应该四舍五入为39
    expect(calculateCalories('瑜伽', 11)).toBe(39)
  })
})

