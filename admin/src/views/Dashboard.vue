<template>
  <div>
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">{{ campusFiltered ? '本校区学员' : '学员总数' }}</div>
        <div class="num">{{ data.userCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">今日待上课预约</div>
        <div class="num">{{ data.bookingToday || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待审核报名</div>
        <div class="num">{{ data.pendingApplies || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">近 7 日签到</div>
        <div class="num">{{ data.practiceWeek || 0 }}</div>
      </div>
    </div>
    <div class="page-card dashboard-section">
      <div class="toolbar">
        <h3>最近预约</h3>
        <el-button link type="primary" @click="router.push('/bookings')">查看全部</el-button>
      </div>
      <div v-if="isMobile" class="mobile-feed">
        <div v-for="(row, index) in previewBookings" :key="row.id || index" class="mobile-feed-item">
          <div class="mobile-feed-head">
            <span class="mobile-feed-title">{{ row.nickname || '—' }}</span>
            <span class="mobile-feed-status">{{ row.status || '—' }}</span>
          </div>
          <div class="mobile-feed-main">{{ row.name || '—' }}</div>
          <div class="mobile-feed-meta">
            <span>{{ row.classDate || '—' }}</span>
            <span v-if="row.timeText">{{ row.timeText }}</span>
            <span v-if="!campusFiltered && row.campusName">{{ row.campusName }}</span>
          </div>
        </div>
        <div v-if="!previewBookings.length" class="mobile-feed-empty">暂无预约记录</div>
        <div v-else-if="hasMoreBookings" class="mobile-feed-more">仅展示最近 {{ MOBILE_PREVIEW_LIMIT }} 条</div>
      </div>
      <el-table v-else :data="data.latestBookings || []" size="small">
        <el-table-column prop="nickname" label="学员" width="120" />
        <el-table-column prop="name" label="课程" />
        <el-table-column v-if="!campusFiltered" prop="campusName" label="校区" width="160" />
        <el-table-column prop="classDate" label="日期" width="120" />
        <el-table-column prop="timeText" label="时间" width="140" />
        <el-table-column prop="status" label="状态" width="100" />
      </el-table>
    </div>
    <div class="page-card">
      <div class="toolbar">
        <h3>最近报名<span v-if="campusFiltered" class="scope-tip">（本校区学员）</span></h3>
        <el-button link type="primary" @click="router.push('/applies')">查看全部</el-button>
      </div>
      <div v-if="isMobile" class="mobile-feed">
        <div v-for="(row, index) in previewApplies" :key="row.id || index" class="mobile-feed-item">
          <div class="mobile-feed-head">
            <span class="mobile-feed-title">{{ row.nickname || '—' }}</span>
            <el-tag size="small" :type="applyStatusTagType(row.status)">{{ applyStatusLabelOf(row.status) }}</el-tag>
          </div>
          <div class="mobile-feed-main">{{ row.title || '—' }}</div>
          <div class="mobile-feed-meta">
            <span>{{ trackLabelOf(row.trackKey) }}</span>
          </div>
        </div>
        <div v-if="!previewApplies.length" class="mobile-feed-empty">暂无报名记录</div>
        <div v-else-if="hasMoreApplies" class="mobile-feed-more">仅展示最近 {{ MOBILE_PREVIEW_LIMIT }} 条</div>
      </div>
      <el-table v-else :data="data.latestApplies || []" size="small">
        <el-table-column prop="nickname" label="学员" width="120" />
        <el-table-column prop="title" label="机会" />
        <el-table-column label="机会类型" width="100">
          <template #default="{ row }">{{ trackLabelOf(row.trackKey) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="applyStatusTagType(row.status)">{{ applyStatusLabelOf(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'
import { applyStatusLabelOf, applyStatusTagType, trackLabelOf } from '../common/growth'

const MOBILE_PREVIEW_LIMIT = 5

const router = useRouter()
const data = reactive({})
const { isMobile } = useBreakpoint()

const previewBookings = computed(() => {
  const list = data.latestBookings || []
  return isMobile.value ? list.slice(0, MOBILE_PREVIEW_LIMIT) : list
})

const previewApplies = computed(() => {
  const list = data.latestApplies || []
  return isMobile.value ? list.slice(0, MOBILE_PREVIEW_LIMIT) : list
})

const hasMoreBookings = computed(
  () => isMobile.value && (data.latestBookings || []).length > MOBILE_PREVIEW_LIMIT,
)

const hasMoreApplies = computed(
  () => isMobile.value && (data.latestApplies || []).length > MOBILE_PREVIEW_LIMIT,
)

async function load() {
  const res = await http.get('/admin/dashboard', { params: campusParams() })
  Object.assign(data, res.data || {})
}

const { campusFiltered, campusParams } = useCampusScope(load)
</script>

<style scoped>
.dashboard-section {
  margin-bottom: 10px;
}

.scope-tip {
  display: block;
  margin-left: 0;
  margin-top: 2px;
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

@media (min-width: 769px) {
  .scope-tip {
    display: inline;
    margin-left: 8px;
    margin-top: 0;
    font-size: 13px;
  }
}
</style>
