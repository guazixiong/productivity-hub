<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
// 静态导入 cronstrue，确保构建时正确打包
import cronstrue from 'cronstrue'

const router = useRouter()

const cronExpression = ref('0 0 * * *')
const cronDescription = ref('')
const cronError = ref('')
const cronPreviews = ref<string[]>([])

const commonExamples = [
  { label: '每分钟', value: '* * * * *' },
  { label: '每小时整点', value: '0 * * * *' },
  { label: '每天 09:30', value: '30 9 * * *' },
  { label: '每周一 10:00', value: '0 10 * * 1' },
  { label: '每月 1 日 08:00', value: '0 8 1 * *' },
]

const formatDateTime = (date: Date) =>
  date.toLocaleString('zh-CN', {
    hour12: false,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })

const analyzeCron = async () => {
  cronError.value = ''
  cronDescription.value = ''
  cronPreviews.value = []

  if (!cronExpression.value.trim()) {
    ElMessage.warning('请输入 Cron 表达式')
    return
  }

  try {
    // 使用静态导入的 cronstrue
    cronDescription.value = cronstrue.toString(cronExpression.value, { locale: 'zh_CN' })
  } catch (error) {
    cronError.value = `无法解析描述: ${(error as Error).message}`
    return
  }

  try {
    // 动态导入 cron-parser 以避免构建时的兼容性问题
    // 使用字符串变量避免静态分析，确保作为动态导入处理
    const cronParserModule = 'cron-parser'
    const parserModule = await import(/* @vite-ignore */ cronParserModule)
    
    // 处理不同的导出方式（CommonJS/ESM）
    // cron-parser 可能是 default export 或 named export
    let parser: any = null
    
    // 尝试获取 parseExpression 方法
    if (parserModule.default && typeof parserModule.default.parseExpression === 'function') {
      parser = parserModule.default
    } else if (typeof parserModule.parseExpression === 'function') {
      parser = parserModule
    } else if (parserModule.default && typeof parserModule.default === 'object') {
      parser = parserModule.default
    } else {
      parser = parserModule
    }
    
    // 验证 parser 对象
    if (!parser || typeof parser.parseExpression !== 'function') {
      throw new Error('cron-parser 模块加载失败：无法找到 parseExpression 方法')
    }
    
    const interval = parser.parseExpression(cronExpression.value, { 
      endDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 30) 
    })
    const previews: string[] = []
    for (let i = 0; i < 5; i += 1) {
      previews.push(formatDateTime(interval.next().toDate()))
    }
    cronPreviews.value = previews
    ElMessage.success('解析完成')
  } catch (error) {
    cronError.value = `表达式不合法: ${(error as Error).message}`
  }
}

const applyExample = (value: string) => {
  cronExpression.value = value
  analyzeCron()
}
</script>

<template>
  <div class="cron-helper">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
      <el-button type="primary" @click="analyzeCron">开始解析</el-button>
    </div>

    <el-card class="cron-card" shadow="hover">
      <div class="cron-input">
        <div class="label">Cron 表达式</div>
        <el-input v-model="cronExpression" placeholder="例如：0 0 * * *" clearable />
      </div>
      <div class="example-list">
        <span class="label">快速示例</span>
        <el-tag v-for="item in commonExamples" :key="item.value" type="info" effect="plain" @click="applyExample(item.value)">
          {{ item.label }}
        </el-tag>
      </div>
      <div v-if="cronError" class="cron-error">{{ cronError }}</div>
      <div v-if="cronDescription" class="cron-description">{{ cronDescription }}</div>
      <div v-if="cronPreviews.length" class="preview">
        <div class="label">未来 5 次执行时间 (本地时区)</div>
        <el-timeline>
          <el-timeline-item v-for="item in cronPreviews" :key="item" :timestamp="item" />
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.cron-helper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.cron-card {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.cron-input {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.example-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.label {
  font-size: 12px;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-weight: 600;
}

.cron-error {
  padding: 12px 16px;
  border-radius: 10px;
  background: rgba(248, 113, 113, 0.1);
  color: #dc2626;
  font-size: 13px;
}

.cron-description {
  padding: 16px;
  border-radius: 12px;
  background: rgba(16, 185, 129, 0.1);
  color: #047857;
  font-size: 16px;
  font-weight: 600;
}

.preview {
  margin-top: 8px;
}
</style>


