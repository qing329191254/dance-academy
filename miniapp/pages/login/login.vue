<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="brand">
      <image v-if="logo" class="logo" :src="logo" mode="aspectFit" />
      <text class="title">高校FOR一GET街舞俱乐部</text>
      <text class="muted subtitle">登录后同步课程、卡包与习练记录</text>
    </view>

    <view class="actions">
      <view class="login-btn" @click="onLogin">微信一键登录</view>

      <view class="agreement">
        <text class="muted">登录即表示同意</text>
        <text class="link" @click="goLegal('user')">《用户协议》</text>
        <text class="muted">和</text>
        <text class="link" @click="goLegal('privacy')">《隐私政策》</text>
      </view>
    </view>
      <app-toast />
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { weixinOneTapLogin, navigateAfterLogin, isProfileComplete } from '@/common/auth.js'
import { showSuccess, showError } from '@/common/toast.js'
import { getBrand } from '@/common/api.js'

const logo = ref('')

onMounted(async () => {
  try {
    const data = await getBrand()
    if (data.studio?.logo) logo.value = data.studio.logo
  } catch (e) {}
})

async function onLogin() {
  try {
    await weixinOneTapLogin()
    if (isProfileComplete()) {
      await showSuccess('登录成功', 1500)
    }
    navigateAfterLogin()
  } catch (err) {
    showError('登录失败，请稍后重试')
  }
}

function goLegal(type) {
  const url =
    type === 'privacy'
      ? '/pages/legal/privacy-policy'
      : '/pages/legal/user-agreement'
  uni.navigateTo({ url })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 120rpx 48rpx 80rpx;
  background: #111111;
}

.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-top: 80rpx;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  margin-bottom: 32rpx;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.subtitle {
  font-size: 26rpx;
  line-height: 1.6;
}

.actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.login-btn {
  width: 100%;
  height: 96rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #9b86eb 0%, #8a74e5 55%, #7560d4 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 36rpx rgba(138, 116, 229, 0.35);
}

.agreement {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  font-size: 22rpx;
  line-height: 1.8;
  text-align: center;
  padding: 0 24rpx;
}

.link {
  color: #8a74e5;
  margin: 0 4rpx;
}
</style>
