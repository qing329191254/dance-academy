<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view v-if="!list.length" class="card"><text class="muted">暂无工作成绩记录</text></view>
      <view v-for="item in list" :key="item.id" class="card item">
        <text class="label">{{ item.periodLabel || '工作成绩' }}</text>
        <text class="content">{{ item.content }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getEmployeePerformance } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'

const list = ref([])

onShow(async () => {
  if (!ensureLogin()) return
  list.value = await getEmployeePerformance()
})
</script>

<style scoped>
.item { margin-bottom: 20rpx; }
.label { display: block; font-size: 30rpx; font-weight: 600; margin-bottom: 12rpx; color: #8a74e5; }
.content { display: block; font-size: 28rpx; line-height: 1.7; white-space: pre-wrap; }
.muted { font-size: 28rpx; }
</style>
