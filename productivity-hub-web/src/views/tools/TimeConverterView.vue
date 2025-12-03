<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const timeInput = ref('')
const timeOutput = ref('')
const timeInputType = ref<'timestamp' | 'datetime'>('timestamp')
const timeOutputType = ref<'timestamp' | 'datetime'>('datetime')

const convertTime = () => {
  if (!timeInput.value.trim()) {
    ElMessage.warning('请输入时间')
    return
  }
  
  try {
    if (timeInputType.value === 'timestamp') {
      const timestamp = parseInt(timeInput.value)
      if (isNaN(timestamp)) {
        ElMessage.error('时间戳格式错误')
        return
      }
      const date = new Date(timestamp)
      if (timeOutputType.value === 'datetime') {
        timeOutput.value = date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
        })
      } else {
        timeOutput.value = timestamp.toString()
      }
    } else {
      const date = new Date(timeInput.value)
      if (isNaN(date.getTime())) {
        ElMessage.error('日期时间格式错误')
        return
      }
      if (timeOutputType.value === 'timestamp') {
        timeOutput.value = date.getTime().toString()
      } else {
        timeOutput.value = date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
        })
      }
    }
    ElMessage.success('转换成功')
  } catch (error) {
    ElMessage.error('转换失败')
  }
}

const initCurrentTimestamp = () => {
  timeInput.value = Date.now().toString()
  timeInputType.value = 'timestamp'
  timeOutputType.value = 'datetime'
  convertTime()
}
</script>

<template>
  <div class="time-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>
    <div class="time-content">
      <div class="time-converter">
      <div class="time-section">
        <h4>输入类型</h4>
        <el-radio-group v-model="timeInputType">
          <el-radio-button label="timestamp">时间戳</el-radio-button>
          <el-radio-button label="datetime">日期时间</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="timeInput"
          :placeholder="timeInputType === 'timestamp' ? '请输入时间戳（毫秒）' : '请输入日期时间'"
          class="time-input"
        />
        <el-button text type="primary" @click="initCurrentTimestamp" v-if="timeInputType === 'timestamp'">
          使用当前时间戳
        </el-button>
      </div>
      <div class="time-section">
        <h4>输出类型</h4>
        <el-radio-group v-model="timeOutputType">
          <el-radio-button label="timestamp">时间戳</el-radio-button>
          <el-radio-button label="datetime">日期时间</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="timeOutput"
          readonly
          placeholder="转换结果将显示在这里..."
          class="time-output"
        />
      </div>
      <el-button type="primary" size="large" @click="convertTime" class="convert-button">
        转换
      </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.time-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  margin-bottom: 8px;
}

.time-content {
  display: flex;
  justify-content: center;
}

.time-converter {
  max-width: 600px;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.time-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.time-section h4 {
  margin: 0;
  font-size: 14px;
  color: #0f172a;
  font-weight: 600;
}

.time-input,
.time-output {
  width: 100%;
}

.convert-button {
  width: 100%;
}
</style>

