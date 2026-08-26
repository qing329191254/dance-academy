<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page" v-if="item">
    <view class="banner">
      <text class="banner-name">{{ item.name }}</text>
      <text class="banner-sub">{{ item.summary }}</text>
    </view>
    <view class="section">
      <view class="card">
        <text class="label">课程介绍</text>
        <text class="body muted">{{ item.desc }}</text>
      </view>
    </view>
    <view class="section">
      <view class="card">
        <text class="label">适合谁</text>
        <text v-for="point in item.highlights" :key="point" class="point muted">· {{ point }}</text>
      </view>
    </view>
    <view class="section">
      <view class="btn-primary action" @click="onAction">{{ item.actionLabel }}</view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { courseSystem } from '@/common/mock.js'
import { legalInfo } from '@/common/legal.js'
import { openBookTab } from '@/common/navigate.js'
import { showToast } from '@/common/toast.js'

const item = ref(null)

onLoad((query) => {
  const key = query?.key || ''
  item.value = courseSystem.find((row) => row.key === key) || courseSystem[0]
  if (item.value?.name) {
    uni.setNavigationBarTitle({ title: item.value.name })
  }
})

function onAction() {
  if (item.value?.actionTab) {
    openBookTab(item.value.actionTab)
    return
  }
  uni.makePhoneCall({
    phoneNumber: legalInfo.phone,
    fail() {
      showToast('暂无法拨打电话')
    },
  })
}
</script>

<style scoped>
.banner {
  margin: 24rpx 32rpx 0;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #1c1c1c, #2f2750);
  padding: 40rpx 36rpx 36rpx;
}

.banner-name {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.banner-sub {
  color: #cfc7ff;
  font-size: 26rpx;
  line-height: 1.5;
}

.label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.body,
.point {
  font-size: 28rpx;
  line-height: 1.7;
}

.point {
  display: block;
}

.action {
  width: 100%;
}
</style>
