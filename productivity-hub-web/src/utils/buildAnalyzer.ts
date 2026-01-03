/**
 * 构建分析工具
 * 用于分析构建产物大小和性能
 */

/**
 * 分析构建产物大小
 */
export function analyzeBuildSize(assets: Array<{ name: string; size: number }>): {
  total: number
  byType: Record<string, number>
  largest: Array<{ name: string; size: number; percentage: number }>
} {
  const total = assets.reduce((sum, asset) => sum + asset.size, 0)
  const byType: Record<string, number> = {}
  const largest: Array<{ name: string; size: number; percentage: number }> = []

  assets.forEach((asset) => {
    const ext = asset.name.split('.').pop() || 'unknown'
    byType[ext] = (byType[ext] || 0) + asset.size

    largest.push({
      name: asset.name,
      size: asset.size,
      percentage: (asset.size / total) * 100,
    })
  })

  // 按大小排序
  largest.sort((a, b) => b.size - a.size)

  return {
    total,
    byType,
    largest: largest.slice(0, 10), // 返回前 10 个最大的文件
  }
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

/**
 * 生成构建报告
 */
export function generateBuildReport(analysis: ReturnType<typeof analyzeBuildSize>): string {
  let report = '\n=== 构建产物分析报告 ===\n\n'
  report += `总大小: ${formatFileSize(analysis.total)}\n\n`
  report += '按类型分类:\n'
  Object.entries(analysis.byType)
    .sort(([, a], [, b]) => b - a)
    .forEach(([type, size]) => {
      report += `  ${type}: ${formatFileSize(size)} (${((size / analysis.total) * 100).toFixed(2)}%)\n`
    })
  report += '\n最大的文件:\n'
  analysis.largest.forEach((item, index) => {
    report += `  ${index + 1}. ${item.name}: ${formatFileSize(item.size)} (${item.percentage.toFixed(2)}%)\n`
  })
  report += '\n'
  return report
}

