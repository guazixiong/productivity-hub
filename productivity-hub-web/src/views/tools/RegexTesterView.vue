<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const pattern = ref('')
const flags = ref('g')
const testText = ref('')
const replaceText = ref('')
const regexError = ref('')
const matches = ref<Array<{ match: string; index: number }>>([])

const escapeHtml = (value: string) =>
  value.replace(/[&<>"']/g, (char) => `&#${char.charCodeAt(0)};`)

const highlightedResult = computed(() => {
  if (!pattern.value) return escapeHtml(testText.value)
  try {
    const regex = new RegExp(pattern.value, flags.value.includes('g') ? flags.value : `${flags.value}g`)
    return escapeHtml(testText.value).replace(regex, (match) => `<mark>${escapeHtml(match)}</mark>`)
  } catch {
    return escapeHtml(testText.value)
  }
})

const replacedOutput = computed(() => {
  if (!pattern.value) return ''
  try {
    const regex = new RegExp(pattern.value, flags.value)
    return testText.value.replace(regex, replaceText.value)
  } catch {
    return ''
  }
})

const runRegex = () => {
  regexError.value = ''
  matches.value = []

  if (!pattern.value) {
    ElMessage.warning('请输入正则表达式')
    return
  }

  try {
    const regex = new RegExp(pattern.value, flags.value.includes('g') ? flags.value : `${flags.value}g`)
    const found: Array<{ match: string; index: number }> = []
    let matchResult = regex.exec(testText.value)

    while (matchResult) {
      found.push({ match: matchResult[0], index: matchResult.index })
      if (matchResult[0] === '') {
        regex.lastIndex += 1
      }
      matchResult = regex.exec(testText.value)
    }

    matches.value = found
    ElMessage.success(`匹配完成，共 ${found.length} 处`)
  } catch (error) {
    regexError.value = (error as Error).message
  }
}
</script>

<template>
  <div class="regex-tester">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
      <el-button type="primary" @click="runRegex">执行匹配</el-button>
    </div>

    <div class="input-grid">
      <el-card shadow="hover">
        <div class="field">
          <label>表达式</label>
          <el-input v-model="pattern" placeholder="例如：(\\d{3})-(\\d{4})" />
        </div>
        <div class="field">
          <label>标志位</label>
          <el-input v-model="flags" placeholder="g i m s u y" />
        </div>
        <div class="field">
          <label>替换为 (可选)</label>
          <el-input v-model="replaceText" placeholder="使用 $1 $2 引用分组" />
        </div>
      </el-card>

      <el-card shadow="hover">
        <div class="field">
          <label>测试文本</label>
          <el-input v-model="testText" type="textarea" :rows="8" placeholder="粘贴要匹配的内容..." />
        </div>
      </el-card>
    </div>

    <div v-if="regexError" class="regex-error">{{ regexError }}</div>

    <div class="result-grid">
      <el-card shadow="hover">
        <template #header>
          <span>高亮结果</span>
        </template>
        <div class="highlight-preview" v-html="highlightedResult" />
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <span>替换预览</span>
        </template>
        <pre class="replace-output">{{ replacedOutput || '暂无替换内容' }}</pre>
      </el-card>
    </div>

    <el-card v-if="matches.length" shadow="hover" class="match-card">
      <template #header>
        <span>匹配详情 ({{ matches.length }})</span>
      </template>
      <el-table :data="matches" size="small" border>
        <el-table-column prop="index" label="位置" width="80" />
        <el-table-column prop="match" label="匹配内容" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.regex-tester {
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

.input-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.field label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.regex-error {
  padding: 12px 16px;
  border-radius: 10px;
  background: rgba(248, 113, 113, 0.12);
  color: #b91c1c;
  font-size: 13px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.highlight-preview {
  min-height: 160px;
  border: 1px dashed var(--surface-border);
  border-radius: 12px;
  padding: 16px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-break: break-all;
  background: rgba(248, 250, 252, 0.8);
}

.highlight-preview mark {
  background: rgba(34, 197, 94, 0.4);
  border-radius: 6px;
  padding: 0 2px;
}

.replace-output {
  min-height: 160px;
  border-radius: 12px;
  padding: 16px;
  background: rgba(248, 250, 252, 0.8);
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  border: 1px solid var(--surface-border);
}

.match-card {
  overflow: hidden;
}

@media (max-width: 768px) {
  .result-grid {
    grid-template-columns: 1fr;
  }
}
</style>


