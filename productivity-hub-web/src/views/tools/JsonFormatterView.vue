<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import isEqual from 'lodash-es/isEqual'

const router = useRouter()

type DiffEntry = {
  path: string
  type: 'added' | 'removed' | 'changed'
  before?: string
  after?: string
}

const diffTypeMap: Record<DiffEntry['type'], string> = {
  added: '新增',
  removed: '移除',
  changed: '变更',
}

const jsonInput = ref('')
const jsonCompareInput = ref('')
const jsonOutput = ref('')
const jsonError = ref('')
const diffEntries = ref<DiffEntry[]>([])
const diffMessage = ref('')

const highlightedJson = computed(() => highlightJson(jsonOutput.value))

const formatJSON = () => {
  jsonError.value = ''
  jsonOutput.value = ''

  if (!jsonInput.value.trim()) {
    ElMessage.warning('请输入JSON内容')
    return
  }

  try {
    const parsed = JSON.parse(jsonInput.value)
    jsonOutput.value = JSON.stringify(parsed, null, 2)
    ElMessage.success('格式化成功')
  } catch (error) {
    jsonError.value = (error as Error).message
    ElMessage.error('JSON格式错误')
  }
}

const validateJSON = () => {
  jsonError.value = ''

  if (!jsonInput.value.trim()) {
    ElMessage.warning('请输入JSON内容')
    return
  }

  try {
    JSON.parse(jsonInput.value)
    ElMessage.success('JSON格式正确')
  } catch (error) {
    jsonError.value = (error as Error).message
    ElMessage.error('JSON格式错误')
  }
}

const minifyJSON = () => {
  jsonError.value = ''

  if (!jsonInput.value.trim()) {
    ElMessage.warning('请输入JSON内容')
    return
  }

  try {
    const parsed = JSON.parse(jsonInput.value)
    jsonOutput.value = JSON.stringify(parsed)
    ElMessage.success('压缩成功')
  } catch (error) {
    jsonError.value = (error as Error).message
    ElMessage.error('JSON格式错误')
  }
}

const compareJSON = () => {
  diffEntries.value = []
  diffMessage.value = ''

  if (!jsonInput.value.trim() || !jsonCompareInput.value.trim()) {
    ElMessage.warning('请同时输入需要对比的两段JSON')
    return
  }

  try {
    const source = JSON.parse(jsonInput.value)
    const target = JSON.parse(jsonCompareInput.value)
    const entries: DiffEntry[] = []

    collectDiffs(source, target, '根节点', entries)
    diffEntries.value = entries

    if (!entries.length) {
      diffMessage.value = '两段JSON完全一致'
      ElMessage.success('两段JSON完全一致')
    } else {
      diffMessage.value = `发现 ${entries.length} 处差异`
      ElMessage.warning('已标注差异')
    }
  } catch (error) {
    diffMessage.value = (error as Error).message
    ElMessage.error('JSON格式错误')
  }
}

const collectDiffs = (left: unknown, right: unknown, path: string, bucket: DiffEntry[]) => {
  if (left === undefined && right === undefined) return

  if (left === undefined) {
    bucket.push({
      path,
      type: 'added',
      after: formatValue(right),
    })
    return
  }

  if (right === undefined) {
    bucket.push({
      path,
      type: 'removed',
      before: formatValue(left),
    })
    return
  }

  if (isPlainObject(left) && isPlainObject(right)) {
    const keys = new Set([...Object.keys(left), ...Object.keys(right)])
    keys.forEach((key) => {
      const nextPath = buildPath(path, key)
      collectDiffs(
        (left as Record<string, unknown>)[key],
        (right as Record<string, unknown>)[key],
        nextPath,
        bucket
      )
    })
    return
  }

  if (Array.isArray(left) && Array.isArray(right)) {
    const maxLen = Math.max(left.length, right.length)
    for (let index = 0; index < maxLen; index += 1) {
      const nextPath = `${path}[${index}]`
      collectDiffs(left[index], right[index], nextPath, bucket)
    }
    return
  }

  if (!isEqual(left, right)) {
    bucket.push({
      path,
      type: 'changed',
      before: formatValue(left),
      after: formatValue(right),
    })
  }
}

const formatValue = (value: unknown) => {
  if (value === undefined) return 'undefined'
  if (typeof value === 'string') return value
  if (typeof value === 'number' || typeof value === 'boolean' || value === null) {
    return String(value)
  }
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}

const isPlainObject = (value: unknown): value is Record<string, unknown> =>
  Object.prototype.toString.call(value) === '[object Object]'

const buildPath = (base: string, key: string) => (base ? `${base}.${key}` : key)

const escapeHtml = (value: string) => value.replace(/[&<>]/g, (char) => `&#${char.charCodeAt(0)};`)

const highlightJson = (value: string) => {
  if (!value) return ''
  const escaped = escapeHtml(value)
  return escaped.replace(
    /("(\\u[\da-fA-F]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)/g,
    (match) => {
      let cls = 'json-number'
      if (/^"/.test(match)) {
        cls = /:$/.test(match) ? 'json-key' : 'json-string'
      } else if (/true|false/.test(match)) {
        cls = 'json-boolean'
      } else if (/null/.test(match)) {
        cls = 'json-null'
      }
      return `<span class="${cls}">${match}</span>`
    }
  )
}
</script>

<template>
  <div class="json-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>
    <div class="json-toolbar">
      <el-button type="primary" @click="formatJSON">格式化</el-button>
      <el-button @click="validateJSON">验证</el-button>
      <el-button @click="minifyJSON">压缩</el-button>
    </div>
    <div class="json-editor">
      <div class="json-section">
        <h4>输入</h4>
        <el-input
          v-model="jsonInput"
          type="textarea"
          :rows="15"
          placeholder="请输入JSON内容..."
          class="json-input"
        />
        <div v-if="jsonError" class="json-error">{{ jsonError }}</div>
      </div>
      <div class="json-section">
        <div class="section-header">
          <h4>输出</h4>
          <el-button size="small" @click="navigator.clipboard.writeText(jsonOutput); ElMessage.success('已复制')">
            复制
          </el-button>
        </div>
        <pre class="json-highlight" v-html="highlightedJson" />
      </div>
    </div>
    <el-divider />
    <div class="json-compare">
      <div class="json-section">
        <div class="section-header">
          <h4>对比输入</h4>
          <el-button size="small" type="primary" plain @click="compareJSON">执行对比</el-button>
        </div>
        <el-input
          v-model="jsonCompareInput"
          type="textarea"
          :rows="12"
          placeholder="请输入另一份JSON内容..."
          class="json-input"
        />
        <p class="section-tip">将需要对比的JSON粘贴至此，点击执行对比即可高亮差异。</p>
      </div>
      <div class="json-section">
        <div class="section-header">
          <h4>差异高亮</h4>
          <el-tag v-if="diffEntries.length" type="warning" effect="dark">{{ diffEntries.length }} 处差异</el-tag>
          <el-tag v-else-if="diffMessage && diffMessage.includes('完全一致')" type="success" effect="light">
            无差异
          </el-tag>
        </div>
        <div v-if="diffMessage" class="diff-message">{{ diffMessage }}</div>
        <el-scrollbar class="diff-scroll">
          <el-empty v-if="!diffEntries.length" description="暂无差异数据" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="(item, index) in diffEntries"
              :key="`${item.path}-${index}`"
              :timestamp="item.path"
              :type="item.type === 'changed' ? 'warning' : item.type === 'added' ? 'success' : 'danger'"
            >
              <div class="diff-item">
                <div class="diff-label" :class="`diff-${item.type}`">
                  {{ diffTypeMap[item.type] }}
                </div>
                <div class="diff-content">
                  <div v-if="item.before !== undefined" class="diff-value diff-before">
                    <span>原值</span>
                    <pre>{{ item.before }}</pre>
                  </div>
                  <div v-if="item.after !== undefined" class="diff-value diff-after">
                    <span>新值</span>
                    <pre>{{ item.after }}</pre>
                  </div>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<style scoped>
.json-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  margin-bottom: 8px;
}

.json-toolbar {
  display: flex;
  gap: 12px;
}

.json-editor {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.json-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #0f172a;
  font-weight: 600;
}

.json-input {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.json-highlight {
  min-height: 320px;
  background: rgba(15, 23, 42, 0.04);
  border-radius: 16px;
  padding: 16px;
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
}

.json-highlight .json-key {
  color: #f97316;
}

.json-highlight .json-string {
  color: #10b981;
}

.json-highlight .json-number {
  color: #6366f1;
}

.json-highlight .json-boolean {
  color: #ec4899;
}

.json-highlight .json-null {
  color: #94a3b8;
  font-style: italic;
}

.json-error {
  margin-top: 8px;
  color: #ef4444;
  font-size: 12px;
}

.json-compare {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: start;
}

.section-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.diff-message {
  margin-bottom: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(99, 102, 241, 0.08);
  color: #312e81;
  font-size: 13px;
}

.diff-scroll {
  max-height: 360px;
}

.diff-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.diff-label {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.diff-added {
  background: rgba(34, 197, 94, 0.15);
  color: #166534;
}

.diff-removed {
  background: rgba(248, 113, 113, 0.15);
  color: #991b1b;
}

.diff-changed {
  background: rgba(249, 115, 22, 0.15);
  color: #9a3412;
}

.diff-content {
  display: grid;
  gap: 12px;
}

.diff-value span {
  display: inline-block;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.diff-value pre {
  margin: 0;
  background: rgba(15, 23, 42, 0.04);
  padding: 8px;
  border-radius: 12px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
}

@media (max-width: 768px) {
  .json-editor {
    grid-template-columns: 1fr;
  }

  .json-compare {
    grid-template-columns: 1fr;
  }
}
</style>

