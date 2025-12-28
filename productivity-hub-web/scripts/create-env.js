#!/usr/bin/env node

/**
 * 环境变量配置文件生成脚本
 * 使用方法: node scripts/create-env.js [development|production]
 */

import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const envType = process.argv[2] || 'development'
const force = process.argv.includes('--force') || process.argv.includes('-f')
const envFile = `.env.${envType}`

const configs = {
  development: {
    VITE_API_BASE_URL: 'http://127.0.0.1:9881',
    VITE_PROXY_TARGET: 'http://127.0.0.1:9881',
  },
  production: {
    VITE_API_BASE_URL: 'http://117.72.32.111:9881',
    VITE_PROXY_TARGET: 'http://117.72.32.111:9881',
  },
}

const config = configs[envType]

if (!config) {
  console.error(`错误: 不支持的环境类型 "${envType}"`)
  console.error('支持的环境类型: development, production')
  process.exit(1)
}

const filePath = path.join(__dirname, '..', envFile)

// 如果文件已存在，询问是否覆盖
if (fs.existsSync(filePath) && !force) {
  console.log(`文件 ${envFile} 已存在`)
  console.log('如需重新生成，请使用 --force 或 -f 参数覆盖')
  console.log(`  例如: npm run env:${envType === 'development' ? 'dev' : 'prod'} -- --force`)
  process.exit(0)
}

// 生成文件内容
const content = `# ${envType === 'development' ? '开发' : '生产'}环境配置
# 后端服务地址（${envType === 'development' ? '开发' : '生产'}环境）
${envType === 'production' ? '# 请根据实际部署情况修改此地址\n' : ''}VITE_API_BASE_URL=${config.VITE_API_BASE_URL}

# Vite 代理目标地址（${envType === 'development' ? '开发环境使用' : '生产环境通常不需要代理，但保留此配置'}）
VITE_PROXY_TARGET=${config.VITE_PROXY_TARGET}
`

// 写入文件
fs.writeFileSync(filePath, content, 'utf8')

console.log(`✅ 已创建 ${envFile} 文件`)
console.log(`📝 请根据实际情况修改配置值`)
console.log(`\n当前配置:`)
console.log(`  VITE_API_BASE_URL=${config.VITE_API_BASE_URL}`)
console.log(`  VITE_PROXY_TARGET=${config.VITE_PROXY_TARGET}`)

