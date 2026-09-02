<template>
  <div class="checkin-pending-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索姓名/课程"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-date-picker
          v-model="classDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="日期"
          clearable
          @change="search"
        />
        <el-select v-model="status" placeholder="状态" @change="search">
          <el-option label="待确认" value="pending" />
          <el-option label="已确认" value="confirmed" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div v-for="row in list" :key="row.id" class="mobile-feed-item">
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.nickname || '—' }}</span>
          <el-tag size="small" :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </div>
        <div class="mobile-feed-main">{{ row.className || '—' }}</div>
        <div class="mobile-feed-meta">
          <span v-if="row.checkinTypeLabel">{{ row.checkinTypeLabel }}</span>
          <span v-if="row.roleLabel">{{ row.roleLabel }}</span>
        </div>
        <div class="mobile-feed-meta">
          <span v-if="row.classDate">{{ row.classDate }}</span>
          <span v-if="row.timeText">{{ row.timeText }}</span>
        </div>
        <div class="mobile-feed-meta">
          <span>扫码 {{ formatTime(row.scannedAt) }}</span>
        </div>
        <div v-if="row.status !== 'pending' && row.confirmedByName" class="mobile-feed-meta muted">
          处理人 {{ row.confirmedByName }}
        </div>
        <div v-if="row.status === 'pending'" class="table-actions">
          <el-button link type="primary" @click="confirm(row)">确认到场</el-button>
          <el-button link type="danger" @click="reject(row)">拒绝</el-button>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无待确认签到</div>
    </div>

    <el-table v-else :data="list">
      <el-table-column prop="nickname" label="姓名" width="120" />
      <el-table-column prop="checkinTypeLabel" label="签到类型" width="100" />
      <el-table-column prop="roleLabel" label="身份" width="90" />
      <el-table-column prop="className" label="课程" />
      <el-table-column prop="classDate" label="日期" width="120" />
      <el-table-column prop="timeText" label="时间" width="130" />
      <el-table-column label="扫码时间" width="180">
        <template #default="{ row }">{{ formatTime(row.scannedAt) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ statusLabel(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left" fixed="right">
        <template #default="{ row }">
          <div v-if="row.status === 'pending'" class="table-actions">
            <el-button link type="primary" @click="confirm(row)">确认到场</el-button>
            <el-button link type="danger" @click="reject(row)">拒绝</el-button>
          </div>
          <span v-else class="muted">{{ row.confirmedByName || '-' }}</span>
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
import { ref } from 'vue'
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
const classDate = ref('')
const status = ref('pending')

function formatTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function statusLabel(value) {
  if (value === 'confirmed') return '已确认'
  if (value === 'rejected') return '已拒绝'
  return '待确认'
}

function statusTagType(value) {
  if (value === 'confirmed') return 'success'
  if (value === 'rejected') return 'danger'
  return 'warning'
}

function search() {
  page.value = 1
  load()
}

async function load() {
  const params = { keyword: keyword.value, status: status.value, page: page.value, size, ...campusParams() }
  if (classDate.value) params.classDate = classDate.value
  const res = await http.get('/admin/checkin-pending', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

const { campusParams } = useCampusScope(load)

async function confirm(row) {
  await http.post(`/admin/checkin-pending/${row.id}/confirm`)
  ElMessage.success('已确认到场')
  await load()
}

async function reject(row) {
  await ElMessageBox.confirm(`确认拒绝「${row.nickname || '用户'}」的签到？`, '提示')
  await http.post(`/admin/checkin-pending/${row.id}/reject`)
  ElMessage.success('已拒绝')
  await load()
}
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filters :deep(.el-input),
.filters :deep(.el-select),
.filters :deep(.el-date-editor) {
  width: 240px;
}

.muted {
  color: #909399;
}

@media (max-width: 768px) {
  .filters :deep(.el-input),
  .filters :deep(.el-select),
  .filters :deep(.el-date-editor) {
    width: 100% !important;
  }
}
</style>
