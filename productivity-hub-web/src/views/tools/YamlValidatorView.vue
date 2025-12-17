<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { parse } from 'yaml'

const router = useRouter()

const yamlInput = ref('')
const yamlError = ref('')
const jsonPreview = ref('')

const validateYaml = () => {
  yamlError.value = ''
  jsonPreview.value = ''

  if (!yamlInput.value.trim()) {
    ElMessage.warning('请输入 YAML 内容')
    return
  }

  try {
    const parsed = parse(yamlInput.value)
    jsonPreview.value = JSON.stringify(parsed, null, 2)
    ElMessage.success('YAML 语法正确')
  } catch (error) {
    yamlError.value = (error as Error).message
  }
}
</script>

<template>
  <div class="yaml-validator">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
      <el-button type="primary" @click="validateYaml">开始校验</el-button>
    </div>
    <div class="validator-grid">
      <el-card shadow="hover">
        <template #header>
          <span>YAML 输入</span>
        </template>
        <el-input
          v-model="yamlInput"
          type="textarea"
          :rows="16"
          placeholder="粘贴或输入 YAML 文本..."
          class="monospace"
        />
        <div v-if="yamlError" class="yaml-error">
          <strong>错误提示：</strong>{{ yamlError }}
        </div>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>JSON 预览</span>
        </template>
        <pre class="json-preview monospace">{{ jsonPreview || '等待校验结果...' }}</pre>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.yaml-validator {
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

.validator-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.monospace {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.yaml-error {
  margin-top: 12px;
  padding: 12px;
  border-radius: 10px;
  background: rgba(248, 113, 113, 0.12);
  color: #b91c1c;
  font-size: 13px;
}

.yaml-error strong {
  display: block;
  margin-bottom: 4px;
}

.json-preview {
  min-height: 300px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  padding: 16px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  border: 1px solid var(--surface-border);
}
</style>


