<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <swiper
      v-if="brandPhotos.length"
      class="gallery"
      circular
      autoplay
      interval="4000"
      indicator-dots
      indicator-color="rgba(255,255,255,.35)"
      indicator-active-color="#ffffff"
    >
      <swiper-item v-for="(item, index) in brandPhotos" :key="index">
        <image
          class="gallery-img"
          :src="item"
          mode="aspectFill"
          @click.stop="previewPhoto(index)"
        />
      </swiper-item>
    </swiper>
    <view v-else class="gallery gallery-empty" />

    <view class="section studio-section">
      <view class="studio-block card">
        <view class="studio">
          <view class="studio-left">
            <image v-if="studio.logo" class="studio-logo" :src="studio.logo" mode="aspectFill" />
            <view v-else class="studio-logo logo-ph" />
            <view class="studio-info">
              <text class="studio-name">{{ studio.name }}</text>
              <text class="studio-hours muted">{{ studio.businessHours }}</text>
            </view>
          </view>
          <view class="phone-action" @click.stop="callStudio">
            <view class="phone-icon-wrap">
              <image class="phone-icon" src="/static/nav/phone.png" mode="aspectFit" />
            </view>
            <text class="phone-label">电话</text>
          </view>
        </view>
        <view class="location-row" @click="openLocation">
          <view class="location-left">
            <image class="location-icon" src="/static/nav/location.svg" mode="aspectFit" />
            <text class="location-text">{{ studio.city }}</text>
          </view>
          <view class="link-arrow" />
        </view>
      </view>
    </view>

    <view class="section">
      <view class="card">
        <text class="title">品牌介绍</text>
        <text class="muted body">
          {{ intro }}
        </text>
      </view>
    </view>
    <view class="section">
      <view class="card item">
        <text class="label">业务</text>
        <text>{{ business }}</text>
      </view>
      <view class="card item">
        <text class="label">理念</text>
        <text>{{ slogan }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getBrand } from '@/common/api.js'
import { brandPhotos as mockPhotos, studio as mockStudio } from '@/common/mock.js'
import { mediaUrl } from '@/common/config.js'
import { selectedCampusId } from '@/common/campus.js'

const studio = reactive({ ...mockStudio })
const brandPhotos = ref([...mockPhotos])
const intro = ref('深耕高校街舞文化的俱乐部品牌。课堂之外，用勤工俭学与舞蹈发展双线赋能大学生成长，增强机构黏性。')
const business = ref('团课 / 固定班 / 私教课 / 成长中心')
const slogan = ref('DANCE UP · BREAK FREE')

const photoPreviewUrls = ref([...brandPhotos.value])

async function loadBrand() {
  try {
    const data = await getBrand(selectedCampusId.value)
    if (data.studio) Object.assign(studio, data.studio)
    brandPhotos.value = (data.photos || []).map(mediaUrl).filter(Boolean)
    if (data.studio?.intro) intro.value = data.studio.intro
    if (data.studio?.business) business.value = data.studio.business
    if (data.studio?.slogan) slogan.value = data.studio.slogan
    photoPreviewUrls.value = [...brandPhotos.value]
  } catch (e) {}
}

onLoad(loadBrand)
onShow(loadBrand)
watch(selectedCampusId, loadBrand)

function callStudio() {
  uni.makePhoneCall({
    phoneNumber: studio.phone,
  })
}

function openLocation() {
  const lat = Number(studio.latitude)
  const lng = Number(studio.longitude)
  if (Number.isFinite(lat) && Number.isFinite(lng) && lat !== 0 && lng !== 0) {
    uni.openLocation({
      latitude: lat,
      longitude: lng,
      name: studio.name,
      address: studio.address,
      fail() {
        uni.showToast({ title: '地图打开失败', icon: 'none' })
      },
    })
    return
  }
  const text = studio.address || studio.location || studio.city || ''
  if (!text) {
    uni.showToast({ title: '暂未设置地址', icon: 'none' })
    return
  }
  uni.setClipboardData({
    data: text,
    success() {
      uni.showToast({ title: '地址已复制', icon: 'none' })
    },
  })
}


function previewPhoto(index) {
  uni.previewImage({
    current: photoPreviewUrls.value[index] || brandPhotos.value[index],
    urls: photoPreviewUrls.value,
    fail() {
      uni.showToast({ title: '图片预览失败', icon: 'none' })
    },
  })
}
</script>

<style scoped>
.gallery {
  width: 100%;
  height: 420rpx;
}

.gallery-img {
  width: 100%;
  height: 420rpx;
  display: block;
}

.gallery-empty {
  background: #161616;
}

.studio-section {
  padding-top: 0;
}

.studio-block {
  padding: 0;
  overflow: hidden;
}

.studio {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx;
}

.location-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  border-top: 1rpx solid #2a2a2a;
}

.location-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-width: 0;
}

.location-icon {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}

.location-text {
  font-size: 28rpx;
  color: #ffffff;
  line-height: 1.4;
}

.location-row .link-arrow {
  color: #a0a0a0;
}

.studio-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.studio-logo {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  flex-shrink: 0;
  background: #000;
}

.studio-info {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  min-width: 0;
}

.studio-name {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
}

.studio-hours {
  font-size: 24rpx;
  line-height: 1.4;
}

.phone-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-left: 24rpx;
  gap: 8rpx;
}

.phone-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #2a2a2a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.phone-icon {
  width: 36rpx;
  height: 36rpx;
}

.phone-label {
  font-size: 22rpx;
  color: #ffffff;
  line-height: 1;
}

.title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.body {
  font-size: 28rpx;
  line-height: 1.7;
}

.item {
  margin-bottom: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  font-size: 28rpx;
}

.label {
  color: #8a74e5;
  font-size: 24rpx;
}
</style>
