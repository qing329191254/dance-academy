<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view v-if="!list.length" class="empty muted">暂无问卷</view>
      <view
        v-for="item in list"
        :key="item.id"
        class="card item"
        @click="openSurvey(item)"
      >
        <view class="head">
          <text class="name">{{ item.title }}</text>
          <text class="status" :class="{ done: item.submitted }">{{ item.submitted ? '已填写' : '去填写' }}</text>
        </view>
        <text v-if="item.description" class="muted desc">{{ item.description }}</text>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getSurveys } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { selectedCampusId } from '@/common/campus.js'
import { openPage } from '@/common/navigate.js'
import { showError } from '@/common/toast.js'

const list = ref([])

async function load() {
  try {
    list.value = (await getSurveys(selectedCampusId.value)) || []
  } catch (e) {
    list.value = []
    showError(e.message || '加载失败')
  }
}

function openSurvey(item) {
  openPage(`/pages/mine/survey-detail?id=${item.id}`)
}

onShow(() => {
  if (!ensureLogin()) return
  load()
})
</script>

<style scoped>
.item {
  margin-bottom: 20rpx;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.name {
  flex: 1;
  font-size: 32rpx;
  font-weight: 600;
}

.status {
  font-size: 24rpx;
  color: #8a74e5;
}

.status.done {
  color: #6a6a6a;
}

.desc {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  line-height: 1.5;
}

.empty {
  text-align: center;
  padding: 80rpx 0;
  font-size: 28rpx;
}
</style>
