<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <text class="page-title">{{ pageTitle }}</text>

      <button
        class="avatar-btn"
        open-type="chooseAvatar"
        hover-class="none"
        :loading="false"
        @chooseavatar="onChooseAvatar"
      >
        <image
          v-if="avatar"
          class="avatar-img"
          :src="avatar"
          mode="aspectFill"
          @load="onAvatarReady"
          @error="onAvatarReady"
        />
        <view v-else class="avatar-placeholder">
          <view class="camera-icon">+</view>
          <text class="upload-text">选择头像</text>
        </view>
        <view v-if="avatarLoading" class="avatar-loading">
          <view class="avatar-spinner" />
        </view>
      </button>

      <view class="form card">
        <view class="form-row">
          <text class="label">昵称</text>
          <input
            v-model="nickname"
            class="input"
            type="text"
            maxlength="20"
            placeholder="请输入昵称"
            placeholder-class="placeholder"
            :adjust-position="false"
            @input="onNickname"
            @blur="onNickname"
          />
        </view>

        <view class="form-row">
          <text class="label">性别</text>
          <view class="gender-group">
            <view class="gender-item" hover-class="none" @tap.stop="selectGender('男')">
              <view class="radio" :class="{ active: gender === '男' }">
                <text v-if="gender === '男'" class="check">✓</text>
              </view>
              <text>男</text>
            </view>
            <view class="gender-item" hover-class="none" @tap.stop="selectGender('女')">
              <view class="radio" :class="{ active: gender === '女' }">
                <text v-if="gender === '女'" class="check">✓</text>
              </view>
              <text>女</text>
            </view>
          </view>
        </view>

        <picker
          mode="date"
          :value="birthday || defaultBirthday"
          :end="today"
          @change="onBirthdayChange"
        >
          <view class="form-row">
            <text class="label">生日<text class="optional">选填</text></text>
            <view class="picker-value">
              <text :class="birthday ? '' : 'placeholder'">
                {{ birthday || '请选择' }}
              </text>
              <text class="arrow">›</text>
            </view>
          </view>
        </picker>
      </view>

      <view class="submit-btn" @click="submit">完成</view>
    </view>
      <app-toast />
  </view>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { completeProfile, getUser, isLoggedIn } from '@/common/auth.js'
import { showToast, showSuccess as showSuccessToast } from '@/common/toast.js'

const avatar = ref('')
const nickname = ref('')
const gender = ref('')
const birthday = ref('')
const today = ref('')
const defaultBirthday = '2000-01-01'
const submitting = ref(false)
const pageTitle = ref('完善资料')
const avatarLoading = ref(false)
let avatarTimer = null

onLoad(() => {
  if (!isLoggedIn()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  const user = getUser()
  nickname.value = user?.nickname || ''
  avatar.value = user?.avatar || ''
  gender.value = user?.gender || ''
  birthday.value = user?.birthday || ''
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  today.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  pageTitle.value = user?.profileComplete ? '个人资料' : '完善资料'
  uni.setNavigationBarTitle({ title: pageTitle.value })
})

function onChooseAvatar(e) {
  const url = e.detail?.avatarUrl
  if (!url) return
  avatarLoading.value = true
  avatar.value = url
  if (avatarTimer) clearTimeout(avatarTimer)
  avatarTimer = setTimeout(() => {
    avatarLoading.value = false
  }, 1600)
}

function onAvatarReady() {
  if (avatarTimer) {
    clearTimeout(avatarTimer)
    avatarTimer = null
  }
  avatarLoading.value = false
}

function onNickname(e) {
  const value = e?.detail?.value
  if (typeof value === 'string') {
    nickname.value = value
  }
}

function onBirthdayChange(e) {
  birthday.value = e.detail.value
}

function selectGender(value) {
  gender.value = value
}

async function submit() {
  if (submitting.value) return
  try {
    uni.hideKeyboard()
  } catch (e) {}
  await nextTick()

  const name = nickname.value.trim()
  if (!avatar.value) {
    showToast('请选择头像')
    return
  }
  if (!name) {
    showToast('请输入昵称')
    return
  }
  if (!gender.value) {
    showToast('请选择性别')
    return
  }

  submitting.value = true
  avatarLoading.value = true
  try {
    await completeProfile({
      nickname: name,
      avatar: avatar.value,
      gender: gender.value,
      birthday: birthday.value,
    })
    await showSuccessToast('保存成功')
    uni.switchTab({ url: '/pages/mine/mine' })
  } catch (err) {
    showToast(err?.message || '保存失败')
  } finally {
    submitting.value = false
    avatarLoading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #111111;
}

.page-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  margin-bottom: 48rpx;
}

.avatar-btn {
  display: flex;
  justify-content: center;
  margin: 0 auto 48rpx;
  padding: 0;
  background: transparent;
  border: none;
  line-height: normal;
  position: relative;
  width: 200rpx;
  height: 200rpx;
  overflow: hidden;
  border-radius: 50%;
}

.avatar-btn::after {
  border: none;
}

.avatar-img,
.avatar-placeholder {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
}

.avatar-img {
  background: #2a2a2a;
}

.avatar-placeholder {
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.camera-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #f0f0f0;
  color: #999999;
  font-size: 40rpx;
  line-height: 72rpx;
  text-align: center;
}

.upload-text {
  font-size: 24rpx;
  color: #999999;
}

.avatar-loading {
  position: absolute;
  inset: 0;
  z-index: 2;
  border-radius: 50%;
  background: rgba(17, 17, 17, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-spinner {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.18);
  border-top-color: #8a74e5;
  animation: avatar-spin 0.8s linear infinite;
}

@keyframes avatar-spin {
  to {
    transform: rotate(360deg);
  }
}

.form {
  padding: 0 28rpx;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 108rpx;
  border-bottom: 1rpx solid #2a2a2a;
}

.form-row:last-child {
  border-bottom: none;
}

.label {
  font-size: 30rpx;
  color: #9a9a9a;
  flex-shrink: 0;
  width: 120rpx;
}

.optional {
  margin-left: 8rpx;
  font-size: 22rpx;
  color: #666666;
}

.input {
  flex: 1;
  text-align: right;
  font-size: 30rpx;
  color: #ffffff;
}

.placeholder {
  color: #666666;
}

.gender-group {
  display: flex;
  align-items: center;
  gap: 40rpx;
}

.gender-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 30rpx;
  color: #ffffff;
}

.radio {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2rpx solid #666666;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radio.active {
  background: #8a74e5;
  border-color: #8a74e5;
}

.check {
  color: #ffffff;
  font-size: 22rpx;
  line-height: 1;
}

.picker-value {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
  font-size: 30rpx;
  color: #ffffff;
}

.arrow {
  color: #666666;
  font-size: 36rpx;
  line-height: 1;
}

.submit-btn {
  margin-top: 64rpx;
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
</style>
