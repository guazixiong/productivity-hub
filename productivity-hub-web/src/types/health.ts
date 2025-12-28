// 健康模块类型定义

// ==================== 运动模块 ====================

// 运动类型枚举
export type ExerciseType = '跑步' | '游泳' | '骑行' | '力量训练' | '瑜伽' | '有氧运动' | '球类运动' | '其他'

// 运动记录
export interface ExerciseRecord {
  id: string
  userId: string
  exerciseType: ExerciseType
  exerciseDate: string
  durationMinutes: number
  caloriesBurned?: number
  distanceKm?: number
  heartRateAvg?: number
  heartRateMax?: number
  trainingPlanId?: string
  trainingPlanName?: string
  exerciseActionRefUrl?: string[]
  notes?: string
  createdAt: string
  updatedAt: string
}

// 运动记录查询参数
export interface ExerciseRecordQuery {
  pageNum?: number
  pageSize?: number
  exerciseType?: string[]
  startDate?: string
  endDate?: string
  trainingPlanId?: string
  sortField?: 'exerciseDate' | 'durationMinutes' | 'caloriesBurned'
  sortOrder?: 'asc' | 'desc'
}

// 运动记录创建/更新DTO
export interface ExerciseRecordDTO {
  exerciseType: ExerciseType
  exerciseDate?: string
  durationMinutes: number
  caloriesBurned?: number
  distanceKm?: number
  heartRateAvg?: number
  heartRateMax?: number
  trainingPlanId?: string
  exerciseActionRefUrl?: string[]
  notes?: string
}

// 训练计划类型枚举
export type PlanType = '减脂' | '增肌' | '塑形' | '耐力提升' | '康复训练' | '其他'

// 训练计划状态枚举
export type PlanStatus = 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'

// 训练计划
export interface TrainingPlan {
  id: string
  userId: string
  planName: string
  planType: PlanType
  targetDurationDays?: number
  targetCaloriesPerDay?: number
  description?: string
  status: PlanStatus
  startDate?: string
  endDate?: string
  exerciseRecordCount?: number
  totalCalories?: number
  createdAt: string
  updatedAt: string
}

// 训练计划创建/更新DTO
export interface TrainingPlanDTO {
  planName: string
  planType: PlanType
  targetDurationDays?: number
  targetCaloriesPerDay?: number
  description?: string
  startDate?: string
  endDate?: string
}

// 运动统计数据
export interface ExerciseStatistics {
  totalDuration: number
  totalCount: number
  totalCalories: number
  totalDistance: number
  averageDuration: number
  averageCalories: number
  typeStatistics: Array<{
    exerciseType: string
    count: number
    totalDuration: number
    totalCalories: number
    totalDistance: number
  }>
  planStatistics: Array<{
    planId: string
    planName: string
    count: number
    totalDuration: number
    totalCalories: number
  }>
}

// 运动趋势数据
export interface ExerciseTrend {
  type: 'duration' | 'count' | 'calories'
  days: number
  data: Array<{
    date: string
    value: number
  }>
}

// ==================== 饮水模块 ====================

// 饮水类型枚举
export type WaterType = '白开水' | '矿泉水' | '纯净水' | '茶水' | '咖啡' | '果汁' | '运动饮料' | '其他'

// 饮水记录
export interface WaterIntake {
  id: string
  userId: string
  intakeDate: string
  intakeTime: string
  waterType: WaterType
  volumeMl: number
  notes?: string
  createdAt: string
  updatedAt: string
}

// 饮水记录查询参数
export interface WaterIntakeQuery {
  pageNum?: number
  pageSize?: number
  startDate?: string
  endDate?: string
  waterType?: string[]
}

// 饮水记录创建/更新DTO
export interface WaterIntakeDTO {
  intakeDate?: string
  intakeTime?: string
  waterType: WaterType
  volumeMl: number
  notes?: string
}

// 饮水目标
export interface WaterTarget {
  id: string
  userId: string
  dailyTargetMl: number
  /** @deprecated 是否提醒由定时任务开关控制，不再使用此字段 */
  reminderEnabled?: number
  reminderIntervals?: string[]
  createdAt: string
  updatedAt: string
}

// 饮水目标设置DTO
export interface WaterTargetDTO {
  dailyTargetMl: number
  reminderIntervals?: string[]
}

// 饮水进度
export interface WaterProgress {
  date: string
  dailyTargetMl: number
  totalIntakeMl: number
  remainingMl: number
  progressPercent: number
  isAchieved: boolean
  intakeCount: number
}

// 饮水统计数据
export interface WaterStatistics {
  totalIntakeMl: number
  totalCount: number
  averageIntakeMl: number
  achievementDays: number
  typeStatistics: Array<{
    waterType: string
    count: number
    totalIntakeMl: number
  }>
}

// 饮水趋势数据
export interface WaterTrend {
  days: number
  data: Array<{
    date: string
    totalIntakeMl: number
    achievementPercent: number
    isAchieved: boolean
  }>
}

// ==================== 体重模块 ====================

// 体重记录
export interface WeightRecord {
  id: string
  userId: string
  recordDate: string
  recordTime: string
  weightKg: number
  bodyFatPercentage?: number
  muscleMassKg?: number
  heightCm?: number
  bmi?: number
  healthStatus?: string
  notes?: string
  createdAt: string
  updatedAt: string
}

// 体重记录查询参数
export interface WeightRecordQuery {
  pageNum?: number
  pageSize?: number
  startDate?: string
  endDate?: string
}

// 体重记录创建/更新DTO
export interface WeightRecordDTO {
  recordDate?: string
  recordTime?: string
  weightKg: number
  bodyFatPercentage?: number
  muscleMassKg?: number
  heightCm?: number
  notes?: string
}

// 性别枚举
export type Gender = 'MALE' | 'FEMALE' | 'OTHER'

// 用户身体信息
export interface UserBodyInfo {
  id: string
  userId: string
  heightCm?: number
  birthDate?: string
  gender?: Gender
  targetWeightKg?: number
  resendEmail?: string
  createdAt: string
  updatedAt: string
}

// 身体信息设置DTO
export interface UserBodyInfoDTO {
  heightCm?: number
  birthDate?: string
  gender?: Gender
  targetWeightKg?: number
  resendEmail?: string
}

// 体重统计数据
export interface WeightStatistics {
  latestWeight: number
  targetWeight?: number
  gapFromTarget?: number
  averageWeight: number
  maxWeight: number
  minWeight: number
  weightChange?: number
  weightChangeRate?: number
  recordCount: number
}

// 体重趋势数据
export interface WeightTrend {
  days: number
  data: Array<{
    date: string
    weightKg: number
    bmi?: number
    bodyFatPercentage?: number
    changeFromPrevious?: number
  }>
  latestWeight: number
  targetWeight?: number
  gapFromTarget?: number
  averageWeight: number
  maxWeight: number
  minWeight: number
}

// ==================== 综合统计 ====================

// 健康数据概览
export interface HealthOverview {
  exercise: {
    todayDuration: number
    todayCount: number
    todayCalories: number
    weekDuration: number
    weekCount: number
    weekCalories: number
    monthDuration: number
    monthCount: number
    monthCalories: number
  }
  water: {
    todayIntakeMl: number
    todayTargetMl: number
    todayProgress: number
    todayIsAchieved: boolean
    weekIntakeMl: number
    weekAchievementDays: number
    monthIntakeMl: number
    monthAchievementDays: number
  }
  weight: {
    latestWeight: number
    targetWeight?: number
    gapFromTarget?: number
    weekChange?: number
    monthChange?: number
  }
  dataCompleteness: {
    exerciseDays: number
    waterDays: number
    weightDays: number
    totalDays: number
    completenessPercent: number
  }
}

// 健康数据日历
export interface HealthCalendar {
  year: number
  month: number
  days: Array<{
    date: string
    hasExercise: boolean
    hasWater: boolean
    hasWeight: boolean
  }>
}

