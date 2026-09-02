<template>
  <div class="practice-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索课程名"
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
          <span class="mobile-feed-title">{{ row.name || '—' }}</span>
          <span class="mobile-feed-status">{{ checkinSourceLabel(row.checkinSource) }}</span>
        </div>
        <div class="mobile-feed-main">学员 ID {{ row.userId ?? '—' }}</div>
        <div class="mobile-feed-meta">
          <span v-if="row.classDate">{{ row.classDate }}</span>
          <span v-if="row.timeText">{{ row.timeText }}</span>
        </div>
        <div class="mobile-feed-meta">
          <span v-if="row.teacherName">{{ row.teacherName }}</span>
          <span v-if="row.room">{{ row.room }}</span>
        </div>
        <div class="mobile-feed-meta">
          <span v-if="row.duration">{{ row.duration }}</span>
          <span v-if="row.operatorName">确认人 {{ row.operatorName }}</span>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无签到记录</div>
    </div>

    <el-table v-else :data="list">
      <el-table-column prop="userId" label="学员ID" width="90" align="left" header-align="left" />
      <el-table-column prop="name" label="课程" align="left" header-align="left" />
      <el-table-column prop="classDate" label="日期" width="120" align="left" header-align="left" />
      <el-table-column prop="timeText" label="时间" width="140" align="left" header-align="left" />
      <el-table-column prop="teacherName" label="老师" width="100" align="left" header-align="left" />
      <el-table-column label="方式" width="120" align="left" header-align="left">
        <template #default="{ row }">{{ checkinSourceLabel(row.checkinSource) }}</template>
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
import { ref } from 'vue'
import http from '../api/http'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'

const { isMobile } = useBreakpoint()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')

function checkinSourceLabel(source) {
  if (source === 'manual') return '手动确认'
  if (source === 'confirmed') return '工作人员确认'
  return '扫码'
}

function search() {
  page.value = 1
  return load()
}

async function load() {
  const params = { keyword: keyword.value, page: page.value, size, ...campusParams() }
  const res = await http.get('/admin/practice', { params })
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
