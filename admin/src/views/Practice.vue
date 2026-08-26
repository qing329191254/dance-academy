<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索课程名" style="width: 240px" clearable @keyup.enter="load" />
        <el-select v-if="campusOptions.length > 1" v-model="campusId" placeholder="校区" clearable style="width: 200px" @change="search">
          <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button @click="load">查询</el-button>
      </div>
    </div>
    <el-table :data="list">
      <el-table-column prop="userId" label="学员ID" width="90" align="left" header-align="left" />
      <el-table-column prop="name" label="课程" align="left" header-align="left" />
      <el-table-column prop="classDate" label="日期" width="120" align="left" header-align="left" />
      <el-table-column prop="timeText" label="时间" width="140" align="left" header-align="left" />
      <el-table-column prop="teacherName" label="老师" width="100" align="left" header-align="left" />
      <el-table-column label="方式" width="120" align="left" header-align="left">
        <template #default="{ row }">{{ row.checkinSource === 'manual' ? '手动确认' : '扫码' }}</template>
      </el-table-column>
      <el-table-column prop="operatorName" label="确认人" width="120" align="left" header-align="left" />
      <el-table-column prop="room" label="教室" width="130" align="left" header-align="left" />
      <el-table-column prop="duration" label="时长" width="100" align="left" header-align="left" />
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
const campusId = ref('')

function search() {
  page.value = 1
  return load()
}

async function load() {
  const params = { keyword: keyword.value, page: page.value, size }
  if (campusId.value) params.campusId = campusId.value
  const res = await http.get('/admin/practice', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}
onMounted(() => {
  campusId.value = defaultCampusId(auth.profile)
  load()
})
</script>
