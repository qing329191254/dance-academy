<template>
  <div class="feedback-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索内容 / 学员 / 联系方式"
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
          <span class="mobile-feed-status">{{ campusName(row.campusId) }}</span>
        </div>
        <div v-if="row.content" class="mobile-feed-main mobile-summary">{{ row.content }}</div>
        <div class="mobile-feed-meta">
          <span v-if="row.contact">{{ row.contact }}</span>
          <span>{{ formatTime(row.createdAt) }}</span>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无意见反馈</div>
    </div>

    <el-table v-else :data="list">
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
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function load() {
  const params = { keyword: keyword.value, page: page.value, size, ...campusParams() }
  const res = await http.get('/admin/feedbacks', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

function search() {
  page.value = 1
  return load()
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
  width: 260px;
}

.mobile-summary {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.45;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .filters :deep(.el-input) {
    width: 100% !important;
  }
}
</style>
