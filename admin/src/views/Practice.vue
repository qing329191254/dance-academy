<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索课程名" style="width: 240px" clearable @keyup.enter="load" />
        <el-button @click="load">查询</el-button>
      </div>
    </div>
    <el-table :data="list">
      <el-table-column prop="userId" label="学员ID" width="90" align="left" header-align="left" />
      <el-table-column prop="name" label="课程" align="left" header-align="left" />
      <el-table-column prop="classDate" label="日期" width="120" align="left" header-align="left" />
      <el-table-column prop="timeText" label="时间" width="140" align="left" header-align="left" />
      <el-table-column prop="teacherName" label="老师" width="100" align="left" header-align="left" />
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
import { onMounted, ref } from 'vue'
import http from '../api/http'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')

async function load() {
  const res = await http.get('/admin/practice', { params: { keyword: keyword.value, page: page.value, size } })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}
onMounted(load)
</script>
