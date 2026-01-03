<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CopyDocument, RefreshRight } from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const router = useRouter()

const length = ref(16)
const includeUppercase = ref(true)
const includeLowercase = ref(true)
const includeNumbers = ref(true)
const includeSymbols = ref(true)
const avoidSimilar = ref(true)
const password = ref('')
const history = ref<string[]>([])

const similarCharacters = /[O0Il1]/g

const lengthLabel = computed(() => `${length.value} 位`)

const buildCharset = () => {
  let charset = ''
  if (includeUppercase.value) charset += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  if (includeLowercase.value) charset += 'abcdefghijklmnopqrstuvwxyz'
  if (includeNumbers.value) charset += '0123456789'
  if (includeSymbols.value) charset += '!@#$%^&*()-_=+[]{};:,.<>?/|~'

  if (avoidSimilar.value) {
    charset = charset.replace(similarCharacters, '')
  }

  return charset
}

const generatePassword = () => {
  const charset = buildCharset()
  if (!charset) {
    ElMessage.error('请至少选择一种字符类型')
    return
  }

  let result = ''
  const randomValues = new Uint32Array(length.value)
  crypto.getRandomValues(randomValues)

  for (let i = 0; i < length.value; i += 1) {
    result += charset[randomValues[i] % charset.length]
  }

  password.value = result
  history.value = [result, ...history.value].slice(0, 5)
}

const copyPassword = async () => {
  if (!password.value) {
    ElMessage.warning('请先生成密码')
    return
  }

  await navigator.clipboard.writeText(password.value)
  ElMessage.success('已复制到剪贴板')
}
</script>

<template>
  <div class="password-generator">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
      <div class="actions">
        <el-button type="primary" :icon="RefreshRight" @click="generatePassword">生成</el-button>
        <el-button :icon="CopyDocument" @click="copyPassword">复制</el-button>
      </div>
    </div>

    <el-card class="config-card" shadow="hover">
      <div class="length-row">
        <div>
          <div class="label">密码长度</div>
          <div class="length">{{ lengthLabel }}</div>
        </div>
        <el-slider v-model="length" :min="6" :max="64" show-input :show-input-controls="false" />
      </div>
      <el-divider />
      <div class="options-grid">
        <el-checkbox v-model="includeUppercase">包含大写字母 (A-Z)</el-checkbox>
        <el-checkbox v-model="includeLowercase">包含小写字母 (a-z)</el-checkbox>
        <el-checkbox v-model="includeNumbers">包含数字 (0-9)</el-checkbox>
        <el-checkbox v-model="includeSymbols">包含符号 (!@#)</el-checkbox>
        <el-checkbox v-model="avoidSimilar">排除易混字符 (O0Il1)</el-checkbox>
      </div>
    </el-card>

    <el-card class="result-card" shadow="hover">
      <div class="password-display" :class="{ empty: !password }">
        {{ password || '点击“生成”获取新密码' }}
      </div>
      <div v-if="history.length" class="history">
        <div class="label">最近生成</div>
        <el-tag
          v-for="item in history"
          :key="item"
          round
          size="large"
          @click="navigator.clipboard.writeText(item); ElMessage.success('已复制到剪贴板')"
        >
          {{ item }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.password-generator {
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

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.config-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.length-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.length {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
}

.options-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.result-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.password-display {
  min-height: 64px;
  border: 1px dashed rgba(99, 102, 241, 0.4);
  border-radius: 12px;
  padding: 16px;
  font-family: 'Courier New', monospace;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  word-break: break-all;
  background: rgba(248, 250, 252, 0.8);
}

.password-display.empty {
  color: var(--text-disabled);
}

.history {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.label {
  font-size: 13px;
  color: var(--text-tertiary);
  letter-spacing: 0.08em;
  font-weight: 600;
}

/* 移动端适配 - REQ-001 */
@media (max-width: 768px) {
  .password-generator {
    padding: 0;
    gap: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 0 12px;
  }

  .actions {
    width: 100%;

    .el-button {
      flex: 1;
    }
  }

  .config-card,
  .result-card {
    margin: 0 12px;
    border-radius: 12px;

    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  .length-row {
    gap: 12px;
  }

  .length {
    font-size: 24px;
  }

  .options-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .password-display {
    min-height: 56px;
    padding: 12px;
    font-size: 16px;
  }

  .history {
    gap: 6px;

    .label {
      width: 100%;
      margin-bottom: 8px;
    }
  }
}
</style>


