<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card hero">
        <text class="muted">老师</text>
        <text class="title">{{ stats.teacherName || '我的课时' }}</text>
      </view>
    </view>

    <view class="section grid">
      <view class="card stat">
        <text class="num">{{ stats.monthSessions || 0 }}</text>
        <text class="muted">本月课堂</text>
      </view>
      <view class="card stat">
        <text class="num">{{ stats.monthHours || 0 }}</text>
        <text class="muted">本月课时</text>
      </view>
      <view class="card stat">
        <text class="num">{{ stats.totalSessions || 0 }}</text>
        <text class="muted">累计课堂</text>
      </view>
      <view class="card stat">
        <text class="num">{{ stats.totalHours || 0 }}</text>
        <text class="muted">累计课时</text>
      </view>
    </view>

    <view class="section">
      <view class="card summary">
        <text class="label">累计到课学员人次</text>
        <text class="value">{{ stats.totalStudents || 0 }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getTeacherStats } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'

const stats = ref({})

onShow(async () => {
  if (!ensureLogin()) return
  try {
    stats.value = await getTeacherStats()
  } catch (e) {
    stats.value = {}
  }
})
</script>

<style scoped>
.hero {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.title {
  font-size: 40rpx;
  font-weight: 700;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  align-items: flex-start;
}

.num {
  font-size: 44rpx;
  font-weight: 700;
  color: #8a74e5;
}

.summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.label {
  font-size: 28rpx;
}

.value {
  font-size: 40rpx;
  font-weight: 700;
  color: #8a74e5;
}
</style>
