/**
 * 卡路里计算工具函数
 */

/**
 * 运动类型定义
 */
export type ExerciseType = 
  | '跑步' 
  | '游泳' 
  | '骑行' 
  | '力量训练' 
  | '瑜伽' 
  | '有氧运动' 
  | '球类运动' 
  | '其他'

/**
 * 运动类型系数映射表(千卡/分钟)
 */
const EXERCISE_COEFFICIENTS: Record<ExerciseType, number> = {
  跑步: 11,        // 取10-12的中间值
  游泳: 9,         // 取8-10的中间值
  骑行: 7,         // 取6-8的中间值
  力量训练: 6,     // 取5-7的中间值
  瑜伽: 3.5,       // 取3-4的中间值
  有氧运动: 8,     // 取7-9的中间值
  球类运动: 9,     // 取8-10的中间值
  其他: 5,         // 默认值
}

/**
 * 根据运动类型和时长计算卡路里消耗
 * 
 * 计算公式: 卡路里 = 运动类型系数 × 时长(分钟)
 * 
 * @param exerciseType 运动类型
 * @param durationMinutes 运动时长(分钟)
 * @returns 卡路里数值(整数,四舍五入)
 */
export function calculateCalories(
  exerciseType: ExerciseType,
  durationMinutes: number
): number {
  if (durationMinutes <= 0) {
    return 0
  }

  const coefficient = EXERCISE_COEFFICIENTS[exerciseType] || EXERCISE_COEFFICIENTS.其他
  const calories = coefficient * durationMinutes
  
  return Math.round(calories)
}

