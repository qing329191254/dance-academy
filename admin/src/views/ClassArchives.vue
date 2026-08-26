<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索课程/老师" style="width: 240px" clearable @keyup.enter="load" />
        <el-select v-model="teacherId" placeholder="老师" clearable style="width: 180px" @change="load">
          <el-option v-for="item in teachers" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-if="campusOptions.length > 1" v-model="campusId" placeholder="校区" clearable style="width: 200px" @change="search">
          <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button @click="load">查询</el-button>
      </div>
    </div>
    <el-table :data="list">
      <el-table-column prop="teacherName" label="老师" width="120" align="left" header-align="left" />
      <el-table-column label="校区" width="180">
        <template #default="{ row }">{{ campusName(row.campusId) }}</template>
      </el-table-column>
      <el-table-column prop="name" label="课程" align="left" header-align="left" />
      <el-table-column prop="classDate" label="日期" width="120" align="left" header-align="left" />
      <el-table-column prop="timeText" label="时间" width="140" align="left" header-align="left" />
      <el-table-column prop="room" label="教室" width="130" align="left" header-align="left" />
      <el-table-column prop="bookedCount" label="预约" width="80" align="left" header-align="left" />
      <el-table-column prop="checkedInCount" label="到课" width="80" align="left" header-align="left" />
      <el-table-column prop="renewalRate" label="续报率" width="100" />
      <el-table-column label="操作" width="90" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
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

  <el-dialog v-model="visible" title="编辑课程档案" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="续报率"><el-input v-model="form.renewalRate" placeholder="如 85%" /></el-form-item>
      <el-form-item label="学员反馈"><el-input v-model="form.studentFeedback" type="textarea" :rows="5" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.note" type="textarea" :rows="3" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { campusName } from '../common/campuses'
import { allowedCampuses, defaultCampusId } from '../common/adminAccess'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const campusOptions = computed(() => allowedCampuses(auth.profile))
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')
const teacherId = ref()
const campusId = ref('')
const teachers = ref([])
const visible = ref(false)
const form = reactive({})

function edit(row) {
  Object.assign(form, {
    id: row.id,
    studentFeedback: row.studentFeedback || '',
    renewalRate: row.renewalRate || '',
    note: row.note || '',
  })
  visible.value = true
}

async function save() {
  await http.put(`/admin/class-archives/${form.id}`, {
    studentFeedback: form.studentFeedback,
    renewalRate: form.renewalRate,
    note: form.note,
  })
  visible.value = false
  ElMessage.success('已保存')
  await load()
}

function formatTime(value) {
  if (!value) return ''
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
  const params = {
    keyword: keyword.value,
    teacherId: teacherId.value,
    page: page.value,
    size,
  }
  if (campusId.value) params.campusId = campusId.value
  const res = await http.get('/admin/class-archives', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

onMounted(async () => {
  campusId.value = defaultCampusId(auth.profile)
  await loadTeachers()
  await load()
})
</script>
