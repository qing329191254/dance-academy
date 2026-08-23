<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">
        <img class="logo" src="/logo.png" alt="FOR一GET" />
        <div>
          <div class="title">FOR一GET</div>
          <div class="sub">街舞俱乐部后台</div>
        </div>
      </div>
      <el-menu :default-active="route.path" router background-color="#16161c" text-color="#c9c7d4" active-text-color="#ffffff">
        <el-menu-item index="/dashboard">工作台</el-menu-item>
        <el-menu-item index="/studio">门店信息</el-menu-item>
        <el-menu-item index="/media">轮播与相册</el-menu-item>
        <el-menu-item index="/teachers">老师管理</el-menu-item>
        <el-menu-item index="/courses">课程管理</el-menu-item>
        <el-menu-item index="/schedules">课表管理</el-menu-item>
        <el-menu-item index="/bookings">预约管理</el-menu-item>
        <el-menu-item index="/users">学员管理</el-menu-item>
        <el-menu-item index="/cards">卡包发放</el-menu-item>
        <el-menu-item index="/opportunities">成长机会</el-menu-item>
        <el-menu-item index="/applies">报名审核</el-menu-item>
        <el-menu-item index="/practice">签到记录</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="crumb">{{ route.meta.title || '管理后台' }}</div>
        <div class="right">
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
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

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
.main {
  padding: 20px;
}
</style>
