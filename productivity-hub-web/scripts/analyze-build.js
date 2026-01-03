/**
 * 构建分析脚本
 * 分析构建产物大小和性能
 */

import { readdir, stat } from 'fs/promises'
import { join } from 'path'
import { fileURLToPath } from 'url'
import { dirname } from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

async function getFileSize(filePath) {
  try {
    const stats = await stat(filePath)
    return stats.size
  } catch {
    return 0
  }
}

async function analyzeDirectory(dir, basePath = '') {
  const files = []
  const entries = await readdir(dir, { withFileTypes: true })

  for (const entry of entries) {
    const fullPath = join(dir, entry.name)
    const relativePath = join(basePath, entry.name)

    if (entry.isDirectory()) {
      const subFiles = await analyzeDirectory(fullPath, relativePath)
      files.push(...subFiles)
    } else {
      const size = await getFileSize(fullPath)
      files.push({
        name: relativePath,
        size,
      })
    }
  }

  return files
}

async function main() {
  const distDir = join(__dirname, '../dist')
  
  try {
    console.log('开始分析构建产物...\n')
    const assets = await analyzeDirectory(distDir)
    
    const total = assets.reduce((sum, asset) => sum + asset.size, 0)
    const byType = {}
    
    assets.forEach((asset) => {
      const ext = asset.name.split('.').pop() || 'unknown'
      byType[ext] = (byType[ext] || 0) + asset.size
    })
    
    const largest = [...assets]
      .sort((a, b) => b.size - a.size)
      .slice(0, 10)
      .map((item) => ({
        ...item,
        percentage: (item.size / total) * 100,
      }))
    
    console.log('=== 构建产物分析报告 ===\n')
    console.log(`总大小: ${formatFileSize(total)}\n`)
    console.log('按类型分类:')
    Object.entries(byType)
      .sort(([, a], [, b]) => b - a)
      .forEach(([type, size]) => {
        console.log(`  ${type}: ${formatFileSize(size)} (${((size / total) * 100).toFixed(2)}%)`)
      })
    console.log('\n最大的文件:')
    largest.forEach((item, index) => {
      console.log(`  ${index + 1}. ${item.name}: ${formatFileSize(item.size)} (${item.percentage.toFixed(2)}%)`)
    })
    console.log('')
  } catch (error) {
    console.error('分析失败:', error.message)
    process.exit(1)
  }
}

function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

main()

