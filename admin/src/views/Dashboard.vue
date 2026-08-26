<template>
  <div>
    <div v-if="campusOptions.length > 1" class="page-card" style="margin-bottom: 16px">
      <div class="filters">
        <span class="filter-label">校区</span>
        <el-select v-model="campusId" placeholder="全部校区" clearable style="width: 220px" @change="load">
          <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </div>
    </div>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { allowedCampuses, defaultCampusId } from '../common/adminAccess'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const data = reactive({})
const campusId = ref('')
const campusOptions = computed(() => allowedCampuses(auth.profile))

async function load() {
  const params = {}
  if (campusId.value) params.campusId = campusId.value
  const res = await http.get('/admin/dashboard', { params })
  Object.assign(data, res.data || {})
}

onMounted(() => {
  campusId.value = defaultCampusId(auth.profile)
  load()
})
</script>

<style scoped>
.filters {
  display: flex;
  align-items: center;
  gap: 12px;
}
.filter-label {
  color: #6b6b76;
  font-size: 13px;
}
</style>
