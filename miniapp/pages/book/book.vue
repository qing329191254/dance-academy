<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="brand-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="brand-inner">
        <app-campus-switch />
        <text class="brand-title">高校FOR-GET舞室</text>
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
    <view v-if="active === 'rank'" class="hint muted">按到课签到次数统计 · {{ currentCampus.shortName }}</view>

    <view v-if="active === 'rank'" class="rank-periods">
      <view
        v-for="item in rankPeriods"
        :key="item.key"
        class="period"
        :class="{ active: rankPeriod === item.key }"
        @click="rankPeriod = item.key"
      >
        {{ item.name }}
      </view>
    </view>

    <view v-if="active === 'rank'" class="section">
      <view v-if="!rankList.length" class="empty muted">该校区暂无签到记录，扫码上课后即可上榜</view>
      <view
        v-for="item in rankList"
        :key="item.rank"
        class="card rank-card"
        :class="{ mine: item.mine }"
      >
        <text class="rank-no" :class="rankTone(item.rank)">{{ item.rank }}</text>
        <image v-if="item.avatar" class="avatar" :src="item.avatar" mode="aspectFill" />
        <view v-else class="avatar" />
        <view class="rank-info">
          <text class="name">{{ item.nickname }}</text>
          <text v-if="item.mine" class="muted">我</text>
        </view>
        <text class="rank-count">{{ item.count }} 次</text>
      </view>
      <view v-if="rankMine && rankMine.rank && !rankMine.onBoard" class="card rank-card mine">
        <text class="rank-no">{{ rankMine.rank }}</text>
        <view class="rank-info">
          <text class="name">我的排名</text>
          <text class="muted">未进入前 20</text>
        </view>
        <text class="rank-count">{{ rankMine.count }} 次</text>
      </view>
    </view>

    <view v-else class="section">
      <view v-if="!currentList.length" class="empty muted">{{ emptyText }}</view>
      <view v-for="item in currentList" :key="item.id" class="card class-card">
        <image v-if="getTeacherAvatar(item.teacher)" class="avatar" :src="getTeacherAvatar(item.teacher)" mode="aspectFill" />
        <view v-else class="avatar" />
        <view class="info">
          <view class="title-row">
            <text class="name">{{ item.name }}</text>
            <text v-if="item.closedDoor" class="closed-tag">{{ item.audienceGroupLabel || '闭门课' }}</text>
          </view>
          <text v-if="item.sectionName || item.styleName" class="section-tag">{{ item.styleName || item.sectionName }}</text>
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
            :class="actionClass(item)"
            @click="toggleBook(item)"
          >
            {{ actionLabel(item) }}
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
import { getSchedules, getTeachers, getLeaderboard, toggleBooking } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { showSuccess, showToast, showError } from '@/common/toast.js'
import { getStatusBarHeight } from '@/common/statusBar.js'
import { BOOKING_TMPL_ID } from '@/common/config.js'
import { selectedCampusId, currentCampus } from '@/common/campus.js'

const statusBarHeight = getStatusBarHeight()

const TOAST_OFFSET = 'calc(env(safe-area-inset-top) + 180rpx)'

const bookTabs = [
  { key: 'group', name: '团课' },
  { key: 'fixed', name: '固定班' },
  { key: 'private', name: '私教课' },
  { key: 'rank', name: '排行榜' },
]

const rankPeriods = [
  { key: 'month', name: '本月' },
  { key: 'all', name: '累计' },
]

const active = ref('group')
const weekDates = ref([])
const selectedDateIndex = ref(0)
const dateScrollId = ref('date-0')
const currentList = ref([])
const teacherAvatars = ref({})
const rankPeriod = ref('month')
const rankList = ref([])
const rankMine = ref(null)

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
  let pending = ''
  try {
    pending = uni.getStorageSync('pendingBookTab') || ''
  } catch (e) {}
  if (pending === 'group' || pending === 'fixed' || pending === 'private') {
    try {
      uni.removeStorageSync('pendingBookTab')
    } catch (e) {}
    if (active.value !== pending) {
      active.value = pending
      return
    }
  }
  if (weekDates.value.length) refreshBookPage()
})

watch([active, selectedDateIndex, selectedCampusId, rankPeriod], () => {
  refreshBookPage()
})

const selectedDateText = computed(() => {
  const day = weekDates.value[selectedDateIndex.value]
  return day ? day.date.replace(/-/g, '.') : ''
})

const emptyText = computed(() => {
  if (active.value === 'fixed') return '该校区暂无固定班'
  if (active.value === 'private') return '该校区暂无私教课'
  return '当日该校区暂无团课安排'
})

async function loadSchedules() {
  const date =
    active.value === 'group' ? weekDates.value[selectedDateIndex.value]?.date : undefined
  try {
    currentList.value = (await getSchedules(active.value, date, selectedCampusId.value)) || []
  } catch (e) {
    currentList.value = []
  }
}

function refreshBookPage() {
  if (active.value === 'rank') {
    loadLeaderboard()
    return
  }
  loadSchedules()
}

async function loadLeaderboard() {
  try {
    const data = await getLeaderboard(rankPeriod.value, selectedCampusId.value)
    rankList.value = data.list || []
    rankMine.value = data.mine || null
  } catch (e) {
    rankList.value = []
    rankMine.value = null
  }
}

function rankTone(rank) {
  if (rank === 1) return 'gold'
  if (rank === 2) return 'silver'
  if (rank === 3) return 'bronze'
  return ''
}

function isBooked(item) {
  return !!item.booked
}

function isQueued(item) {
  return !!item.queued
}

function actionLabel(item) {
  if (item.sessionCancelled || item.status === '已取消') return '已取消'
  if (isBooked(item)) return '取消预约'
  if (isQueued(item)) return '退出排队'
  if (active.value === 'group' && item.status === '已满') return '排队'
  if (active.value === 'group' && item.canBook === false) return '无可用卡'
  return '预约'
}

function actionClass(item) {
  if (item.sessionCancelled || item.status === '已取消') return 'btn-disabled'
  if (isBooked(item) || isQueued(item)) return 'btn-cancel'
  if (active.value === 'group' && item.status === '已满') return 'btn-queue'
  if (active.value === 'group' && item.canBook === false) return 'btn-disabled'
  return 'btn-ghost'
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
  if (item.sessionCancelled || item.status === '已取消') {
    showError(item.bookBlockReason || '本课因人数不足已取消', toastOptions)
    return
  }
  if (
    active.value === 'group'
    && !isBooked(item)
    && !isQueued(item)
    && item.status !== '已满'
    && item.canBook === false
  ) {
    showError(item.bookBlockReason || '没有可用的对应次卡', toastOptions)
    return
  }
  const date =
    active.value === 'group' ? weekDates.value[selectedDateIndex.value]?.date : undefined
  const booked = isBooked(item)
  const queued = isQueued(item)
  try {
    if (!booked && !queued && active.value === 'group' && item.status !== '已满') {
      await askBookingSubscribe()
    }
    const result = await toggleBooking(item.id, date)
    await loadSchedules()
    if (result.booked) {
      const dateTip = active.value === 'group' ? `${selectedDateText.value} ` : ''
      showSuccess(`已预约 ${dateTip}${item.time} ${item.name}`, toastOptions)
    } else if (result.queued) {
      const queueTip = result.queueNo ? `，当前第 ${result.queueNo} 位` : ''
      showSuccess((result.message || '已加入排队') + queueTip, toastOptions)
    } else {
      showToast(result.message || (queued ? '已退出排队' : '已取消预约'), toastOptions)
    }
  } catch (e) {
    showError(e.message || '操作失败', toastOptions)
  }
}
</script>

<style scoped>
.brand-header {
  background: #111111;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.35);
  overflow: visible;
}

.brand-inner {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0 96px 0 16rpx;
  position: relative;
}

.brand-title {
  position: absolute;
  left: 0;
  right: 0;
  font-size: 32rpx;
  font-weight: 700;
  color: #ffffff;
  text-align: center;
  line-height: 44px;
  padding: 0 200rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  pointer-events: none;
}

.tabs {
  display: flex;
  gap: 10rpx;
  padding: 24rpx 24rpx 8rpx;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: 999rpx;
  background: #1c1c1c;
  color: #bdbdbd;
  font-size: 24rpx;
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

.title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-bottom: 10rpx;
}

.title-row .name {
  margin-bottom: 0;
}

.closed-tag {
  display: inline-block;
  margin-left: 0;
  padding: 2rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(138, 116, 229, 0.18);
  color: #8a74e5;
  font-size: 22rpx;
  font-weight: 500;
  vertical-align: middle;
}

.section-tag {
  display: inline-block;
  margin: 0 0 10rpx 0;
  padding: 2rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.08);
  color: #cfcfcf;
  font-size: 22rpx;
  font-weight: 500;
}

.btn-disabled {
  background: rgba(255, 255, 255, 0.06);
  color: #777;
  border: 1rpx solid rgba(255, 255, 255, 0.12);
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

.btn-queue {
  background: rgba(232, 195, 106, 0.12);
  color: #e8c36a;
  border: 1rpx solid rgba(232, 195, 106, 0.35);
}

.rank-periods {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 32rpx 0;
}

.period {
  padding: 10rpx 28rpx;
  border-radius: 999rpx;
  background: #1c1c1c;
  color: #bdbdbd;
  font-size: 24rpx;
}

.period.active {
  background: rgba(138, 116, 229, 0.22);
  color: #fff;
  border: 1rpx solid #8a74e5;
}

.rank-card {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.rank-card.mine {
  border: 1rpx solid rgba(138, 116, 229, 0.45);
}

.rank-no {
  width: 56rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 700;
  color: #9a9a9a;
  flex-shrink: 0;
}

.rank-no.gold {
  color: #e8c36a;
}

.rank-no.silver {
  color: #c5c5c5;
}

.rank-no.bronze {
  color: #d08a5a;
}

.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-info .muted {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
}

.rank-count {
  color: #8a74e5;
  font-size: 28rpx;
  font-weight: 700;
  flex-shrink: 0;
}
</style>
