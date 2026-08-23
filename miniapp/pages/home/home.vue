<template>
  <page-meta root-background-color="#000000" background-color="#000000" page-style="background-color:#000000;" />
  <view class="page">
    <view v-show="showSplash && splashSrc" class="launch-splash">
      <image class="launch-bg" :src="splashSrc" mode="aspectFill" />
      <view class="launch-mask" />
      <button class="launch-skip" plain hover-class="launch-skip-hover" @tap.stop="dismissSplash">
        跳过 {{ splashCountdown }}
      </button>
    </view>
    <view
      class="custom-navbar"
      :class="{ 'navbar-visible': showNavTitle }"
      :style="{ paddingTop: statusBarHeight + 'px' }"
    >
      <view class="navbar-inner">
        <text class="navbar-title" :class="{ show: showNavTitle }">高校FOR一GET街舞俱乐部</text>
      </view>
    </view>

    <view class="page-content">
      <swiper
        v-if="banners.length"
        class="hero"
        circular
        autoplay
        interval="4000"
        indicator-dots
        indicator-color="rgba(255,255,255,.35)"
        indicator-active-color="#ffffff"
      >
        <swiper-item v-for="(item, index) in banners" :key="index">
          <image
            class="hero-img"
            :src="item"
            mode="aspectFill"
            @click.stop="previewBanner(index)"
          />
        </swiper-item>
      </swiper>
      <view v-else class="hero hero-empty" />

      <view class="nav-row">
        <view class="nav-item" @click="go('/pages/book/book', true)">
          <view class="nav-icon">
            <image class="nav-icon-img" src="/static/nav/book.png" mode="aspectFit" />
          </view>
          <text>约课</text>
        </view>
        <view class="nav-item" @click="go('/pages/brand/brand')">
          <view class="nav-icon">
            <image class="nav-icon-img" src="/static/nav/brand.png" mode="aspectFit" />
          </view>
          <text>品牌</text>
        </view>
        <view class="nav-item" @click="go('/pages/course/list')">
          <view class="nav-icon">
            <image class="nav-icon-img" src="/static/nav/course.png" mode="aspectFit" />
          </view>
          <text>课程介绍</text>
        </view>
      </view>

      <view class="section">
        <view class="section-head">
          <text class="section-title">明星老师</text>
          <view class="section-more" @click="go('/pages/teachers/teachers')">
            <text>查看更多</text>
            <view class="link-arrow" />
          </view>
        </view>
        <scroll-view scroll-x class="teacher-scroll" :show-scrollbar="false">
          <view class="teacher-list">
            <view v-for="t in teachers" :key="t.id" class="teacher-item" @click="go(`/pages/teachers/detail?id=${t.id}`)">
              <image v-if="t.avatar" class="avatar" :src="t.avatar" mode="aspectFill" />
              <view v-else class="avatar avatar-ph" />
              <text class="name">{{ t.name }}</text>
              <text class="muted style">{{ t.style }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="section">
        <view class="section-head">
          <text class="section-title">课程介绍</text>
          <view class="section-more" @click="go('/pages/course/list')">
            <text>去看看</text>
            <view class="link-arrow" />
          </view>
        </view>
        <view v-for="c in homeCourses" :key="c.id" class="course-card card" @click="go(`/pages/course/detail?id=${c.id}`)">
          <view class="course-main">
            <text class="course-name">{{ c.name }}</text>
            <text class="muted">{{ c.desc }}</text>
          </view>
          <view class="course-side">
            <text class="price">¥{{ c.price }}</text>
            <text class="tag">{{ c.level }}</text>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="studio card">
          <view class="studio-left">
            <image v-if="studio.logo" class="studio-logo" :src="studio.logo" mode="aspectFit" />
            <view v-else class="studio-logo avatar-ph" />
            <view>
              <text class="studio-name">{{ studio.name }}</text>
              <text class="muted">{{ studio.location }}</text>
            </view>
          </view>
          <view class="phone" @click.stop="callStudio">
            <image class="phone-icon" src="/static/nav/phone.png" mode="aspectFit" />
          </view>
        </view>
      </view>

      <view class="page-bottom" />
    </view>
  </view>
</template>

<script setup>
import { computed, ref, reactive } from 'vue'
import { onLoad, onShow, onUnload, onPageScroll } from '@dcloudio/uni-app'
import { getHome } from '@/common/api.js'
import { teachers as mockTeachers, courses as mockCourses, studio as mockStudio } from '@/common/mock.js'
import { preloadTabPagesAsync } from '@/common/preloadTabs.js'
import { openPage, switchTabPage } from '@/common/navigate.js'
import { applyPageBackground, PAGE_BG, SPLASH_BG } from '@/common/pageTheme.js'
import { mediaUrl } from '@/common/config.js'

function readSplashCache() {
  return mediaUrl(uni.getStorageSync('splashImage') || '')
}

let launchSplashPending = true

const showSplash = ref(!!readSplashCache())
const splashCountdown = ref(3)
const showNavTitle = ref(false)
const statusBarHeight = ref(44)
let splashTimer = null
let splashSafetyTimer = null

function clearSplashTimer() {
  if (splashTimer) {
    clearInterval(splashTimer)
    splashTimer = null
  }
}

function clearSplashSafetyTimer() {
  if (splashSafetyTimer) {
    clearTimeout(splashSafetyTimer)
    splashSafetyTimer = null
  }
}

function dismissSplash() {
  clearSplashTimer()
  clearSplashSafetyTimer()
  showSplash.value = false
  launchSplashPending = false
  applyPageBackground(PAGE_BG)
  uni.showTabBar({ animation: false })
}

function startSplashTimer() {
  if (!showSplash.value || !splashSrc.value) {
    dismissSplash()
    return
  }
  applyPageBackground(SPLASH_BG)
  uni.hideTabBar({ animation: false })
  splashTimer = setInterval(() => {
    if (splashCountdown.value <= 1) {
      dismissSplash()
      return
    }
    splashCountdown.value -= 1
  }, 1000)
  splashSafetyTimer = setTimeout(() => {
    if (showSplash.value) dismissSplash()
  }, 4500)
}

onLoad(() => {
  preloadTabPagesAsync()
  loadHome()
  if (showSplash.value) startSplashTimer()
  else applyPageBackground(PAGE_BG)
  try {
    const info = uni.getSystemInfoSync()
    statusBarHeight.value = info.statusBarHeight || 44
  } catch (e) {
    statusBarHeight.value = 44
  }
})

onPageScroll((e) => {
  showNavTitle.value = e.scrollTop > 120
})

onShow(() => {
  if (showSplash.value) {
    applyPageBackground(SPLASH_BG)
    uni.hideTabBar({ animation: false })
    return
  }
  applyPageBackground(PAGE_BG)
  uni.showTabBar({ animation: false })
})

onUnload(() => {
  clearSplashTimer()
  clearSplashSafetyTimer()
  uni.showTabBar({ animation: false })
})

const banners = ref([])
const teachers = ref(mockTeachers)
const courses = ref(mockCourses)
const homeCourses = computed(() => (courses.value || []).slice(0, 3))
const studio = reactive({
  ...mockStudio,
  splashImage: readSplashCache(),
})
const splashSrc = computed(() => studio.splashImage || '')

const bannerPreviewUrls = ref([...banners.value])

function resolveBannerPreviewUrls() {
  bannerPreviewUrls.value = [...banners.value]
  banners.value.forEach((src, index) => {
    uni.getImageInfo({
      src,
      success(res) {
        bannerPreviewUrls.value[index] = res.path
      },
    })
  })
}

async function loadHome() {
  try {
    const data = await getHome()
    banners.value = (data.banners || []).filter(Boolean)
    if (data.teachers?.length) teachers.value = data.teachers
    if (data.courses?.length) courses.value = data.courses
    if (data.studio) Object.assign(studio, data.studio)
    if (studio.splashImage) uni.setStorageSync('splashImage', studio.splashImage)
    else uni.removeStorageSync('splashImage')
    resolveBannerPreviewUrls()
  } catch (e) {
    resolveBannerPreviewUrls()
  }
}

function previewBanner(index) {
  const urls = bannerPreviewUrls.value
  const current = urls[index] || banners.value[index]
  uni.previewImage({
    current,
    urls,
    fail() {
      uni.showToast({ title: '图片预览失败', icon: 'none' })
    },
  })
}

function go(url, tab = false) {
  if (tab) {
    switchTabPage(url)
    return
  }
  openPage(url)
}

function callStudio() {
  uni.makePhoneCall({
    phoneNumber: studio.phone,
  })
}
</script>

<style scoped>
.launch-splash {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: #000000;
  overflow: hidden;
}

.launch-bg {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.launch-mask {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.08), rgba(0, 0, 0, 0.35));
}

.launch-skip {
  position: absolute;
  right: 32rpx;
  bottom: calc(48rpx + env(safe-area-inset-bottom));
  z-index: 3;
  margin: 0;
  padding: 14rpx 32rpx;
  min-width: 140rpx;
  line-height: 1.4;
  border-radius: 999rpx;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 26rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.35);
}

.launch-skip::after {
  border: none;
}

.launch-skip-hover {
  background: rgba(0, 0, 0, 0.75);
  opacity: 0.92;
}

.page {
  width: 100%;
  min-height: 100vh;
  background: #111111;
}

.page-content {
  width: 100%;
}

.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  pointer-events: none;
  background: transparent;
  transition: background 0.25s ease;
}

.custom-navbar.navbar-visible {
  background: #111111;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.35);
}

.navbar-inner {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 100rpx;
}

.navbar-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #ffffff;
  text-align: center;
  line-height: 44px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.navbar-title.show {
  opacity: 1;
}

.page-bottom {
  height: 24rpx;
  padding-bottom: env(safe-area-inset-bottom);
}

.hero {
  width: 100%;
  height: 560rpx;
}

.hero-img {
  width: 100%;
  height: 560rpx;
  display: block;
}

.hero-empty {
  background: #161616;
}

.avatar-ph {
  background: #2a2a2a;
}

.nav-row {
  display: flex;
  justify-content: space-around;
  padding: 36rpx 24rpx 8rpx;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 24rpx;
  color: #fff;
}

.nav-item text {
  margin-top: 14rpx;
}

.nav-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 18rpx;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid #2a2a2a;
}

.nav-icon-img {
  width: 52rpx;
  height: 52rpx;
}

.teacher-scroll {
  width: 100%;
  white-space: nowrap;
}

.teacher-list {
  display: inline-flex;
  padding-right: 24rpx;
}

.teacher-item {
  width: 140rpx;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 36rpx;
}

.teacher-item .name {
  margin-top: 10rpx;
}

.teacher-item .style {
  margin-top: 6rpx;
}

.avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: #2a2a2a;
  flex-shrink: 0;
}

.name {
  font-size: 26rpx;
  color: #fff;
}

.style {
  font-size: 22rpx;
}

.course-card {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.course-main {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding-right: 20rpx;
}

.course-main .muted {
  margin-top: 10rpx;
}

.course-name {
  font-size: 30rpx;
  font-weight: 600;
}

.course-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
}

.course-side .tag {
  margin-top: 12rpx;
}

.price {
  color: #8a74e5;
  font-size: 30rpx;
  font-weight: 600;
}

.studio {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.studio-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.studio-logo {
  width: 88rpx;
  height: 88rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.studio-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.phone {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #8a74e5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.phone-icon {
  width: 40rpx;
  height: 40rpx;
}
</style>
