<script setup lang="ts">
import { computed, ref } from 'vue'

interface StageCommand {
  cmd: string
  note?: string
}

interface Stage {
  title: string
  subtitle?: string
  commands: StageCommand[]
}

const commitTypes = [
  { type: 'feat', desc: '新功能' },
  { type: 'fix', desc: '缺陷修复' },
  { type: 'chore', desc: '杂项/脚手架' },
  { type: 'refactor', desc: '重构（无新功能/修复）' },
  { type: 'docs', desc: '文档' },
  { type: 'test', desc: '测试' },
  { type: 'ci', desc: '持续集成' },
  { type: 'build', desc: '构建/依赖' },
  { type: 'perf', desc: '性能优化' },
  { type: 'style', desc: '格式/样式' },
]

const stages: Stage[] = [
  {
    title: '查看当前状态',
    subtitle: '识别你正处于哪个阶段',
    commands: [
      { cmd: 'git status', note: '全局视图' },
      { cmd: 'git diff', note: '工作区 vs 暂存区' },
      { cmd: 'git diff --cached', note: '暂存区 vs 上次提交' },
      { cmd: 'git log --oneline -5', note: '最近提交' },
    ],
  },
  {
    title: '未暂存/新文件',
    subtitle: '准备好再进入下一步',
    commands: [
      { cmd: 'git add .', note: '全部添加' },
      { cmd: 'git add <file>', note: '精确挑选' },
      { cmd: 'git add -p', note: '交互式分块' },
      { cmd: 'git checkout -- <file>', note: '丢弃工作区改动（谨慎）' },
    ],
  },
  {
    title: '已暂存待提交',
    subtitle: '确认后提交',
    commands: [
      { cmd: 'git diff --cached', note: '核对暂存内容' },
      { cmd: 'git reset <file>', note: '撤销单个暂存' },
      { cmd: 'git reset', note: '撤销全部暂存' },
      { cmd: 'git commit -m "feat(scope): subject"', note: '按规范提交' },
    ],
  },
  {
    title: '暂存现场 / 切分任务',
    subtitle: '需要中断或换上下文',
    commands: [
      { cmd: 'git stash push -m "wip: note"', note: '保存现场' },
      { cmd: 'git stash list', note: '查看列表' },
      { cmd: 'git stash apply', note: '恢复并保留' },
      { cmd: 'git stash pop', note: '恢复并删除记录' },
    ],
  },
  {
    title: '准备推送',
    subtitle: '本地领先远端',
    commands: [
      { cmd: 'git log --oneline origin/<branch>..HEAD', note: '查看差异' },
      { cmd: 'git push', note: '推送当前分支' },
      { cmd: 'git push -u origin <branch>', note: '创建并跟踪远端分支' },
    ],
  },
  {
    title: '需要同步远程',
    subtitle: '落后远端/有冲突风险',
    commands: [
      { cmd: 'git pull', note: '拉取并合并' },
      { cmd: 'git pull --rebase', note: '保持线性历史' },
      { cmd: 'git add <file> && git rebase --continue', note: '解决冲突后继续' },
    ],
  },
  {
    title: '清理无用文件',
    subtitle: '删除未跟踪垃圾文件',
    commands: [
      { cmd: 'git clean -nd', note: '预览将被删除的内容' },
      { cmd: 'git clean -fd', note: '实际删除（谨慎）' },
    ],
  },
  {
    title: '分支与历史管理',
    subtitle: '切换、回退、重来',
    commands: [
      { cmd: 'git switch -c <branch>', note: '创建并切换分支' },
      { cmd: 'git switch <branch>', note: '切换分支' },
      { cmd: 'git reset --soft <commit>', note: '回退到提交并保留暂存' },
      { cmd: 'git reset <commit>', note: '回退并保留工作区' },
      { cmd: 'git reset --hard <commit>', note: '硬回退（危险，需确认）' },
    ],
  },
]

const activeStage = ref(0)
const progressPercent = computed(() => {
  if (stages.length <= 1) return 100
  return (activeStage.value / (stages.length - 1)) * 100
})
const currentStage = computed(() => stages[activeStage.value])

const selectStage = (index: number) => {
  activeStage.value = index
}

const prevStage = () => {
  if (activeStage.value > 0) {
    activeStage.value -= 1
  }
}

const nextStage = () => {
  if (activeStage.value < stages.length - 1) {
    activeStage.value += 1
  }
}
</script>

<template>
  <div class="git-toolbox">
    <el-card class="hero-card" shadow="hover">
      <div class="hero-content">
        <div>
          <p class="hero-eyebrow">Git 工具箱</p>
          <h1>进度判断 × 常用命令 × 提交规范</h1>
          <p class="hero-subtitle">
            根据你当前的开发进度快速查找应使用的 git 命令，附带 Conventional Commits
            提交格式提示。
          </p>
        </div>
        <el-tag type="success" effect="dark" round>日常速查</el-tag>
      </div>
    </el-card>

    <el-card class="commit-card" shadow="hover">
      <div class="section-header">
        <h2>提交规范（Conventional Commits）</h2>
        <p>格式：&lt;type&gt;(scope?): &lt;subject&gt;，主题行用祈使句，≤ 50 字符。</p>
      </div>
      <div class="commit-grid">
        <div class="commit-types">
          <h3>常用 type</h3>
          <div class="type-chips">
            <el-tag v-for="item in commitTypes" :key="item.type" type="info" effect="plain">
              {{ item.type }} — {{ item.desc }}
            </el-tag>
          </div>
        </div>
        <div class="commit-examples">
          <h3>示例</h3>
          <ul>
            <li><code>feat(auth): add login throttle</code></li>
            <li><code>fix(api): handle null headers</code></li>
            <li><code>chore: bump eslint config</code></li>
          </ul>
          <p class="small-text">需要正文时，主题行后留空行，正文条列说明动机与变更点。</p>
        </div>
      </div>
    </el-card>

    <el-card class="stage-card" shadow="hover">
      <div class="section-header">
        <h2>按进度选命令</h2>
        <p>用进度条选择你当前所处的阶段，右侧立即给出对应命令。</p>
      </div>

      <div class="progress-wrapper">
        <el-button size="small" @click="prevStage" :disabled="activeStage === 0">上一阶段</el-button>
        <div class="progress-bar" role="group" aria-label="Git 进度选择">
          <div class="progress-rail" />
          <div class="progress-fill" :style="{ width: progressPercent + '%' }" />
          <div class="progress-nodes">
            <button
              v-for="(stage, idx) in stages"
              :key="stage.title"
              class="progress-node"
              :class="{
                active: idx === activeStage,
                done: idx < activeStage,
              }"
              type="button"
              @click="selectStage(idx)"
            >
              <span class="node-index">{{ idx + 1 }}</span>
              <span class="node-label">{{ stage.title }}</span>
            </button>
          </div>
        </div>
        <el-button
          size="small"
          @click="nextStage"
          :disabled="activeStage === stages.length - 1"
        >
          下一阶段
        </el-button>
      </div>

      <div class="stage-detail">
        <div class="stage-header">
          <h3>{{ currentStage.title }}</h3>
          <p v-if="currentStage.subtitle">{{ currentStage.subtitle }}</p>
        </div>
        <div class="command-list">
          <div
            v-for="command in currentStage.commands"
            :key="command.cmd"
            class="command-item"
          >
            <code class="cmd">{{ command.cmd }}</code>
            <span class="note" v-if="command.note">{{ command.note }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.git-toolbox {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card {
  border-radius: 16px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: #fff;
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.hero-eyebrow {
  margin: 0 0 4px 0;
  font-size: 13px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  opacity: 0.8;
}

.hero-card h1 {
  margin: 0 0 8px 0;
  font-size: 26px;
  font-weight: 700;
}

.hero-subtitle {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

.commit-card,
.stage-card {
  border-radius: 14px;
}

.stage-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-header h2,
.section-header p {
  margin: 0;
}

.section-header p {
  color: #64748b;
  margin-top: 4px;
}

.commit-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
  margin-top: 12px;
}

.commit-types h3,
.commit-examples h3 {
  margin: 0 0 8px 0;
}

.type-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.commit-examples ul {
  padding-left: 20px;
  margin: 0 0 8px 0;
}

.commit-examples li {
  margin-bottom: 4px;
}

.small-text {
  margin: 0;
  color: #94a3b8;
}

.progress-wrapper {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.progress-bar {
  position: relative;
  padding: 18px 0 12px;
}

.progress-rail {
  position: absolute;
  top: 20px;
  left: 0;
  right: 0;
  height: 4px;
  background: #e2e8f0;
  border-radius: 999px;
}

.progress-fill {
  position: absolute;
  top: 20px;
  left: 0;
  height: 4px;
  background: linear-gradient(90deg, #22c55e, #10b981);
  border-radius: 999px;
  transition: width 0.2s ease;
}

.progress-nodes {
  position: relative;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 8px;
  z-index: 1;
}

.progress-node {
  position: relative;
  display: grid;
  gap: 4px;
  justify-items: center;
  padding: 8px 10px 6px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.progress-node .node-index {
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 50%;
  background: #e2e8f0;
  color: #0f172a;
  font-weight: 600;
  font-size: 13px;
  text-align: center;
}

.progress-node .node-label {
  font-size: 13px;
  color: #0f172a;
  text-align: center;
  line-height: 1.3;
}

.progress-node.done .node-index {
  background: #22c55e;
  color: #fff;
}

.progress-node.active {
  border-color: #22c55e;
  background: #ecfdf3;
  box-shadow: 0 6px 18px -12px rgba(16, 185, 129, 0.7);
}

.progress-node.active .node-index {
  background: #15803d;
  color: #fff;
}

.stage-detail {
  padding: 4px 0 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stage-header h3 {
  margin: 0;
}

.stage-header p {
  margin: 4px 0 0 0;
  color: #64748b;
}

.command-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.command-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cmd {
  background: #0f172a;
  color: #e2e8f0;
  padding: 6px 10px;
  border-radius: 8px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
}

.note {
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .progress-wrapper {
    grid-template-columns: 1fr;
  }

  .progress-nodes {
    grid-template-columns: 1fr;
  }
}
</style>

