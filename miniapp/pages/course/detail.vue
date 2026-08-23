<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page" v-if="course">
    <view class="banner">
      <text class="banner-name">{{ course.name }}</text>
      <text class="banner-level">{{ course.level }}</text>
    </view>
    <view class="section">
      <view class="card">
        <text class="label">课程介绍</text>
        <text class="body muted">{{ course.desc }}</text>
      </view>
    </view>
    <view class="section">
      <view class="card buy">
        <view>
          <text class="price">¥{{ course.price }}</text>
          <text class="muted">课程价格</text>
        </view>
        <view class="btn-primary" @click="buy">立即购买</view>
      </view>
    </view>
      <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCourse } from '@/common/api.js'
import { courses } from '@/common/mock.js'
import { showToast } from '@/common/toast.js'

const course = ref(null)

onLoad(async (query) => {
  const id = Number(query.id) || 0
  course.value = courses.find((c) => c.id === id) || null
  try {
    const data = await getCourse(id)
    if (data) course.value = data
  } catch (e) {}
})

function buy() {
  showToast('请联系门店或课程顾问完成购课')
}
</script>

<style scoped>
.banner {
  height: 280rpx;
  margin: 24rpx 32rpx 0;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #1c1c1c, #2f2750);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 36rpx;
  gap: 12rpx;
}

.banner-name {
  font-size: 44rpx;
  font-weight: 700;
}

.banner-level {
  color: #8a74e5;
  font-size: 26rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.body {
  font-size: 28rpx;
  line-height: 1.7;
}

.buy {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  display: block;
  font-size: 40rpx;
  color: #8a74e5;
  font-weight: 700;
  margin-bottom: 6rpx;
}
</style>
