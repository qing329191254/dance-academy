<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="brand-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="brand-inner">
        <app-campus-switch />
        <text class="brand-title">高校FOR-GET舞室</text>
      </view>
    </view>

    <view class="section">
      <view class="intro card">
        <text class="title">成长中心</text>
        <text class="muted body">{{ growthIntro }}</text>
        <text class="tip">{{ growthLevelTip }}</text>
      </view>
    </view>

    <view class="section">
      <view class="card level-card">
        <text class="level-title">我的等级</text>
        <view class="level-row">
          <view v-for="track in growthTracks" :key="track.key" class="level-item">
            <text class="level-line">{{ track.line }}</text>
            <view class="level-meta">
              <text class="level-stage">{{ track.current }}</text>
              <text class="level-pill">{{ track.level }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="module card work" @click="go('/pages/growth/work')">
        <view class="module-top">
          <text class="module-name">勤工俭学</text>
          <view class="module-arrow">
            <text>进入</text>
            <view class="link-arrow" />
          </view>
        </view>
        <text class="muted">{{ workModuleSummary }}</text>
        <view class="chips">
          <text v-for="item in workTracks" :key="item.key" class="chip">{{ item.name }} {{ item.level }}</text>
        </view>
      </view>

      <view class="module card dance" @click="go('/pages/growth/dance')">
        <view class="module-top">
          <text class="module-name">舞蹈发展</text>
          <view class="module-arrow">
            <text>进入</text>
            <view class="link-arrow" />
          </view>
        </view>
        <text class="muted">{{ danceModuleSummary }}</text>
        <view class="chips">
          <text v-for="item in danceTracks" :key="item.key" class="chip">{{ item.name }} {{ item.level }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getGrowthContent, getMine } from '@/common/api.js'
import { growthIntro as mockIntro, userGrowthProfile, workTracks as mockWorkTracks, danceTracks as mockDanceTracks } from '@/common/mock.js'
import { isLoggedIn, isProfileComplete } from '@/common/auth.js'
import { openPage } from '@/common/navigate.js'
import { getStatusBarHeight } from '@/common/statusBar.js'
import { selectedCampusId } from '@/common/campus.js'

const statusBarHeight = getStatusBarHeight()
const growthIntro = ref(mockIntro)
const growthLevelTip = ref('新学员默认享有 T1 权益，可通过年限、考核等途径升级至 T2 / T3。')
const workModuleSummary = ref('兼职 → 实习 → 管理（T1-T3）')
const danceModuleSummary = ref('演出 → 商演 → 教师（T1-T3）')
const workTracks = ref([...mockWorkTracks])
const danceTracks = ref([...mockDanceTracks])
const growthTracks = ref([
  { key: 'work', ...userGrowthProfile.work },
  { key: 'dance', ...userGrowthProfile.dance },
])

async function loadContent() {
  try {
    const data = await getGrowthContent(selectedCampusId.value)
    if (data.intro) growthIntro.value = data.intro
    if (data.levelTip) growthLevelTip.value = data.levelTip
    if (data.workModuleSummary) workModuleSummary.value = data.workModuleSummary
    if (data.danceModuleSummary) danceModuleSummary.value = data.danceModuleSummary
    if (data.workTracks?.length) workTracks.value = data.workTracks
    if (data.danceTracks?.length) danceTracks.value = data.danceTracks
  } catch (e) {
    // 保留 mock 兜底
  }
}

async function loadMine() {
  if (!isLoggedIn() || !isProfileComplete()) return
  try {
    const data = await getMine(selectedCampusId.value)
    if (data.growth) {
      growthTracks.value = [
        { key: 'work', ...data.growth.work },
        { key: 'dance', ...data.growth.dance },
      ]
    }
  } catch (e) {}
}

onShow(async () => {
  await Promise.all([loadContent(), loadMine()])
})

watch(selectedCampusId, () => {
  loadContent()
  loadMine()
})

function go(url) {
  openPage(url)
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
  left: 50%;
  transform: translateX(-50%);
  font-size: 34rpx;
  font-weight: 600;
  white-space: nowrap;
}

.intro {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
}

.body {
  font-size: 28rpx;
  line-height: 1.7;
}

.tip {
  font-size: 24rpx;
  color: #8a74e5;
  line-height: 1.6;
}

.level-card {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.level-title {
  font-size: 30rpx;
  font-weight: 600;
}

.level-row {
  display: flex;
  gap: 20rpx;
}

.level-item {
  flex: 1;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 16rpx;
  padding: 20rpx;
}

.level-line {
  display: block;
  font-size: 24rpx;
  color: #8a74e5;
  margin-bottom: 12rpx;
}

.level-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.level-stage {
  font-size: 28rpx;
  font-weight: 600;
}

.level-pill {
  font-size: 22rpx;
  color: #d7d0ff;
  background: rgba(138, 116, 229, 0.18);
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
}

.module {
  margin-bottom: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.work {
  background: linear-gradient(145deg, #1c1c1c, #2a2038);
}

.dance {
  background: linear-gradient(145deg, #1c1c1c, #242038);
}

.module-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-name {
  font-size: 36rpx;
  font-weight: 700;
}

.module-arrow {
  display: inline-flex;
  align-items: center;
  color: #8a74e5;
  font-size: 26rpx;
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
