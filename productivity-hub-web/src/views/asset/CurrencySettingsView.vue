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
        <el-table :data="currencies" border style="width: 100%; max-width: 600px">
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
  return `1 ${exchangeRate.value.from} = ${exchangeRate.value.rate} ${exchangeRate.value.to}`
})

const loadCurrencies = async () => {
  loading.value = true
  try {
    const res = await currencyApi.getCurrencyList()
    currencies.value = res.data || []
    if (currencies.value.length > 0 && !form.defaultCurrency) {
      exchangeForm.from = currencies.value[0].code
      exchangeForm.to = currencies.value.length > 1 ? currencies.value[1].code : currencies.value[0].code
    }
  } finally {
    loading.value = false
  }
}

const loadDefaultCurrency = async () => {
  try {
    const res = await currencyApi.getDefaultCurrency()
    form.defaultCurrency = res.data || ''
  } catch (error) {
    // 如果获取失败，使用默认值 CNY
    form.defaultCurrency = 'CNY'
  }
}

const loadExchangeRate = async () => {
  if (!exchangeForm.from || !exchangeForm.to) {
    ElMessage.warning('请选择源货币和目标货币')
    return
  }
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
    exchangeRate.value = res.data
  } catch (error) {
    ElMessage.error('获取汇率失败')
  } finally {
    loadingRate.value = false
  }
}

const handleDefaultCurrencyChange = () => {
  // 可以在这里添加一些逻辑
}

const handleExchangeRateChange = () => {
  exchangeRate.value = null
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
  } catch (error) {
    ElMessage.error('保存失败')
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

