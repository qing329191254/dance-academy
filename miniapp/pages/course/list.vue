<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="module card trial" @click="goTrial">
        <view class="module-top">
          <view>
            <text class="module-name">{{ trialCourse.name }}</text>
            <text class="muted summary">{{ trialCourse.summary }}</text>
          </view>
          <view v-if="trialCourse.price" class="price-box">
            <text class="price-mark">¥</text>
            <text class="price">{{ trialCourse.price }}</text>
            <text class="price-unit">/ {{ trialCourse.unit }}</text>
          </view>
        </view>
        <text v-if="trialCourse.tag" class="tag">{{ trialCourse.tag }}</text>
      </view>

      <view class="module card system" @click="go('/pages/course/system')">
        <view class="module-top">
          <text class="module-name">课程产品</text>
          <view class="module-arrow">
            <text>进入</text>
            <view class="link-arrow" />
          </view>
        </view>
        <text class="muted summary">{{ systemHomeSummary }}</text>
        <view class="chips">
          <text v-for="item in courseSystem" :key="item.id || item.key" class="chip">{{ item.name }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getCourseIntro } from '@/common/api.js'
import { trialCourse as mockTrial, courseSystem as mockSystem } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

const trialCourse = reactive({ ...mockTrial })
const courseSystem = ref([...mockSystem])
const systemHomeSummary = ref('特色固定班、次卡、通卡\n私教、定制课、商演赛事')

function applyIntro(intro) {
  if (intro?.trial) Object.assign(trialCourse, intro.trial)
  if (intro?.systemModules?.length) courseSystem.value = intro.systemModules
  if (intro?.systemHomeSummary) systemHomeSummary.value = intro.systemHomeSummary
}

onMounted(async () => {
  try {
    applyIntro(await getCourseIntro())
  } catch (e) {
    // 保留 mock 兜底
  }
})

function go(url) {
  openPage(url)
}

function goTrial() {
  const query = trialCourse.id ? `?id=${trialCourse.id}` : ''
  openPage(`/pages/course/trial${query}`)
}
</script>

<style scoped>
.module {
  margin-bottom: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.trial {
  background: linear-gradient(145deg, #1c1c1c, #2a2038);
}

.system {
  background: linear-gradient(145deg, #1c1c1c, #242038);
}

.module-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20rpx;
}

.module-name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
}

.summary {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  line-height: 1.6;
  white-space: pre-line;
}

.price-box {
  display: flex;
  align-items: flex-end;
  flex-shrink: 0;
  color: #8a74e5;
  padding-top: 4rpx;
}

.price-mark {
  font-size: 26rpx;
  font-weight: 700;
  margin-bottom: 6rpx;
}

.price {
  font-size: 56rpx;
  font-weight: 700;
  line-height: 1;
}

.price-unit {
  font-size: 22rpx;
  color: #9a9a9a;
  margin: 0 0 6rpx 6rpx;
}

.module-arrow {
  display: inline-flex;
  align-items: center;
  color: #8a74e5;
  font-size: 26rpx;
  flex-shrink: 0;
}

.chips {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.chip {
  font-size: 22rpx;
  color: #d7d0ff;
  background: rgba(138, 116, 229, 0.18);
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
}
</style>
