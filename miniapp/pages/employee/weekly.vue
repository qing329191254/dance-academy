<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card">
        <text class="block-title">提交周报</text>
        <input v-model="weekLabel" class="input" placeholder="周期，如 2026-08-18~2026-08-24" />
        <textarea v-model="content" class="textarea" placeholder="填写本周工作内容" />
        <view class="btn-primary" @tap="submit">提交周报</view>
      </view>
    </view>
    <view class="section">
      <view class="card">
        <text class="block-title">历史周报</text>
        <view v-if="!list.length" class="muted empty">暂无记录</view>
        <view v-for="item in list" :key="item.id" class="item">
          <text class="label">{{ item.weekLabel }}</text>
          <text class="content">{{ item.content }}</text>
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getEmployeeWeeklyReports, submitEmployeeWeeklyReport } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { showError, showSuccess } from '@/common/toast.js'

const weekLabel = ref('')
const content = ref('')
const list = ref([])

async function load() {
  list.value = await getEmployeeWeeklyReports()
}

async function submit() {
  try {
    await submitEmployeeWeeklyReport({ weekLabel: weekLabel.value, content: content.value })
    showSuccess('周报已提交')
    content.value = ''
    await load()
  } catch (e) {
    showError(e.message || '提交失败')
  }
}

onShow(() => {
  if (!ensureLogin()) return
  load()
})
</script>

<style scoped>
.block-title { display: block; font-size: 30rpx; font-weight: 600; margin-bottom: 16rpx; }
.input, .textarea { width: 100%; background: #1c1c1c; border-radius: 12rpx; padding: 20rpx; color: #fff; margin-bottom: 16rpx; box-sizing: border-box; }
.textarea { min-height: 220rpx; }
.item { padding: 20rpx 0; border-top: 1rpx solid #2a2a2a; }
.label { display: block; font-size: 26rpx; color: #8a74e5; margin-bottom: 8rpx; }
.content { display: block; font-size: 28rpx; line-height: 1.6; white-space: pre-wrap; }
.empty { padding: 20rpx 0; }
</style>
