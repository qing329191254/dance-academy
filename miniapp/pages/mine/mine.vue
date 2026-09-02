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

    <view v-if="profileReady && !isTeacherUser" class="section">
      <view class="card growth-card">
        <view class="growth-head">
          <text class="growth-title">成长等级</text>
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

        <text class="muted growth-tip">新学员默认 T1，可通过年限与考核升级</text>
      </view>
    </view>

    <view v-else-if="loggedIn && !profileReady" class="section">
      <view class="card login-tip" @click="goProfile">
        <text class="login-tip-title">请完善个人资料</text>
        <text class="muted login-tip-desc">完善后即可查看卡包、预约与已上课程</text>
      </view>
    </view>

    <view v-else-if="!loggedIn" class="section">
      <view class="card login-tip" @click="goLogin">
        <text class="login-tip-title">登录后查看学习数据</text>
        <text class="muted login-tip-desc">卡包、预约与已上课程登录后同步展示</text>
      </view>
    </view>

    <view class="section">
      <view class="card">
        <template v-if="isEmployeeUser">
          <text class="section-title block">工作服务</text>
          <view class="services">
            <view
              v-for="item in employeeWorkServices"
              :key="item.key"
              class="service"
              @click="onServiceClick(item)"
            >
              <view class="s-icon">
                <image class="s-icon-img" :src="item.icon" mode="aspectFit" />
              </view>
              <text>{{ item.name }}</text>
            </view>
          </view>
          <text class="section-title block section-title-sub">学习服务</text>
          <view class="services">
            <view
              v-for="item in employeeLearnServices"
              :key="item.key"
              class="service"
              @click="onServiceClick(item)"
            >
              <view class="s-icon">
                <image class="s-icon-img" :src="item.icon" mode="aspectFit" />
              </view>
              <text>{{ item.name }}</text>
            </view>
          </view>
        </template>
        <template v-else>
          <text class="section-title block">我的服务</text>
          <view class="services">
            <view
              v-for="item in displayServices"
              :key="item.key"
              class="service"
              @click="onServiceClick(item)"
            >
              <view class="s-icon">
                <image class="s-icon-img" :src="item.icon" mode="aspectFit" />
              </view>
              <text>{{ item.name }}</text>
            </view>
          </view>
        </template>
      </view>
    </view>

    <view v-if="loggedIn" class="section">
      <view class="logout-btn" @click="confirmLogout">退出登录</view>
    </view>

    <view class="footer">
      <text class="muted brand-name">高校FOR-GET舞室</text>
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
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { userGrowthProfile } from '@/common/mock.js'
import { getMine } from '@/common/api.js'
import { selectedCampusId } from '@/common/campus.js'
import { startCheckInScan } from '@/common/checkin.js'
import { getUser, isLoggedIn, logout, isProfileComplete, isTeacher, isEmployee, saveUser } from '@/common/auth.js'
import { openPage, switchTabPage } from '@/common/navigate.js'
import { showSuccess, showError } from '@/common/toast.js'
import { mediaUrl } from '@/common/config.js'

const statusBarHeight = ref(44)
const headerTop = ref(88)
const growthTracks = ref([
  { key: 'work', ...userGrowthProfile.work },
  { key: 'dance', ...userGrowthProfile.dance },
])
const showLogoutConfirm = ref(false)
const loggedIn = ref(false)
const profileReady = ref(false)
const isTeacherUser = ref(false)
const isEmployeeUser = ref(false)
const userAvatar = ref('/static/avatars/guest.png')
const userNickname = ref('点击登录')
const userSubtitle = ref('微信一键登录')

function displayAvatar(url) {
  return mediaUrl(url) || '/static/avatars/guest.png'
}

function refreshUser() {
  loggedIn.value = isLoggedIn()
  profileReady.value = loggedIn.value && isProfileComplete()
  isTeacherUser.value = profileReady.value && isTeacher()
  isEmployeeUser.value = profileReady.value && isEmployee()
  const user = getUser()
  if (profileReady.value && user) {
    userAvatar.value = displayAvatar(user.avatar)
    if (isTeacherUser.value) {
      userNickname.value = user.nickname || '教师'
      userSubtitle.value = '已登录 · 教师'
    } else if (isEmployeeUser.value) {
      userNickname.value = user.nickname || '员工'
      userSubtitle.value = '已登录 · 员工 · 学员'
    } else {
      userNickname.value = user.nickname || '学员'
      userSubtitle.value = '已登录 · 学员'
    }
    return
  }
  if (loggedIn.value) {
    userAvatar.value = displayAvatar(user?.avatar)
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
    return
  }
  try {
    const data = await getMine(selectedCampusId.value)
    if (data.growth) {
      growthTracks.value = [
        { key: 'work', ...data.growth.work },
        { key: 'dance', ...data.growth.dance },
      ]
    }
    if (data.user) {
      userAvatar.value = displayAvatar(data.user.avatar) || userAvatar.value
      userNickname.value = data.user.nickname || userNickname.value
      isTeacherUser.value = data.user.role === 'teacher'
      isEmployeeUser.value = data.user.role === 'employee'
      const cached = getUser()
      if (cached) {
        saveUser({ ...cached, ...data.user })
      }
    }
  } catch (e) {
    // ignore
  }
}

const studentServices = [
  {
    name: '扫码签到',
    icon: '/static/mine/scan.png',
    key: 'scan-class',
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
    name: '排队课程',
    icon: '/static/mine/queue.svg',
    key: 'queue',
    url: '/pages/mine/queue',
    needLogin: true,
  },
  {
    name: '已上课程',
    icon: '/static/mine/attended.svg',
    key: 'practice',
    url: '/pages/mine/practice',
    needLogin: true,
  },
  {
    name: '练舞预约',
    icon: '/static/mine/practice-room.svg',
    key: 'practice-room',
    url: '/pages/mine/practice-room',
    needLogin: true,
  },
  {
    name: '成长中心',
    icon: '/static/mine/growth.png',
    key: 'growth',
    needLogin: false,
  },
  {
    name: '学员须知',
    icon: '/static/mine/notice.svg',
    key: 'notice',
    url: '/pages/mine/notice',
    needLogin: false,
  },
  {
    name: '意见反馈',
    icon: '/static/mine/feedback.svg',
    key: 'feedback',
    url: '/pages/mine/feedback',
    needLogin: true,
  },
  {
    name: '反馈老师',
    icon: '/static/mine/notice.svg',
    key: 'teacher-review',
    url: '/pages/mine/teacher-review',
    needLogin: true,
  },
  {
    name: '问卷调查',
    icon: '/static/mine/survey.svg',
    key: 'survey',
    url: '/pages/mine/survey',
    needLogin: true,
  },
]

const teacherServices = [
  {
    name: '考勤签到',
    icon: '/static/mine/scan.png',
    key: 'scan',
    needLogin: true,
  },
  {
    name: '我的课表',
    icon: '/static/mine/booking.png',
    key: 'teacher-schedule',
    url: '/pages/teacher/schedule',
    needLogin: true,
  },
  {
    name: '课时统计',
    icon: '/static/mine/growth.png',
    key: 'teacher-stats',
    url: '/pages/teacher/stats',
    needLogin: true,
  },
  {
    name: '课程档案',
    icon: '/static/mine/card.png',
    key: 'teacher-archive',
    url: '/pages/teacher/archive',
    needLogin: true,
  },
  {
    name: '学员评价',
    icon: '/static/mine/notice.svg',
    key: 'teacher-reviews',
    url: '/pages/teacher/reviews',
    needLogin: true,
  },
  {
    name: '教师简历',
    icon: '/static/mine/survey.svg',
    key: 'teacher-resume',
    url: '/pages/teacher/resume',
    needLogin: true,
  },
]

const employeeWorkServices = [
  {
    name: '现场签到',
    icon: '/static/mine/scan.png',
    key: 'employee-checkin',
    url: '/pages/employee/checkin',
    needLogin: true,
  },
  {
    name: '值班签到',
    icon: '/static/mine/notice.svg',
    key: 'scan-duty',
    needLogin: true,
  },
  {
    name: '岗位信息',
    icon: '/static/mine/notice.svg',
    key: 'employee-profile',
    url: '/pages/employee/profile',
    needLogin: true,
  },
  {
    name: '周报',
    icon: '/static/mine/feedback.svg',
    key: 'employee-weekly',
    url: '/pages/employee/weekly',
    needLogin: true,
  },
  {
    name: '工作成绩',
    icon: '/static/mine/growth.png',
    key: 'employee-performance',
    url: '/pages/employee/performance',
    needLogin: true,
  },
]

const employeeLearnServices = [
  {
    name: '上课签到',
    icon: '/static/mine/scan.png',
    key: 'scan-class',
    needLogin: true,
  },
  ...studentServices.filter((item) => item.key !== 'scan-class'),
]

const displayServices = computed(() => {
  if (isTeacherUser.value) return teacherServices
  return studentServices
})

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
  if (loggedIn.value) {
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

function onServiceClick(item) {
  if (item.needLogin && !profileReady.value) {
    if (loggedIn.value && !isProfileComplete()) {
      goProfile()
      return
    }
    goLogin()
    return
  }
  if (item.key === 'scan-class') {
    startScan('class')
    return
  }
  if (item.key === 'scan-duty') {
    startScan('duty')
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

function startScan(mode) {
  const options = mode ? { mode } : {}
  startCheckInScan({
    onResult(outcome) {
      if (outcome.ok) {
        showSuccess(outcome.message || (outcome.pending ? '已提交，请等待工作人员确认' : '签到成功'))
        loadMine()
        return
      }
      showError(outcome.message || '签到失败')
    },
  }, options)
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
}

.growth-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #ffffff;
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

.section-title-sub {
  margin-top: 12rpx;
  padding-top: 8rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.06);
}

.services {
  display: flex;
  flex-wrap: wrap;
}

.service {
  width: 25%;
  margin-bottom: 28rpx;
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
