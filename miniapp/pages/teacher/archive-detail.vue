<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page" v-if="detail">
    <view class="section">
      <view class="card">
        <text class="title">{{ detail.name }}</text>
        <text class="muted">{{ formatDate(detail.date) }} · {{ detail.time }}</text>
        <text class="muted">教室：{{ detail.room }}</text>
        <text class="muted">预约 {{ detail.bookedCount }} 人 · 到课 {{ detail.checkedInCount }} 人</text>
        <text v-if="detail.renewalRate" class="muted">续报率：{{ detail.renewalRate }}</text>
        <text v-if="detail.studentFeedback" class="muted feedback">学员反馈：{{ detail.studentFeedback }}</text>
      </view>
    </view>

    <view class="section">
      <view class="card block">
        <text class="block-title">学员到课确认</text>
        <view v-if="!detail.bookings?.length" class="empty muted">暂无预约学员</view>
        <view v-for="item in detail.bookings" :key="item.id" class="row">
          <view class="left">
            <text>{{ item.nickname || '学员' }}</text>
            <text v-if="item.checkedIn" class="sub muted">
              {{ item.checkinSource === 'manual' ? '手动确认' : item.checkinSource === 'confirmed' ? '工作人员确认' : '扫码签到' }}
              <text v-if="item.operatorName"> · {{ item.operatorName }}</text>
            </text>
            <text v-else-if="item.checkinPending" class="sub pending">待工作人员确认</text>
          </view>
          <text v-if="item.checkedIn" class="done">已到课</text>
          <text v-else-if="item.checkinPending" class="pending-tag">待确认</text>
          <view v-else class="btn-mini" @tap="confirm(item)">手动确认</view>
        </view>
      </view>
    </view>
  </view>
  <app-toast />
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getTeacherArchiveDetail, manualTeacherCheckin } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { showError, showSuccess } from '@/common/toast.js'

const id = ref('')
const detail = ref(null)

function formatDate(value) {
  return value ? String(value).replace(/-/g, '.') : ''
}

async function loadDetail() {
  if (!id.value) return
  try {
    detail.value = await getTeacherArchiveDetail(id.value)
  } catch (e) {
    detail.value = null
  }
}

async function confirm(item) {
  try {
    await manualTeacherCheckin({
      userId: item.userId,
      scheduleId: detail.value.scheduleId,
      classDate: detail.value.date,
    })
    showSuccess('已确认到课')
    await loadDetail()
  } catch (e) {
    showError(e.message || '确认失败')
  }
}

onLoad((query) => {
  id.value = query.id || ''
})

onShow(() => {
  if (!ensureLogin()) return
  loadDetail()
})
</script>

<style scoped>
.title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.muted {
  display: block;
  font-size: 26rpx;
  line-height: 1.6;
}

.block-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 18rpx 0;
  border-top: 1rpx solid #2a2a2a;
  font-size: 28rpx;
}

.left {
  flex: 1;
}

.sub {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
}

.pending-tag {
  color: #e8c36a;
  font-size: 24rpx;
}

.sub.pending {
  color: #e8c36a;
}

.done {
  color: #8a74e5;
  font-size: 24rpx;
}

.btn-mini {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(138, 116, 229, 0.18);
  color: #cbbcff;
  font-size: 24rpx;
}

.empty {
  padding: 20rpx 0;
}
</style>
