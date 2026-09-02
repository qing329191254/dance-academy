<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page" v-if="ready">
    <view class="banner">
      <text v-if="trialCourse.tag" class="tag">{{ trialCourse.tag }}</text>
      <text class="banner-name">{{ trialCourse.name }}</text>
      <view v-if="trialCourse.price" class="price-row">
        <text class="price-mark">¥</text>
        <text class="price">{{ trialCourse.price }}</text>
        <text class="price-unit">/ {{ trialCourse.unit }}</text>
      </view>
    </view>

    <view class="section">
      <view class="card">
        <text class="label">课程介绍</text>
        <text class="body muted">{{ trialCourse.desc }}</text>
      </view>
    </view>

    <view v-if="trialCourse.highlights?.length" class="section">
      <view class="card">
        <text class="label">包含内容</text>
        <text v-for="item in trialCourse.highlights" :key="item" class="point muted">· {{ item }}</text>
      </view>
    </view>

    <view class="section">
      <view class="actions">
        <view class="btn-ghost" @click="callStudio">联系顾问</view>
        <view class="btn-primary" @click="goBook">去约课</view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app'
import { reactive, ref } from 'vue'
import { getCourseIntro, getCourseModule } from '@/common/api.js'
import { trialCourse as mockTrial } from '@/common/mock.js'
import { getLegalInfo, loadLegalInfo } from '@/common/legal.js'
import { selectedCampusId } from '@/common/campus.js'
import { openBookTab } from '@/common/navigate.js'
import { showToast } from '@/common/toast.js'

const trialCourse = reactive({ ...mockTrial })
const ready = ref(false)

function applyTrial(data) {
  if (!data) return
  Object.assign(trialCourse, data)
  if (data.name) uni.setNavigationBarTitle({ title: data.name })
}

onLoad(async (query) => {
  try {
    if (query?.id) {
      applyTrial(await getCourseModule(query.id))
    } else {
      const intro = await getCourseIntro()
      applyTrial(intro.trial)
    }
    await loadLegalInfo(selectedCampusId.value)
  } catch (e) {
    applyTrial(mockTrial)
  } finally {
    ready.value = true
  }
})

function callStudio() {
  const phone = getLegalInfo().phone
  uni.makePhoneCall({
    phoneNumber: phone,
    fail() {
      showToast('暂无法拨打电话')
    },
  })
}

function goBook() {
  openBookTab('group')
}
</script>

<style scoped>
.banner {
  margin: 24rpx 32rpx 0;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #1c1c1c, #2f2750);
  padding: 40rpx 36rpx 36rpx;
}

.banner-name {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  margin: 16rpx 0 20rpx;
}

.price-row {
  display: flex;
  align-items: flex-end;
  color: #8a74e5;
}

.price-mark {
  font-size: 28rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
}

.price {
  font-size: 64rpx;
  font-weight: 700;
  line-height: 1;
}

.price-unit {
  font-size: 24rpx;
  color: #9a9a9a;
  margin: 0 0 8rpx 8rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.body {
  font-size: 28rpx;
  line-height: 1.7;
}

.point {
  display: block;
  font-size: 28rpx;
  line-height: 1.8;
}

.actions {
  display: flex;
  gap: 20rpx;
}

.actions .btn-ghost,
.actions .btn-primary {
  flex: 1;
}
</style>
