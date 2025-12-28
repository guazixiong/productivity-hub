<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

interface Question {
  id: string
  title: string
  description: string
  weight: number // 权重
  reverse?: boolean // 是否反向计分（分数越高越不好）
  scoreLabels?: string[] // 1-5 分对应的语义标签
}

const questions: Question[] = [
  {
    id: 'need',
    title: '真实需求程度',
    description: '如果今天不买，对你生活或工作的具体影响有多大？',
    weight: 20,
    scoreLabels: [
      '完全可以不买，几乎没有影响',
      '短期没影响，可以再等等',
      '有一点影响，但可以先观望',
      '会带来明显改善，早点买会更好',
      '不买会直接影响生活或工作质量',
    ],
  },
  {
    id: 'price',
    title: '价格合理性',
    description: '结合你的收入和同类商品价格，你觉得这个价格偏贵、合理还是惊喜？',
    weight: 15,
    scoreLabels: [
      '明显超出预算，性价比很低',
      '有点贵，但还能接受',
      '差不多算合理价格',
      '偏划算，感觉值这个价',
      '价格很惊喜，远超预期',
    ],
  },
  {
    id: 'frequency',
    title: '使用频率预期',
    description: '一年内，你大概会用到这个商品多少次？',
    weight: 15,
    scoreLabels: [
      '可能一年都想不到几次',
      '偶尔会用，但不算常用',
      '算是一般常用工具',
      '会经常用到，是高频场景',
      '几乎每天都会用到',
    ],
  },
  {
    id: 'alternative',
    title: '替代方案',
    description: '是否已经有能替代它的东西，或者有更便宜的型号、租借等选择？',
    weight: 10,
    reverse: true, // 替代方案越多，分数越低
    scoreLabels: [
      '几乎没有可替代方案',
      '有一点替代空间，但效果差不少',
      '有还行的替代方案',
      '有性价比更高的替代或租借方式',
      '已经有类似东西，或随时能找到替代',
    ],
  },
  {
    id: 'finance',
    title: '财务压力',
    description: '这笔支出会不会影响到房租、还贷、储蓄等必要支出？',
    weight: 15,
    scoreLabels: [
      '会直接挤占房租/还贷/储蓄等必要支出',
      '会有一点压力，需要挪用其他预算',
      '压力不大，但需要稍微规划一下',
      '基本不影响整体预算，只是占个小头',
      '完全不影响，有充足余地',
    ],
  },
  {
    id: 'impulse',
    title: '冲动程度',
    description: '从看到优惠或被种草到想买，大概间隔了多久？',
    weight: 5,
    reverse: true, // 越冲动越不好
    scoreLabels: [
      '已经冷静很久，还是觉得值得买',
      '考虑了几天，感觉还不错',
      '想了大半天，有点犹豫',
      '刚看到没多久就很想买',
      '几乎是被种草/看到优惠就立刻想下单',
    ],
  },
  {
    id: 'longterm',
    title: '长期价值',
    description: '一年之后，它大概率还能持续为你带来效率、健康或情绪价值吗？',
    weight: 20,
    scoreLabels: [
      '很可能很快吃灰或被闲置',
      '有一点长期价值，但不算刚需',
      '大概能用一阵子，看情况',
      '一年后大概率还会持续使用',
      '一年后仍能稳定带来效率/健康/情绪价值',
    ],
  },
]

const answers = ref<Record<string, number>>({})
const showResult = ref(false)
const resultSectionRef = ref<HTMLElement | null>(null)

const tagAbbrMap: Record<string, string> = {
  need: '需求',
  price: '价格',
  frequency: '频次',
  alternative: '替代',
  finance: '压力',
  impulse: '冲动',
  longterm: '长期',
}

const resetState = () => {
  answers.value = {}
  showResult.value = false
}

const currentScore = computed(() => {
  let totalScore = 0
  let totalWeight = 0

  questions.forEach((question) => {
    const answer = answers.value[question.id]
    if (answer !== undefined && answer !== null) {
      let score = answer
      // 如果是反向计分，需要转换
      if (question.reverse) {
        score = 6 - answer // 1->5, 2->4, 3->3, 4->2, 5->1
      }
      totalScore += score * question.weight
      totalWeight += question.weight
    }
  })

  if (totalWeight === 0) return 0
  return Math.round((totalScore / totalWeight) * 20) // 转换为0-100分
})

const allAnswered = computed(() => {
  return questions.every((q) => answers.value[q.id] !== undefined && answers.value[q.id] !== null)
})

const questionTags = computed(() => {
  return questions.map((q) => {
    const filled = answers.value[q.id] !== undefined && answers.value[q.id] !== null
    return {
      id: q.id,
      title: q.title,
      abbr: tagAbbrMap[q.id] ?? q.title.slice(0, 4),
      filled,
    }
  })
})

const answeredCount = computed(() => questionTags.value.filter((tag) => tag.filled).length)

const resultLevel = computed(() => {
  const score = currentScore.value
  if (score >= 80) {
    return {
      level: '强烈推荐（前提：不影响基本开支）',
      color: 'success',
      message: '在你的当前情况和使用场景下，这个商品非常适合你。如果是大额支出，仍建议简单做一下价格比对。',
      emoji: '✅',
    }
  } else if (score >= 60) {
    return {
      level: '可以购买',
      color: 'primary',
      message: '整体来看是值得的；你可以再确认下使用频率和替代方案，避免买了吃灰。',
      emoji: '👍',
    }
  } else if (score >= 40) {
    return {
      level: '再想一想',
      color: 'warning',
      message: '目前更多是「想要」而不是「必须」。可以延后几天再看，或者寻找更划算的替代方案。',
      emoji: '🤔',
    }
  } else {
    return {
      level: '暂时不买更好',
      color: 'danger',
      message: '目前你的需求和状态下，这笔支出性价比不高，可能是冲动或情绪驱动的消费。',
      emoji: '🛑',
    }
  }
})

const scoreDetails = computed(() => {
  return questions
    .map((question) => {
      const answer = answers.value[question.id]
      if (answer === undefined || answer === null) return null

      let score = answer
      if (question.reverse) {
        score = 6 - answer
      }
      const contribution = Math.round((score * question.weight * 20) / 100)

      return {
        question: question.title,
        answer,
        contribution,
        weight: question.weight,
      }
    })
    .filter((item): item is NonNullable<typeof item> => item !== null)
})

const handleSubmit = () => {
  if (!allAnswered.value) {
    ElMessage.warning('请回答所有问题')
    return
  }
  showResult.value = true
  nextTick(() => {
    if (resultSectionRef.value) {
      resultSectionRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
      return
    }
    window.scrollTo({ top: 0, behavior: 'smooth' })
  })
}

const handleReset = () => {
  resetState()
  ElMessage.success('已重置')
}

onBeforeRouteLeave(() => {
  resetState()
})

const getScoreLabel = (score: number) => {
  if (score >= 4) return '非常符合'
  if (score >= 3) return '比较符合'
  if (score >= 2) return '一般'
  if (score >= 1) return '不太符合'
  return '完全不符合'
}
</script>

<template>
  <div class="worth-buying-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>

    <div class="content-wrapper">
      <el-card class="question-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h2>值不值得买 · 理性消费助手</h2>
            <p class="subtitle">用 7 个关键问题，快速做一轮「冲动 vs 价值」自检，不替你做决定，只帮你看清自己。</p>
          </div>
        </template>

        <div v-if="!showResult" class="questions-section">
          <div class="questions-layout">
            <div class="questions-main">
              <div class="tips">
                <div class="tips-title">小提示：</div>
                <ul class="tips-list">
                  <li>1 分 = 完全不符合，5 分 = 非常符合，请诚实对自己就好。</li>
                  <li>结果仅供参考，别因为分数高就勉强自己买。</li>
                  <li>如果是大额消费，建议睡一觉，第二天再看一遍结果。</li>
                </ul>
              </div>

              <div
                v-for="question in questions"
                :key="question.id"
                class="question-item"
              >
                <div class="question-header">
                  <h3 class="question-title">{{ question.title }}</h3>
                  <span class="question-weight">权重: {{ question.weight }}%</span>
                </div>
                <p class="question-description">{{ question.description }}</p>
                <div class="answer-section">
                  <el-radio-group
                    v-model="answers[question.id]"
                    class="score-radio-group"
                  >
                    <el-radio-button
                      v-for="score in [1, 2, 3, 4, 5]"
                      :key="score"
                      :label="score"
                      class="score-radio"
                    >
                      <div class="score-content">
                        <span class="score-number">{{ score }}</span>
                        <span class="score-label">
                          {{ question.scoreLabels?.[score - 1] ?? getScoreLabel(score) }}
                        </span>
                      </div>
                    </el-radio-button>
                  </el-radio-group>
                </div>
              </div>

              <div class="action-buttons">
                <el-button type="primary" size="large" :disabled="!allAnswered" @click="handleSubmit">
                  查看评估结果
                </el-button>
                <el-button :icon="Refresh" @click="handleReset">重置</el-button>
              </div>
            </div>

            <el-affix :offset="16" class="sidebar-affix">
              <div class="questions-sidebar">
                <div class="sidebar-header">
                  <div class="sidebar-title">问题进度</div>
                  <div class="sidebar-progress">{{ answeredCount }}/{{ questions.length }}</div>
                </div>
                <div class="sidebar-subtitle">右侧标签为缩写，便于快速确认填写</div>
                <div class="tags-list">
                  <div
                    v-for="tag in questionTags"
                    :key="tag.id"
                    class="tag-item"
                    :class="{ filled: tag.filled }"
                  >
                    <div class="tag-abbr">{{ tag.abbr }}</div>
                    <div class="tag-info">
                      <span class="tag-title">{{ tag.title }}</span>
                      <span class="tag-status" :class="tag.filled ? 'status-filled' : 'status-pending'">
                        {{ tag.filled ? '已填' : '未填' }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </el-affix>
          </div>
        </div>

        <div v-else ref="resultSectionRef" class="result-section">
          <div class="result-header">
            <div class="result-score">
              <div class="score-circle" :class="`score-${resultLevel.color}`">
                <span class="score-value">{{ currentScore }}</span>
                <span class="score-unit">分</span>
              </div>
            </div>
            <div class="result-info">
              <h2 class="result-level" :class="`text-${resultLevel.color}`">
                {{ resultLevel.emoji }} {{ resultLevel.level }}
              </h2>
              <p class="result-message">{{ resultLevel.message }}</p>
              <p class="result-disclaimer">
                本工具不会替你下单，只是帮你把「冲动」和「价值」拆开放在桌面上看清楚，最终决定仍然在你手里。
              </p>
            </div>
          </div>

          <el-divider />

          <div class="score-details">
            <h3>得分详情</h3>
            <div class="details-list">
              <div
                v-for="(detail, index) in scoreDetails"
                :key="index"
                class="detail-item"
              >
                <div class="detail-header">
                  <span class="detail-question">{{ detail.question }}</span>
                  <span class="detail-contribution">+{{ detail.contribution }}分</span>
                </div>
                <el-progress
                  :percentage="(detail.contribution / currentScore) * 100"
                  :color="resultLevel.color === 'success' ? '#67c23a' : resultLevel.color === 'warning' ? '#e6a23c' : resultLevel.color === 'danger' ? '#f56c6c' : '#409eff'"
                  :show-text="false"
                />
              </div>
            </div>
          </div>

          <div class="result-actions">
            <el-button type="primary" @click="handleReset">重新评估</el-button>
            <el-button @click="router.push('/tools')">返回工具箱</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 16px;
}

.worth-buying-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-wrapper {
  max-width: 1100px;
  margin: 0 auto;
  width: 100%;
}

.question-card {
  min-height: 500px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: var(--el-text-color-primary);
}

.subtitle {
  margin: 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.tips {
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  border: 1px dashed var(--el-border-color-light);
}

.tips-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.tips-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.7;
}

.questions-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.questions-layout {
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 16px;
  align-items: start;
}

.sidebar-affix {
  width: 100%;
}

.sidebar-affix .el-affix {
  width: 100%;
}

.questions-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-item {
  padding: 20px;
  background: var(--el-bg-color-page);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.question-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.question-weight {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  padding: 4px 8px;
  border-radius: 4px;
}

.question-description {
  margin: 8px 0 16px 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.answer-section {
  margin-top: 16px;
}

.score-radio-group {
  display: flex;
  width: 100%;
  gap: 8px;
}

.score-radio {
  flex: 1;
}

.score-radio :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 12px 8px;
  border-radius: 6px;
}

.score-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.score-number {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-color-primary);
}

.score-label {
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-light);
}

.questions-sidebar {
  position: sticky;
  top: 16px;
  background: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.sidebar-progress {
  font-size: 13px;
  color: var(--el-color-primary);
  font-weight: 600;
}

.sidebar-subtitle {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.tags-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tag-item {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 6px;
  border: 1px dashed var(--el-border-color-light);
  background: var(--el-fill-color-blank);
}

.tag-item.filled {
  border-color: var(--el-color-primary-light-7);
  background: var(--el-color-primary-light-9);
}

.tag-abbr {
  min-width: 40px;
  text-align: center;
  font-weight: 700;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-8);
  border-radius: 4px;
  padding: 6px 8px;
  font-size: 13px;
}

.tag-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 8px;
}

.tag-title {
  font-size: 13px;
  color: var(--el-text-color-primary);
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-light);
}

.status-filled {
  color: var(--el-color-success);
  border-color: var(--el-color-success-light-5);
  background: var(--el-color-success-light-9);
}

.status-pending {
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
}

.result-section {
  padding: 20px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 32px;
  margin-bottom: 24px;
}

.result-score {
  flex-shrink: 0;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.score-success {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}

.score-primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.score-warning {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
  color: white;
}

.score-danger {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  color: white;
}

.score-value {
  font-size: 48px;
  line-height: 1;
}

.score-unit {
  font-size: 16px;
  opacity: 0.9;
}

.result-info {
  flex: 1;
}

.result-level {
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 700;
}

.result-message {
  margin: 0;
  font-size: 16px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

.result-disclaimer {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.text-success {
  color: #67c23a;
}

.text-primary {
  color: #409eff;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.score-details {
  margin-top: 24px;
}

.score-details h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: var(--el-text-color-primary);
}

.details-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  padding: 12px;
  background: var(--el-bg-color-page);
  border-radius: 6px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.detail-question {
  font-size: 14px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.detail-contribution {
  font-size: 14px;
  color: var(--el-color-primary);
  font-weight: 600;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-light);
}

@media (max-width: 768px) {
  .result-header {
    flex-direction: column;
    text-align: center;
  }

  .questions-layout {
    grid-template-columns: 1fr;
  }

  .questions-sidebar {
    position: static;
  }

  .sidebar-affix .el-affix {
    position: static;
  }

  .score-radio-group {
    flex-direction: column;
  }

  .score-radio {
    width: 100%;
  }
}
</style>

