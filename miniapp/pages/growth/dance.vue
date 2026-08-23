<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <text class="lead muted">舞蹈发展成长线：演出练胆 → 商演实践 → 教师考核与任教。无支付环节，专注赋能与报名。</text>
    </view>
    <view class="section">
      <view
        v-for="item in danceTracks"
        :key="item.key"
        class="card track"
        @click="go(item.key)"
      >
        <view class="left">
          <text class="name">{{ item.name }}</text>
          <text class="muted">{{ item.desc }}</text>
        </view>
        <view class="badge">{{ item.level }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { danceTracks } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

function go(key) {
  openPage(`/pages/growth/track?key=${key}`)
}
</script>

<style scoped>
.lead {
  font-size: 28rpx;
  line-height: 1.7;
  display: block;
}

.track {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.left {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  flex: 1;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
}

.badge {
  min-width: 80rpx;
  text-align: center;
  padding: 12rpx 18rpx;
  border-radius: 16rpx;
  background: rgba(138, 116, 229, 0.18);
  color: #8a74e5;
  font-weight: 700;
}
</style>
