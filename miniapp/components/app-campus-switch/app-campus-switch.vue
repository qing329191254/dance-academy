<template>
  <view class="campus-switch">
    <view class="trigger" @click.stop="openSheet">
      <image class="pin" src="/static/nav/location.svg" mode="aspectFit" />
      <text class="name">{{ currentCampus.shortName }}</text>
      <view class="chevron" />
    </view>
    <view v-if="open" class="sheet-mask" @click.stop="open = false" @touchmove.stop.prevent>
      <view class="sheet" @click.stop>
        <text class="sheet-title">切换校区</text>
        <view
          v-for="item in CAMPUSES"
          :key="item.id"
          class="sheet-item"
          :class="{ active: item.id === currentCampus.id }"
          @click="pick(item)"
        >
          <text class="sheet-name">{{ item.name }}</text>
          <text v-if="item.id === currentCampus.id" class="check">✓</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { CAMPUSES, currentCampus, selectCampus } from '@/common/campus.js'

const open = ref(false)

function openSheet() {
  open.value = true
}

function pick(item) {
  selectCampus(item.id)
  open.value = false
}
</script>

<style scoped>
.campus-switch {
  position: relative;
  z-index: 2;
  pointer-events: auto;
  max-width: 220rpx;
}

.trigger {
  display: flex;
  align-items: center;
  gap: 6rpx;
  max-width: 220rpx;
  padding: 8rpx 4rpx;
}

.pin {
  width: 28rpx;
  height: 28rpx;
  flex-shrink: 0;
}

.name {
  min-width: 0;
  font-size: 24rpx;
  font-weight: 600;
  color: #ffffff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.45);
}

.chevron {
  width: 10rpx;
  height: 10rpx;
  margin-top: -4rpx;
  border-right: 2rpx solid #ffffff;
  border-bottom: 2rpx solid #ffffff;
  transform: rotate(45deg);
  flex-shrink: 0;
  opacity: 0.9;
}

.sheet-mask {
  position: fixed;
  inset: 0;
  z-index: 4000;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  background: #1c1c1c;
  border-radius: 24rpx 24rpx 0 0;
  padding: 12rpx 0 calc(24rpx + env(safe-area-inset-bottom));
}

.sheet-title {
  display: block;
  padding: 24rpx 32rpx 12rpx;
  text-align: center;
  font-size: 26rpx;
  color: #9a9a9a;
}

.sheet-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 28rpx 40rpx;
}

.sheet-item.active .sheet-name {
  color: #8a74e5;
  font-weight: 600;
}

.sheet-name {
  font-size: 30rpx;
  color: #ffffff;
}

.check {
  color: #8a74e5;
  font-size: 30rpx;
}
</style>
