<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索姓名/微信ID" style="width: 260px" clearable @keyup.enter="load" />
        <el-button @click="load">查询</el-button>
      </div>
    </div>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="70" align="left" header-align="left" />
      <el-table-column prop="nickname" label="真实姓名" width="120" align="left" header-align="left" />
      <el-table-column label="角色" width="100" align="left" header-align="left">
        <template #default="{ row }">
          {{ roleLabel(row.role) }}
        </template>
      </el-table-column>
      <el-table-column label="校区" width="160">
        <template #default="{ row }">{{ row.campusId ? campusName(row.campusId) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="130" align="left" header-align="left" />
      <el-table-column prop="school" label="学校" width="140" align="left" header-align="left" />
      <el-table-column prop="collegeGrade" label="学院年级" width="140" align="left" header-align="left" />
      <el-table-column prop="gender" label="性别" width="80" align="left" header-align="left" />
      <el-table-column prop="birthday" label="生日" width="120" align="left" header-align="left" />
      <el-table-column prop="workLevel" label="勤工等级" width="100" align="left" header-align="left" />
      <el-table-column prop="workStage" label="勤工阶段" width="100" align="left" header-align="left" />
      <el-table-column prop="danceLevel" label="舞蹈等级" width="100" align="left" header-align="left" />
      <el-table-column prop="danceStage" label="舞蹈阶段" width="100" align="left" header-align="left" />
      <el-table-column prop="openid" label="微信ID" align="left" header-align="left" />
      <el-table-column label="操作" width="120" align="left" header-align="left">
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

  <el-dialog v-model="visible" title="编辑学员" width="520px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="真实姓名"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.role">
          <el-option label="学员" value="student" />
          <el-option label="老师" value="teacher" />
          <el-option label="员工" value="employee" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.role === 'teacher'" label="绑定老师">
        <el-select v-model="form.teacherId" placeholder="选择老师档案" clearable>
          <el-option v-for="item in teachers" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.role === 'employee'" label="所属校区">
        <el-select v-model="form.campusId" placeholder="选择校区">
          <el-option v-for="item in CAMPUSES" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.role === 'employee'" label="岗位名称"><el-input v-model="form.jobTitle" /></el-form-item>
      <el-form-item v-if="form.role === 'employee'" label="岗位职责"><el-input v-model="form.jobDescription" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" maxlength="11" /></el-form-item>
      <el-form-item label="学校"><el-input v-model="form.school" /></el-form-item>
      <el-form-item label="学院年级"><el-input v-model="form.collegeGrade" /></el-form-item>
      <el-form-item label="性别">
        <el-select v-model="form.gender" placeholder="请选择" clearable>
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="生日">
        <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择生日" />
      </el-form-item>
      <el-form-item label="勤工等级">
        <el-select v-model="form.workLevel">
          <el-option label="T1" value="T1" /><el-option label="T2" value="T2" /><el-option label="T3" value="T3" />
        </el-select>
      </el-form-item>
      <el-form-item label="勤工阶段"><el-input v-model="form.workStage" /></el-form-item>
      <el-form-item label="舞蹈等级">
        <el-select v-model="form.danceLevel">
          <el-option label="T1" value="T1" /><el-option label="T2" value="T2" /><el-option label="T3" value="T3" />
        </el-select>
      </el-form-item>
      <el-form-item label="舞蹈阶段"><el-input v-model="form.danceStage" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { CAMPUSES, campusName } from '../common/campuses'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')
const visible = ref(false)
const teachers = ref([])
const form = reactive({})

function roleLabel(role) {
  if (role === 'teacher') return '老师'
  if (role === 'employee') return '员工'
  return '学员'
}

async function loadTeachers() {
  const res = await http.get('/admin/teachers')
  teachers.value = res.data || []
}

async function load() {
  const res = await http.get('/admin/users', { params: { keyword: keyword.value, page: page.value, size } })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}
function edit(row) {
  Object.assign(form, row)
  if (!form.role) form.role = 'student'
  visible.value = true
}
async function save() {
  if (form.role === 'teacher' && !form.teacherId) {
    ElMessage.warning('请为老师账号绑定老师档案')
    return
  }
  if (form.role === 'employee' && !form.campusId) {
    ElMessage.warning('请为员工选择所属校区')
    return
  }
  if (form.role !== 'teacher') {
    form.teacherId = null
  }
  if (form.role !== 'employee') {
    form.campusId = null
    form.jobTitle = ''
    form.jobDescription = ''
  }
  await http.put(`/admin/users/${form.id}`, form)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}
onMounted(async () => {
  await loadTeachers()
  await load()
})
</script>
