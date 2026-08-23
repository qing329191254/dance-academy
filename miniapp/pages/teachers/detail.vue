<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page" v-if="teacher">
    <view class="hero card">
      <image v-if="teacher.avatar" class="avatar" :src="teacher.avatar" mode="aspectFill" />
      <view v-else class="avatar" />
      <view class="meta">
        <text class="name">{{ teacher.name }}</text>
        <text class="style accent">{{ teacher.style }}</text>
      </view>
    </view>

    <view class="section">
      <view class="card">
        <text class="label">老师介绍</text>
        <text class="body muted">{{ teacher.intro }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getTeacher } from '@/common/api.js'
import { teachers } from '@/common/mock.js'

const teacher = ref(null)

onLoad(async (query) => {
  const id = Number(query.id) || 0
  teacher.value = teachers.find((t) => t.id === id) || null
  try {
    const data = await getTeacher(id)
    if (data) teacher.value = data
  } catch (e) {}
})
</script>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  gap: 28rpx;
  margin: 24rpx 32rpx 0;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #2a2a2a;
  flex-shrink: 0;
}

.meta {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.name {
  font-size: 40rpx;
  font-weight: 700;
}

.style {
  font-size: 28rpx;
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
</style>
