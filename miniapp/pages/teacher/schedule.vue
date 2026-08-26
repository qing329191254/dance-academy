<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <scroll-view scroll-x class="dates" :show-scrollbar="false">
      <view
        v-for="(item, index) in weekDates"
        :key="item.date"
        class="date-item"
        :class="{ active: selectedDateIndex === index }"
        @tap="selectedDateIndex = index"
      >
        <text class="week">{{ item.week }}</text>
        <text class="day">{{ item.day }}</text>
      </view>
    </scroll-view>

    <view class="section">
      <view v-if="!list.length" class="empty muted">该日期暂无课程</view>
      <view v-for="item in list" :key="item.id" class="card item" @tap="openRoster(item)">
        <view class="head">
          <text class="name">{{ item.name }}</text>
          <text class="tag" :class="{ done: item.teacherChecked }">
            {{ item.teacherChecked ? '已签到' : '待签到' }}
          </text>
        </view>
        <text class="accent time">{{ item.time }}</text>
        <text class="muted">教室：{{ item.room }}</text>
        <text class="muted">预约 {{ item.bookedCount }} 人 · 到课 {{ item.checkedInCount }} 人</text>
        <text class="link">到课确认 ›</text>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getTeacherSchedules } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'

const weekDates = ref([])
const selectedDateIndex = ref(0)
const list = ref([])

const selectedDate = computed(() => weekDates.value[selectedDateIndex.value]?.date)

function buildWeekDates() {
  const weekNames = ['日', '一', '二', '三', '四', '五', '六']
  const result = []
  const base = new Date()
  for (let i = 0; i < 7; i += 1) {
    const d = new Date(base)
    d.setDate(base.getDate() + i)
    const pad = (n) => String(n).padStart(2, '0')
    result.push({
      date: `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`,
      week: i === 0 ? '今天' : `周${weekNames[d.getDay()]}`,
      day: `${pad(d.getMonth() + 1)}.${pad(d.getDate())}`,
    })
  }
  weekDates.value = result
}

async function loadList() {
  if (!selectedDate.value) return
  try {
    list.value = await getTeacherSchedules(selectedDate.value)
  } catch (e) {
    list.value = []
  }
}

function openRoster(item) {
  uni.navigateTo({
    url: `/pages/teacher/roster?scheduleId=${item.id}&date=${encodeURIComponent(item.date || selectedDate.value)}`,
  })
}

watch(selectedDate, () => {
  loadList()
})

onShow(() => {
  if (!ensureLogin()) return
  buildWeekDates()
  loadList()
})
</script>

<style scoped>
.dates {
  white-space: nowrap;
  padding: 24rpx 32rpx 8rpx;
}

.date-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 112rpx;
  height: 112rpx;
  margin-right: 16rpx;
  border-radius: 20rpx;
  background: #1c1c1c;
  color: #bdbdbd;
}

.date-item.active {
  background: rgba(138, 116, 229, 0.22);
  color: #ffffff;
  border: 1rpx solid rgba(138, 116, 229, 0.45);
}

.week {
  font-size: 22rpx;
}

.day {
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 600;
}

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
  color: #e8c36a;
}

.tag.done {
  color: #8a74e5;
}

.time {
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

.link {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #8a74e5;
}
</style>
