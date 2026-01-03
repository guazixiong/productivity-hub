<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'
import isEqual from 'lodash-es/isEqual'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const router = useRouter()

type JsonMode = 'format' | 'diff'

type DiffEntry = {
  path: string
  type: 'added' | 'removed' | 'changed'
  before?: string
  after?: string
  beforeValue?: unknown
  afterValue?: unknown
}
type DiffStats = Record<DiffEntry['type'], number>

const diffTypeMap: Record<DiffEntry['type'], string> = {
  added: '新增',
  removed: '移除',
  changed: '变更',
}

const activeMode = ref<JsonMode>('format')
const jsonInput = ref('')
const jsonCompareInput = ref('')
const jsonOutput = ref('')
const jsonError = ref('')
const diffEntries = ref<DiffEntry[]>([])
const diffMessage = ref('')

const highlightedJson = computed(() => highlightJson(jsonOutput.value))
const diffStats = computed<DiffStats>(() => {
  return diffEntries.value.reduce<DiffStats>(
    (stats, entry) => {
      stats[entry.type] += 1
      return stats
    },
    { added: 0, removed: 0, changed: 0 }
  )
})
const diffSummary = computed(() => {
  if (!diffEntries.value.length) return ''
  const parts: string[] = []
  if (diffStats.value.added) {
    parts.push(`目标 JSON 比源 JSON 多 ${diffStats.value.added} 处`)
  }
  if (diffStats.value.removed) {
    parts.push(`源 JSON 比目标 JSON 多 ${diffStats.value.removed} 处`)
  }
  if (diffStats.value.changed) {
    parts.push(`有 ${diffStats.value.changed} 处字段值不同`)
  }
  return parts.join('，')
})

const removedJsonText = computed(() => buildDiffJson(diffEntries.value, 'removed', 'beforeValue'))
const addedJsonText = computed(() => buildDiffJson(diffEntries.value, 'added', 'afterValue'))
const changedBeforeJsonText = computed(() => buildDiffJson(diffEntries.value, 'changed', 'beforeValue'))
const changedAfterJsonText = computed(() => buildDiffJson(diffEntries.value, 'changed', 'afterValue'))

const buildDiffJson = (
  entries: DiffEntry[],
  type: DiffEntry['type'],
  valueKey: 'beforeValue' | 'afterValue'
) => {
  const filtered = entries.filter((item) => item.type === type && item[valueKey] !== undefined)
  if (!filtered.length) return ''

  const result: any = {}

  filtered.forEach((entry) => {
    const rawPath = entry.path || ''
    const normalizedPath = rawPath
      .replace(/^根节点\.?/, '') // 去掉根节点前缀
      .trim()

    setValueByPath(result, normalizedPath, entry[valueKey])
  })

  return JSON.stringify(result, null, 2)
}

const setValueByPath = (target: any, path: string, value: unknown) => {
  // 根节点直接返回原始值，调用方按需处理
  if (!path) {
    return value
  }

  const tokens: (string | number)[] = []
  const regex = /([^[.\]]+)|\[(\d+)\]/g
  let match: RegExpExecArray | null

  while ((match = regex.exec(path))) {
    if (match[1]) {
      tokens.push(match[1])
    } else if (match[2]) {
      tokens.push(Number(match[2]))
    }
  }

  let current: any = target

  tokens.forEach((token, index) => {
    const isLast = index === tokens.length - 1

    if (typeof token === 'number') {
      // 当前应该是数组容器
      if (!Array.isArray(current)) {
        // 如果当前不是数组，则在上一层已经挂载了 current 的引用，这里只需要确保它是数组
        // 由于 current 是引用类型，这里的修改会反映到 target 中的实际对象上
        current.length = Math.max(current.length ?? 0, 0)
      }

      if (isLast) {
        current[token] = value
      } else {
        const nextToken = tokens[index + 1]
        if (current[token] === undefined) {
          current[token] = typeof nextToken === 'number' ? [] : {}
        }
        current = current[token]
      }
    } else {
      // 当前应该是普通对象容器
      if (typeof current !== 'object' || current === null || Array.isArray(current)) {
        // 重置为对象容器，保持引用
        current = {}
      }

      if (isLast) {
        current[token] = value
      } else {
        const nextToken = tokens[index + 1]
        if (current[token] === undefined) {
          current[token] = typeof nextToken === 'number' ? [] : {}
        }
        current = current[token]
      }
    }
  })

  return target
}

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
      afterValue: right,
    })
    return
  }

  if (right === undefined) {
    bucket.push({
      path,
      type: 'removed',
      before: formatValue(left),
      beforeValue: left,
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
      beforeValue: left,
      afterValue: right,
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
    <div class="mode-switch">
      <el-radio-group v-model="activeMode" size="large">
        <el-radio-button label="format">JSON 格式化</el-radio-button>
        <el-radio-button label="diff">JSON 对比</el-radio-button>
      </el-radio-group>
    </div>
    <template v-if="activeMode === 'format'">
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
    </template>
    <template v-else>
      <el-divider />
      <div class="json-compare">
        <div class="json-section">
          <div class="section-header">
            <h4>源 JSON</h4>
            <el-button size="small" type="primary" plain @click="compareJSON">执行对比</el-button>
          </div>
          <el-input
            v-model="jsonInput"
            type="textarea"
            :rows="12"
            placeholder="请输入第一份JSON内容..."
            class="json-input"
          />
          <p class="section-tip">填入参考JSON，将与右侧内容进行逐项比较。</p>
        </div>
        <div class="json-section">
          <div class="section-header">
            <h4>目标 JSON</h4>
          </div>
          <el-input
            v-model="jsonCompareInput"
            type="textarea"
            :rows="12"
            placeholder="请输入另一份JSON内容..."
            class="json-input"
          />
          <p class="section-tip">确保两段JSON结构正确后点击执行对比。</p>
        </div>
        <div class="json-section diff-section">
          <div class="section-header diff-header">
            <h4>差异高亮</h4>
            <div v-if="diffEntries.length" class="diff-header-count">
              <el-tag size="small" type="success" effect="plain">+{{ diffStats.added }}</el-tag>
              <el-tag size="small" type="danger" effect="plain">-{{ diffStats.removed }}</el-tag>
              <el-tag size="small" type="warning" effect="plain">±{{ diffStats.changed }}</el-tag>
            </div>
            <el-tag v-else-if="diffMessage && diffMessage.includes('完全一致')" type="success" effect="light">
              无差异
            </el-tag>
          </div>
          <div v-if="diffMessage" class="diff-message">
            <div>{{ diffMessage }}</div>
            <div v-if="diffSummary" class="diff-summary">{{ diffSummary }}</div>
          </div>
          <div class="diff-legend">
            <span class="legend legend-added">目标新增</span>
            <span class="legend legend-removed">源端独有</span>
            <span class="legend legend-changed">值不同</span>
          </div>
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
    </template>
  </div>
</template>

<style scoped>
.json-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  margin-bottom: 8px;
}

.mode-switch {
  display: flex;
  justify-content: flex-start;
}

.json-toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.json-editor {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.json-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.json-section h4 {
  margin: 0;
  font-size: 16px;
  color: var(--text-primary);
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

.diff-header {
  gap: 8px;
}

.diff-header-count {
  display: flex;
  gap: 6px;
}

.json-highlight {
  min-height: 320px;
  background: rgba(15, 23, 42, 0.04);
  border-radius: 12px;
  padding: 16px;
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
  border: 1px solid var(--surface-border);
}

.json-highlight .json-key {
  color: #f97316;
}

.json-highlight .json-string {
  color: #10b981;
}

.json-highlight .json-number {
  color: var(--primary-color);
}

.json-highlight .json-boolean {
  color: #ec4899;
}

.json-highlight .json-null {
  color: var(--text-disabled);
  font-style: italic;
}

.json-error {
  margin-top: 8px;
  padding: 12px;
  border-radius: 8px;
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  font-size: 13px;
}

.json-compare {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

.diff-section {
  grid-column: span 2;
}

.section-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.diff-message {
  margin-bottom: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  background: var(--primary-light);
  color: var(--primary-hover);
  font-size: 13px;
}

.diff-summary {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.diff-legend {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.legend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.legend::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.legend-added::before {
  background: #22c55e;
}

.legend-removed::before {
  background: #ef4444;
}

.legend-changed::before {
  background: #f97316;
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
  padding: 4px 12px;
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
  color: var(--text-tertiary);
  margin-bottom: 4px;
}

.diff-value pre {
  margin: 0;
  background: rgba(15, 23, 42, 0.04);
  padding: 12px;
  border-radius: 10px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  border: 1px solid var(--surface-border);
}

.diff-before pre {
  border: 1px solid rgba(248, 113, 113, 0.4);
}

.diff-after pre {
  border: 1px solid rgba(34, 197, 94, 0.4);
}

.diff-json-summary {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.diff-json-block h5 {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 600;
}

.diff-json-block pre {
  margin: 0;
  background: rgba(15, 23, 42, 0.04);
  padding: 12px;
  border-radius: 10px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  border: 1px solid var(--surface-border);
}

/* 移动端适配 - REQ-001 */
@media (max-width: 768px) {
  .json-container {
    padding: 0;
    gap: 16px;
  }

  .page-header {
    padding: 0 12px;
  }

  .mode-switch {
    padding: 0 12px;

    :deep(.el-radio-group) {
      width: 100%;
    }

    :deep(.el-radio-button) {
      flex: 1;

      .el-radio-button__inner {
        width: 100%;
      }
    }
  }

  .json-toolbar {
    padding: 0 12px;
    flex-wrap: wrap;

    .el-button {
      flex: 1;
      min-width: 0;
    }
  }

  .json-editor {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 0 12px;
  }

  .json-section {
    gap: 8px;

    h4 {
      font-size: 14px;
    }
  }

  .json-input {
    font-size: 12px;
  }

  .json-highlight {
    min-height: 200px;
    padding: 12px;
    font-size: 12px;
  }

  .json-compare {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 0 12px;
  }

  .diff-section {
    grid-column: span 1;
  }

  .section-tip {
    font-size: 11px;
  }

  .diff-message {
    padding: 10px 12px;
    font-size: 12px;
  }

  .diff-scroll {
    max-height: 300px;
  }

  .diff-item {
    gap: 6px;
  }

  .diff-value pre {
    padding: 10px;
    font-size: 11px;
  }
}
</style>

