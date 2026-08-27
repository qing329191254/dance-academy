<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <text class="lead muted">{{ systemLead }}</text>
    </view>
    <view class="section">
      <view
        v-for="item in courseSystem"
        :key="item.id || item.key"
        class="card track"
        @click="go(item)"
      >
        <view class="left">
          <text class="name">{{ item.name }}</text>
          <text class="muted">{{ item.summary }}</text>
        </view>
        <view class="link-arrow" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getCourseIntro } from '@/common/api.js'
import { courseSystem as mockSystem } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

const courseSystem = ref([...mockSystem])
const systemLead = ref('按学习方式和目标选择：固定班、次通卡、私教，或定制赛事与商演。')

onMounted(async () => {
  try {
    const intro = await getCourseIntro()
    if (intro.systemModules?.length) courseSystem.value = intro.systemModules
    if (intro.systemLead) systemLead.value = intro.systemLead
  } catch (e) {
    // 保留 mock 兜底
  }
})

function go(item) {
  const query = item.id ? `id=${item.id}` : `key=${item.key || ''}`
  openPage(`/pages/course/system-detail?${query}`)
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
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 10rpx;
}

.link-arrow {
  color: #8a74e5;
}
</style>
