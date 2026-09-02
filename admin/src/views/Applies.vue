<template>
  <div class="applies-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索学员/机会"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="status" placeholder="状态" clearable @change="search">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div v-for="row in list" :key="row.id" class="mobile-feed-item">
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.nickname || '—' }}</span>
          <el-tag size="small" :type="applyStatusTagType(row.status)">{{ applyStatusLabelOf(row.status) }}</el-tag>
        </div>
        <div class="mobile-feed-main">{{ row.title || '—' }}</div>
        <div class="mobile-feed-meta">
          <span>{{ trackLabelOf(row.trackKey) }}</span>
        </div>
        <div class="mobile-feed-meta">
          <a
            v-if="row.resumeUrl"
            class="resume-link"
            :href="mediaSrc(row.resumeUrl)"
            target="_blank"
            rel="noreferrer"
          >
            {{ row.resumeName || '查看简历' }}
          </a>
          <span v-else class="muted">暂无简历</span>
        </div>
        <div v-if="row.status === 'pending'" class="table-actions">
          <el-button link type="primary" @click="setStatus(row, 'approved')">通过</el-button>
          <el-button link type="danger" @click="setStatus(row, 'rejected')">拒绝</el-button>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无报名记录</div>
    </div>

    <el-table v-else :data="list">
      <el-table-column prop="nickname" label="学员" width="120" align="left" header-align="left" />
      <el-table-column prop="title" label="机会" align="left" header-align="left" />
      <el-table-column label="机会类型" width="100" align="left" header-align="left">
        <template #default="{ row }">{{ trackLabelOf(row.trackKey) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="left" header-align="left">
        <template #default="{ row }">
          <el-tag size="small" :type="applyStatusTagType(row.status)">{{ applyStatusLabelOf(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="简历" width="140" align="left" header-align="left">
        <template #default="{ row }">
          <a v-if="row.resumeUrl" class="resume-link" :href="mediaSrc(row.resumeUrl)" target="_blank" rel="noreferrer">
            {{ row.resumeName || '查看简历' }}
          </a>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left">
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { mediaSrc } from '../utils/media'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'
import { applyStatusLabelOf, applyStatusTagType, trackLabelOf } from '../common/growth'

const { isMobile } = useBreakpoint()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')
const status = ref('')

function search() {
  page.value = 1
  return load()
}

async function load() {
  const res = await http.get('/admin/applies', {
    params: { keyword: keyword.value, status: status.value, page: page.value, size, ...campusParams() },
  })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

const { campusParams } = useCampusScope(load)

async function setStatus(row, next) {
  await http.put(`/admin/applies/${row.id}`, { status: next })
  ElMessage.success('已更新')
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
.filters :deep(.el-select) {
  width: 240px;
}

.resume-link {
  color: var(--el-color-primary);
  text-decoration: none;
}

.resume-link:hover {
  text-decoration: underline;
}

.muted {
  color: #909399;
}

@media (max-width: 768px) {
  .filters :deep(.el-input),
  .filters :deep(.el-select) {
    width: 100% !important;
  }
}
</style>
