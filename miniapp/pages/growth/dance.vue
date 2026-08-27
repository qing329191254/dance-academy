<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <text class="lead muted">{{ danceLead }}</text>
    </view>
    <view class="section">
      <view
        v-for="item in danceTracks"
        :key="item.key"
        class="card track"
        @click="go(item.key)"
      >
        <view class="left">
          <text class="name">{{ item.name }}</text>
          <text class="muted desc">{{ item.desc }}</text>
        </view>
        <view class="badge">{{ item.level }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getGrowthContent } from '@/common/api.js'
import { danceTracks as mockTracks } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'
import { selectedCampusId } from '@/common/campus.js'

const danceLead = ref('舞蹈发展成长线：演出练胆 → 商演实践 → 教师考证与任教。点击进入可查看近期机会并报名。')
const danceTracks = ref([...mockTracks])

async function loadContent() {
  try {
    const data = await getGrowthContent(selectedCampusId.value)
    if (data.danceLead) danceLead.value = data.danceLead
    if (data.danceTracks?.length) danceTracks.value = data.danceTracks
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
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.left {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  flex: 1;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
}

.desc {
  white-space: pre-line;
}

.badge {
  min-width: 80rpx;
  text-align: center;
  padding: 12rpx 18rpx;
  border-radius: 16rpx;
  background: rgba(138, 116, 229, 0.18);
  color: #8a74e5;
  font-weight: 700;
}
</style>
