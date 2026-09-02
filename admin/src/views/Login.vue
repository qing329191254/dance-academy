<template>
  <div class="login-page">
    <div class="card">
      <img class="brand-logo" src="/logo.png" alt="高校FOR-GET舞室" />
      <div class="name">高校FOR-GET舞室</div>
      <div class="hint">机构管理后台</div>
      <el-form @submit.prevent="onSubmit">
        <el-form-item>
          <el-input v-model="username" placeholder="账号" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" placeholder="密码" type="password" show-password size="large" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="submit" @click="onSubmit">登录</el-button>
      </el-form>
    </div>
    <a
      class="icp"
      href="https://beian.miit.gov.cn/"
      target="_blank"
      rel="noopener noreferrer"
    >蜀ICP备2026050020号-1</a>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const username = ref('')
const password = ref('')
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()

async function onSubmit() {
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top, #2a2344, #111118 55%);
  position: relative;
  padding-bottom: 56px;
  box-sizing: border-box;
}
.card {
  width: 380px;
  background: #fff;
  border-radius: 16px;
  padding: 36px 32px 28px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.28);
}
.brand-logo {
  display: block;
  width: 72px;
  height: 72px;
  border-radius: 16px;
  object-fit: contain;
  margin-bottom: 16px;
  background: #2a1818;
}
.name {
  font-size: 20px;
  font-weight: 700;
}
.hint {
  color: #8a8a96;
  margin: 8px 0 28px;
}
.submit {
  width: 100%;
}
.icp {
  position: absolute;
  left: 50%;
  bottom: 20px;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.55);
  font-size: 13px;
  text-decoration: none;
  letter-spacing: 0.02em;
  white-space: nowrap;
}
.icp:hover {
  color: rgba(255, 255, 255, 0.85);
}

@media (max-width: 768px) {
  .login-page {
    padding: 24px 16px 64px;
    align-items: flex-start;
    padding-top: max(48px, 12vh);
  }
  .card {
    width: 100%;
    max-width: 400px;
    padding: 28px 20px 24px;
    box-sizing: border-box;
  }
  .icp {
    bottom: calc(16px + env(safe-area-inset-bottom, 0px));
  }
}
</style>
