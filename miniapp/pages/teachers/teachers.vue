<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view
        v-for="t in teachers"
        :key="t.id"
        class="card teacher"
        @click="go(`/pages/teachers/detail?id=${t.id}`)"
      >
        <image class="avatar" :src="t.avatar" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ t.name }}</text>
          <text class="accent">{{ t.style }}</text>
          <text class="muted">{{ t.intro }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getTeachers } from '@/common/api.js'
import { teachers as mockTeachers } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

const teachers = ref(mockTeachers)

onShow(async () => {
  try {
    const list = await getTeachers()
    if (list?.length) teachers.value = list
  } catch (e) {}
})

function go(url) {
  openPage(url)
}
</script>

<style scoped>
.teacher {
  display: flex;
  gap: 24rpx;
  margin-bottom: 20rpx;
}

.avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: #2a2a2a;
  flex-shrink: 0;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
}
</style>
