<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card">
        <text class="title">提交练舞预约</text>
        <text class="muted tip">学员福利，不扣课时。提交后需管理员审核，通过后即可按约定时段练舞。</text>

        <view class="field">
          <text class="label">姓名</text>
          <input
            class="text-input"
            v-model="name"
            maxlength="20"
            placeholder="用于审核名单"
            placeholder-class="placeholder"
          />
        </view>

        <view class="field">
          <text class="label">教室</text>
          <picker mode="selector" :range="classroomNames" :value="classroomIndex" @change="onClassroomChange">
            <view class="picker-value">
              <text :class="selectedClassroom ? '' : 'placeholder'">{{ selectedClassroom?.name || '请选择教室' }}</text>
              <text class="arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="field">
          <text class="label">日期</text>
          <picker mode="date" :value="classDate" :start="minDate" @change="onDateChange">
            <view class="picker-value">
              <text>{{ classDate }}</text>
              <text class="arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="field">
          <text class="label">时段</text>
          <picker
            mode="selector"
            :range="slotLabels"
            :value="slotIndex"
            :disabled="!slotOptions.length"
            @change="onSlotChange"
          >
            <view class="picker-value">
              <text :class="selectedSlot ? '' : 'placeholder'">{{ slotPlaceholder }}</text>
              <text class="arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="btn-primary submit" :class="{ disabled: submitting }" @click="submit">
          {{ submitting ? '提交中...' : '提交预约' }}
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">我的预约</text>
      </view>
      <view v-if="!bookings.length" class="empty muted">暂无练舞预约</view>
      <view v-for="item in bookings" :key="item.id" class="card item">
        <view class="head">
          <text class="name">{{ item.classroomName || '教室' }}</text>
          <text class="status">{{ item.statusLabel }}</text>
        </view>
        <view class="date-line">
          <text class="date">{{ item.classDate }}</text>
          <text class="accent time">{{ item.timeText }}</text>
        </view>
        <text class="muted">姓名：{{ item.name }}</text>
        <text v-if="item.rejectReason" class="muted reason">备注：{{ item.rejectReason }}</text>
        <view
          v-if="item.status === 'pending' || item.status === 'approved'"
          class="cancel-btn"
          @click="cancelItem(item)"
        >
          取消预约
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  cancelPracticeRoomBooking,
  createPracticeRoomBooking,
  getPracticeRoomBookings,
  getPracticeRoomSlots,
  getPracticeRooms,
} from '@/common/api.js'
import { ensureLogin, getUser } from '@/common/auth.js'
import { selectedCampusId } from '@/common/campus.js'
import { showError, showSuccess, showToast } from '@/common/toast.js'

const classrooms = ref([])
const classroomIndex = ref(0)
const classDate = ref(today())
const minDate = today()
const slots = ref([])
const slotIndex = ref(0)
const name = ref('')
const bookings = ref([])
const submitting = ref(false)
const loadingSlots = ref(false)

const selectedClassroom = computed(() => classrooms.value[classroomIndex.value] || null)
const classroomNames = computed(() => classrooms.value.map((item) => item.name))
const slotOptions = computed(() => slots.value.filter((item) => item.available !== false))
const selectedSlot = computed(() => slotOptions.value[slotIndex.value] || null)
const slotLabels = computed(() =>
  slotOptions.value.map((item) => {
    const count = Number(item.practiceCount || 0)
    return count > 0 ? `${item.label}（已约 ${count} 人）` : item.label
  }),
)
const slotPlaceholder = computed(() => {
  if (!selectedClassroom.value) return '请先选择教室'
  if (loadingSlots.value) return '加载时段中...'
  if (!slots.value.length) return '暂无可约时段'
  if (!slotOptions.value.length) return '当日时段均已占用'
  return selectedSlot.value?.label || '请选择时段'
})

function today() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

async function loadClassrooms() {
  try {
    classrooms.value = (await getPracticeRooms(selectedCampusId.value)) || []
    if (classroomIndex.value >= classrooms.value.length) {
      classroomIndex.value = 0
    }
  } catch (e) {
    classrooms.value = []
    showError(e.message || '加载教室失败')
  }
}

async function loadSlots() {
  const classroom = selectedClassroom.value
  if (!classroom?.id || !classDate.value) {
    slots.value = []
    slotIndex.value = 0
    return
  }
  loadingSlots.value = true
  try {
    slots.value = (await getPracticeRoomSlots(classroom.id, classDate.value)) || []
    slotIndex.value = 0
  } catch (e) {
    slots.value = []
    showError(e.message || '加载时段失败')
  } finally {
    loadingSlots.value = false
  }
}

async function loadBookings() {
  try {
    bookings.value = (await getPracticeRoomBookings()) || []
  } catch (e) {
    bookings.value = []
  }
}

function onClassroomChange(e) {
  classroomIndex.value = Number(e.detail.value || 0)
}

function onDateChange(e) {
  classDate.value = e.detail.value
}

function onSlotChange(e) {
  slotIndex.value = Number(e.detail.value || 0)
}

async function submit() {
  if (!ensureLogin()) return
  if (!name.value.trim()) {
    showToast('请填写姓名')
    return
  }
  if (!selectedClassroom.value?.id) {
    showToast('请选择教室')
    return
  }
  if (!selectedSlot.value?.id) {
    showToast('请选择可约时段')
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await createPracticeRoomBooking({
      name: name.value.trim(),
      classroomId: selectedClassroom.value.id,
      slotId: selectedSlot.value.id,
      classDate: classDate.value,
    })
    showSuccess('已提交，等待审核')
    await Promise.all([loadBookings(), loadSlots()])
  } catch (e) {
    showError(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

async function cancelItem(item) {
  try {
    await cancelPracticeRoomBooking(item.id)
    showToast('已取消')
    await Promise.all([loadBookings(), loadSlots()])
  } catch (e) {
    showError(e.message || '取消失败')
  }
}

watch([selectedClassroom, classDate], () => {
  loadSlots()
})

onShow(async () => {
  if (!ensureLogin()) return
  const user = getUser()
  if (!name.value && user?.nickname) {
    name.value = user.nickname
  }
  await loadClassrooms()
  await loadSlots()
  await loadBookings()
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

.text-input,
.picker-value {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background: #242424;
  color: #ffffff;
  font-size: 28rpx;
  box-sizing: border-box;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.placeholder {
  color: #6a6a6a;
}

.arrow {
  color: #6a6a6a;
}

.submit {
  margin-top: 16rpx;
  height: 88rpx;
  width: 100%;
}

.submit.disabled {
  opacity: 0.6;
}

.item {
  margin-bottom: 20rpx;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
}

.status {
  font-size: 24rpx;
  color: #8a74e5;
}

.date-line {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.date {
  font-size: 28rpx;
  font-weight: 600;
}

.time {
  font-size: 28rpx;
}

.muted {
  font-size: 26rpx;
}

.reason {
  display: block;
  margin-top: 8rpx;
}

.empty {
  text-align: center;
  padding: 40rpx 0 80rpx;
  font-size: 28rpx;
}

.cancel-btn {
  margin-top: 24rpx;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  border-radius: 999rpx;
  font-size: 26rpx;
  color: #e57373;
  background: rgba(229, 115, 115, 0.12);
  border: 1rpx solid rgba(229, 115, 115, 0.35);
}
</style>
