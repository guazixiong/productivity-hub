#!/usr/bin/env node

/**
 * 环境变量配置文件检查脚本
 * 检查 .env.production 和 .env.development 文件中的端口配置是否正确
 */

import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const EXPECTED_PORTS = {
  development: 9881,
  production: 9881,
}

function checkEnvFile(envType) {
  const envFile = `.env.${envType}`
  const filePath = path.join(__dirname, '..', envFile)
  const expectedPort = EXPECTED_PORTS[envType]

  if (!fs.existsSync(filePath)) {
    console.log(`❌ ${envFile} 文件不存在`)
    console.log(`   运行 'npm run env:${envType === 'development' ? 'dev' : 'prod'}' 创建该文件`)
    return false
  }

  const content = fs.readFileSync(filePath, 'utf8')
  const lines = content.split('\n')

  let hasApiBaseUrl = false
  let hasWrongPort = false
  let currentPort = null

  for (const line of lines) {
    if (line.startsWith('VITE_API_BASE_URL=')) {
      hasApiBaseUrl = true
      const value = line.split('=')[1]?.trim()
      if (value) {
        try {
          const url = new URL(value)
          currentPort = url.port ? parseInt(url.port, 10) : (url.protocol === 'https:' ? 443 : 80)
          if (currentPort !== expectedPort) {
            hasWrongPort = true
          }
        } catch (e) {
          console.log(`⚠️  ${envFile} 中的 VITE_API_BASE_URL 格式不正确: ${value}`)
          return false
        }
      }
    }
  }

  if (!hasApiBaseUrl) {
    console.log(`⚠️  ${envFile} 中缺少 VITE_API_BASE_URL 配置`)
    return false
  }

  if (hasWrongPort) {
    console.log(`❌ ${envFile} 中的端口配置不正确`)
    console.log(`   当前端口: ${currentPort}`)
    console.log(`   期望端口: ${expectedPort}`)
    console.log(`   请修改 VITE_API_BASE_URL 为正确的端口`)
    return false
  }

  console.log(`✅ ${envFile} 配置正确 (端口: ${currentPort || expectedPort})`)
  return true
}

console.log('检查环境变量配置...\n')

const devOk = checkEnvFile('development')
const prodOk = checkEnvFile('production')

console.log('')

if (!devOk || !prodOk) {
  console.log('修复建议:')
  if (!devOk) {
    console.log('  1. 开发环境: 运行 npm run env:dev 重新生成配置文件')
  }
  if (!prodOk) {
    console.log('  2. 生产环境: 运行 npm run env:prod 重新生成配置文件')
    console.log('     或手动编辑 .env.production，确保 VITE_API_BASE_URL=http://117.72.32.111:9881')
  }
  process.exit(1)
} else {
  console.log('✅ 所有环境变量配置正确！')
  process.exit(0)
}

