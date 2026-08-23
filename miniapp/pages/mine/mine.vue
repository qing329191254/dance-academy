<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="hero" :style="{ paddingTop: headerTop + 'px' }">
      <view class="profile" @click="onProfileTap">
        <image class="avatar" :src="userAvatar" mode="aspectFill" />
        <view class="user">
          <text class="login">{{ userNickname }}</text>
          <text class="muted">{{ userSubtitle }}</text>
        </view>
      </view>
    </view>

    <view v-if="profileReady" class="stats section">
      <view class="stat" @click="goAuth('/pages/mine/cards')">
        <text class="num">{{ myCards.length }}</text>
        <text>卡包</text>
      </view>
      <view class="stat" @click="goAuth('/pages/mine/courses')">
        <text class="num">{{ myCourses.length }}</text>
        <text>课程</text>
      </view>
      <view class="stat" @click="goAuth('/pages/mine/bookings')">
        <text class="num">{{ bookingCount }}</text>
        <text>已约</text>
      </view>
      <view class="stat" @click="goAuth('/pages/mine/practice')">
        <text class="num">{{ practiceCount }}</text>
        <text>习练</text>
      </view>
    </view>

    <view v-if="profileReady" class="section">
      <view class="card growth-card">
        <view class="growth-head" @click="goGrowth">
          <text class="growth-title">成长等级</text>
          <view class="growth-more">
            <text class="muted">成长中心</text>
            <view class="link-arrow" />
          </view>
        </view>

        <view class="growth-tracks">
          <view
            v-for="(track, index) in growthTracks"
            :key="track.key"
            class="track-row"
            :class="{ 'track-row-last': index === growthTracks.length - 1 }"
            @click="go(track.url)"
          >
            <view class="track-main">
              <text class="track-line">{{ track.line }}</text>
              <text class="track-path muted">{{ track.path }}</text>
            </view>
            <view class="track-level">
              <text class="track-stage">{{ track.current }}</text>
              <view class="level-pill">{{ track.level }}</view>
            </view>
          </view>
        </view>

        <text class="muted growth-tip">新学员默认 T1，可通过消费、年限与考核升级</text>
      </view>
    </view>

    <view v-else-if="loggedIn" class="section">
      <view class="card login-tip" @click="goProfile">
        <text class="login-tip-title">请完善个人资料</text>
        <text class="muted login-tip-desc">完善后即可查看卡包、课程与学习记录</text>
      </view>
    </view>

    <view v-else class="section">
      <view class="card login-tip" @click="goLogin">
        <text class="login-tip-title">登录后查看学习数据</text>
        <text class="muted login-tip-desc">卡包、课程、预约与习练记录登录后同步展示</text>
      </view>
    </view>

    <view class="section">
      <view class="card">
        <text class="section-title block">我的服务</text>
        <view class="services">
          <view
            v-for="item in services"
            :key="item.name"
            class="service"
            @click="onServiceClick(item)"
          >
            <view class="s-icon">
              <image class="s-icon-img" :src="item.icon" mode="aspectFit" />
            </view>
            <text>{{ item.name }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="loggedIn" class="section">
      <view class="logout-btn" @click="confirmLogout">退出登录</view>
    </view>

    <view class="footer">
      <text class="muted brand-name">高校FOR一GET街舞俱乐部</text>
      <view class="legal-links">
        <text class="link" @click="goLegal('user')">用户协议</text>
        <text class="muted dot">·</text>
        <text class="link" @click="goLegal('privacy')">隐私政策</text>
      </view>
    </view>

    <app-modal
      v-model:show="showLogoutConfirm"
      title="确认退出"
      content="确定要退出当前账号吗？"
      :show-cancel="true"
      cancel-text="取消"
      confirm-text="退出"
      @confirm="doLogout"
    />

      <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { userGrowthProfile } from '@/common/mock.js'
import { getMine } from '@/common/api.js'
import { startCheckInScan } from '@/common/checkin.js'
import { getUser, isLoggedIn, logout, isProfileComplete } from '@/common/auth.js'
import { openPage, switchTabPage } from '@/common/navigate.js'
import { showSuccess, showError } from '@/common/toast.js'

const statusBarHeight = ref(44)
const headerTop = ref(88)
const practiceCount = ref(0)
const bookingCount = ref(0)
const myCards = ref([])
const myCourses = ref([])
const growthTracks = ref([
  { key: 'work', ...userGrowthProfile.work },
  { key: 'dance', ...userGrowthProfile.dance },
])
const showLogoutConfirm = ref(false)
const loggedIn = ref(false)
const profileReady = ref(false)
const userAvatar = ref('/static/avatars/guest.png')
const userNickname = ref('点击登录')
const userSubtitle = ref('微信一键登录')

function refreshUser() {
  loggedIn.value = isLoggedIn()
  profileReady.value = loggedIn.value && isProfileComplete()
  const user = getUser()
  if (profileReady.value && user) {
    userAvatar.value = user.avatar || '/static/avatars/guest.png'
    userNickname.value = user.nickname || '学员'
    userSubtitle.value = '已登录 · 高校街舞学员'
    return
  }
  if (loggedIn.value) {
    userAvatar.value = user?.avatar || '/static/avatars/guest.png'
    userNickname.value = '请完善资料'
    userSubtitle.value = '完善后即可使用全部功能'
    return
  }
  userAvatar.value = '/static/avatars/guest.png'
  userNickname.value = '点击登录'
  userSubtitle.value = '微信一键登录'
}

async function loadMine() {
  if (!profileReady.value) {
    myCards.value = []
    myCourses.value = []
    bookingCount.value = 0
    practiceCount.value = 0
    return
  }
  try {
    const data = await getMine()
    myCards.value = data.cards || []
    myCourses.value = data.courses || []
    bookingCount.value = (data.bookings || []).length
    practiceCount.value = (data.practice || []).length
    if (data.growth) {
      growthTracks.value = [
        { key: 'work', ...data.growth.work },
        { key: 'dance', ...data.growth.dance },
      ]
    }
    if (data.user) {
      userAvatar.value = data.user.avatar || userAvatar.value
      userNickname.value = data.user.nickname || userNickname.value
    }
  } catch (e) {
    myCards.value = []
    myCourses.value = []
  }
}

const services = [
  {
    name: '扫码签到',
    icon: '/static/mine/scan.png',
    key: 'scan',
    needLogin: true,
  },
  {
    name: '卡包',
    icon: '/static/mine/card.png',
    key: 'cards',
    url: '/pages/mine/cards',
    needLogin: true,
  },
  {
    name: '已约课程',
    icon: '/static/mine/booking.png',
    key: 'bookings',
    url: '/pages/mine/bookings',
    needLogin: true,
  },
  {
    name: '成长中心',
    icon: '/static/mine/growth.png',
    key: 'growth',
    needLogin: false,
  },
]

onLoad(() => {
  try {
    const info = uni.getSystemInfoSync()
    statusBarHeight.value = info.statusBarHeight || 44
    headerTop.value = statusBarHeight.value + 44
  } catch (e) {
    statusBarHeight.value = 44
    headerTop.value = 88
  }
})

onShow(() => {
  refreshUser()
  loadMine()
})

function goProfile() {
  openPage('/pages/login/profile')
}

function onProfileTap() {
  if (loggedIn.value && !isProfileComplete()) {
    goProfile()
    return
  }
  if (profileReady.value) {
    goProfile()
    return
  }
  goLogin()
}

function confirmLogout() {
  showLogoutConfirm.value = true
}

function doLogout() {
  logout()
  refreshUser()
  showSuccess('退出成功')
}

function goLogin() {
  go('/pages/login/login')
}

function goAuth(url) {
  if (!profileReady.value) {
    if (loggedIn.value && !isProfileComplete()) {
      goProfile()
      return
    }
    goLogin()
    return
  }
  go(url)
}

function onServiceClick(item) {
  if (item.needLogin && !profileReady.value) {
    if (loggedIn.value && !isProfileComplete()) {
      goProfile()
      return
    }
    goLogin()
    return
  }
  if (item.key === 'scan') {
    startScan()
    return
  }
  if (item.key === 'growth') {
    goGrowth()
    return
  }
  if (item.url) {
    go(item.url)
  }
}

function go(url) {
  openPage(url)
}

function startScan() {
  startCheckInScan({
    onResult(outcome) {
      if (outcome.ok) {
        showSuccess(outcome.message || '签到成功')
        loadMine()
        return
      }
      showError(outcome.message || '签到失败')
    },
  })
}

function goGrowth() {
  switchTabPage('/pages/growth/index')
}

function goLegal(type) {
  const url =
    type === 'privacy'
      ? '/pages/legal/privacy-policy'
      : '/pages/legal/user-agreement'
  openPage(url)
}
</script>

<style scoped>
.page {
  background: #111111;
}

.hero {
  background: linear-gradient(
    180deg,
    rgba(138, 116, 229, 0.55) 0%,
    rgba(138, 116, 229, 0.22) 42%,
    rgba(17, 17, 17, 0) 100%
  );
  padding-bottom: 32rpx;
}

.profile {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 48rpx 32rpx 16rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #2a2a2a;
  border: 4rpx solid rgba(255, 255, 255, 0.12);
  flex-shrink: 0;
}

.user {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.login {
  font-size: 36rpx;
  font-weight: 700;
}

.stats {
  display: flex;
  justify-content: space-between;
  padding-top: 8rpx;
}

.stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #fff;
}

.num {
  font-size: 40rpx;
  color: #8a74e5;
  font-weight: 700;
}

.login-tip {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding: 36rpx 28rpx;
}

.login-tip-title {
  font-size: 30rpx;
  font-weight: 600;
}

.login-tip-desc {
  font-size: 24rpx;
  line-height: 1.6;
}

.growth-card {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.growth-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.growth-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #ffffff;
}

.growth-more {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
}

.growth-tracks {
  display: flex;
  flex-direction: column;
}

.track-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #2a2a2a;
}

.track-row-last {
  border-bottom: none;
  padding-bottom: 0;
}

.track-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.track-line {
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
}

.track-path {
  font-size: 22rpx;
  line-height: 1.5;
}

.track-level {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.track-stage {
  font-size: 22rpx;
  color: #9a9a9a;
}

.level-pill {
  min-width: 64rpx;
  height: 48rpx;
  padding: 0 14rpx;
  border-radius: 12rpx;
  background: #242424;
  color: #8a74e5;
  font-size: 26rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.growth-tip {
  font-size: 22rpx;
  line-height: 1.5;
}

.block {
  display: block;
  margin-bottom: 28rpx;
}

.services {
  display: flex;
  justify-content: space-between;
}

.service {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  font-size: 24rpx;
}

.s-icon {
  width: 84rpx;
  height: 84rpx;
  border-radius: 20rpx;
  background: #242424;
  display: flex;
  align-items: center;
  justify-content: center;
}

.s-icon-img {
  width: 52rpx;
  height: 52rpx;
}

.footer {
  text-align: center;
  font-size: 22rpx;
  padding: 40rpx 0 20rpx;
}

.brand-name {
  display: block;
  margin-bottom: 12rpx;
}

.legal-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rpx;
}

.link {
  color: #8a74e5;
  font-size: 22rpx;
}

.dot {
  font-size: 22rpx;
}

.logout-btn {
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border-radius: 16rpx;
  background: #1a1a1a;
  color: #e57373;
  font-size: 28rpx;
}
</style>
