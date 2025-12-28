/**
 * BMI计算工具函数
 * 
 * 关联需求: REQ-006
 * 关联组件: COMP-REQ-001-01-06
 * 关联测试用例: TC-COMP-REQ-001-01-06-02, TC-COMP-REQ-001-01-06-03
 */

/**
 * BMI标准状态
 */
export type BMIStatus = '偏瘦' | '正常' | '偏胖' | '肥胖'

/**
 * 根据体重和身高计算BMI值
 * 
 * 计算公式: BMI = 体重(kg) / 身高(m)²
 * 
 * @param weightKg 体重(kg)
 * @param heightCm 身高(cm),可选
 * @returns BMI值(保留2位小数),如果身高未提供则返回null
 */
export function calculateBMI(
  weightKg: number,
  heightCm?: number
): number | null {
  if (weightKg <= 0) {
    return null
  }

  if (!heightCm || heightCm <= 0) {
    return null
  }

  // 将身高从cm转换为m
  const heightM = heightCm / 100
  const bmi = weightKg / (heightM * heightM)
  
  // 保留2位小数
  return Math.round(bmi * 100) / 100
}

/**
 * 根据BMI值判断健康状态
 * 
 * BMI标准:
 * - 偏瘦: < 18.5
 * - 正常: 18.5 - 24.9
 * - 偏胖: 25 - 29.9
 * - 肥胖: ≥ 30
 * 
 * @param bmi BMI值
 * @returns BMI状态
 */
export function getBMIStatus(bmi: number): BMIStatus {
  if (bmi < 18.5) {
    return '偏瘦'
  } else if (bmi >= 18.5 && bmi < 25) {
    return '正常'
  } else if (bmi >= 25 && bmi < 30) {
    return '偏胖'
  } else {
    return '肥胖'
  }
}

