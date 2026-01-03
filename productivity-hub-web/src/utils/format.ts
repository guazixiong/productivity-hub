/**
 * 格式化工具函数
 */

/**
 * 格式化货币金额
 * @param amount 金额
 * @param currency 货币代码，默认为 CNY
 * @returns 格式化后的货币字符串，如 ¥1,234.56
 */
export function formatCurrency(amount: number | string | null | undefined, currency: string = 'CNY'): string {
  if (amount === null || amount === undefined) {
    return '-'
  }

  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  if (isNaN(num)) {
    return '-'
  }

  // 货币符号映射
  const currencySymbols: Record<string, string> = {
    CNY: '¥',
    USD: '$',
    EUR: '€',
    GBP: '£',
    JPY: '¥',
  }

  const symbol = currencySymbols[currency] || currency + ' '

  // 保留2位小数，添加千分位分隔符
  const formatted = num.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })

  return `${symbol}${formatted}`
}

/**
 * 格式化日期
 * @param date 日期字符串或 Date 对象
 * @param format 格式，默认为 'YYYY-MM-DD'
 * @returns 格式化后的日期字符串
 */
export function formatDate(
  date: string | Date | null | undefined,
  format: string = 'YYYY-MM-DD'
): string {
  if (!date) {
    return '-'
  }

  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) {
    return '-'
  }

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期时间
 * @param date 日期字符串、Date 对象或时间戳（毫秒）
 * @param format 格式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期时间字符串
 */
export function formatDateTime(
  date: string | Date | number | null | undefined,
  format: string = 'YYYY-MM-DD HH:mm:ss'
): string {
  if (date === null || date === undefined) {
    return '-'
  }

  let d: Date
  if (typeof date === 'number') {
    // 如果是时间戳，判断是秒还是毫秒（大于 10^10 认为是毫秒）
    d = new Date(date > 10000000000 ? date : date * 1000)
  } else if (typeof date === 'string') {
    d = new Date(date)
  } else {
    d = date
  }

  if (isNaN(d.getTime())) {
    return '-'
  }

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化数字（添加千分位分隔符）
 * @param num 数字
 * @param decimals 小数位数，默认为 0
 * @returns 格式化后的数字字符串
 */
export function formatNumber(num: number | string | null | undefined, decimals: number = 0): string {
  if (num === null || num === undefined) {
    return '-'
  }

  const n = typeof num === 'string' ? parseFloat(num) : num
  if (isNaN(n)) {
    return '-'
  }

  return n.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  })
}

