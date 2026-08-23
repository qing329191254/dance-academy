<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view v-for="item in cardList" :key="item.id" class="pass-card">
        <image class="pass-bg" :src="item.cover" mode="aspectFill" />
        <view class="pass-mask" />
        <view class="pass-content">
          <view class="head">
            <text class="name">{{ item.name }}</text>
            <text class="type-tag">{{ item.type }}</text>
          </view>
          <view class="foot">
            <view class="meta-list">
              <view class="meta-item">
                <text class="meta-label">剩余次数</text>
                <text class="meta-value accent">{{ item.remain }}/{{ item.total }}</text>
              </view>
              <view class="meta-item">
                <text class="meta-label">有效期至</text>
                <text class="meta-value">{{ item.expire }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCards } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'

const cardList = ref([])

onShow(async () => {
  if (!ensureLogin()) return
  try {
    cardList.value = await getCards()
  } catch (e) {
    cardList.value = []
  }
})
</script>

<style scoped>
.pass-card {
  position: relative;
  height: 360rpx;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.35);
}

.pass-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.pass-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(0, 0, 0, 0.15) 0%,
    rgba(0, 0, 0, 0.05) 45%,
    rgba(0, 0, 0, 0.72) 100%
  );
}

.pass-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 32rpx;
}

.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.name {
  flex: 1;
  font-size: 36rpx;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.3;
  text-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.45);
}

.type-tag {
  flex-shrink: 0;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.35);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.foot {
  display: flex;
  align-items: flex-end;
}

.meta-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}

.meta-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.meta-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
}

.accent {
  color: #e8deff;
}
</style>
