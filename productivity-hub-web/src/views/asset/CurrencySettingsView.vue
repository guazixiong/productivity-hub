<template>
  <div class="currency-settings-view">
    <el-card>
      <template #header>
        <span>货币设置</span>
      </template>

      <el-form :model="form" label-width="150px" style="max-width: 600px">
        <el-form-item label="默认货币">
          <el-select
            v-model="form.defaultCurrency"
            placeholder="请选择默认货币"
            style="width: 200px"
            @change="handleDefaultCurrencyChange"
          >
            <el-option
              v-for="currency in currencies"
              :key="currency.code"
              :label="`${currency.name} (${currency.symbol})`"
              :value="currency.code"
            />
          </el-select>
          <el-button
            type="primary"
            style="margin-left: 12px"
            :loading="saving"
            @click="handleSave"
          >
            保存
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="currency-list-section">
        <h3>支持的货币</h3>
        <el-table 
          :data="currencies" 
          border 
          style="width: 100%; max-width: 600px"
          v-loading="loading"
          element-loading-text="加载中..."
        >
          <el-table-column prop="code" label="货币代码" width="120" />
          <el-table-column prop="name" label="货币名称" width="150" />
          <el-table-column prop="symbol" label="货币符号" width="120" />
          <el-table-column label="是否默认" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.code === form.defaultCurrency" type="success">默认</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-divider />

      <div class="exchange-rate-section">
        <h3>汇率转换</h3>
        <el-form :model="exchangeForm" label-width="150px" style="max-width: 600px">
          <el-form-item label="源货币">
            <el-select
              v-model="exchangeForm.from"
              placeholder="请选择源货币"
              style="width: 200px"
              @change="handleExchangeRateChange"
            >
              <el-option
                v-for="currency in currencies"
                :key="currency.code"
                :label="`${currency.name} (${currency.code})`"
                :value="currency.code"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="目标货币">
            <el-select
              v-model="exchangeForm.to"
              placeholder="请选择目标货币"
              style="width: 200px"
              @change="handleExchangeRateChange"
            >
              <el-option
                v-for="currency in currencies"
                :key="currency.code"
                :label="`${currency.name} (${currency.code})`"
                :value="currency.code"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="汇率">
            <el-input
              v-model="exchangeRateDisplay"
              readonly
              style="width: 200px"
              placeholder="请选择源货币和目标货币"
            />
            <el-button
              type="primary"
              style="margin-left: 12px"
              :loading="loadingRate"
              :disabled="!exchangeForm.from || !exchangeForm.to"
              @click="loadExchangeRate"
            >
              查询汇率
            </el-button>
          </el-form-item>
          <el-form-item v-if="exchangeRate" label="更新时间">
            <span>{{ formatDateTime(exchangeRate.updateTime) }}</span>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { currencyApi } from '@/services/currencyApi'
import type { Currency, ExchangeRate } from '@/types/currency'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const saving = ref(false)
const loadingRate = ref(false)
const currencies = ref<Currency[]>([])
const exchangeRate = ref<ExchangeRate | null>(null)

const form = reactive<{
  defaultCurrency: string
}>({
  defaultCurrency: '',
})

const exchangeForm = reactive<{
  from: string
  to: string
}>({
  from: '',
  to: '',
})

const exchangeRateDisplay = computed(() => {
  if (!exchangeRate.value) return ''
  // 格式化汇率，保留4位小数
  const rate = typeof exchangeRate.value.rate === 'number' 
    ? exchangeRate.value.rate.toFixed(4) 
    : exchangeRate.value.rate
  return `1 ${exchangeRate.value.from} = ${rate} ${exchangeRate.value.to}`
})

const loadCurrencies = async () => {
  loading.value = true
  try {
    const res = await currencyApi.getCurrencyList()
    // request 函数已经解包了 data，直接使用 res
    currencies.value = res || []
    // 初始化汇率表单的默认值
    if (currencies.value.length > 0) {
      if (!exchangeForm.from) {
        exchangeForm.from = currencies.value[0].code
      }
      if (!exchangeForm.to) {
        exchangeForm.to = currencies.value.length > 1 ? currencies.value[1].code : currencies.value[0].code
      }
    }
  } catch (error) {
    ElMessage.error('获取货币列表失败')
    console.error('获取货币列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadDefaultCurrency = async () => {
  try {
    const res = await currencyApi.getDefaultCurrency()
    // request 函数已经解包了 data，直接使用 res
    form.defaultCurrency = res || ''
    // 如果获取到默认货币，且汇率表单未设置，则使用默认货币作为源货币
    if (form.defaultCurrency && !exchangeForm.from) {
      exchangeForm.from = form.defaultCurrency
    }
  } catch (error) {
    // 如果获取失败，使用默认值 CNY
    console.warn('获取默认货币失败，使用默认值 CNY:', error)
    form.defaultCurrency = 'CNY'
    if (!exchangeForm.from) {
      exchangeForm.from = 'CNY'
    }
  }
}

const loadExchangeRate = async () => {
  if (!exchangeForm.from || !exchangeForm.to) {
    ElMessage.warning('请选择源货币和目标货币')
    return
  }
  // 相同货币，汇率为1
  if (exchangeForm.from === exchangeForm.to) {
    exchangeRate.value = {
      from: exchangeForm.from,
      to: exchangeForm.to,
      rate: 1,
      updateTime: new Date().toISOString(),
    }
    return
  }
  loadingRate.value = true
  try {
    const res = await currencyApi.getExchangeRate({
      from: exchangeForm.from,
      to: exchangeForm.to,
    })
    // request 函数已经解包了 data，直接使用 res
    if (res) {
      exchangeRate.value = res
    } else {
      ElMessage.warning('未获取到汇率信息')
    }
  } catch (error: any) {
    const errorMsg = error?.message || '获取汇率失败'
    ElMessage.error(errorMsg)
    console.error('获取汇率失败:', error)
  } finally {
    loadingRate.value = false
  }
}

const handleDefaultCurrencyChange = () => {
  // 可以在这里添加一些逻辑
}

const handleExchangeRateChange = () => {
  exchangeRate.value = null
  // 如果两个货币都已选择，自动查询汇率
  if (exchangeForm.from && exchangeForm.to) {
    // 延迟一下，避免频繁请求
    setTimeout(() => {
      if (exchangeForm.from && exchangeForm.to) {
        loadExchangeRate()
      }
    }, 300)
  }
}

const handleSave = async () => {
  if (!form.defaultCurrency) {
    ElMessage.warning('请选择默认货币')
    return
  }
  saving.value = true
  try {
    await currencyApi.setDefaultCurrency({
      currencyCode: form.defaultCurrency,
    })
    ElMessage.success('保存成功')
  } catch (error: any) {
    const errorMsg = error?.message || '保存失败'
    ElMessage.error(errorMsg)
    console.error('保存默认货币失败:', error)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadCurrencies(), loadDefaultCurrency()])
})
</script>

<style scoped>
.currency-settings-view {
  padding: 16px;
}

.currency-list-section,
.exchange-rate-section {
  margin-top: 24px;
}

.currency-list-section h3,
.exchange-rate-section h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}
</style>

