<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">
        <img class="logo" src="/logo.png" alt="高校FOR-GET舞室" />
        <div>
          <div class="title">高校FOR-GET舞室</div>
          <div class="sub">管理后台</div>
        </div>
      </div>
      <div class="menu-scroll">
      <el-menu
        :default-active="route.path"
        :default-openeds="defaultOpeneds"
        router
        background-color="#16161c"
        text-color="#c9c7d4"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">工作台</el-menu-item>

        <el-sub-menu index="group-store">
          <template #title>门店运营</template>
          <el-menu-item index="/studio">门店信息</el-menu-item>
          <el-menu-item index="/media">轮播与相册</el-menu-item>
          <el-menu-item index="/classrooms">教室管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="group-course">
          <template #title>课程教务</template>
          <el-menu-item index="/teachers">老师档案</el-menu-item>
          <el-menu-item index="/courses">课程产品</el-menu-item>
          <el-menu-item index="/schedules">课表管理</el-menu-item>
          <el-menu-item index="/bookings">预约管理</el-menu-item>
          <el-menu-item index="/class-archives">课堂档案</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="group-user">
          <template #title>用户与会员</template>
          <el-menu-item index="/users">小程序用户</el-menu-item>
          <el-menu-item index="/cards">卡包发放</el-menu-item>
          <el-menu-item index="/opportunities">成长机会</el-menu-item>
          <el-menu-item index="/growth">成长文案</el-menu-item>
          <el-menu-item index="/applies">报名审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="group-attendance">
          <template #title>考勤签到</template>
          <el-menu-item index="/checkin-pending">待确认签到</el-menu-item>
          <el-menu-item index="/practice">签到记录</el-menu-item>
          <el-menu-item index="/teacher-attendance">教师考勤</el-menu-item>
          <el-menu-item index="/employee-duty">员工考勤</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="group-feedback">
          <template #title>反馈评价</template>
          <el-menu-item index="/teacher-reviews">评价教师</el-menu-item>
          <el-menu-item index="/feedback">意见反馈</el-menu-item>
          <el-menu-item index="/surveys">问卷调查</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="auth.isSuperAdmin" index="group-system">
          <template #title>系统设置</template>
          <el-menu-item index="/schools">校区管理员</el-menu-item>
          <el-menu-item index="/admins">管理员</el-menu-item>
        </el-sub-menu>
      </el-menu>
      </div>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="crumb">{{ route.meta.title || '管理后台' }}</div>
        <div class="right">
          <template v-if="campusOptions.length > 1">
            <span class="campus-label">校区</span>
            <el-select
              v-model="campusId"
              placeholder="全部校区"
              clearable
              style="width: 200px"
              @change="onCampusChange"
            >
              <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </template>
          <span class="role-tag">{{ roleLabel(auth.profile?.role) }}</span>
          <span>{{ auth.profile?.name || auth.profile?.username || '管理员' }}</span>
          <el-button text type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'
import { useCampusStore } from '../stores/campus'
import { allowedCampuses, roleLabel } from '../common/adminAccess'
import { loadCampuses } from '../common/campuses'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const campusStore = useCampusStore()
const { campusId } = storeToRefs(campusStore)
const campusOptions = computed(() => allowedCampuses(auth.profile))

function onCampusChange(value) {
  campusStore.setCampus(value || '')
}

const defaultOpeneds = computed(() => {
  const path = route.path
  if (path.startsWith('/studio') || path.startsWith('/media') || path.startsWith('/classrooms')) return ['group-store']
  if (
    path.startsWith('/teachers') ||
    path.startsWith('/courses') ||
    path.startsWith('/schedules') ||
    path.startsWith('/bookings') ||
    path.startsWith('/class-archives')
  ) {
    return ['group-course']
  }
  if (
    path.startsWith('/users') ||
    path.startsWith('/cards') ||
    path.startsWith('/opportunities') ||
    path.startsWith('/growth') ||
    path.startsWith('/applies')
  ) {
    return ['group-user']
  }
  if (
    path.startsWith('/checkin-pending') ||
    path.startsWith('/practice') ||
    path.startsWith('/teacher-attendance') ||
    path.startsWith('/employee-duty')
  ) {
    return ['group-attendance']
  }
  if (path.startsWith('/teacher-reviews') || path.startsWith('/feedback') || path.startsWith('/surveys')) {
    return ['group-feedback']
  }
  if (path.startsWith('/schools') || path.startsWith('/admins')) {
    return ['group-system']
  }
  return []
})

onMounted(async () => {
  if (auth.token) {
    try {
      await loadCampuses()
      await auth.fetchMe()
      campusStore.syncWithProfile(auth.profile)
    } catch {
      auth.logout()
      router.push('/login')
    }
  }
})

watch(
  () => auth.profile,
  (profile) => {
    if (profile) campusStore.syncWithProfile(profile)
  },
)

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background: #16161c;
  color: #fff;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}
.menu-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: scroll;
  scrollbar-gutter: stable;
}
.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 20px 16px 16px;
}
.logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  object-fit: contain;
  background: #2a1818;
  flex-shrink: 0;
}
.title {
  font-weight: 700;
  font-size: 13px;
  line-height: 1.3;
}
.sub {
  font-size: 12px;
  color: #9a98a8;
  margin-top: 2px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.crumb {
  font-weight: 600;
}
.right {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #555;
}
.campus-label {
  font-size: 13px;
  color: #6b6b76;
}
.role-tag {
  font-size: 12px;
  color: #8a8a96;
  padding: 2px 8px;
  background: #f4f4f6;
  border-radius: 999px;
}
.main {
  padding: 20px;
}
.aside :deep(.el-sub-menu__title) {
  margin: 4px 10px;
  border-radius: 8px;
}
.aside :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  padding-left: 44px !important;
}
.aside :deep(.el-menu-item),
.aside :deep(.el-sub-menu__title) {
  box-sizing: border-box;
}
</style>
