<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view v-if="records.length" class="section">
      <view v-for="item in records" :key="item.id" class="card item">
        <view class="head">
          <text class="name">{{ item.name }}</text>
          <text class="tag">已签到</text>
        </view>
        <text class="date-line">{{ item.date }} · {{ item.time }}</text>
        <text class="muted">{{ item.teacher }} · {{ item.room }} · {{ item.duration }}</text>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-title">暂无已上课程</text>
      <text class="muted">完成课程签到后，记录会显示在这里</text>
      <view class="btn-primary empty-btn" @click="goScan">去扫码签到</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { startCheckInScan } from '@/common/checkin.js'
import { getPractice } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'

const records = ref([])

async function loadRecords() {
  try {
    records.value = await getPractice()
  } catch (e) {
    records.value = []
  }
}

onShow(() => {
  if (!ensureLogin()) return
  loadRecords()
})

function goScan() {
  if (!ensureLogin()) return
  startCheckInScan({
    onResult() {
      loadRecords()
    },
  }, { mode: 'class' })
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
  margin-bottom: 10rpx;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
}

.tag {
  font-size: 22rpx;
  color: #8a74e5;
}

.date-line {
  display: block;
  font-size: 28rpx;
  margin-bottom: 8rpx;
}

.muted {
  font-size: 26rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 48rpx;
  text-align: center;
  gap: 16rpx;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
}

.empty-btn {
  margin-top: 24rpx;
  padding: 20rpx 48rpx;
  font-size: 28rpx;
}
</style>
