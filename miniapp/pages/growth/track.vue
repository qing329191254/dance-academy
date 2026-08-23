<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="header card">
        <text class="line">{{ meta.line }}</text>
        <text class="title">{{ meta.name }} · {{ meta.level }}</text>
        <text class="muted">以下为近期机会，感兴趣可报名；机构可把报名链接发给学员。</text>
      </view>
    </view>

    <view class="section">
      <view v-if="!list.length" class="empty muted">暂无机会，敬请期待</view>
      <view
        v-for="item in list"
        :key="item.id"
        class="card opp"
        @click="open(item.id)"
      >
        <view class="top">
          <text class="name">{{ item.title }}</text>
          <text class="tag">{{ item.level }}</text>
        </view>
        <text class="muted summary">{{ item.summary }}</text>
        <view class="bottom">
          <text class="muted">截止 {{ item.deadline }} · 名额 {{ item.spots }}</text>
          <view class="accent link-with-arrow">
            <text>报名</text>
            <view class="link-arrow" />
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOpportunities } from '@/common/api.js'
import { trackMeta } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

const key = ref('parttime')
const list = ref([])
const meta = computed(() => trackMeta[key.value] || { line: '成长', name: '路径', level: 'T1' })

onLoad(async (query) => {
  key.value = query.key || 'parttime'
  uni.setNavigationBarTitle({
    title: `${meta.value.name}机会`,
  })
  try {
    list.value = (await getOpportunities(key.value)) || []
  } catch (e) {
    list.value = []
  }
})

function open(id) {
  openPage(`/pages/growth/opportunity?id=${id}&key=${key.value}`)
}
</script>

<style scoped>
.header {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.line {
  color: #8a74e5;
  font-size: 24rpx;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
}

.opp {
  margin-bottom: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.name {
  font-size: 30rpx;
  font-weight: 600;
  flex: 1;
}

.summary {
  font-size: 26rpx;
  line-height: 1.6;
}

.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
}

.link-with-arrow {
  display: inline-flex;
  align-items: center;
}

.empty {
  text-align: center;
  padding: 80rpx 0;
}
</style>
