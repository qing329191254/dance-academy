<template>
  <div class="employee-duty-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索课程"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-button @click="search">查询</el-button>
      </div>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div v-for="row in list" :key="row.id" class="mobile-feed-item">
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.nickname || '—' }}</span>
          <el-tag size="small" :type="statusTagType(row)">{{ statusLabel(row) }}</el-tag>
        </div>
        <div class="mobile-feed-main">{{ row.className || '—' }}</div>
        <div class="mobile-feed-meta">
          <span v-if="row.classDate">{{ row.classDate }}</span>
          <span v-if="row.timeText">{{ row.timeText }}</span>
        </div>
        <div class="mobile-feed-meta">
          <span>{{ campusName(row.campusId) }}</span>
        </div>
        <div class="mobile-feed-meta">
          <span>签到 {{ formatTime(row.checkedAt) }}</span>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无员工考勤记录</div>
    </div>

    <el-table v-else :data="list">
      <el-table-column prop="nickname" label="员工" width="120" />
      <el-table-column prop="className" label="关联课程" />
      <el-table-column prop="classDate" label="日期" width="120" />
      <el-table-column prop="timeText" label="时间" width="140" />
      <el-table-column label="校区" width="180">
        <template #default="{ row }">{{ campusName(row.campusId) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="140">
        <template #default="{ row }">{{ statusLabel(row) }}</template>
      </el-table-column>
      <el-table-column label="签到时间" width="180">
        <template #default="{ row }">{{ formatTime(row.checkedAt) }}</template>
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
import { ref } from 'vue'
import http from '../api/http'
import { campusName } from '../common/campuses'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'

const { isMobile } = useBreakpoint()
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

function statusLabel(row) {
  if (row.status === 'late') return `迟到 ${row.lateMinutes} 分钟`
  return '准时'
}

function statusTagType(row) {
  return row.status === 'late' ? 'warning' : 'success'
}

function search() {
  page.value = 1
  return load()
}

async function load() {
  const params = { keyword: keyword.value, page: page.value, size, ...campusParams() }
  const res = await http.get('/admin/employee-duty', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

const { campusParams } = useCampusScope(load)
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filters :deep(.el-input) {
  width: 240px;
}

@media (max-width: 768px) {
  .filters :deep(.el-input) {
    width: 100% !important;
  }
}
</style>
