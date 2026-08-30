<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索姓名/微信ID/手机号" style="width: 240px" clearable @keyup.enter="search" />
        <el-select v-model="roleFilter" placeholder="全部角色" clearable style="width: 130px" @change="onFilterChange">
          <el-option label="学员" value="student" />
          <el-option label="教师" value="teacher" />
          <el-option label="员工" value="employee" />
        </el-select>
        <el-select
          v-model="memberTagFilter"
          placeholder="全部业务标签"
          clearable
          style="width: 160px"
          @change="onFilterChange"
        >
          <el-option v-for="item in memberTagOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button @click="search">查询</el-button>
        <el-button type="primary" plain @click="openClaim">添加学员到本校区</el-button>
      </div>
    </div>
    <el-table :data="list">
      <el-table-column prop="nickname" label="姓名" width="110" align="left" header-align="left" show-overflow-tooltip />
      <el-table-column label="角色" width="90" align="left" header-align="left">
        <template #default="{ row }">
          <el-tag size="small" :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="120" align="left" header-align="left" />
      <el-table-column label="关联校区" min-width="160" align="left" header-align="left">
        <template #default="{ row }">
          <div v-if="(row.campusLabels || []).length" class="tag-list">
            <el-tag v-for="label in row.campusLabels" :key="label" size="small" effect="plain">{{ label }}</el-tag>
          </div>
          <span v-else-if="row.role === 'employee'">{{ campusName(row.campusId) }}</span>
          <span v-else-if="row.role === 'teacher'">{{ teacherName(row.teacherId) }}</span>
          <span v-else class="muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="school" label="就读学校" width="140" align="left" header-align="left" show-overflow-tooltip />
      <el-table-column prop="collegeGrade" label="学院年级" width="120" align="left" header-align="left" show-overflow-tooltip />
      <el-table-column prop="cardTypes" label="卡类" width="100" align="left" header-align="left" show-overflow-tooltip>
        <template #default="{ row }">{{ row.cardTypes || '-' }}</template>
      </el-table-column>
      <el-table-column label="业务标签" min-width="160" align="left" header-align="left">
        <template #default="{ row }">
          <div v-if="(row.memberTagLabels || []).length" class="tag-list">
            <el-tag v-for="label in row.memberTagLabels" :key="label" size="small" type="info" effect="plain">
              {{ label }}
            </el-tag>
          </div>
          <span v-else class="muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="闭门分组" width="100" align="left" header-align="left">
        <template #default="{ row }">{{ row.closedClassGroupLabel || '普通' }}</template>
      </el-table-column>
      <el-table-column label="微信ID" min-width="160" align="left" header-align="left">
        <template #default="{ row }">
          <div class="openid-cell">
            <span class="openid-text" :title="row.openid">{{ row.openid || '-' }}</span>
            <el-button v-if="row.openid" link type="primary" class="copy-btn" @click="copyText(row.openid)">复制</el-button>
          </div>
        </template>
      </el-table-column>
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

  <el-dialog v-model="visible" :title="dialogTitle" width="580px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="姓名"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.role">
          <el-option label="学员" value="student" />
          <el-option label="教师" value="teacher" />
          <el-option label="员工" value="employee" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.role === 'teacher'" label="绑定老师">
        <el-select v-model="form.teacherId" placeholder="选择老师档案" clearable>
          <el-option v-for="item in teachers" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.role === 'employee'" label="所属校区">
        <el-select v-model="form.campusId" placeholder="选择舞室校区">
          <el-option v-for="item in editableCampuses" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.role === 'employee'" label="岗位名称"><el-input v-model="form.jobTitle" /></el-form-item>
      <el-form-item v-if="form.role === 'employee'" label="岗位职责">
        <el-input v-model="form.jobDescription" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" maxlength="11" /></el-form-item>
      <el-form-item v-if="form.role === 'student' || form.role === 'employee'" label="就读学校">
        <el-input v-model="form.school" maxlength="80" placeholder="学员自填，可后台修改" />
      </el-form-item>
      <el-form-item v-if="form.role === 'student'" label="学院年级"><el-input v-model="form.collegeGrade" /></el-form-item>
      <el-form-item label="性别">
        <el-select v-model="form.gender" placeholder="请选择" clearable>
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="生日">
        <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择生日" />
      </el-form-item>
      <template v-if="form.role === 'student'">
        <el-form-item label="关联校区">
          <div class="member-tags">
            <div class="campus-hint muted">可多选，加入后与其他校区并存。管理员仅能调整自己可管校区。</div>
            <el-checkbox-group v-model="form.campusIds">
              <el-checkbox v-for="item in editableCampuses" :key="item.id" :label="item.id">
                {{ item.name }}
              </el-checkbox>
            </el-checkbox-group>
            <div v-if="otherCampusLabels.length" class="campus-hint muted" style="margin-top: 8px">
              其他校区（只读）：{{ otherCampusLabels.join('、') }}
            </div>
          </div>
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
        <el-form-item label="闭门分组">
          <el-select v-model="form.closedClassGroup" placeholder="普通学员" clearable>
            <el-option label="高阶闭门" value="advanced" />
            <el-option label="零基础闭门" value="foundation" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务标签">
          <div class="member-tags">
            <el-alert
              v-if="!campusId"
              type="warning"
              :closable="false"
              show-icon
              title="请先在顶部选择校区，再为学员标记业务标签"
            />
            <template v-else>
              <div class="campus-hint muted">作用于当前校区：{{ campusName(campusId) }}（可多选）</div>
              <el-checkbox-group v-model="form.memberTags">
                <el-checkbox v-for="item in memberTagOptions" :key="item.value" :label="item.value">
                  {{ item.label }}
                </el-checkbox>
              </el-checkbox-group>
            </template>
          </div>
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="claimVisible" title="添加学员到本校区" width="720px">
    <el-alert
      v-if="!campusId"
      type="warning"
      :closable="false"
      show-icon
      title="请先在顶部选择校区，再搜索并加入本校区"
      style="margin-bottom: 12px"
    />
    <div v-else class="claim-bar">
      <el-input
        v-model="claimKeyword"
        placeholder="输入姓名 / 手机号 / 微信 ID（至少 2 字）"
        clearable
        style="width: 360px"
        @keyup.enter="searchClaim"
      />
      <el-button type="primary" :loading="claimLoading" @click="searchClaim">搜索</el-button>
      <span class="muted">将加入：{{ campusName(campusId) }}（与其他校区并存）</span>
    </div>
    <el-table :data="claimList" style="margin-top: 12px" max-height="420">
      <el-table-column prop="nickname" label="姓名" width="110" />
      <el-table-column prop="phone" label="电话" width="120" />
      <el-table-column prop="school" label="就读学校" min-width="120" show-overflow-tooltip />
      <el-table-column label="已关联校区" min-width="160">
        <template #default="{ row }">
          {{ (row.campusLabels || []).join('、') || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="openid" label="微信ID" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="!campusId || (row.campusIds || []).includes(campusId)"
            @click="claimUser(row)"
          >
            {{ (row.campusIds || []).includes(campusId) ? '已在本校区' : '加入本校区' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { campusName } from '../common/campuses'
import { allowedCampuses } from '../common/adminAccess'
import { useCampusScope } from '../composables/useCampusScope'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref(route.query.keyword ? String(route.query.keyword) : '')
const roleFilter = ref(route.query.role ? String(route.query.role) : '')
const memberTagFilter = ref('')
const memberTagOptions = ref([])
const visible = ref(false)
const teachers = ref([])
const form = reactive({})

const claimVisible = ref(false)
const claimKeyword = ref('')
const claimList = ref([])
const claimLoading = ref(false)

const editableCampuses = computed(() => allowedCampuses(auth.profile))

const otherCampusLabels = computed(() => {
  const allowedIds = new Set(editableCampuses.value.map((item) => item.id))
  const allIds = form.campusIdsAll || []
  return allIds
    .filter((id) => !allowedIds.has(id))
    .map((id) => campusName(id))
    .filter(Boolean)
})

const dialogTitle = computed(() => {
  const map = {
    student: '编辑学员',
    teacher: '编辑教师账号',
    employee: '编辑员工',
  }
  return map[form.role] || '编辑用户'
})

function roleLabel(role) {
  const map = { student: '学员', teacher: '教师', employee: '员工' }
  return map[role] || '学员'
}

function roleTagType(role) {
  if (role === 'teacher') return 'warning'
  if (role === 'employee') return 'success'
  return ''
}

function teacherName(teacherId) {
  if (!teacherId) return '未绑定'
  return teachers.value.find((item) => item.id === teacherId)?.name || `档案#${teacherId}`
}

async function loadTeachers() {
  const res = await http.get('/admin/teachers')
  teachers.value = res.data || []
}

async function loadMemberTagOptions() {
  const res = await http.get('/admin/member-tag-options')
  memberTagOptions.value = res.data || []
}

async function load() {
  const params = {
    keyword: keyword.value,
    role: roleFilter.value || '',
    memberTag: memberTagFilter.value || '',
    page: page.value,
    size,
    ...campusParams(),
  }
  const res = await http.get('/admin/users', { params })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}

const { campusId, campusParams } = useCampusScope(load)

function onFilterChange() {
  page.value = 1
  load()
}

function search() {
  page.value = 1
  return load()
}

function edit(row) {
  Object.assign(form, row)
  if (!form.role) form.role = 'student'
  if (!form.closedClassGroup) form.closedClassGroup = null
  form.memberTags = Array.isArray(row.memberTags) ? [...row.memberTags] : []
  const allCampusIds = Array.isArray(row.campusIds) ? [...row.campusIds] : []
  form.campusIdsAll = allCampusIds
  const allowedIds = new Set(editableCampuses.value.map((item) => item.id))
  form.campusIds = allCampusIds.filter((id) => allowedIds.has(id))
  visible.value = true
}

function openClaim() {
  claimKeyword.value = ''
  claimList.value = []
  claimVisible.value = true
  if (!campusId.value) {
    ElMessage.warning('请先在顶部选择校区')
  }
}

async function searchClaim() {
  if (!campusId.value) {
    ElMessage.warning('请先在顶部选择校区')
    return
  }
  const q = claimKeyword.value.trim()
  if (q.length < 2) {
    ElMessage.warning('请输入至少 2 个字符')
    return
  }
  claimLoading.value = true
  try {
    const res = await http.get('/admin/users/search-for-claim', {
      params: { keyword: q, page: 1, size: 50 },
    })
    claimList.value = res.data?.list || []
  } finally {
    claimLoading.value = false
  }
}

async function claimUser(row) {
  if (!campusId.value) {
    ElMessage.warning('请先在顶部选择校区')
    return
  }
  await http.post(`/admin/users/${row.id}/claim-campus`, { campusId: campusId.value })
  ElMessage.success('已加入本校区')
  row.campusIds = [...(row.campusIds || []), campusId.value]
  const label = campusName(campusId.value)
  row.campusLabels = [...new Set([...(row.campusLabels || []), label])]
  await load()
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

async function save() {
  if (form.role === 'teacher' && !form.teacherId) {
    ElMessage.warning('请为教师账号绑定老师档案')
    return
  }
  if (form.role === 'employee' && !form.campusId) {
    ElMessage.warning('请为员工选择所属校区')
    return
  }
  if (form.role === 'student' && Array.isArray(form.memberTags) && form.memberTags.length && !campusId.value) {
    ElMessage.warning('请先在顶部选择校区，再保存业务标签')
    return
  }
  if (form.role !== 'teacher') {
    form.teacherId = null
  }
  const employeeCampusId = form.role === 'employee' ? form.campusId : null
  if (form.role !== 'employee') {
    form.jobTitle = ''
    form.jobDescription = ''
  }
  await http.put(`/admin/users/${form.id}`, {
    ...form,
    campusId: employeeCampusId,
    campusIds: form.role === 'student' ? (form.campusIds || []) : undefined,
    closedClassGroup: form.role === 'student' ? form.closedClassGroup || null : null,
  })
  if (form.role === 'student' && campusId.value) {
    await http.put(`/admin/users/${form.id}/member-tags`, {
      campusId: campusId.value,
      tags: form.memberTags || [],
    })
  }
  visible.value = false
  ElMessage.success('已保存')
  await load()
}

onMounted(async () => {
  await Promise.all([loadTeachers(), loadMemberTagOptions()])
})
</script>

<style scoped>
.openid-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.openid-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copy-btn {
  flex-shrink: 0;
  padding: 0;
}

.muted {
  color: #8a8a96;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.member-tags {
  width: 100%;
}

.campus-hint {
  margin-bottom: 10px;
  font-size: 13px;
}

.member-tags :deep(.el-checkbox) {
  margin-right: 16px;
  margin-bottom: 8px;
}

.claim-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
