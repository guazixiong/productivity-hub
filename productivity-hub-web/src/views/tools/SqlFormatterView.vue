<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const sqlInput = ref('')
const sqlOutput = ref('')
const sqlError = ref('')
const indentSize = ref(2)
const keywordCase = ref<'upper' | 'lower'>('upper')

// 简单的SQL格式化函数
const formatSQL = () => {
  sqlError.value = ''
  sqlOutput.value = ''

  if (!sqlInput.value.trim()) {
    ElMessage.warning('请输入SQL语句')
    return
  }

  try {
    const formatted = formatSqlString(sqlInput.value, indentSize.value, keywordCase.value)
    sqlOutput.value = formatted
    ElMessage.success('格式化成功')
  } catch (error) {
    sqlError.value = (error as Error).message
    ElMessage.error('格式化失败')
  }
}

const formatSqlString = (sql: string, indent: number, caseType: 'upper' | 'lower'): string => {
  // 移除多余的空格和换行
  let formatted = sql.trim().replace(/\s+/g, ' ')

  // SQL关键字列表
  const keywords = [
    'SELECT', 'FROM', 'WHERE', 'JOIN', 'INNER', 'LEFT', 'RIGHT', 'FULL', 'OUTER',
    'ON', 'AND', 'OR', 'NOT', 'IN', 'EXISTS', 'LIKE', 'BETWEEN', 'IS', 'NULL',
    'INSERT', 'INTO', 'VALUES', 'UPDATE', 'SET', 'DELETE', 'CREATE', 'TABLE',
    'ALTER', 'DROP', 'INDEX', 'VIEW', 'DATABASE', 'SCHEMA', 'GRANT', 'REVOKE',
    'ORDER', 'BY', 'GROUP', 'HAVING', 'DISTINCT', 'AS', 'UNION', 'ALL',
    'CASE', 'WHEN', 'THEN', 'ELSE', 'END', 'IF', 'ELSEIF', 'WHILE', 'FOR',
    'DECLARE', 'BEGIN', 'COMMIT', 'ROLLBACK', 'TRANSACTION', 'PROCEDURE',
    'FUNCTION', 'TRIGGER', 'CURSOR', 'EXEC', 'EXECUTE', 'CALL',
    'LIMIT', 'OFFSET', 'TOP', 'FETCH', 'NEXT', 'PRIMARY', 'KEY', 'FOREIGN',
    'CONSTRAINT', 'UNIQUE', 'CHECK', 'DEFAULT', 'AUTO_INCREMENT', 'IDENTITY',
    'INT', 'VARCHAR', 'CHAR', 'TEXT', 'DATE', 'DATETIME', 'TIMESTAMP',
    'DECIMAL', 'FLOAT', 'DOUBLE', 'BOOLEAN', 'BIT', 'BLOB', 'CLOB'
  ]

  // 将关键字转换为指定大小写
  const caseKeywords = keywords.map(k => caseType === 'upper' ? k.toUpperCase() : k.toLowerCase())
  const keywordMap = new Map(keywords.map((k, i) => [k.toLowerCase(), caseKeywords[i]]))

  // 识别并格式化关键字
  const keywordRegex = new RegExp(`\\b(${keywords.join('|')})\\b`, 'gi')
  formatted = formatted.replace(keywordRegex, (match) => {
    return keywordMap.get(match.toLowerCase()) || match
  })

  // 添加换行和缩进
  const indentStr = ' '.repeat(indent)
  let result = ''
  let currentIndent = 0
  let inString = false
  let stringChar = ''
  let i = 0

  while (i < formatted.length) {
    const char = formatted[i]
    const nextChar = formatted[i + 1] || ''

    // 处理字符串
    if ((char === '"' || char === "'" || char === '`') && formatted[i - 1] !== '\\') {
      if (!inString) {
        inString = true
        stringChar = char
      } else if (char === stringChar) {
        inString = false
        stringChar = ''
      }
      result += char
      i++
      continue
    }

    if (inString) {
      result += char
      i++
      continue
    }

    // 处理关键字后的换行
    if (char === ' ' && nextChar !== ' ') {
      const wordEnd = i
      const wordStart = Math.max(0, wordEnd - 20)
      const recentText = formatted.substring(wordStart, wordEnd).toUpperCase()
      
      // 在这些关键字后添加换行
      const newlineAfter = ['SELECT', 'FROM', 'WHERE', 'JOIN', 'ON', 'AND', 'OR', 
                            'ORDER', 'GROUP', 'HAVING', 'INSERT', 'UPDATE', 'SET', 
                            'VALUES', 'CREATE', 'ALTER', 'DROP']
      
      for (const keyword of newlineAfter) {
        if (recentText.endsWith(' ' + keyword)) {
          result += '\n' + indentStr.repeat(currentIndent)
          if (['AND', 'OR'].includes(keyword)) {
            currentIndent = Math.max(0, currentIndent - 1)
          }
          break
        }
      }
    }

    // 处理括号
    if (char === '(') {
      result += char
      currentIndent++
      // 检查是否是函数调用（不换行）
      const beforeParen = formatted.substring(Math.max(0, i - 20), i).trim()
      const isFunction = /[a-zA-Z_]\s*$/.test(beforeParen)
      if (!isFunction && beforeParen.length > 0) {
        result += '\n' + indentStr.repeat(currentIndent)
      }
    } else if (char === ')') {
      currentIndent = Math.max(0, currentIndent - 1)
      result += '\n' + indentStr.repeat(currentIndent) + char
    } else if (char === ',') {
      result += char
      // 在SELECT列表中的逗号后换行
      const beforeComma = formatted.substring(Math.max(0, i - 50), i).toUpperCase()
      if (beforeComma.includes('SELECT') && !beforeComma.includes('FROM')) {
        result += '\n' + indentStr.repeat(currentIndent + 1)
      }
    } else {
      result += char
    }

    i++
  }

  // 清理多余的空行
  result = result.replace(/\n\s*\n\s*\n/g, '\n\n')
  
  // 确保每行末尾没有多余空格
  result = result.split('\n').map(line => line.trimEnd()).join('\n')

  return result.trim()
}

const minifySQL = () => {
  sqlError.value = ''
  sqlOutput.value = ''

  if (!sqlInput.value.trim()) {
    ElMessage.warning('请输入SQL语句')
    return
  }

  try {
    // 移除多余空格和换行，但保留必要的空格
    let minified = sqlInput.value
      .replace(/\/\*[\s\S]*?\*\//g, '') // 移除多行注释
      .replace(/--.*$/gm, '') // 移除单行注释
      .replace(/\s+/g, ' ') // 多个空格替换为单个
      .replace(/\s*([(),;])\s*/g, '$1') // 移除括号和逗号周围的空格
      .trim()
    
    sqlOutput.value = minified
    ElMessage.success('压缩成功')
  } catch (error) {
    sqlError.value = (error as Error).message
    ElMessage.error('压缩失败')
  }
}

const copyOutput = () => {
  if (!sqlOutput.value) {
    ElMessage.warning('没有可复制的内容')
    return
  }
  navigator.clipboard.writeText(sqlOutput.value)
  ElMessage.success('已复制到剪贴板')
}

const clearAll = () => {
  sqlInput.value = ''
  sqlOutput.value = ''
  sqlError.value = ''
}
</script>

<template>
  <div class="sql-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>

    <div class="sql-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="formatSQL">格式化</el-button>
        <el-button @click="minifySQL">压缩</el-button>
        <el-button @click="clearAll">清空</el-button>
      </div>
      <div class="toolbar-right">
        <span class="toolbar-label">缩进：</span>
        <el-input-number v-model="indentSize" :min="1" :max="8" :step="1" size="small" style="width: 80px" />
        <span class="toolbar-label">关键字：</span>
        <el-radio-group v-model="keywordCase" size="small">
          <el-radio-button label="upper">大写</el-radio-button>
          <el-radio-button label="lower">小写</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="sql-editor">
      <div class="sql-section">
        <h4>输入 SQL</h4>
        <el-input
          v-model="sqlInput"
          type="textarea"
          :rows="20"
          placeholder="请输入SQL语句..."
          class="sql-input"
        />
        <div v-if="sqlError" class="sql-error">{{ sqlError }}</div>
      </div>
      <div class="sql-section">
        <div class="section-header">
          <h4>格式化结果</h4>
          <el-button size="small" @click="copyOutput" :disabled="!sqlOutput">复制</el-button>
        </div>
        <el-input
          v-model="sqlOutput"
          type="textarea"
          :rows="20"
          placeholder="格式化后的SQL将显示在这里..."
          readonly
          class="sql-output"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.sql-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  margin-bottom: 8px;
}

.sql-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px;
  background: var(--primary-light);
  border-radius: 12px;
}

.toolbar-left {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.sql-editor {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.sql-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sql-section h4 {
  margin: 0;
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 600;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0;
  flex-wrap: wrap;
  gap: 8px;
}

.section-header h4 {
  margin: 0;
}

.sql-input,
.sql-output {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.sql-error {
  margin-top: 8px;
  padding: 12px;
  border-radius: 8px;
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  font-size: 13px;
}

@media (max-width: 768px) {
  .sql-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-right {
    justify-content: flex-start;
  }

  .sql-editor {
    grid-template-columns: 1fr;
  }
}
</style>

