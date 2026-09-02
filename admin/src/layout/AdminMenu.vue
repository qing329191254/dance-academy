<template>
  <el-menu
    :default-active="route.path"
    :default-openeds="defaultOpeneds"
    router
    background-color="#16161c"
    text-color="#c9c7d4"
    active-text-color="#ffffff"
    @select="onSelect"
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
      <el-menu-item index="/dance-categories">板块与舞种</el-menu-item>
      <el-menu-item index="/teachers">老师档案</el-menu-item>
      <el-menu-item index="/courses">课程产品</el-menu-item>
      <el-menu-item index="/schedules">课表管理</el-menu-item>
      <el-menu-item index="/bookings">预约管理</el-menu-item>
      <el-menu-item index="/class-archives">课堂档案</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="group-user">
      <template #title>用户与会员</template>
      <el-menu-item index="/users">小程序用户</el-menu-item>
      <el-menu-item v-if="auth.isSuperAdmin" index="/cards">卡包发放</el-menu-item>
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
      <el-menu-item index="/schools">校区管理</el-menu-item>
      <el-menu-item index="/admins">管理员</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const emit = defineEmits(['select'])

const route = useRoute()
const auth = useAuthStore()

const defaultOpeneds = computed(() => {
  const path = route.path
  if (path.startsWith('/studio') || path.startsWith('/media') || path.startsWith('/classrooms')) return ['group-store']
  if (
    path.startsWith('/dance-categories') ||
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

function onSelect() {
  emit('select')
}
</script>
