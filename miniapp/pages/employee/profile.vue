<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page" v-if="profile">
    <view class="section">
      <view class="card">
        <text class="title">{{ profile.jobTitle || '岗位待配置' }}</text>
        <text class="muted">所属校区：{{ campusName(profile.campusId) }}</text>
        <text class="content">{{ profile.jobDescription || '请联系管理员在后台配置岗位职责' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getEmployeeProfile } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { CAMPUSES } from '@/common/campus.js'

const profile = ref(null)

function campusName(id) {
  return CAMPUSES.find((item) => item.id === id)?.name || id || '-'
}

async function load() {
  profile.value = await getEmployeeProfile()
}

onShow(() => {
  if (!ensureLogin()) return
  load()
})
</script>

<style scoped>
.title { display: block; font-size: 36rpx; font-weight: 700; margin-bottom: 12rpx; }
.muted { display: block; font-size: 26rpx; margin-bottom: 20rpx; }
.content { display: block; font-size: 28rpx; line-height: 1.7; white-space: pre-wrap; }
</style>
