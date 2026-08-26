<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view v-if="!list.length && !loading" class="empty muted">暂无学员评价</view>
      <view v-for="item in list" :key="item.id" class="card review-card">
        <view class="head">
          <text class="name">{{ item.nickname || '学员' }}</text>
          <text class="time muted">{{ formatTime(item.createdAt) }}</text>
        </view>
        <text class="content">{{ item.content }}</text>
        <view class="actions">
          <view class="btn-danger" @tap="remove(item)">删除</view>
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { deleteTeacherReview, getTeacherReviews } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { showError, showSuccess } from '@/common/toast.js'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const size = 20

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())}`
}

async function load(reset = false) {
  if (loading.value) return
  if (reset) {
    page.value = 1
    list.value = []
  }
  loading.value = true
  try {
    const data = await getTeacherReviews(page.value, size)
    const rows = data.list || []
    total.value = data.total || 0
    list.value = reset ? rows : list.value.concat(rows)
  } catch {
    if (reset) list.value = []
  } finally {
    loading.value = false
  }
}

async function remove(item) {
  const ok = await new Promise((resolve) => {
    uni.showModal({
      title: '删除评价',
      content: '确认删除这条学员评价？',
      success: (res) => resolve(!!res.confirm),
    })
  })
  if (!ok) return
  try {
    await deleteTeacherReview(item.id)
    showSuccess('已删除')
    await load(true)
  } catch (e) {
    showError(e.message || '删除失败')
  }
}

onShow(() => {
  if (!ensureLogin()) return
  load(true)
})

onReachBottom(() => {
  if (list.value.length >= total.value) return
  page.value += 1
  load(false)
})
</script>

<style scoped>
.review-card {
  margin-bottom: 20rpx;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.name {
  font-size: 30rpx;
  font-weight: 600;
}

.time {
  font-size: 22rpx;
}

.content {
  display: block;
  font-size: 28rpx;
  line-height: 1.7;
  color: #d8d8de;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20rpx;
}

.btn-danger {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(245, 108, 108, 0.16);
  color: #f56c6c;
  font-size: 24rpx;
}

.empty {
  padding: 40rpx 0;
  text-align: center;
}
</style>
