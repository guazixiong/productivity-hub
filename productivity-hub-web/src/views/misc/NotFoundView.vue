<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在或仍在建设中</p>
    <router-link to="/configs">返回控制台</router-link>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #f8fafc;
}

h1 {
  font-size: 64px;
  margin: 0;
}
</style>

