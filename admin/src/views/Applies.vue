<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索学员/机会" style="width: 240px" clearable @keyup.enter="load" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 140px" @change="load">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-button @click="load">查询</el-button>
      </div>
    </div>
    <el-table :data="list">
      <el-table-column prop="nickname" label="学员" width="120" align="left" header-align="left" />
      <el-table-column prop="title" label="机会" align="left" header-align="left" />
      <el-table-column prop="trackKey" label="赛道" width="120" align="left" header-align="left" />
      <el-table-column prop="status" label="状态" width="120" align="left" header-align="left" />
      <el-table-column label="操作" width="180" align="left" header-align="left">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="row.status === 'pending'" link type="primary" @click="setStatus(row, 'approved')">通过</el-button>
            <el-button v-if="row.status === 'pending'" link type="danger" @click="setStatus(row, 'rejected')">拒绝</el-button>
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
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')
const status = ref('')

async function load() {
  const res = await http.get('/admin/applies', { params: { keyword: keyword.value, status: status.value, page: page.value, size } })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}
async function setStatus(row, next) {
  await http.put(`/admin/applies/${row.id}`, { status: next })
  ElMessage.success('已更新')
  await load()
}
onMounted(load)
</script>
