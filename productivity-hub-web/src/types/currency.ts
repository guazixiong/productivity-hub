/**
 * 货币类型定义
 */

/**
 * 货币信息
 */
export interface Currency {
  /** 货币代码 */
  code: string
  /** 货币名称 */
  name: string
  /** 货币符号 */
  symbol: string
}

/**
 * 汇率信息
 */
export interface ExchangeRate {
  /** 源货币代码 */
  from: string
  /** 目标货币代码 */
  to: string
  /** 汇率 */
  rate: number
  /** 更新时间 */
  updateTime: string
}

/**
 * 设置默认货币DTO
 */
export interface CurrencyDefaultDTO {
  /** 货币代码 */
  currencyCode: string
}

/**
 * 获取汇率参数
 */
export interface ExchangeRateParams {
  /** 源货币代码 */
  from: string
  /** 目标货币代码 */
  to: string
}

