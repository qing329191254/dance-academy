<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view v-if="myCourses.length" class="section">
      <view v-for="item in myCourses" :key="item.id" class="card item">
        <view class="head">
          <text class="name">{{ item.name }}</text>
          <text class="status">{{ item.status }}</text>
        </view>
        <text class="muted teacher">授课老师：{{ item.teacher }}</text>
        <view class="meta-row">
          <text class="muted">学习进度</text>
          <text class="accent">{{ item.progress }}</text>
        </view>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-title">暂无课程</text>
      <text class="muted">报名课程后会显示在这里</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyCourses } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'

const myCourses = ref([])

onShow(async () => {
  if (!ensureLogin()) return
  try {
    const list = await getMyCourses()
    myCourses.value = Array.isArray(list) ? list : []
  } catch (e) {
    myCourses.value = []
  }
})
</script>

<style scoped>
.item {
  margin-bottom: 20rpx;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
}

.status {
  font-size: 24rpx;
  color: #8a74e5;
}

.teacher {
  display: block;
  font-size: 26rpx;
  margin-bottom: 16rpx;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid #2a2a2a;
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
</style>
