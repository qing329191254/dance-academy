<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索名称 / 老师 / 教室 / 时间"
          style="width: 260px"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="type" placeholder="类型" clearable style="width: 130px" @change="search">
          <el-option label="团课" value="group" />
          <el-option label="固定班" value="fixed" />
          <el-option label="私教" value="private" />
        </el-select>
        <el-select v-model="campusId" placeholder="校区" clearable style="width: 200px" @change="search">
          <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="enabled" placeholder="启用状态" clearable style="width: 140px" @change="search">
          <el-option label="启用" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
      <el-button type="primary" @click="edit()">新增课表</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="type" label="类型" width="90" align="left" header-align="left">
        <template #default="{ row }">{{ typeLabel[row.type] || row.type }}</template>
      </el-table-column>
      <el-table-column label="校区" width="180">
        <template #default="{ row }">{{ campusName(row.campusId) }}</template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="timeText" label="时间" width="150" />
      <el-table-column prop="teacherName" label="老师" width="100" />
      <el-table-column prop="room" label="教室" width="130" />
      <el-table-column label="星期" width="90">
        <template #default="{ row }">{{ weekdayLabel[row.weekday] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="capacity" label="名额" width="80" />
      <el-table-column label="闭门" width="100">
        <template #default="{ row }">
          <span v-if="row.closedDoor">{{ closedClassGroupLabel(row.audienceGroup) }}</span>
          <span v-else class="muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="200" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="primary" @click="showQr(row)">签到码</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top: 16px"
      background
      layout="total, sizes, prev, pager, next"
      :total="total"
      v-model:current-page="page"
      v-model:page-size="size"
      :page-sizes="[10, 15, 30]"
      @current-change="load"
      @size-change="search"
    />
  </div>

  <el-dialog v-model="visible" :title="form.id ? '编辑课表' : '新增课表'" width="560px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="类型">
        <el-select v-model="form.type">
          <el-option label="团课" value="group" />
          <el-option label="固定班" value="fixed" />
          <el-option label="私教" value="private" />
        </el-select>
      </el-form-item>
      <el-form-item label="校区">
        <el-select v-model="form.campusId">
          <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="时间"><el-input v-model="form.timeText" placeholder="16:00-17:15" /></el-form-item>
      <el-form-item label="老师">
        <el-select v-model="form.teacherId" filterable @change="onTeacher">
          <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="教室"><el-input v-model="form.room" /></el-form-item>
      <el-form-item v-if="form.type === 'group'" label="星期">
        <el-select v-model="form.weekday">
          <el-option v-for="(label, value) in weekdayLabel" :key="value" :label="label" :value="Number(value)" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.type === 'group'" label="闭门课">
        <el-switch v-model="form.closedDoor" />
      </el-form-item>
      <el-form-item v-if="form.type === 'group' && form.closedDoor" label="面向分组">
        <el-select v-model="form.audienceGroup" placeholder="选择分组">
          <el-option label="高潜闭门（跳得好）" value="advanced" />
          <el-option label="基础闭门（需补基础）" value="foundation" />
        </el-select>
      </el-form-item>
      <el-form-item label="星级"><el-input-number v-model="form.stars" :min="1" :max="5" /></el-form-item>
      <el-form-item label="名额"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
      <el-form-item label="状态"><el-input v-model="form.status" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="qrVisible" title="现场签到码" width="420px" @closed="onQrClosed">
    <div v-loading="qrLoading" class="qr-panel">
      <img v-if="qrDataUrl" :src="qrDataUrl" alt="签到二维码" class="qr-image" />
      <div class="qr-meta">
        <strong>{{ qrMeta.className || '课程' }}</strong>
        <p>{{ qrMeta.date }} {{ qrMeta.time }}</p>
        <p>{{ [qrMeta.teacher, qrMeta.room].filter(Boolean).join(' · ') }}</p>
      </div>
      <p class="qr-hint">请工作人员当场展示此码供学员/老师/员工扫描。扫码后需在「待确认签到」或员工小程序确认到场；二维码约 60 秒自动刷新，请勿截图转发。</p>
    </div>
    <template #footer>
      <el-button @click="qrVisible = false">关闭</el-button>
      <el-button type="primary" :disabled="!qrDataUrl" @click="downloadQr">下载当前二维码</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QRCode from 'qrcode'
import http from '../api/http'
import { campusName } from '../common/campuses'
import { closedClassGroupLabel } from '../common/closedClass'
import { allowedCampuses, defaultCampusId } from '../common/adminAccess'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const campusOptions = computed(() => allowedCampuses(auth.profile))

const typeLabel = { group: '团课', fixed: '固定班', private: '私教' }
const weekdayLabel = { 0: '周日', 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六' }

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const type = ref('')
const campusId = ref('')
const enabled = ref()
const teachers = ref([])
const visible = ref(false)
const qrVisible = ref(false)
const qrLoading = ref(false)
const qrDataUrl = ref('')
const qrSessionId = ref(null)
let qrRefreshTimer = null
const qrMeta = reactive({ className: '', date: '', time: '', teacher: '', room: '' })
const form = reactive({})
const original = reactive({ timeText: '', weekday: null, type: '' })

function queryParams() {
  const params = { keyword: keyword.value, page: page.value, size: size.value }
  if (type.value) params.type = type.value
  if (campusId.value) params.campusId = campusId.value
  if (enabled.value === true || enabled.value === false) params.enabled = enabled.value
  return params
}

async function load() {
  const res = await http.get('/admin/schedules', { params: queryParams() })
  list.value = res.data?.list || []
  total.value = res.data?.total || 0
}

async function loadTeachers() {
  teachers.value = (await http.get('/admin/teachers')).data || []
}

function search() {
  page.value = 1
  return load()
}
function edit(row) {
  const fallbackCampus = defaultCampusId(auth.profile) || campusOptions.value[0]?.id || 'shizishan'
  Object.assign(form, {
    id: null, type: 'group', campusId: fallbackCampus, name: '', timeText: '', teacherId: null, teacherName: '',
    room: '', weekday: 1, stars: 3, capacity: 20, status: '可预约', sortOrder: 0, enabled: true,
    closedDoor: false, audienceGroup: '',
  }, row || {})
  if (!form.closedDoor) {
    form.closedDoor = false
    form.audienceGroup = ''
  }
  Object.assign(original, {
    timeText: form.timeText || '',
    weekday: form.weekday,
    type: form.type || '',
  })
  visible.value = true
}
function onTeacher(id) {
  const t = teachers.value.find((x) => x.id === id)
  form.teacherName = t ? t.name : ''
}
async function save() {
  if (form.type === 'group' && form.closedDoor && !form.audienceGroup) {
    ElMessage.warning('请选择闭门课面向分组')
    return
  }
  if (form.id && form.type === 'group') {
    const timeChanged = String(form.timeText || '') !== String(original.timeText || '')
    const weekdayChanged = Number(form.weekday) !== Number(original.weekday)
    if (timeChanged || weekdayChanged) {
      const res = await http.get(`/admin/schedules/${form.id}/pending-count`)
      const n = Number(res.data?.count || 0)
      if (n > 0) {
        try {
          await ElMessageBox.confirm(
            `该团课已有 ${n} 人预约。改时间或星期后，学员预约记录和课前提醒仍按原时间，不会自动重发。请自行通知学员。`,
            '提示',
            { type: 'warning', confirmButtonText: '仍要保存', cancelButtonText: '取消' },
          )
        } catch {
          return
        }
      }
    }
  }
  if (form.id) await http.put(`/admin/schedules/${form.id}`, form)
  else await http.post('/admin/schedules', form)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除该课表？', '提示')
  await http.delete(`/admin/schedules/${row.id}`)
  ElMessage.success('已删除')
  if (list.value.length === 1 && page.value > 1) page.value -= 1
  await load()
}
function formatToday() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function stopQrRefresh() {
  if (qrRefreshTimer) {
    clearInterval(qrRefreshTimer)
    qrRefreshTimer = null
  }
  qrSessionId.value = null
}

async function refreshQrPayload() {
  if (!qrSessionId.value) return
  try {
    const res = await http.get(`/admin/checkin-sessions/${qrSessionId.value}/payload`)
    const payload = res.data.payload
    Object.assign(qrMeta, res.data.text || {})
    qrDataUrl.value = res.data.qrDataUrl || await QRCode.toDataURL(payload, {
      width: 320,
      margin: 2,
      errorCorrectionLevel: 'M',
      color: { dark: '#16161c', light: '#ffffff' },
    })
  } catch {
    qrDataUrl.value = ''
  }
}

function onQrClosed() {
  stopQrRefresh()
  if (qrSessionId.value) {
    http.post(`/admin/checkin-sessions/${qrSessionId.value}/close`).catch(() => {})
  }
}

async function showQr(row) {
  stopQrRefresh()
  qrDataUrl.value = ''
  Object.assign(qrMeta, {
    className: row.name || '',
    date: formatToday(),
    time: row.timeText || '',
    teacher: row.teacherName || '',
    room: row.room || '',
  })
  qrVisible.value = true
  qrLoading.value = true
  try {
    const res = await http.post('/admin/checkin-sessions', {
      scheduleId: row.id,
      classDate: qrMeta.date,
    })
    qrSessionId.value = res.data.id
    Object.assign(qrMeta, res.data)
    await refreshQrPayload()
    qrRefreshTimer = setInterval(refreshQrPayload, 45000)
  } catch {
    qrVisible.value = false
  } finally {
    qrLoading.value = false
  }
}
function downloadQr() {
  if (!qrDataUrl.value) return
  const safe = String(qrMeta.className || '课程').replace(/[\\/:*?"<>|]/g, '_')
  const link = document.createElement('a')
  link.href = qrDataUrl.value
  link.download = `签到码-${safe}.png`
  link.click()
}
onMounted(() => {
  campusId.value = defaultCampusId(auth.profile)
  loadTeachers()
  load()
})
onUnmounted(stopQrRefresh)
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}
.qr-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  min-height: 280px;
}
.qr-image {
  width: 260px;
  height: 260px;
  background: #fff;
}
.qr-meta {
  margin-top: 12px;
  color: #16161c;
}
.qr-meta p {
  margin: 4px 0 0;
  color: #6b6b76;
  font-size: 13px;
}
.qr-hint {
  margin: 14px 0 0;
  color: #8a8a96;
  font-size: 13px;
  line-height: 1.5;
}
.muted {
  color: #8a8a96;
}
</style>
