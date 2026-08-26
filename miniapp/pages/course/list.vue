<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="module card trial" @click="go('/pages/course/trial')">
        <view class="module-top">
          <view>
            <text class="module-name">体验课</text>
            <text class="muted summary">{{ trialCourse.summary }}</text>
          </view>
          <view class="price-box">
            <text class="price-mark">¥</text>
            <text class="price">{{ trialCourse.price }}</text>
            <text class="price-unit">/ {{ trialCourse.unit }}</text>
          </view>
        </view>
        <text class="tag">{{ trialCourse.tag }}</text>
      </view>

      <view class="module card system" @click="go('/pages/course/system')">
        <view class="module-top">
          <text class="module-name">课程体系介绍</text>
          <view class="module-arrow">
            <text>进入</text>
            <view class="link-arrow" />
          </view>
        </view>
        <text class="muted summary">固定班、次通卡、私教与定制商演</text>
        <view class="chips">
          <text v-for="item in courseSystem" :key="item.key" class="chip">{{ item.name }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { trialCourse, courseSystem } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

function go(url) {
  openPage(url)
}
</script>

<style scoped>
.module {
  margin-bottom: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.trial {
  background: linear-gradient(145deg, #1c1c1c, #2a2038);
}

.system {
  background: linear-gradient(145deg, #1c1c1c, #242038);
}

.module-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20rpx;
}

.module-name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
}

.summary {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  line-height: 1.5;
}

.price-box {
  display: flex;
  align-items: flex-end;
  flex-shrink: 0;
  color: #8a74e5;
  padding-top: 4rpx;
}

.price-mark {
  font-size: 26rpx;
  font-weight: 700;
  margin-bottom: 6rpx;
}

.price {
  font-size: 56rpx;
  font-weight: 700;
  line-height: 1;
}

.price-unit {
  font-size: 22rpx;
  color: #9a9a9a;
  margin: 0 0 6rpx 6rpx;
}

.module-arrow {
  display: inline-flex;
  align-items: center;
  color: #8a74e5;
  font-size: 26rpx;
  flex-shrink: 0;
}

.chips {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.chip {
  font-size: 22rpx;
  color: #d7d0ff;
  background: rgba(138, 116, 229, 0.18);
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
}
</style>
