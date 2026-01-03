<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Switch } from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const router = useRouter()

type UnitCategory = 'volume' | 'length' | 'area' | 'time' | 'angle' | 'speed' | 'temperature' | 'pressure' | 'energy' | 'power'

interface Unit {
  name: string
  symbol: string
  toBase: number // 转换为基准单位的倍数
}

interface UnitCategoryConfig {
  name: string
  baseUnit: string
  units: Unit[]
}

const categories: Record<UnitCategory, UnitCategoryConfig> = {
  volume: {
    name: '体积',
    baseUnit: '立方米',
    units: [
      { name: '立方米', symbol: 'm³', toBase: 1 },
      { name: '升', symbol: 'L', toBase: 0.001 },
      { name: '毫升', symbol: 'mL', toBase: 0.000001 },
      { name: '立方厘米', symbol: 'cm³', toBase: 0.000001 },
      { name: '立方分米', symbol: 'dm³', toBase: 0.001 },
      { name: '立方英尺', symbol: 'ft³', toBase: 0.0283168 },
      { name: '立方英寸', symbol: 'in³', toBase: 0.0000163871 },
      { name: '加仑(美)', symbol: 'gal(US)', toBase: 0.00378541 },
      { name: '加仑(英)', symbol: 'gal(UK)', toBase: 0.00454609 },
      { name: '夸脱', symbol: 'qt', toBase: 0.000946353 },
      { name: '品脱', symbol: 'pt', toBase: 0.000473176 },
    ],
  },
  length: {
    name: '长度',
    baseUnit: '米',
    units: [
      { name: '米', symbol: 'm', toBase: 1 },
      { name: '千米', symbol: 'km', toBase: 1000 },
      { name: '厘米', symbol: 'cm', toBase: 0.01 },
      { name: '毫米', symbol: 'mm', toBase: 0.001 },
      { name: '微米', symbol: 'μm', toBase: 0.000001 },
      { name: '纳米', symbol: 'nm', toBase: 0.000000001 },
      { name: '英寸', symbol: 'in', toBase: 0.0254 },
      { name: '英尺', symbol: 'ft', toBase: 0.3048 },
      { name: '码', symbol: 'yd', toBase: 0.9144 },
      { name: '英里', symbol: 'mi', toBase: 1609.34 },
      { name: '海里', symbol: 'nmi', toBase: 1852 },
      { name: '里', symbol: '里', toBase: 500 },
      { name: '丈', symbol: '丈', toBase: 3.333 },
      { name: '尺', symbol: '尺', toBase: 0.3333 },
      { name: '寸', symbol: '寸', toBase: 0.03333 },
    ],
  },
  area: {
    name: '面积',
    baseUnit: '平方米',
    units: [
      { name: '平方米', symbol: 'm²', toBase: 1 },
      { name: '平方千米', symbol: 'km²', toBase: 1000000 },
      { name: '平方厘米', symbol: 'cm²', toBase: 0.0001 },
      { name: '平方毫米', symbol: 'mm²', toBase: 0.000001 },
      { name: '公顷', symbol: 'ha', toBase: 10000 },
      { name: '亩', symbol: '亩', toBase: 666.667 },
      { name: '平方英尺', symbol: 'ft²', toBase: 0.092903 },
      { name: '平方英寸', symbol: 'in²', toBase: 0.00064516 },
      { name: '平方码', symbol: 'yd²', toBase: 0.836127 },
      { name: '英亩', symbol: 'acre', toBase: 4046.86 },
    ],
  },
  time: {
    name: '时间',
    baseUnit: '秒',
    units: [
      { name: '秒', symbol: 's', toBase: 1 },
      { name: '毫秒', symbol: 'ms', toBase: 0.001 },
      { name: '微秒', symbol: 'μs', toBase: 0.000001 },
      { name: '纳秒', symbol: 'ns', toBase: 0.000000001 },
      { name: '分钟', symbol: 'min', toBase: 60 },
      { name: '小时', symbol: 'h', toBase: 3600 },
      { name: '天', symbol: 'd', toBase: 86400 },
      { name: '周', symbol: 'week', toBase: 604800 },
      { name: '月', symbol: 'month', toBase: 2592000 },
      { name: '年', symbol: 'year', toBase: 31536000 },
    ],
  },
  angle: {
    name: '角度',
    baseUnit: '度',
    units: [
      { name: '度', symbol: '°', toBase: 1 },
      { name: '弧度', symbol: 'rad', toBase: 57.2958 },
      { name: '分', symbol: "'", toBase: 0.0166667 },
      { name: '秒', symbol: '"', toBase: 0.000277778 },
      { name: '梯度', symbol: 'grad', toBase: 0.9 },
    ],
  },
  speed: {
    name: '速度',
    baseUnit: '米/秒',
    units: [
      { name: '米/秒', symbol: 'm/s', toBase: 1 },
      { name: '千米/小时', symbol: 'km/h', toBase: 0.277778 },
      { name: '英里/小时', symbol: 'mph', toBase: 0.44704 },
      { name: '节', symbol: 'kn', toBase: 0.514444 },
      { name: '英尺/秒', symbol: 'ft/s', toBase: 0.3048 },
      { name: '光速', symbol: 'c', toBase: 299792458 },
      { name: '马赫', symbol: 'Ma', toBase: 343 },
    ],
  },
  temperature: {
    name: '温度',
    baseUnit: '摄氏度',
    units: [
      { name: '摄氏度', symbol: '°C', toBase: 1 },
      { name: '华氏度', symbol: '°F', toBase: 1 },
      { name: '开尔文', symbol: 'K', toBase: 1 },
    ],
  },
  pressure: {
    name: '压力',
    baseUnit: '帕斯卡',
    units: [
      { name: '帕斯卡', symbol: 'Pa', toBase: 1 },
      { name: '千帕', symbol: 'kPa', toBase: 1000 },
      { name: '兆帕', symbol: 'MPa', toBase: 1000000 },
      { name: '巴', symbol: 'bar', toBase: 100000 },
      { name: '标准大气压', symbol: 'atm', toBase: 101325 },
      { name: '毫米汞柱', symbol: 'mmHg', toBase: 133.322 },
      { name: '英寸汞柱', symbol: 'inHg', toBase: 3386.39 },
      { name: '磅/平方英寸', symbol: 'psi', toBase: 6894.76 },
    ],
  },
  energy: {
    name: '热量/能量',
    baseUnit: '焦耳',
    units: [
      { name: '焦耳', symbol: 'J', toBase: 1 },
      { name: '千焦', symbol: 'kJ', toBase: 1000 },
      { name: '卡路里', symbol: 'cal', toBase: 4.184 },
      { name: '千卡', symbol: 'kcal', toBase: 4184 },
      { name: '千瓦时', symbol: 'kWh', toBase: 3600000 },
      { name: '英热单位', symbol: 'BTU', toBase: 1055.06 },
      { name: '电子伏特', symbol: 'eV', toBase: 0.000000000000000000160218 },
    ],
  },
  power: {
    name: '功率',
    baseUnit: '瓦特',
    units: [
      { name: '瓦特', symbol: 'W', toBase: 1 },
      { name: '千瓦', symbol: 'kW', toBase: 1000 },
      { name: '兆瓦', symbol: 'MW', toBase: 1000000 },
      { name: '马力(公制)', symbol: 'PS', toBase: 735.499 },
      { name: '马力(英制)', symbol: 'hp', toBase: 745.7 },
      { name: '英热单位/小时', symbol: 'BTU/h', toBase: 0.293071 },
      { name: '卡路里/秒', symbol: 'cal/s', toBase: 4.184 },
    ],
  },
}

const activeCategory = ref<UnitCategory>('length')
const inputValue = ref<number | null>(null)
const inputUnit = ref<string>('')
const outputUnit = ref<string>('')

const currentCategory = computed(() => categories[activeCategory.value])
const currentUnits = computed(() => currentCategory.value.units)

// 初始化单位
const initUnits = () => {
  if (currentUnits.value.length >= 2) {
    inputUnit.value = currentUnits.value[0].name
    outputUnit.value = currentUnits.value[1].name
  }
}

// 温度转换特殊处理
const convertTemperature = (value: number, from: string, to: string): number => {
  let celsius = 0

  // 转换为摄氏度
  if (from === '摄氏度') {
    celsius = value
  } else if (from === '华氏度') {
    celsius = (value - 32) * 5 / 9
  } else if (from === '开尔文') {
    celsius = value - 273.15
  }

  // 从摄氏度转换为目标单位
  if (to === '摄氏度') {
    return celsius
  } else if (to === '华氏度') {
    return celsius * 9 / 5 + 32
  } else if (to === '开尔文') {
    return celsius + 273.15
  }

  return celsius
}

const convert = () => {
  if (inputValue.value === null || inputValue.value === undefined) {
    ElMessage.warning('请输入数值')
    return
  }

  if (!inputUnit.value || !outputUnit.value) {
    ElMessage.warning('请选择单位')
    return
  }

  const fromUnit = currentUnits.value.find(u => u.name === inputUnit.value)
  const toUnit = currentUnits.value.find(u => u.name === outputUnit.value)

  if (!fromUnit || !toUnit) {
    ElMessage.error('单位选择错误')
    return
  }

  // 温度转换特殊处理
  if (activeCategory.value === 'temperature') {
    const result = convertTemperature(inputValue.value, fromUnit.name, toUnit.name)
    ElMessage.success(`转换结果: ${result.toFixed(6)} ${toUnit.symbol}`)
    return
  }

  // 其他单位转换
  const baseValue = inputValue.value * fromUnit.toBase
  const result = baseValue / toUnit.toBase

  ElMessage.success(`转换结果: ${result.toFixed(6)} ${toUnit.symbol}`)
}

const outputValue = computed(() => {
  if (inputValue.value === null || inputValue.value === undefined) {
    return ''
  }

  if (!inputUnit.value || !outputUnit.value) {
    return ''
  }

  const fromUnit = currentUnits.value.find(u => u.name === inputUnit.value)
  const toUnit = currentUnits.value.find(u => u.name === outputUnit.value)

  if (!fromUnit || !toUnit) {
    return ''
  }

  // 温度转换特殊处理
  if (activeCategory.value === 'temperature') {
    const result = convertTemperature(inputValue.value, fromUnit.name, toUnit.name)
    return result.toFixed(6)
  }

  // 其他单位转换
  const baseValue = inputValue.value * fromUnit.toBase
  const result = baseValue / toUnit.toBase
  return result.toFixed(6)
})

const outputDisplay = computed(() => {
  if (!outputValue.value) return ''
  const toUnit = currentUnits.value.find(u => u.name === outputUnit.value)
  return toUnit ? `${outputValue.value} ${toUnit.symbol}` : outputValue.value
})

const swapUnits = () => {
  const temp = inputUnit.value
  inputUnit.value = outputUnit.value
  outputUnit.value = temp
}

// 监听分类变化，重新初始化单位
const onCategoryChange = () => {
  initUnits()
  inputValue.value = null
}

// 初始化
initUnits()
</script>

<template>
  <div class="unit-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>

    <div class="unit-content">
      <div class="category-selector">
        <h4>选择换算类型</h4>
        <el-radio-group v-model="activeCategory" @change="onCategoryChange" size="large">
          <el-radio-button label="length">长度</el-radio-button>
          <el-radio-button label="area">面积</el-radio-button>
          <el-radio-button label="volume">体积</el-radio-button>
          <el-radio-button label="time">时间</el-radio-button>
          <el-radio-button label="angle">角度</el-radio-button>
          <el-radio-button label="speed">速度</el-radio-button>
          <el-radio-button label="temperature">温度</el-radio-button>
          <el-radio-button label="pressure">压力</el-radio-button>
          <el-radio-button label="energy">热量</el-radio-button>
          <el-radio-button label="power">功率</el-radio-button>
        </el-radio-group>
      </div>

      <div class="converter-panel">
        <div class="converter-header">
          <h3>{{ currentCategory.name }}换算</h3>
        </div>

        <div class="converter-body">
          <div class="input-section">
            <div class="input-group">
              <el-input-number
                v-model="inputValue"
                :precision="10"
                :step="1"
                :min="activeCategory === 'temperature' ? undefined : 0"
                placeholder="输入数值"
                size="large"
                class="value-input"
              />
              <el-select v-model="inputUnit" placeholder="选择单位" size="large" class="unit-select">
                <el-option
                  v-for="unit in currentUnits"
                  :key="unit.name"
                  :label="`${unit.name} (${unit.symbol})`"
                  :value="unit.name"
                />
              </el-select>
            </div>
          </div>

          <div class="swap-button-wrapper">
            <el-button circle @click="swapUnits" title="交换单位">
              <el-icon><Switch /></el-icon>
            </el-button>
          </div>

          <div class="output-section">
            <div class="input-group">
              <el-input
                :value="outputDisplay"
                readonly
                placeholder="转换结果"
                size="large"
                class="value-input"
              />
              <el-select v-model="outputUnit" placeholder="选择单位" size="large" class="unit-select">
                <el-option
                  v-for="unit in currentUnits"
                  :key="unit.name"
                  :label="`${unit.name} (${unit.symbol})`"
                  :value="unit.name"
                />
              </el-select>
            </div>
          </div>

          <div class="convert-button-wrapper">
            <el-button type="primary" size="large" @click="convert" :disabled="inputValue === null">
              转换
            </el-button>
          </div>
        </div>

        <div class="units-list">
          <h4>可用单位</h4>
          <div class="units-grid">
            <el-tag
              v-for="unit in currentUnits"
              :key="unit.name"
              :type="inputUnit === unit.name || outputUnit === unit.name ? 'primary' : 'info'"
              effect="plain"
              class="unit-tag"
            >
              {{ unit.name }} ({{ unit.symbol }})
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.unit-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  margin-bottom: 8px;
}

.unit-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.category-selector {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-selector h4 {
  margin: 0;
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.converter-panel {
  background: var(--surface-color);
  border: 1px solid var(--surface-border);
  border-radius: 20px;
  padding: 24px;
  box-shadow: var(--surface-shadow);
}

.converter-header {
  margin-bottom: 24px;
}

.converter-header h3 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
  font-weight: 600;
}

.converter-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  gap: 12px;
}

.value-input {
  flex: 1;
}

.unit-select {
  width: 200px;
}

.swap-button-wrapper {
  display: flex;
  justify-content: center;
}

.convert-button-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.convert-button-wrapper .el-button {
  min-width: 120px;
}

.units-list {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--surface-border);
}

.units-list h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 600;
}

.units-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.unit-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.unit-tag:hover {
  transform: translateY(-2px);
}

/* 移动端适配 - REQ-001 */
@media (max-width: 768px) {
  .unit-container {
    padding: 0;
    gap: 16px;
  }

  .page-header {
    padding: 0 12px;
  }

  .unit-content {
    gap: 16px;
  }

  .category-selector {
    padding: 0 12px;
    gap: 12px;

    h4 {
      font-size: 14px;
    }
  }

  :deep(.el-radio-group) {
    flex-direction: column;
    gap: 8px;
  }

  :deep(.el-radio-button) {
    width: 100%;

    .el-radio-button__inner {
      width: 100%;
    }
  }

  .converter-panel {
    border-radius: 12px;
    padding: 16px;
    margin: 0 12px;
  }

  .converter-header {
    margin-bottom: 16px;

    h3 {
      font-size: 18px;
    }
  }

  .converter-body {
    gap: 16px;
  }

  .input-group {
    flex-direction: column;
    gap: 12px;
  }

  .value-input {
    width: 100%;
  }

  .unit-select {
    width: 100%;
  }

  .swap-button-wrapper {
    margin: 8px 0;
  }

  .convert-button-wrapper {
    margin-top: 12px;

    .el-button {
      width: 100%;
    }
  }

  .units-list {
    margin-top: 24px;
    padding-top: 16px;

    h4 {
      font-size: 13px;
      margin-bottom: 12px;
    }
  }

  .units-grid {
    gap: 6px;
  }
}
</style>

