<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page" v-if="item">
    <view class="banner">
      <text class="banner-name">{{ item.name }}</text>
      <text class="banner-sub">{{ item.summary }}</text>
    </view>
    <view class="section">
      <view class="card">
        <text class="label">课程介绍</text>
        <text class="body muted">{{ item.desc }}</text>
      </view>
    </view>
    <view v-if="item.highlights?.length" class="section">
      <view class="card">
        <text class="label">适合谁</text>
        <text v-for="point in item.highlights" :key="point" class="point muted">· {{ point }}</text>
      </view>
    </view>
    <view v-if="item.actionTab && item.actionLabel" class="section">
      <view class="btn-primary action" @click="onAction">{{ item.actionLabel }}</view>
    </view>
    <view v-else-if="item.customerServiceQr" class="section">
      <view class="card service-card">
        <text class="label">联系客服</text>
        <text class="service-tip muted">长按识别二维码，添加客服咨询课程</text>
        <image
          class="service-qr"
          :src="item.customerServiceQr"
          mode="aspectFit"
          :show-menu-by-longpress="true"
          @click="previewQr"
        />
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCourseIntro, getCourseModule } from '@/common/api.js'
import { courseSystem as mockSystem } from '@/common/mock.js'
import { openBookTab } from '@/common/navigate.js'

const item = ref(null)

async function loadModule(query) {
  const id = query?.id
  const key = query?.key || ''
  try {
    if (id) {
      item.value = await getCourseModule(id)
      return
    }
    const intro = await getCourseIntro()
    item.value = intro.systemModules.find((row) => row.key === key) || intro.systemModules[0] || null
    if (item.value) return
  } catch (e) {
    // fall through to mock
  }
  item.value = mockSystem.find((row) => row.key === key) || mockSystem[0]
}

onLoad(async (query) => {
  await loadModule(query)
  if (item.value?.name) {
    uni.setNavigationBarTitle({ title: item.value.name })
  }
})

function onAction() {
  if (item.value?.actionTab) {
    openBookTab(item.value.actionTab)
  }
}

function previewQr() {
  if (!item.value?.customerServiceQr) return
  uni.previewImage({
    current: item.value.customerServiceQr,
    urls: [item.value.customerServiceQr],
  })
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
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.banner-sub {
  color: #cfc7ff;
  font-size: 26rpx;
  line-height: 1.5;
}

.label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.body,
.point {
  font-size: 28rpx;
  line-height: 1.7;
}

.point {
  display: block;
}

.action {
  width: 100%;
}

.service-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx;
}

.service-card .label {
  align-self: flex-start;
}

.service-tip {
  align-self: flex-start;
  margin-bottom: 24rpx;
  font-size: 24rpx;
}

.service-qr {
  width: 460rpx;
  height: 560rpx;
  max-width: 100%;
  border-radius: 16rpx;
  background: #ffffff;
}
</style>
