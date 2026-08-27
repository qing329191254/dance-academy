<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <text class="lead muted">{{ workLead }}</text>
    </view>
    <view class="section">
      <view
        v-for="item in workTracks"
        :key="item.key"
        class="card track"
        @click="go(item.key)"
      >
        <view class="track-head">
          <text class="name">{{ item.name }}</text>
          <view class="badge">{{ item.level }}</view>
        </view>
        <text class="muted desc">{{ item.desc }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getGrowthContent } from '@/common/api.js'
import { workTracks as mockTracks } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'
import { selectedCampusId } from '@/common/campus.js'

const workLead = ref('勤工俭学成长线：从校园兼职到实习，再到管理角色。点击进入可查看近期机会并报名。')
const workTracks = ref([...mockTracks])

async function loadContent() {
  try {
    const data = await getGrowthContent(selectedCampusId.value)
    if (data.workLead) workLead.value = data.workLead
    if (data.workTracks?.length) workTracks.value = data.workTracks
  } catch (e) {
    // 保留 mock 兜底
  }
}

onMounted(loadContent)
onShow(loadContent)
watch(selectedCampusId, loadContent)

function go(key) {
  openPage(`/pages/growth/track?key=${key}`)
}
</script>

<style scoped>
.lead {
  font-size: 28rpx;
  line-height: 1.7;
  display: block;
}

.track {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.track-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
}

.desc {
  display: block;
  line-height: 1.6;
  white-space: pre-line;
  word-break: keep-all;
}

.badge {
  flex-shrink: 0;
  min-width: 72rpx;
  text-align: center;
  padding: 10rpx 14rpx;
  border-radius: 16rpx;
  background: rgba(138, 116, 229, 0.18);
  color: #8a74e5;
  font-weight: 700;
  font-size: 26rpx;
}
</style>
