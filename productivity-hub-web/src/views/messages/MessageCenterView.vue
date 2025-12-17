<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { MessageChannel } from '@/types/messages'

const router = useRouter()

type ChannelSchema = {
  label: string
  description: string
}

const schemas: Record<MessageChannel, ChannelSchema> = {
  sendgrid: {
    label: 'SendGrid 邮件',
    description: '支持多收件人与富文本正文，发送结果实时返回',
  },
  dingtalk: {
    label: '钉钉 Webhook',
    description: '机器人签名与消息类型支持文本、Markdown、链接',
  },
}

const navigateToComposer = (channel: MessageChannel) => {
  router.push(`/messages/composer/${channel}`)
}
</script>

<template>
  <div class="message-center">
    <div class="channel-grid">
      <el-card
        v-for="(schema, key) in schemas"
        :key="key"
        class="channel-card"
        shadow="hover"
        @click="navigateToComposer(key as MessageChannel)"
      >
        <div class="channel-card__title">
          <h3>{{ schema.label }}</h3>
          <el-tag size="small" effect="dark">{{ key }}</el-tag>
        </div>
        <p>{{ schema.description }}</p>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.message-center {
  padding: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(99, 102, 241, 0.06) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(139, 92, 246, 0.05) 0%, transparent 50%),
    linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  background-attachment: fixed;
  min-height: calc(100vh - 60px);
  padding: 32px;
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 28px;
  max-width: 1200px;
  margin: 0 auto;
}

.channel-card {
  border-radius: 20px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%);
  backdrop-filter: blur(12px) saturate(180%);
  position: relative;
  overflow: hidden;
}

.channel-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #ec4899);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.channel-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 
    0 20px 50px rgba(99, 102, 241, 0.2),
    0 0 0 1px rgba(99, 102, 241, 0.1) inset;
  border-color: rgba(99, 102, 241, 0.3);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.95) 100%);
}

.channel-card:hover::before {
  opacity: 1;
}

.channel-card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.channel-card h3 {
  margin: 0;
  font-size: 20px;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
  letter-spacing: -0.3px;
}

.channel-card p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
}
</style>

