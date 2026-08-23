<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view
        v-for="c in courses"
        :key="c.id"
        class="card course"
        @click="goDetail(c.id)"
      >
        <view class="main">
          <text class="name">{{ c.name }}</text>
          <text class="muted desc">{{ c.desc }}</text>
        </view>
        <view class="side">
          <text class="price">¥{{ c.price }}</text>
          <text class="tag">{{ c.level }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCourses } from '@/common/api.js'
import { courses as mockCourses } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

const courses = ref(mockCourses)

onShow(async () => {
  try {
    const list = await getCourses()
    if (list?.length) courses.value = list
  } catch (e) {}
})

function goDetail(id) {
  openPage(`/pages/course/detail?id=${id}`)
}
</script>

<style scoped>
.course {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.main {
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 10rpx;
}

.desc {
  font-size: 26rpx;
  line-height: 1.6;
}

.side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
  gap: 12rpx;
}

.price {
  color: #8a74e5;
  font-size: 30rpx;
  font-weight: 600;
}
</style>
