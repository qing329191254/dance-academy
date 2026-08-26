<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view v-if="!list.length" class="empty muted">暂无课堂档案</view>
      <view
        v-for="item in list"
        :key="item.id"
        class="card item"
        @tap="openDetail(item.id)"
      >
        <view class="head">
          <text class="name">{{ item.name }}</text>
          <text class="tag">{{ item.teacherCheckedAt ? '已签到' : '未签到' }}</text>
        </view>
        <text class="date">{{ formatDate(item.date) }} · {{ item.time }}</text>
        <text class="muted">教室：{{ item.room }}</text>
        <text class="muted">预约 {{ item.bookedCount }} 人 · 到课 {{ item.checkedInCount }} 人</text>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getTeacherArchives } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { openPage } from '@/common/navigate.js'

const list = ref([])

function formatDate(value) {
  return value ? String(value).replace(/-/g, '.') : ''
}

async function refresh() {
  try {
    const data = await getTeacherArchives()
    list.value = data.list || []
  } catch (e) {
    list.value = []
  }
}

function openDetail(id) {
  openPage(`/pages/teacher/archive-detail?id=${id}`)
}

onShow(() => {
  if (!ensureLogin()) return
  refresh()
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

.tag {
  font-size: 22rpx;
  color: #8a74e5;
}

.date {
  display: block;
  font-size: 28rpx;
  margin-bottom: 8rpx;
}

.muted {
  display: block;
  font-size: 24rpx;
  line-height: 1.6;
}

.empty {
  text-align: center;
  padding: 80rpx 0;
}
</style>
