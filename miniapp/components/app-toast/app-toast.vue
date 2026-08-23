<template>
  <view v-if="state.visible" class="toast-mask" :style="{ paddingTop: state.offsetTop }">
    <view class="toast">
      <text class="toast-text">{{ state.title }}</text>
    </view>
  </view>
</template>

<script setup>
import { useToastState } from '@/common/toast.js'

defineOptions({
  name: 'app-toast',
})

const state = useToastState()
</script>

<style scoped>
.toast-mask {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 0 48rpx 48rpx;
  pointer-events: none;
}

.toast {
  max-width: 560rpx;
  padding: 24rpx 40rpx;
  border-radius: 20rpx;
  background: rgba(28, 28, 28, 0.96);
  border: 1rpx solid #2e2e2e;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.45);
}

.toast-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: #ffffff;
  text-align: center;
}
</style>
