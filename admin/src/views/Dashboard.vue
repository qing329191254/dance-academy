<template>
  <div>
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">学员总数</div>
        <div class="num">{{ data.userCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">今日待上课预约</div>
        <div class="num">{{ data.bookingToday || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待审核报名</div>
        <div class="num">{{ data.pendingApplies || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">近 7 日签到</div>
        <div class="num">{{ data.practiceWeek || 0 }}</div>
      </div>
    </div>
    <div class="page-card" style="margin-bottom: 16px">
      <div class="toolbar">
        <h3>最近预约</h3>
        <el-button link type="primary" @click="router.push('/bookings')">查看全部</el-button>
      </div>
      <el-table :data="data.latestBookings || []" size="small">
        <el-table-column prop="nickname" label="学员" width="120" />
        <el-table-column prop="name" label="课程" />
        <el-table-column prop="classDate" label="日期" width="120" />
        <el-table-column prop="timeText" label="时间" width="140" />
        <el-table-column prop="status" label="状态" width="100" />
      </el-table>
    </div>
    <div class="page-card">
      <div class="toolbar">
        <h3>最近报名</h3>
        <el-button link type="primary" @click="router.push('/applies')">查看全部</el-button>
      </div>
      <el-table :data="data.latestApplies || []" size="small">
        <el-table-column prop="nickname" label="学员" width="120" />
        <el-table-column prop="title" label="机会" />
        <el-table-column prop="trackKey" label="赛道" width="140" />
        <el-table-column prop="status" label="状态" width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const data = reactive({})

onMounted(async () => {
  const res = await http.get('/admin/dashboard')
  Object.assign(data, res.data || {})
})
</script>
