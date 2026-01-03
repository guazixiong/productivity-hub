/**
 * 资产设置类型定义
 */

/**
 * 资产设置信息
 */
export interface AssetSettings {
  /** 退役资产不计入总额 */
  excludeRetired: boolean
  /** 总资产算二手盈利 */
  calculateProfit: boolean
}

/**
 * 更新资产设置DTO
 */
export interface AssetSettingsUpdateDTO {
  /** 退役资产不计入总额 */
  excludeRetired: boolean
  /** 总资产算二手盈利 */
  calculateProfit: boolean
}

