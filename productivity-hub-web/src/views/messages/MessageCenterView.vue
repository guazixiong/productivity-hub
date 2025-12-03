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
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.channel-card {
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.16);
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.channel-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.15);
  border-color: #6366f1;
  background: rgba(255, 255, 255, 0.95);
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
  font-size: 18px;
  color: #1e1b4b;
  font-weight: 600;
}

.channel-card p {
  margin: 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
}
</style>

