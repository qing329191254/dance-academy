<template>
  <div class="bookings-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索学员/课程" clearable @keyup.enter="search" @clear="search" />
        <el-select v-model="status" placeholder="状态" clearable @change="search">
          <el-option label="待上课" value="待上课" />
          <el-option label="排队中" value="排队中" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已取消" value="已取消" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
      <el-button type="primary" class="toolbar-add" @click="openCreate">帮学员预约</el-button>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div v-for="row in list" :key="row.id" class="mobile-feed-item">
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.nickname || '—' }}</span>
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
        </div>
        <div class="mobile-feed-main">{{ row.name || '—' }}</div>
        <div class="mobile-feed-meta">
          <span>{{ campusName(row.campusId) }}</span>
          <span v-if="row.classDate">{{ row.classDate }}</span>
          <span v-if="row.timeText">{{ row.timeText }}</span>
        </div>
        <div class="mobile-feed-meta">
          <span v-if="row.teacherName">{{ row.teacherName }}</span>
          <span v-if="row.room">{{ row.room }}</span>
          <span :class="row.checkedIn ? 'checked' : 'muted'">{{ row.checkedIn ? '已签到' : '未签到' }}</span>
        </div>
        <div class="table-actions">
          <el-button
            v-if="row.status === '待上课' && !row.checkedIn"
            link
            type="primary"
            @click="manualCheckin(row)"
          >
            手动签到
          </el-button>
          <el-button v-if="row.status === '待上课'" link type="primary" @click="completeBooking(row)">完成</el-button>
          <el-button
            v-if="row.status === '待上课' || row.status === '排队中'"
            link
            type="danger"
            @click="cancelBooking(row)"
          >
            取消预约
          </el-button>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无预约</div>
    </div>

    <el-table v-else :data="list">
      <el-table-column prop="nickname" label="学员" width="120" align="left" header-align="left" />
      <el-table-column label="校区" width="120">
        <template #default="{ row }">{{ campusName(row.campusId) }}</template>
      </el-table-column>
      <el-table-column prop="name" label="课程" align="left" header-align="left" />
      <el-table-column prop="classDate" label="日期" width="120" align="left" header-align="left">
        <template #default="{ row }">{{ row.classDate || '-' }}</template>
      </el-table-column>
      <el-table-column prop="timeText" label="时间" width="140" align="left" header-align="left" />
      <el-table-column prop="teacherName" label="老师" width="100" align="left" header-align="left" />
      <el-table-column prop="room" label="教室" width="130" align="left" header-align="left" />
      <el-table-column label="状态" width="100" align="left" header-align="left">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签到" width="80" align="left" header-align="left">
        <template #default="{ row }">
          <span v-if="row.checkedIn" class="checked">已签到</span>
          <span v-else class="muted">未签到</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button
              v-if="row.status === '待上课' && !row.checkedIn"
              link
              type="primary"
              @click="manualCheckin(row)"
            >
              手动签到
            </el-button>
            <el-button
              v-if="row.status === '待上课'"
              link
              type="primary"
              @click="completeBooking(row)"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status === '待上课' || row.status === '排队中'"
              link
              type="danger"
              @click="cancelBooking(row)"
            >
              取消预约
            </el-button>
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

  <el-dialog v-model="createVisible" title="帮学员预约" width="520px">
    <el-form :model="createForm" label-width="90px">
      <el-form-item label="学员">
        <el-select
          v-model="createForm.userId"
          filterable
          remote
          reserve-keyword
          placeholder="搜索姓名"
          :remote-method="searchUsers"
          :loading="userLoading"
          style="width: 100%"
        >
          <el-option
            v-for="item in userOptions"
            :key="item.id"
            :label="`${item.nickname || '未命名'}（ID ${item.id}）`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="campusOptions.length > 1" label="校区">
        <el-select v-model="createForm.campusId" placeholder="选择校区" style="width: 100%" @change="onCampusChange">
          <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="课程">
        <el-select v-model="createForm.scheduleId" placeholder="选择课程" style="width: 100%" @change="onScheduleChange">
          <el-option
            v-for="item in scheduleOptions"
            :key="item.id"
            :label="`${item.name} · ${item.timeText}${item.type === 'group' ? '' : ` · ${typeLabel[item.type] || item.type}`}`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="selectedSchedule?.type === 'group'" label="上课日期">
        <el-date-picker
          v-model="createForm.classDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" :loading="creating" @click="submitCreate">确认预约</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { allowedCampuses } from '../common/adminAccess'
import { campusName } from '../common/campuses'
import { useAuthStore } from '../stores/auth'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'

const auth = useAuthStore()
const { isMobile } = useBreakpoint()
const campusOptions = computed(() => allowedCampuses(auth.profile))
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')
const status = ref('')
const createVisible = ref(false)
const creating = ref(false)
const userLoading = ref(false)
const userOptions = ref([])
const scheduleOptions = ref([])
const createForm = reactive({
  userId: null,
  campusId: '',
  scheduleId: null,
  classDate: '',
})

const typeLabel = {
  group: '团课',
  fixed: '固定班',
  private: '私教',
}

const selectedSchedule = computed(() => scheduleOptions.value.find((item) => item.id === createForm.scheduleId) || null)

function statusTagType(value) {
  if (value === '待上课') return 'success'
  if (value === '排队中') return 'warning'
  if (value === '已完成') return 'info'
  return 'default'
}

function search() {
  page.value = 1
  return load()
}

async function load() {
  const params = { keyword: keyword.value, status: status.value, page: page.value, size, ...campusParams() }
  const res = await http.get('/admin/bookings', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

const { campusId, campusParams } = useCampusScope(load)

async function searchUsers(query) {
  if (!query) {
    userOptions.value = []
    return
  }
  userLoading.value = true
  try {
    const res = await http.get('/admin/users', { params: { keyword: query, page: 1, size: 20 } })
    userOptions.value = res.data.list || []
  } finally {
    userLoading.value = false
  }
}

async function loadSchedules() {
  const params = {}
  if (createForm.campusId) params.campusId = createForm.campusId
  const res = await http.get('/admin/schedules', { params })
  scheduleOptions.value = (res.data || []).filter((item) => item.enabled !== false)
}

function onCampusChange() {
  createForm.scheduleId = null
  createForm.classDate = ''
  loadSchedules()
}

function onScheduleChange() {
  createForm.classDate = ''
}

function openCreate() {
  Object.assign(createForm, {
    userId: null,
    campusId: campusId.value || campusOptions.value[0]?.id || '',
    scheduleId: null,
    classDate: '',
  })
  userOptions.value = []
  createVisible.value = true
  loadSchedules()
}

async function submitCreate() {
  if (!createForm.userId) {
    ElMessage.warning('请选择学员')
    return
  }
  if (!createForm.scheduleId) {
    ElMessage.warning('请选择课程')
    return
  }
  if (selectedSchedule.value?.type === 'group' && !createForm.classDate) {
    ElMessage.warning('请选择上课日期')
    return
  }
  creating.value = true
  try {
    await http.post('/admin/bookings', {
      userId: createForm.userId,
      scheduleId: createForm.scheduleId,
      classDate: createForm.classDate || '',
    })
    createVisible.value = false
    ElMessage.success('预约成功')
    await load()
  } finally {
    creating.value = false
  }
}

async function cancelBooking(row) {
  await ElMessageBox.confirm(
    `确认取消「${row.nickname || '学员'}」的预约：${row.name}${row.classDate ? `（${row.classDate}）` : ''}？`,
    '取消预约',
    { type: 'warning' },
  )
  await http.post(`/admin/bookings/${row.id}/cancel`)
  ElMessage.success('已取消预约')
  await load()
}

async function completeBooking(row) {
  await ElMessageBox.confirm(`确认将「${row.name}」标记为已完成？`, '完成课程')
  await http.put(`/admin/bookings/${row.id}`, { status: '已完成' })
  ElMessage.success('已更新')
  await load()
}

async function manualCheckin(row) {
  if (!row.classDate) {
    ElMessage.warning('该课程缺少上课日期，无法签到')
    return
  }
  await http.post('/admin/checkin/manual', {
    userId: row.userId,
    scheduleId: row.scheduleId,
    classDate: row.classDate,
  })
  ElMessage.success('已手动签到')
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

@media (max-width: 768px) {
  .filters :deep(.el-input),
  .filters :deep(.el-select) {
    width: 100% !important;
  }

  .toolbar-add {
    width: 100%;
    margin-left: 0 !important;
  }
}

.checked {
  color: #67c23a;
}

.muted {
  color: #909399;
}
</style>
