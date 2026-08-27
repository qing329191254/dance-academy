<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索课程" style="width: 240px" clearable @keyup.enter="load" />
        <el-button @click="load">查询</el-button>
      </div>
    </div>
    <el-table :data="list">
      <el-table-column prop="nickname" label="教师" width="120" />
      <el-table-column prop="className" label="课程" />
      <el-table-column prop="classDate" label="日期" width="120" />
      <el-table-column prop="timeText" label="时间" width="140" />
      <el-table-column label="校区" width="180">
        <template #default="{ row }">{{ campusName(row.campusId) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">{{ row.status === 'late' ? `迟到 ${row.lateMinutes} 分钟` : '准时' }}</template>
      </el-table-column>
      <el-table-column label="打卡时间" width="180">
        <template #default="{ row }">{{ formatTime(row.checkedAt) }}</template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 16px" background layout="total, prev, pager, next" :total="total" v-model:current-page="page" :page-size="size" @current-change="load" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import http from '../api/http'
import { campusName } from '../common/campuses'
import { useCampusScope } from '../composables/useCampusScope'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')

function formatTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function search() { page.value = 1; load() }

async function load() {
  const params = { keyword: keyword.value, page: page.value, size, ...campusParams() }
  const res = await http.get('/admin/teacher-attendance', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

const { campusParams } = useCampusScope(load)
</script>
