<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索学员/课程" style="width: 240px" clearable @keyup.enter="load" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 140px" @change="load">
          <el-option label="待上课" value="待上课" />
          <el-option label="排队中" value="排队中" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已取消" value="已取消" />
        </el-select>
        <el-select v-if="campusOptions.length > 1" v-model="campusId" placeholder="校区" clearable style="width: 200px" @change="search">
          <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button @click="load">查询</el-button>
      </div>
    </div>
    <el-table :data="list">
      <el-table-column prop="nickname" label="学员" width="120" align="left" header-align="left" />
      <el-table-column prop="name" label="课程" align="left" header-align="left" />
      <el-table-column prop="classDate" label="日期" width="120" align="left" header-align="left" />
      <el-table-column prop="timeText" label="时间" width="140" align="left" header-align="left" />
      <el-table-column prop="teacherName" label="老师" width="100" align="left" header-align="left" />
      <el-table-column prop="room" label="教室" width="130" align="left" header-align="left" />
      <el-table-column prop="status" label="状态" width="100" align="left" header-align="left" />
      <el-table-column label="操作" width="260" align="left" header-align="left">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="row.status === '待上课' && !row.checkedIn" link type="primary" @click="manualCheckin(row)">手动签到</el-button>
            <el-button v-if="row.status === '待上课'" link type="primary" @click="setStatus(row, '已完成')">完成</el-button>
            <el-button v-if="row.status === '待上课' || row.status === '排队中'" link type="danger" @click="setStatus(row, '已取消')">取消</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top: 16px"
      background
      layout="total, prev, pager, next"
      :total="total"
      v-model:current-page="page"
      :page-size="size"
      @current-change="load"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { allowedCampuses, defaultCampusId } from '../common/adminAccess'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const campusOptions = computed(() => allowedCampuses(auth.profile))
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')
const status = ref('')
const campusId = ref('')

function search() {
  page.value = 1
  return load()
}

async function load() {
  const params = { keyword: keyword.value, status: status.value, page: page.value, size }
  if (campusId.value) params.campusId = campusId.value
  const res = await http.get('/admin/bookings', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}
async function setStatus(row, next) {
  await http.put(`/admin/bookings/${row.id}`, { status: next })
  ElMessage.success('已更新')
  await load()
}
async function manualCheckin(row) {
  await http.post('/admin/checkin/manual', {
    userId: row.userId,
    scheduleId: row.scheduleId,
    classDate: row.classDate,
  })
  ElMessage.success('已手动签到')
  await load()
}
onMounted(() => {
  campusId.value = defaultCampusId(auth.profile)
  load()
})
</script>
