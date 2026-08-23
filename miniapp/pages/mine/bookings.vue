<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view v-if="!bookingList.length" class="empty muted">暂无已约课程</view>
      <view v-for="item in bookingList" :key="item.id || item.key" class="card item">
        <view class="head">
          <text class="name">{{ item.name }}</text>
          <text class="status">{{ item.status }}</text>
        </view>
        <view class="date-line">
          <text v-if="item.dateText" class="date">{{ item.dateText }}</text>
          <text class="accent time">{{ item.time }}</text>
        </view>
        <text class="muted">老师：{{ item.teacher }} · {{ item.room }}</text>
        <view class="cancel-btn" @click="cancelBooking(item)">取消预约</view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getBookings, toggleBooking } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { showToast, showError } from '@/common/toast.js'

const bookingList = ref([])

async function refreshBookings() {
  try {
    bookingList.value = await getBookings()
  } catch (e) {
    bookingList.value = []
  }
}

onShow(() => {
  if (!ensureLogin()) return
  refreshBookings()
})

async function cancelBooking(item) {
  try {
    await toggleBooking(item.scheduleId, item.date || undefined)
    await refreshBookings()
    showToast('已取消预约')
  } catch (e) {
    showError(e.message || '取消失败')
  }
}
</script>

<style scoped>
.item {
  margin-bottom: 20rpx;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
}

.status {
  font-size: 24rpx;
  color: #8a74e5;
}

.date-line {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.date {
  font-size: 28rpx;
  font-weight: 600;
}

.time {
  font-size: 28rpx;
}

.muted {
  font-size: 26rpx;
}

.empty {
  text-align: center;
  padding: 80rpx 0;
  font-size: 28rpx;
}

.cancel-btn {
  margin-top: 24rpx;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  border-radius: 999rpx;
  font-size: 26rpx;
  color: #e57373;
  background: rgba(229, 115, 115, 0.12);
  border: 1rpx solid rgba(229, 115, 115, 0.35);
}
</style>
