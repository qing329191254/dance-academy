<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <text class="lead muted">按学习方式和目标选择：固定班、次通卡、私教，或定制赛事与商演。</text>
    </view>
    <view class="section">
      <view
        v-for="item in courseSystem"
        :key="item.key"
        class="card track"
        @click="go(item.key)"
      >
        <view class="left">
          <text class="name">{{ item.name }}</text>
          <text class="muted">{{ item.summary }}</text>
        </view>
        <view class="link-arrow" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { courseSystem } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

function go(key) {
  openPage(`/pages/course/system-detail?key=${key}`)
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
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 10rpx;
}

.link-arrow {
  color: #8a74e5;
}
</style>
