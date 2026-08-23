<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="brand-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="brand-inner">
        <text class="brand-title">高校FOR一GET街舞俱乐部</text>
      </view>
    </view>

    <view class="section">
      <view class="intro card">
        <text class="title">成长中心</text>
        <text class="muted body">{{ growthIntro }}</text>
        <text class="tip">新学员默认享有 T1 权益，可通过消费、年限、考核等途径升级至 T2 / T3。</text>
      </view>
    </view>

    <view class="section">
      <view class="module card work" @click="go('/pages/growth/work')">
        <view class="module-top">
          <text class="module-name">勤工俭学</text>
          <view class="module-arrow">
            <text>进入</text>
            <view class="link-arrow" />
          </view>
        </view>
        <text class="muted">兼职 → 实习 → 管理（T1-T3）</text>
        <view class="chips">
          <text class="chip">兼职 T1</text>
          <text class="chip">实习 T2</text>
          <text class="chip">管理 T3</text>
        </view>
      </view>

      <view class="module card dance" @click="go('/pages/growth/dance')">
        <view class="module-top">
          <text class="module-name">舞蹈发展</text>
          <view class="module-arrow">
            <text>进入</text>
            <view class="link-arrow" />
          </view>
        </view>
        <text class="muted">演出 → 商演 → 教师（T1-T3）</text>
        <view class="chips">
          <text class="chip">演出 T1</text>
          <text class="chip">商演 T2</text>
          <text class="chip">教师 T3</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { growthIntro } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'
import { getStatusBarHeight } from '@/common/statusBar.js'

const statusBarHeight = getStatusBarHeight()

function go(url) {
  openPage(url)
}
</script>

<style scoped>
.brand-header {
  background: #111111;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.35);
}

.brand-inner {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 100rpx;
}

.brand-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #ffffff;
  text-align: center;
  line-height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.intro {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.title {
  font-size: 40rpx;
  font-weight: 700;
}

.body {
  font-size: 28rpx;
  line-height: 1.7;
}

.tip {
  font-size: 24rpx;
  color: #8a74e5;
  line-height: 1.6;
}

.module {
  margin-bottom: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.work {
  background: linear-gradient(145deg, #1c1c1c, #242038);
}

.dance {
  background: linear-gradient(145deg, #1c1c1c, #2a2030);
}

.module-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-name {
  font-size: 34rpx;
  font-weight: 700;
}

.module-arrow {
  display: inline-flex;
  align-items: center;
  color: #8a74e5;
  font-size: 26rpx;
}

.chips {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-top: 8rpx;
}

.chip {
  font-size: 22rpx;
  color: #d7d0ff;
  background: rgba(138, 116, 229, 0.18);
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
}
</style>
