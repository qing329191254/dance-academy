<template>
  <div class="login-page">
    <div :class="isMobile ? 'login-inner' : 'card'">
      <template v-if="isMobile">
        <div class="login-brand">
          <img class="brand-logo" src="/logo.png" alt="高校FOR-GET舞室" />
          <div class="name">高校FOR-GET舞室</div>
          <div class="hint">机构管理后台</div>
        </div>
      </template>
      <template v-else>
        <img class="brand-logo" src="/logo.png" alt="高校FOR-GET舞室" />
        <div class="name">高校FOR-GET舞室</div>
        <div class="hint">机构管理后台</div>
      </template>

      <el-form :class="{ 'login-form': isMobile }" @submit.prevent="onSubmit">
        <el-form-item :label="isMobile ? '账号' : undefined">
          <el-input
            v-model="username"
            :placeholder="isMobile ? '请输入账号' : '账号'"
            size="large"
            :autocomplete="isMobile ? 'username' : undefined"
          />
        </el-form-item>
        <el-form-item :label="isMobile ? '密码' : undefined">
          <el-input
            v-model="password"
            :placeholder="isMobile ? '请输入密码' : '密码'"
            type="password"
            show-password
            size="large"
            :autocomplete="isMobile ? 'current-password' : undefined"
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="submit" @click="onSubmit">
          登录
        </el-button>
      </el-form>

      <a
        v-if="isMobile"
        class="icp icp--inline"
        href="https://beian.miit.gov.cn/"
        target="_blank"
        rel="noopener noreferrer"
      >蜀ICP备2026050020号-1</a>
    </div>
    <a
      v-if="!isMobile"
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
import { useBreakpoint } from '../composables/useBreakpoint'

const username = ref('')
const password = ref('')
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()
const { isMobile } = useBreakpoint()

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

.card :deep(.el-input__inner:-webkit-autofill),
.card :deep(.el-input__inner:-webkit-autofill:hover),
.card :deep(.el-input__inner:-webkit-autofill:focus),
.card :deep(.el-input__inner:-webkit-autofill:active) {
  -webkit-box-shadow: 0 0 0 1000px #fff inset !important;
  box-shadow: 0 0 0 1000px #fff inset !important;
  -webkit-text-fill-color: #16161c !important;
  caret-color: #16161c;
  transition: background-color 99999s ease-out 0s, color 99999s ease-out 0s;
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
    align-items: flex-start;
    background: #f0edf8;
    padding: 0;
    padding-bottom: 0;
  }

  .login-inner {
    width: 100%;
    min-height: 100dvh;
    max-width: none;
    background: transparent;
    border-radius: 0;
    box-shadow: none;
    padding: calc(32px + env(safe-area-inset-top, 0px)) 24px calc(24px + env(safe-area-inset-bottom, 0px));
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
  }

  .login-brand {
    margin-bottom: 8px;
  }

  .brand-logo {
    width: 64px;
    height: 64px;
    border-radius: 14px;
    margin-bottom: 14px;
  }

  .name {
    font-size: 22px;
  }

  .hint {
    margin: 6px 0 32px;
    font-size: 14px;
  }

  .login-form {
    flex: 1;
  }

  .login-form :deep(.el-form-item) {
    display: block;
    margin-bottom: 20px;
  }

  .login-form :deep(.el-form-item__label) {
    display: block;
    width: auto !important;
    height: auto;
    padding: 0 0 8px;
    text-align: left;
    float: none;
    color: #303038;
    font-weight: 500;
    line-height: 1.4;
  }

  .login-form :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }

  .login-form :deep(.el-input__wrapper) {
    background: #fff;
    box-shadow: 0 0 0 1px #dcdfe6 inset;
  }

  .login-form :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px var(--brand) inset;
  }

  .login-form :deep(.el-input__inner) {
    height: 48px;
    line-height: 48px;
    font-size: 16px;
  }

  .submit {
    min-height: 48px;
    margin-top: 12px;
    font-size: 16px;
    font-weight: 600;
  }

  .icp--inline {
    position: static;
    transform: none;
    display: block;
    margin-top: auto;
    padding-top: 24px;
    text-align: center;
    color: #b0b0b8;
    font-size: 12px;
    white-space: normal;
  }

  .icp--inline:hover {
    color: #8a8a96;
  }
}
</style>

<style>
.login-page input:-webkit-autofill,
.login-page input:-webkit-autofill:hover,
.login-page input:-webkit-autofill:focus,
.login-page input:-webkit-autofill:active,
.login-page .el-input__inner:-webkit-autofill,
.login-page .el-input__inner:-webkit-autofill:hover,
.login-page .el-input__inner:-webkit-autofill:focus,
.login-page .el-input__inner:-webkit-autofill:active,
.login-page input:-internal-autofill-selected,
.login-page .el-input__inner:-internal-autofill-selected,
.login-page input:autofill,
.login-page .el-input__inner:autofill {
  -webkit-box-shadow: 0 0 0 1000px #fff inset !important;
  box-shadow: 0 0 0 1000px #fff inset !important;
  background-color: #fff !important;
  -webkit-text-fill-color: #16161c !important;
  caret-color: #16161c;
  transition: background-color 99999s ease-out 0s, color 99999s ease-out 0s;
}
</style>
