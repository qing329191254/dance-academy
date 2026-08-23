<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="brand-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="brand-inner">
        <text class="brand-title">高校FOR一GET街舞俱乐部</text>
      </view>
    </view>

    <view class="tabs">
      <view
        v-for="tab in bookTabs"
        :key="tab.key"
        class="tab"
        :class="{ active: active === tab.key }"
        @click="active = tab.key"
      >
        {{ tab.name }}
      </view>
    </view>

    <scroll-view
      v-if="active === 'group'"
      class="date-scroll"
      scroll-x
      :show-scrollbar="false"
      :scroll-into-view="dateScrollId"
    >
      <view class="date-row">
        <view
          v-for="(day, index) in weekDates"
          :id="'date-' + index"
          :key="day.date"
          class="date-item"
          :class="{ active: selectedDateIndex === index }"
          @click="selectedDateIndex = index"
        >
          <text class="date-label">{{ day.label }}</text>
          <text class="date-day">{{ day.day }}</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="active === 'fixed'" class="hint muted">固定周期开班，报名后按固定时段上课</view>
    <view v-if="active === 'private'" class="hint muted">私教需与老师协商后预约具体日期时间</view>

    <view class="section">
      <view v-if="!currentList.length" class="empty muted">当日暂无团课安排</view>
      <view v-for="item in currentList" :key="item.id" class="card class-card">
        <image v-if="getTeacherAvatar(item.teacher)" class="avatar" :src="getTeacherAvatar(item.teacher)" mode="aspectFill" />
        <view v-else class="avatar" />
        <view class="info">
          <text class="name">{{ item.name }}</text>
          <view class="meta">
            <text v-if="active === 'group'" class="date-text">{{ selectedDateText }}</text>
            <text class="accent">{{ item.time }}</text>
            <text class="muted"> · {{ item.teacher }}</text>
          </view>
          <text class="muted room">教室：{{ item.room }}</text>
          <view class="stars">
            <text v-for="n in 5" :key="n" :class="n <= item.stars ? 'on' : 'off'">★</text>
          </view>
        </view>
        <view class="side">
          <text class="tag">{{ item.status }}</text>
          <view
            class="action"
            :class="isBooked(item) ? 'btn-cancel' : 'btn-ghost'"
            @click="toggleBook(item)"
          >
            {{ isBooked(item) ? '取消预约' : '预约' }}
          </view>
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getSchedules, getTeachers, toggleBooking } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { showSuccess, showToast, showError } from '@/common/toast.js'
import { getStatusBarHeight } from '@/common/statusBar.js'
import { BOOKING_TMPL_ID } from '@/common/config.js'

const statusBarHeight = getStatusBarHeight()

const TOAST_OFFSET = 'calc(env(safe-area-inset-top) + 180rpx)'

const bookTabs = [
  { key: 'group', name: '团课' },
  { key: 'fixed', name: '固定班' },
  { key: 'private', name: '私教课' },
]

const active = ref('group')
const weekDates = ref([])
const selectedDateIndex = ref(0)
const dateScrollId = ref('date-0')
const currentList = ref([])
const teacherAvatars = ref({})

const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

function buildWeekDates() {
  const days = []
  const today = new Date()
  for (let i = 0; i < 7; i++) {
    const d = new Date(today)
    d.setDate(today.getDate() + i)
    days.push({
      date: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`,
      label: i === 0 ? '今天' : weekdays[d.getDay()],
      day: `${d.getMonth() + 1}/${d.getDate()}`,
    })
  }
  return days
}

function getTeacherAvatar(name) {
  return teacherAvatars.value[name] || ''
}

onLoad(async () => {
  weekDates.value = buildWeekDates()
  try {
    const teachers = await getTeachers()
    const map = {}
    teachers.forEach((t) => {
      map[t.name] = t.avatar
    })
    teacherAvatars.value = map
  } catch (e) {}
  await loadSchedules()
})

onShow(() => {
  if (weekDates.value.length) loadSchedules()
})

watch([active, selectedDateIndex], () => {
  loadSchedules()
})

const selectedDateText = computed(() => {
  const day = weekDates.value[selectedDateIndex.value]
  return day ? day.date.replace(/-/g, '.') : ''
})

async function loadSchedules() {
  const date =
    active.value === 'group' ? weekDates.value[selectedDateIndex.value]?.date : undefined
  try {
    currentList.value = (await getSchedules(active.value, date)) || []
  } catch (e) {
    currentList.value = []
  }
}

function isBooked(item) {
  return !!item.booked
}

function askBookingSubscribe() {
  return new Promise((resolve) => {
    // #ifdef MP-WEIXIN
    uni.requestSubscribeMessage({
      tmplIds: [BOOKING_TMPL_ID],
      complete() {
        resolve()
      },
    })
    // #endif
    // #ifndef MP-WEIXIN
    resolve()
    // #endif
  })
}

async function toggleBook(item) {
  if (!ensureLogin()) return
  const toastOptions = { offsetTop: TOAST_OFFSET }
  const date =
    active.value === 'group' ? weekDates.value[selectedDateIndex.value]?.date : undefined
  const booking = isBooked(item)
  try {
    if (!booking) {
      await askBookingSubscribe()
    }
    const result = await toggleBooking(item.id, date)
    await loadSchedules()
    if (result.booked) {
      const dateTip = active.value === 'group' ? `${selectedDateText.value} ` : ''
      showSuccess(`已预约 ${dateTip}${item.time} ${item.name}`, toastOptions)
    } else {
      showToast(result.message || '已取消预约', toastOptions)
    }
  } catch (e) {
    showError(e.message || '预约失败', toastOptions)
  }
}
</script>

<style scoped>
.brand-header {
  background: #111111;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.35);
}

.brand-inner {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 100rpx;
}

.brand-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #ffffff;
  text-align: center;
  line-height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tabs {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx 8rpx;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 999rpx;
  background: #1c1c1c;
  color: #bdbdbd;
  font-size: 28rpx;
}

.tab.active {
  background: #8a74e5;
  color: #fff;
}

.date-scroll {
  width: 100%;
  white-space: nowrap;
  padding: 16rpx 0 8rpx;
}

.date-row {
  display: inline-flex;
  gap: 16rpx;
  padding: 0 32rpx;
}

.date-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 96rpx;
  padding: 16rpx 20rpx;
  border-radius: 20rpx;
  background: #1c1c1c;
  color: #bdbdbd;
}

.date-item.active {
  background: rgba(138, 116, 229, 0.25);
  color: #fff;
  border: 1rpx solid #8a74e5;
}

.date-label {
  font-size: 22rpx;
  margin-bottom: 6rpx;
}

.date-day {
  font-size: 28rpx;
  font-weight: 600;
}

.hint {
  padding: 8rpx 32rpx 0;
  font-size: 24rpx;
}

.empty {
  text-align: center;
  padding: 80rpx 0;
  font-size: 28rpx;
}

.class-card {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #2a2a2a;
  flex-shrink: 0;
  margin-right: 20rpx;
}

.info {
  flex: 1;
  min-width: 0;
  padding-right: 16rpx;
}

.name {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 10rpx;
}

.side {
  width: 136rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.side .tag {
  margin-bottom: 16rpx;
}

.meta {
  margin-bottom: 8rpx;
  font-size: 26rpx;
}

.date-text {
  color: #8a74e5;
  margin-right: 8rpx;
}

.room {
  font-size: 24rpx;
  display: block;
  margin-bottom: 10rpx;
}

.stars {
  letter-spacing: 4rpx;
  font-size: 24rpx;
}

.on {
  color: #8a74e5;
}

.off {
  color: #3a3a3a;
}

.action {
  width: 136rpx;
  height: 56rpx;
  padding: 0 8rpx;
  font-size: 22rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  white-space: nowrap;
}

.btn-cancel {
  background: rgba(229, 115, 115, 0.15);
  color: #e57373;
  border: 1rpx solid rgba(229, 115, 115, 0.35);
}
</style>
