<template>
  <div>
    <el-alert
      v-if="!campusId"
      type="warning"
      :closable="false"
      show-icon
      class="campus-hint"
      title="请先在顶部选择校区，再管理该校区教室"
    />
    <el-alert
      v-else
      type="info"
      :closable="false"
      show-icon
      class="campus-hint"
      :title="`正在管理：${campusLabel}`"
    />

    <el-tabs v-model="tab" class="page-tabs">
      <el-tab-pane label="教室列表" name="rooms">
        <div class="page-card">
          <div class="toolbar">
            <el-button type="primary" :disabled="!campusId" @click="editRoom()">新增教室</el-button>
          </div>
          <el-table :data="rooms">
            <el-table-column prop="name" label="教室名称" min-width="140" />
            <el-table-column prop="shortName" label="简称" width="120" />
            <el-table-column label="练舞" width="80">
              <template #default="{ row }">{{ row.allowPractice ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column label="租赁" width="80">
              <template #default="{ row }">{{ row.allowRental ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column label="时段" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">{{ slotSummary(row) }}</template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="启用" width="80">
              <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="140" class-name="col-actions" align="left" header-align="left">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" @click="editRoom(row)">编辑</el-button>
                  <el-button link type="danger" @click="removeRoom(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="租赁登记" name="rentals">
        <div class="page-card">
          <div class="toolbar">
            <el-button type="primary" :disabled="!campusId" @click="editRental()">新增租赁占用</el-button>
          </div>
          <el-table :data="rentals">
            <el-table-column prop="classDate" label="日期" width="120" />
            <el-table-column prop="classroomName" label="教室" width="140" />
            <el-table-column label="时段" width="140">
              <template #default="{ row }">{{ row.startTime }}-{{ row.endTime }}</template>
            </el-table-column>
            <el-table-column prop="contactName" label="联系人" width="120" />
            <el-table-column prop="phone" label="电话" width="130" />
            <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">{{ row.status === 'cancelled' ? '已取消' : '已确认' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="140" class-name="col-actions" align="left" header-align="left">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" @click="editRental(row)">编辑</el-button>
                  <el-button v-if="row.status !== 'cancelled'" link type="danger" @click="cancelRental(row)">取消</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="练舞预约" name="bookings">
        <div class="page-card">
          <div class="toolbar">
            <div class="filters">
              <el-input v-model="bookingKeyword" placeholder="搜索姓名" style="width: 200px" clearable @keyup.enter="searchBookings" />
              <el-select v-model="bookingStatus" placeholder="全部状态" clearable style="width: 140px" @change="searchBookings">
                <el-option label="待审核" value="pending" />
                <el-option label="已同意" value="approved" />
                <el-option label="已拒绝" value="rejected" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
              <el-button @click="searchBookings">查询</el-button>
            </div>
          </div>
          <el-table :data="bookings">
            <el-table-column prop="name" label="姓名" width="110" />
            <el-table-column prop="classroomName" label="教室" width="140" />
            <el-table-column prop="classDate" label="日期" width="120" />
            <el-table-column prop="timeText" label="时段" width="130" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">{{ bookingStatusLabel(row.status) }}</template>
            </el-table-column>
            <el-table-column prop="rejectReason" label="备注" min-width="140" show-overflow-tooltip />
            <el-table-column label="操作" width="140" class-name="col-actions" align="left" header-align="left">
              <template #default="{ row }">
                <div class="table-actions">
                  <template v-if="row.status === 'pending'">
                    <el-button link type="primary" @click="approveBooking(row)">同意</el-button>
                    <el-button link type="danger" @click="rejectBooking(row)">拒绝</el-button>
                  </template>
                  <span v-else class="muted">-</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            style="margin-top: 16px"
            background
            layout="total, prev, pager, next"
            :total="bookingTotal"
            v-model:current-page="bookingPage"
            :page-size="bookingSize"
            @current-change="loadBookings"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>

  <el-dialog v-model="roomVisible" :title="roomForm.id ? '编辑教室' : '新增教室'" width="640px">
    <el-form :model="roomForm" label-width="100px">
      <el-form-item label="教室名称"><el-input v-model="roomForm.name" maxlength="80" /></el-form-item>
      <el-form-item label="简称"><el-input v-model="roomForm.shortName" maxlength="40" placeholder="可选" /></el-form-item>
      <el-form-item label="允许练舞"><el-switch v-model="roomForm.allowPractice" /></el-form-item>
      <el-form-item label="允许租赁"><el-switch v-model="roomForm.allowRental" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="roomForm.enabled" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="roomForm.sortOrder" :min="0" /></el-form-item>
      <el-form-item label="可约时段">
        <div class="slot-list">
          <div v-for="(slot, index) in roomForm.slots" :key="index" class="slot-row">
            <el-time-select v-model="slot.startTime" start="08:00" step="00:30" end="23:00" placeholder="开始" style="width: 120px" />
            <span>-</span>
            <el-time-select v-model="slot.endTime" start="08:00" step="00:30" end="23:30" placeholder="结束" style="width: 120px" />
            <el-switch v-model="slot.enabled" active-text="启用" />
            <el-button link type="danger" @click="roomForm.slots.splice(index, 1)">删除</el-button>
          </div>
          <el-button @click="addSlot">新增时段</el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="roomVisible = false">取消</el-button>
      <el-button type="primary" @click="saveRoom">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="rentalVisible" :title="rentalForm.id ? '编辑租赁占用' : '新增租赁占用'" width="520px">
    <el-form :model="rentalForm" label-width="90px">
      <el-form-item label="教室">
        <el-select v-model="rentalForm.classroomId" placeholder="选择教室" style="width: 100%">
          <el-option v-for="item in rentalRooms" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="rentalForm.classDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
      </el-form-item>
      <el-form-item label="开始">
        <el-time-select v-model="rentalForm.startTime" start="08:00" step="00:30" end="23:00" placeholder="开始" style="width: 100%" />
      </el-form-item>
      <el-form-item label="结束">
        <el-time-select v-model="rentalForm.endTime" start="08:00" step="00:30" end="23:30" placeholder="结束" style="width: 100%" />
      </el-form-item>
      <el-form-item label="联系人"><el-input v-model="rentalForm.contactName" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="rentalForm.phone" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="rentalForm.remark" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="rentalVisible = false">取消</el-button>
      <el-button type="primary" @click="saveRental">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { campusName } from '../common/campuses'
import { useCampusScope } from '../composables/useCampusScope'

const tab = ref('rooms')
const rooms = ref([])
const rentals = ref([])
const bookings = ref([])
const bookingTotal = ref(0)
const bookingPage = ref(1)
const bookingSize = 20
const bookingKeyword = ref('')
const bookingStatus = ref('pending')
const roomVisible = ref(false)
const rentalVisible = ref(false)
const roomForm = reactive(emptyRoom())
const rentalForm = reactive(emptyRental())

const campusLabel = computed(() => campusName(campusId.value))
const rentalRooms = computed(() => rooms.value.filter((item) => item.allowRental !== false))

function emptyRoom() {
  return {
    id: null,
    name: '',
    shortName: '',
    allowPractice: true,
    allowRental: true,
    enabled: true,
    sortOrder: 0,
    slots: [],
  }
}

function emptyRental() {
  return {
    id: null,
    classroomId: null,
    classDate: '',
    startTime: '19:00',
    endTime: '21:00',
    contactName: '',
    phone: '',
    remark: '',
  }
}

function slotSummary(row) {
  const slots = (row.slots || []).filter((item) => item.enabled !== false)
  if (!slots.length) return '-'
  return slots.map((item) => `${item.startTime}-${item.endTime}`).join('、')
}

function bookingStatusLabel(status) {
  return { pending: '待审核', approved: '已同意', rejected: '已拒绝', cancelled: '已取消' }[status] || status
}

async function loadRooms() {
  if (!campusId.value) {
    rooms.value = []
    return
  }
  const res = await http.get('/admin/classrooms', { params: { campusId: campusId.value } })
  rooms.value = res.data || []
}

async function loadRentals() {
  if (!campusId.value) {
    rentals.value = []
    return
  }
  const res = await http.get('/admin/room-rentals', { params: { campusId: campusId.value } })
  rentals.value = res.data || []
}

async function loadBookings() {
  if (!campusId.value) {
    bookings.value = []
    bookingTotal.value = 0
    return
  }
  const res = await http.get('/admin/practice-room-bookings', {
    params: {
      campusId: campusId.value,
      status: bookingStatus.value || '',
      keyword: bookingKeyword.value,
      page: bookingPage.value,
      size: bookingSize,
    },
  })
  bookings.value = res.data?.list || []
  bookingTotal.value = res.data?.total || 0
}

async function load() {
  await Promise.all([loadRooms(), loadRentals(), loadBookings()])
}

function searchBookings() {
  bookingPage.value = 1
  return loadBookings()
}

function editRoom(row) {
  Object.assign(roomForm, emptyRoom(), row || {}, {
    slots: (row?.slots || []).map((item) => ({ ...item })),
  })
  roomVisible.value = true
}

function addSlot() {
  roomForm.slots.push({ startTime: '19:00', endTime: '21:00', enabled: true, sortOrder: roomForm.slots.length })
}

async function saveRoom() {
  if (!roomForm.name?.trim()) {
    ElMessage.warning('请填写教室名称')
    return
  }
  const payload = {
    ...roomForm,
    name: roomForm.name.trim(),
    shortName: roomForm.shortName?.trim() || '',
    slots: roomForm.slots,
  }
  if (roomForm.id) await http.put(`/admin/classrooms/${roomForm.id}`, payload, { params: { campusId: campusId.value } })
  else await http.post('/admin/classrooms', payload, { params: { campusId: campusId.value } })
  roomVisible.value = false
  ElMessage.success('已保存')
  await loadRooms()
}

async function removeRoom(row) {
  await ElMessageBox.confirm(`确认删除教室「${row.name}」？`, '提示')
  await http.delete(`/admin/classrooms/${row.id}`)
  ElMessage.success('已删除')
  await loadRooms()
}

function editRental(row) {
  Object.assign(rentalForm, emptyRental(), row || {})
  rentalVisible.value = true
}

async function saveRental() {
  if (!rentalForm.classroomId || !rentalForm.classDate || !rentalForm.startTime || !rentalForm.endTime) {
    ElMessage.warning('请完整填写教室、日期和时段')
    return
  }
  const payload = { ...rentalForm }
  if (rentalForm.id) {
    await http.put(`/admin/room-rentals/${rentalForm.id}`, payload, { params: { campusId: campusId.value } })
  } else {
    await http.post('/admin/room-rentals', payload, { params: { campusId: campusId.value } })
  }
  rentalVisible.value = false
  ElMessage.success('已保存')
  await loadRentals()
}

async function cancelRental(row) {
  await ElMessageBox.confirm('确认取消该租赁占用？取消后该时段可再被练舞预约。', '提示')
  await http.post(`/admin/room-rentals/${row.id}/cancel`)
  ElMessage.success('已取消')
  await loadRentals()
}

async function approveBooking(row) {
  await http.post(`/admin/practice-room-bookings/${row.id}/approve`)
  ElMessage.success('已同意')
  await loadBookings()
}

async function rejectBooking(row) {
  try {
    const { value } = await ElMessageBox.prompt('可填写拒绝原因', '拒绝预约', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '选填',
    })
    await http.post(`/admin/practice-room-bookings/${row.id}/reject`, { reason: value || '' })
    ElMessage.success('已拒绝')
    await loadBookings()
  } catch (e) {
    // 用户取消弹窗时不提示错误
  }
}

const { campusId } = useCampusScope(load)
</script>

<style scoped>
.campus-hint {
  margin-bottom: 16px;
}
.slot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}
.slot-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.muted {
  color: #999;
}
</style>
