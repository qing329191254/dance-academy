<template>
  <view v-if="visible" class="splash" :class="{ hiding }">
    <image class="bg" :src="src || '/static/splash.jpg'" mode="aspectFill" />
    <view class="mask" />
    <button
      class="skip-btn"
      plain
      hover-class="skip-btn-hover"
      @tap.stop="dismiss"
    >
      跳过 {{ countdown }}
    </button>
  </view>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { preloadTabPagesAsync } from '@/common/preloadTabs.js'

defineOptions({
  name: 'splash-overlay',
})

defineProps({
  src: { type: String, default: '' },
})
const emit = defineEmits(['done'])

const visible = ref(true)
const hiding = ref(false)
const countdown = ref(3)
let timer = null

function clearTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function finish() {
  if (hiding.value) return
  clearTimer()
  hiding.value = true
  setTimeout(() => {
    visible.value = false
    emit('done')
  }, 280)
}

function dismiss() {
  finish()
}

onMounted(() => {
  preloadTabPagesAsync()

  timer = setInterval(() => {
    if (countdown.value <= 1) {
      dismiss()
      return
    }
    countdown.value -= 1
  }, 1000)
})

onUnmounted(() => {
  clearTimer()
})
</script>

<style scoped>
.splash {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: #000000;
  overflow: hidden;
  opacity: 1;
  transition: opacity 0.28s ease;
}

.splash.hiding {
  opacity: 0;
  pointer-events: none;
}

.bg {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.mask {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.08), rgba(0, 0, 0, 0.35));
}

.skip-btn {
  position: absolute;
  right: 32rpx;
  bottom: calc(48rpx + env(safe-area-inset-bottom));
  z-index: 3;
  margin: 0;
  padding: 14rpx 32rpx;
  min-width: 140rpx;
  line-height: 1.4;
  border-radius: 999rpx;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 26rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.35);
}

.skip-btn::after {
  border: none;
}

.skip-btn-hover {
  background: rgba(0, 0, 0, 0.75);
  opacity: 0.92;
}
</style>
