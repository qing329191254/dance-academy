<template>
  <div class="teacher-reviews-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索学员/评价内容"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="teacherId" placeholder="老师" clearable @change="search">
          <el-option v-for="item in teachers" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div v-for="row in list" :key="row.id" class="mobile-feed-item">
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.teacherName || '—' }}</span>
          <span class="mobile-feed-status">{{ row.nickname || '—' }}</span>
        </div>
        <div v-if="row.content" class="mobile-feed-main mobile-summary">{{ row.content }}</div>
        <div class="mobile-feed-meta">
          <span>{{ formatTime(row.createdAt) }}</span>
        </div>
        <div class="table-actions">
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无教师评价</div>
    </div>

    <el-table v-else :data="list">
      <el-table-column prop="teacherName" label="老师" width="120" />
      <el-table-column prop="nickname" label="学员" width="120" />
      <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
      <el-table-column label="时间" width="180">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="danger" @click="remove(row)">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'

const { isMobile } = useBreakpoint()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')
const teacherId = ref()
const teachers = ref([])

function formatTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function search() {
  page.value = 1
  return load()
}

async function loadTeachers() {
  const res = await http.get('/admin/teachers')
  teachers.value = res.data || []
}

async function load() {
  const params = { keyword: keyword.value, page: page.value, size, ...campusParams() }
  if (teacherId.value) params.teacherId = teacherId.value
  const res = await http.get('/admin/teacher-reviews', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

const { campusParams } = useCampusScope(load)

async function remove(row) {
  await ElMessageBox.confirm(`确认删除「${row.nickname || '学员'}」对 ${row.teacherName || '老师'} 的评价？`, '提示')
  await http.delete(`/admin/teacher-reviews/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

onMounted(loadTeachers)
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filters :deep(.el-input),
.filters :deep(.el-select) {
  width: 240px;
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
  .filters :deep(.el-input),
  .filters :deep(.el-select) {
    width: 100% !important;
  }
}
</style>
