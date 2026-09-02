<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card">
        <text class="title">现场签到</text>
        <text class="muted tip">选择本节课程并开启签到，将二维码展示给到场人员扫描；扫描后需在此处确认到场。</text>

        <view class="field">
          <text class="label">日期</text>
          <picker mode="date" :value="classDate" @change="onDateChange">
            <view class="picker-value">
              <text>{{ classDate }}</text>
              <text class="arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="field">
          <text class="label">选择课程</text>
          <picker mode="selector" :range="scheduleNames" :value="scheduleIndex" @change="onScheduleChange">
            <view class="picker-value">
              <text :class="selectedSchedule ? '' : 'placeholder'">{{ selectedSchedule?.name || '请选择本节课程' }}</text>
              <text class="arrow">›</text>
            </view>
          </picker>
        </view>

        <view v-if="selectedSchedule" class="meta muted">
          <text>{{ selectedSchedule.timeText }} · {{ selectedSchedule.room }}</text>
          <text>{{ selectedSchedule.teacherName }}</text>
        </view>

        <view class="actions-row">
          <view class="btn-primary action-btn" :class="{ disabled: opening }" @tap="openSession">
            {{ session ? '重新开启' : '开启签到' }}
          </view>
          <view v-if="session" class="btn-ghost action-btn" @tap="closeSession">结束签到</view>
        </view>
      </view>
    </view>

    <view v-if="session" class="section">
      <view class="card qr-card">
        <text class="block-title">展示此码供扫描</text>
        <text class="muted refresh-tip">二维码约 {{ expiresIn }} 秒自动刷新，请勿截图转发</text>
        <image v-if="qrDataUrl" class="qr-image" :src="qrDataUrl" mode="aspectFit" />
        <view v-else class="qr-loading muted">加载二维码...</view>
      </view>
    </view>

    <view v-if="session" class="section">
      <view class="card">
        <view class="head-row">
          <text class="block-title">待确认 {{ pendingList.length }} 人</text>
          <text class="link" @tap="loadPending">刷新</text>
        </view>
        <view v-if="!pendingList.length" class="empty muted">暂无待确认记录</view>
        <view v-for="item in pendingList" :key="item.id" class="pending-row">
          <view class="left">
            <text class="name">{{ item.nickname || '用户' }}</text>
            <text class="sub muted">{{ item.checkinTypeLabel || item.roleLabel }} · {{ formatTime(item.scannedAt) }}</text>
          </view>
          <view class="row-actions">
            <view class="btn-mini danger" @tap="rejectItem(item)">拒绝</view>
            <view class="btn-mini primary" @tap="confirmItem(item)">确认到场</view>
          </view>
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  closeEmployeeCheckinSession,
  confirmEmployeeCheckinPending,
  getEmployeeCheckinPending,
  getEmployeeCheckinSchedules,
  getEmployeeCheckinSessionPayload,
  openEmployeeCheckinSession,
  rejectEmployeeCheckinPending,
} from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { showError, showSuccess } from '@/common/toast.js'

const classDate = ref(formatToday())
const schedules = ref([])
const scheduleIndex = ref(0)
const session = ref(null)
const qrDataUrl = ref('')
const expiresIn = ref(60)
const pendingList = ref([])
const opening = ref(false)
let refreshTimer = null
let pendingTimer = null

const scheduleNames = computed(() => schedules.value.map((item) => `${item.name} ${item.timeText || ''}`.trim()))
const selectedSchedule = computed(() => schedules.value[scheduleIndex.value] || null)

function formatToday() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function onDateChange(e) {
  classDate.value = e.detail.value
  loadSchedules()
}

function onScheduleChange(e) {
  const index = Number(e.detail.value)
  scheduleIndex.value = Number.isFinite(index) ? index : 0
  syncSessionFromSchedule()
}

async function loadSchedules() {
  try {
    schedules.value = await getEmployeeCheckinSchedules(classDate.value)
    if (scheduleIndex.value >= schedules.value.length) {
      scheduleIndex.value = 0
    }
    syncSessionFromSchedule()
  } catch {
    schedules.value = []
    session.value = null
  }
}

function syncSessionFromSchedule() {
  const active = selectedSchedule.value?.activeSession
  session.value = active || null
  if (session.value) {
    startRefreshLoop()
    loadPending()
  } else {
    stopTimers()
    qrDataUrl.value = ''
    pendingList.value = []
  }
}

async function openSession() {
  if (!selectedSchedule.value || opening.value) return
  opening.value = true
  try {
    session.value = await openEmployeeCheckinSession({
      scheduleId: selectedSchedule.value.id,
      classDate: classDate.value,
    })
    showSuccess('签到已开启')
    await loadSchedules()
    startRefreshLoop()
    await loadPending()
  } catch (e) {
    showError(e.message || '开启失败')
  } finally {
    opening.value = false
  }
}

async function closeSession() {
  if (!session.value?.id) return
  const ok = await new Promise((resolve) => {
    uni.showModal({
      title: '结束签到',
      content: '确认结束本场签到？二维码将立即失效。',
      success: (res) => resolve(!!res.confirm),
    })
  })
  if (!ok) return
  try {
    await closeEmployeeCheckinSession(session.value.id)
    showSuccess('已结束签到')
    session.value = null
    stopTimers()
    qrDataUrl.value = ''
    pendingList.value = []
    await loadSchedules()
  } catch (e) {
    showError(e.message || '操作失败')
  }
}

async function refreshQr() {
  if (!session.value?.id) return
  try {
    const data = await getEmployeeCheckinSessionPayload(session.value.id)
    qrDataUrl.value = data.qrDataUrl || ''
    expiresIn.value = data.expiresIn || 60
  } catch (e) {
    qrDataUrl.value = ''
  }
}

async function loadPending() {
  if (!selectedSchedule.value) return
  try {
    pendingList.value = await getEmployeeCheckinPending(selectedSchedule.value.id, classDate.value)
  } catch {
    pendingList.value = []
  }
}

function startRefreshLoop() {
  stopTimers()
  refreshQr()
  pendingTimer = setInterval(loadPending, 5000)
  refreshTimer = setInterval(refreshQr, 45000)
}

function stopTimers() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  if (pendingTimer) {
    clearInterval(pendingTimer)
    pendingTimer = null
  }
}

async function confirmItem(item) {
  try {
    await confirmEmployeeCheckinPending(item.id)
    showSuccess('已确认到场')
    await loadPending()
  } catch (e) {
    showError(e.message || '确认失败')
  }
}

async function rejectItem(item) {
  const ok = await new Promise((resolve) => {
    uni.showModal({
      title: '拒绝签到',
      content: `确认拒绝「${item.nickname || '用户'}」的签到？`,
      success: (res) => resolve(!!res.confirm),
    })
  })
  if (!ok) return
  try {
    await rejectEmployeeCheckinPending(item.id)
    showSuccess('已拒绝')
    await loadPending()
  } catch (e) {
    showError(e.message || '操作失败')
  }
}

watch(classDate, () => {
  if (session.value) {
    loadPending()
  }
})

onShow(() => {
  if (!ensureLogin()) return
  loadSchedules()
})

onUnmounted(() => {
  stopTimers()
})
</script>

<style scoped>
.title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.tip {
  display: block;
  font-size: 24rpx;
  line-height: 1.6;
  margin-bottom: 28rpx;
}

.field {
  margin-bottom: 24rpx;
}

.label {
  display: block;
  font-size: 26rpx;
  margin-bottom: 12rpx;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background: #242424;
  color: #ffffff;
  font-size: 28rpx;
}

.placeholder {
  color: #6a6a6a;
}

.arrow {
  color: #6a6a6a;
}

.meta text {
  display: block;
  font-size: 24rpx;
  line-height: 1.6;
}

.actions-row {
  display: flex;
  gap: 16rpx;
  margin-top: 28rpx;
}

.action-btn {
  flex: 1;
  height: 84rpx;
  line-height: 84rpx;
  text-align: center;
}

.action-btn.disabled {
  opacity: 0.6;
}

.btn-ghost {
  border-radius: 999rpx;
  border: 1rpx solid rgba(138, 116, 229, 0.45);
  color: #8a74e5;
  font-size: 28rpx;
}

.block-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.refresh-tip {
  display: block;
  font-size: 22rpx;
  margin-bottom: 20rpx;
}

.qr-card {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-image {
  width: 420rpx;
  height: 420rpx;
  background: #ffffff;
  border-radius: 16rpx;
}

.qr-loading {
  padding: 80rpx 0;
}

.head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.link {
  font-size: 24rpx;
  color: #8a74e5;
}

.pending-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 20rpx 0;
  border-top: 1rpx solid rgba(255, 255, 255, 0.06);
}

.name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}

.sub {
  display: block;
  font-size: 22rpx;
  margin-top: 6rpx;
}

.row-actions {
  display: flex;
  gap: 12rpx;
}

.btn-mini {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
}

.btn-mini.primary {
  background: rgba(138, 116, 229, 0.22);
  color: #8a74e5;
}

.btn-mini.danger {
  background: rgba(245, 108, 108, 0.16);
  color: #f56c6c;
}

.empty {
  padding: 24rpx 0;
  text-align: center;
}
</style>
