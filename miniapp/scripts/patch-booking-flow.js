const fs = require('fs')
const path = require('path')

const ROOT = path.join(__dirname, '..')

function write(rel, content) {
  fs.writeFileSync(path.join(ROOT, rel), content, 'utf8')
  console.log('wrote', rel)
}

write('pages/book/book.vue', fs.readFileSync(path.join(ROOT, 'pages/book/book.vue'), 'utf8')
  .replace(
    `function toggleBook(item) {
  const key = getItemKey(item)
  const toastOptions = { offsetTop: TOAST_OFFSET }

  if (isBooked(item)) {
    removeBooking(key)
    refreshBookings()
    showToast('已取消预约', toastOptions)
    return
  }

  addBooking(key)
  refreshBookings()
  const dateTip = active.value === 'group' ? \`\${selectedDateText.value} \` : ''
  showSuccess(\`已预约 \${dateTip}\${item.time} \${item.name}\`, toastOptions)
}`,
    `function toggleBook(item) {
  const key = getItemKey(item)
  const toastOptions = { offsetTop: TOAST_OFFSET }

  if (isBooked(item)) {
    removeBooking(key)
    refreshBookings()
    showToast('已取消预约', toastOptions)
    return
  }

  const date =
    active.value === 'group'
      ? weekDates.value[selectedDateIndex.value]?.date
      : ''

  addBooking({
    key,
    tab: active.value,
    courseId: item.id,
    name: item.name,
    date,
    dateText: active.value === 'group' ? selectedDateText.value : '',
    time: item.time,
    teacher: item.teacher,
    room: item.room,
    status: '待上课',
  })
  refreshBookings()
  const dateTip = active.value === 'group' ? \`\${selectedDateText.value} \` : ''
  showSuccess(\`已预约 \${dateTip}\${item.time} \${item.name}\`, toastOptions)
}`
  )
  .replace(
    `.side {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}`,
    `.side {
  width: 136rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}`
  )
  .replace(
    `.action {
  padding: 12rpx 28rpx;
  font-size: 24rpx;
  white-space: nowrap;
  border-radius: 999rpx;
}`,
    `.action {
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
}`
  ))

write('pages/mine/bookings.vue', `<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view v-if="!bookingList.length" class="empty muted">暂无已约课程</view>
      <view v-for="item in bookingList" :key="item.key" class="card item">
        <view class="head">
          <text class="name">{{ item.name }}</text>
          <text class="status">{{ item.status }}</text>
        </view>
        <view class="date-line">
          <text v-if="item.dateText" class="date">{{ item.dateText }}</text>
          <text class="accent time">{{ item.time }}</text>
        </view>
        <text class="muted">老师：{{ item.teacher }} · {{ item.room }}</text>
        <view class="cancel-btn" @click="cancelBooking(item)">取消预约</view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getBookings, removeBooking } from '@/common/booking.js'
import { ensureLogin } from '@/common/auth.js'
import { showToast } from '@/common/toast.js'

const bookingList = ref([])

function refreshBookings() {
  bookingList.value = getBookings()
}

onShow(() => {
  if (!ensureLogin()) return
  refreshBookings()
})

function cancelBooking(item) {
  removeBooking(item.key)
  refreshBookings()
  showToast('已取消预约')
}
</script>

<style scoped>
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

.empty {
  text-align: center;
  padding: 80rpx 0;
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
`)

write('pages/growth/opportunity.vue', `<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page" v-if="item">
    <view class="section">
      <view class="card">
        <text class="tag">{{ item.level }} · {{ meta.name }}</text>
        <text class="title">{{ item.title }}</text>
        <text class="muted summary">{{ item.summary }}</text>
        <view class="meta-row">
          <text>截止日期</text>
          <text>{{ item.deadline }}</text>
        </view>
        <view class="meta-row">
          <text>剩余名额</text>
          <text>{{ item.spots }}</text>
        </view>
        <view class="meta-row">
          <text>所需级别</text>
          <text>{{ item.level }}</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="card note">
        <text class="note-title">报名说明</text>
        <text class="muted">提交后进入机构后台名单，线上/线下筛选后通过小程序通知结果。成长中心不涉及收费支付。</text>
      </view>
    </view>

    <view class="actions">
      <view class="btn-ghost action-btn" @click="share">复制报名链接</view>
      <view
        class="action-btn apply-btn"
        :class="applied ? 'btn-cancel' : 'btn-primary'"
        @click="toggleApply"
      >
        {{ applied ? '取消报名' : '立即报名' }}
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { opportunities, trackMeta } from '@/common/mock.js'
import {
  addApply,
  buildApplyKey,
  isApplied,
  removeApply,
} from '@/common/opportunityApply.js'
import { showSuccess, showToast } from '@/common/toast.js'

const key = ref('parttime')
const id = ref('')
const applied = ref(false)
const list = computed(() => opportunities[key.value] || [])
const item = computed(() => list.value.find((o) => o.id === id.value))
const meta = computed(() => trackMeta[key.value] || { name: '成长' })

onLoad((query) => {
  key.value = query.key || 'parttime'
  id.value = query.id || ''
})

onShow(() => {
  applied.value = isApplied(buildApplyKey(key.value, id.value))
})

function toggleApply() {
  const applyKey = buildApplyKey(key.value, id.value)
  if (!item.value) return

  if (applied.value) {
    removeApply(applyKey)
    applied.value = false
    showToast('已取消报名')
    return
  }

  addApply({
    key: applyKey,
    trackKey: key.value,
    opportunityId: id.value,
    title: item.value.title,
    level: item.value.level,
    deadline: item.value.deadline,
    line: meta.value.line,
    trackName: meta.value.name,
  })
  applied.value = true
  showSuccess('报名成功')
}

function share() {
  uni.setClipboardData({
    data: \`/pages/growth/opportunity?key=\${key.value}&id=\${id.value}\`,
    success: () => {
      showToast('链接已复制')
    },
  })
}
</script>

<style scoped>
.tag {
  display: inline-flex;
  margin-bottom: 16rpx;
}

.title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.summary {
  display: block;
  font-size: 28rpx;
  line-height: 1.7;
  margin-bottom: 28rpx;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  padding: 18rpx 0;
  border-top: 1rpx solid #2a2a2e;
  font-size: 26rpx;
  color: #ddd;
}

.note {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.note-title {
  font-size: 28rpx;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 32rpx 48rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  border-radius: 999rpx;
  box-sizing: border-box;
  white-space: nowrap;
}

.apply-btn.btn-primary {
  padding: 0;
}

.btn-cancel {
  background: rgba(229, 115, 115, 0.15);
  color: #e57373;
  border: 1rpx solid rgba(229, 115, 115, 0.35);
}
</style>
`)

// patch mine.vue booking count
let mine = fs.readFileSync(path.join(ROOT, 'pages/mine/mine.vue'), 'utf8')
if (!mine.includes("from '@/common/booking.js'")) {
  mine = mine.replace(
    "import { myCards, myCourses, myBookings } from '@/common/mock.js'",
    "import { myCards, myCourses } from '@/common/mock.js'\nimport { getBookings } from '@/common/booking.js'"
  )
}
mine = mine.replace(
  '<text class="num">{{ myBookings.length }}</text>',
  '<text class="num">{{ bookingCount }}</text>'
)
if (!mine.includes('const bookingCount')) {
  mine = mine.replace(
    'const practiceCount = ref(0)',
    'const practiceCount = ref(0)\nconst bookingCount = ref(0)'
  )
  mine = mine.replace(
    'function refreshPracticeCount() {\n  practiceCount.value = getPracticeRecords().length\n}',
    `function refreshPracticeCount() {
  practiceCount.value = getPracticeRecords().length
}

function refreshBookingCount() {
  bookingCount.value = getBookings().length
}`
  )
  mine = mine.replace(
    'onShow(() => {\n  refreshUser()\n  refreshPracticeCount()\n})',
    'onShow(() => {\n  refreshUser()\n  refreshPracticeCount()\n  refreshBookingCount()\n})'
  )
}
write('pages/mine/mine.vue', mine)

console.log('patch done')
