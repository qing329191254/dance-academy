<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card">
        <text class="title">意见反馈</text>
        <text class="muted tip">课程、教室、约课或成长中心的问题与建议，都可以写在这里。</text>
        <textarea
          class="input"
          v-model="content"
          maxlength="500"
          placeholder="请填写你的意见或建议"
          placeholder-class="placeholder"
        />
        <text class="count muted">{{ content.length }}/500</text>
        <view class="field">
          <text class="label">联系方式</text>
          <input
            class="contact"
            v-model="contact"
            maxlength="40"
            placeholder="手机号或微信，方便回访"
            placeholder-class="placeholder"
          />
        </view>
        <view class="btn-primary submit" :class="{ disabled: submitting }" @click="submit">
          {{ submitting ? '提交中...' : '提交反馈' }}
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { submitFeedback } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { selectedCampusId } from '@/common/campus.js'
import { showSuccess, showError, showToast } from '@/common/toast.js'

const content = ref('')
const contact = ref('')
const submitting = ref(false)

async function submit() {
  if (!ensureLogin()) return
  const text = content.value.trim()
  const contactText = contact.value.trim()
  if (text.length < 5) {
    showToast('请至少填写 5 个字')
    return
  }
  if (!contactText) {
    showToast('请填写联系方式')
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await submitFeedback({
      content: text,
      contact: contactText,
      campusId: selectedCampusId.value,
    })
    content.value = ''
    contact.value = ''
    showSuccess('已提交，感谢反馈')
  } catch (e) {
    showError(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.tip {
  display: block;
  font-size: 24rpx;
  line-height: 1.6;
  margin-bottom: 24rpx;
}

.input {
  width: 100%;
  min-height: 240rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #242424;
  color: #ffffff;
  font-size: 28rpx;
  line-height: 1.6;
  box-sizing: border-box;
}

.placeholder {
  color: #6a6a6a;
}

.count {
  display: block;
  text-align: right;
  font-size: 22rpx;
  margin-top: 12rpx;
}

.field {
  margin-top: 28rpx;
}

.label {
  display: block;
  font-size: 26rpx;
  margin-bottom: 12rpx;
}

.contact {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background: #242424;
  color: #ffffff;
  font-size: 28rpx;
  box-sizing: border-box;
}

.submit {
  margin-top: 40rpx;
  height: 88rpx;
  width: 100%;
}

.submit.disabled {
  opacity: 0.6;
}
</style>
