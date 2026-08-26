<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索内容 / 学员 / 联系方式" style="width: 260px" clearable @keyup.enter="search" />
        <el-select v-if="campusOptions.length > 1" v-model="campusId" placeholder="校区" clearable style="width: 200px" @change="search">
          <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
    </div>
    <el-table :data="list">
      <el-table-column prop="nickname" label="学员" width="120" />
      <el-table-column label="校区" width="180">
        <template #default="{ row }">{{ campusName(row.campusId) }}</template>
      </el-table-column>
      <el-table-column prop="contact" label="联系方式" width="160" />
      <el-table-column prop="content" label="反馈内容" show-overflow-tooltip />
      <el-table-column label="时间" width="180">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
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
import http from '../api/http'
import { campusName } from '../common/campuses'
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

function formatTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function load() {
  const params = { keyword: keyword.value, page: page.value, size }
  if (campusId.value) params.campusId = campusId.value
  const res = await http.get('/admin/feedbacks', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

function search() {
  page.value = 1
  return load()
}

onMounted(() => {
  campusId.value = defaultCampusId(auth.profile)
  load()
})
</script>
