<template>
  <div class="users-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索姓名/微信ID/手机号"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="roleFilter" placeholder="全部角色" clearable @change="onFilterChange">
          <el-option label="学员" value="student" />
          <el-option label="教师" value="teacher" />
          <el-option label="员工" value="employee" />
        </el-select>
        <el-select v-model="memberTagFilter" placeholder="全部业务标签" clearable @change="onFilterChange">
          <el-option v-for="item in memberTagOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
      <el-button type="primary" plain class="toolbar-add" @click="openClaim">添加学员到本校区</el-button>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div v-for="row in list" :key="row.id" class="mobile-feed-item">
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.nickname || '—' }}</span>
          <el-tag size="small" :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
        </div>
        <div class="mobile-feed-main">{{ row.phone || '暂无电话' }}</div>
        <div class="mobile-feed-meta">
          <span>{{ campusDisplay(row) }}</span>
        </div>
        <div v-if="row.school || row.collegeGrade" class="mobile-feed-meta">
          <span v-if="row.school">{{ row.school }}</span>
          <span v-if="row.collegeGrade">{{ row.collegeGrade }}</span>
        </div>
        <div v-if="row.cardTypes || (row.memberTagLabels || []).length" class="mobile-feed-meta">
          <span v-if="row.cardTypes">卡类 {{ row.cardTypes }}</span>
          <span v-if="(row.memberTagLabels || []).length">{{ (row.memberTagLabels || []).join('、') }}</span>
        </div>
        <div v-if="row.role === 'student' && row.closedClassGroupLabel" class="mobile-feed-meta">
          <span>{{ row.closedClassGroupLabel }}</span>
        </div>
        <div v-if="row.openid" class="mobile-feed-meta openid-row">
          <span class="openid-text">{{ row.openid }}</span>
          <el-button link type="primary" class="copy-btn" @click="copyText(row.openid)">复制</el-button>
        </div>
        <div class="table-actions">
          <el-button link type="primary" @click="openProfile(row)">档案</el-button>
          <el-button link type="primary" @click="edit(row)">编辑</el-button>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无用户</div>
    </div>

    <el-table v-else :data="list">
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
      <el-table-column label="操作" width="140" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" @click="openProfile(row)">档案</el-button>
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

  <el-drawer v-model="profileVisible" size="560px" :title="profileTitle" destroy-on-close>
    <div v-loading="profileLoading" class="profile-drawer">
      <div class="profile-summary">
        <div>首次上课：{{ profile.firstClassDate || '暂无' }}</div>
        <div>上课次数：{{ profile.classCount || 0 }}</div>
      </div>
      <h4>次卡与有效期</h4>
      <el-table :data="profile.cards || []" size="small" empty-text="暂无卡包">
        <el-table-column prop="name" label="卡名" min-width="120" />
        <el-table-column label="剩余" width="90">
          <template #default="{ row }">{{ row.remain ?? 0 }}/{{ row.total ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="到期" min-width="120">
          <template #default="{ row }">{{ row.expireDate || (row.validDays ? `首次到课后${row.validDays}天` : '不过期') }}</template>
        </el-table-column>
      </el-table>
      <div class="profile-history-head">
        <h4>上课记录</h4>
        <el-date-picker
          v-model="profileMonth"
          type="month"
          value-format="YYYY-MM"
          placeholder="全部月份"
          clearable
          style="width: 150px"
          @change="onProfileMonthChange"
        />
      </div>
      <el-table
        v-loading="profileHistoryLoading"
        :data="profile.classHistory || []"
        size="small"
        max-height="360"
        empty-text="暂无上课记录"
      >
        <el-table-column prop="classDate" label="日期" width="110" />
        <el-table-column prop="name" label="课程" min-width="120" />
        <el-table-column prop="timeText" label="时间" width="110" />
        <el-table-column prop="teacherName" label="老师" width="90" />
      </el-table>
      <el-pagination
        v-if="profile.classHistoryTotal > profileHistorySize"
        class="profile-pager"
        background
        small
        layout="total, prev, pager, next"
        :total="profile.classHistoryTotal"
        v-model:current-page="profileHistoryPage"
        :page-size="profileHistorySize"
        @current-change="loadProfileHistory"
      />
    </div>
  </el-drawer>

  <el-dialog v-model="claimVisible" title="添加学员到本校区" width="720px" class="claim-dialog">
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
        @keyup.enter="searchClaim"
      />
      <el-button type="primary" :loading="claimLoading" @click="searchClaim">搜索</el-button>
      <span class="muted claim-hint">将加入：{{ campusName(campusId) }}（与其他校区并存）</span>
    </div>

    <div v-if="isMobile" class="mobile-feed claim-list">
      <div v-for="row in claimList" :key="row.id" class="mobile-feed-item">
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.nickname || '—' }}</span>
        </div>
        <div class="mobile-feed-main">{{ row.phone || '—' }}</div>
        <div v-if="row.school" class="mobile-feed-meta">{{ row.school }}</div>
        <div class="mobile-feed-meta">
          <span>{{ (row.campusLabels || []).join('、') || '暂无关联校区' }}</span>
        </div>
        <div v-if="row.openid" class="mobile-feed-meta openid-row">
          <span class="openid-text">{{ row.openid }}</span>
        </div>
        <div class="table-actions">
          <el-button
            link
            type="primary"
            :disabled="!campusId || (row.campusIds || []).includes(campusId)"
            @click="claimUser(row)"
          >
            {{ (row.campusIds || []).includes(campusId) ? '已在本校区' : '加入本校区' }}
          </el-button>
        </div>
      </div>
      <div v-if="!claimList.length" class="mobile-feed-empty">搜索后可添加学员</div>
    </div>

    <el-table v-else :data="claimList" style="margin-top: 12px" max-height="420">
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
import { useBreakpoint } from '../composables/useBreakpoint'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const { isMobile } = useBreakpoint()
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

const profileVisible = ref(false)
const profileLoading = ref(false)
const profileHistoryLoading = ref(false)
const profileMonth = ref('')
const profileHistoryPage = ref(1)
const profileHistorySize = 15
const profileUserId = ref(null)
const profile = reactive({
  firstClassDate: '',
  classCount: 0,
  classHistory: [],
  classHistoryTotal: 0,
  cards: [],
  user: null,
})
const profileTitle = computed(() => {
  const name = profile.user?.nickname || '学员'
  return `${name} · 个人档案`
})

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

function campusDisplay(row) {
  if ((row.campusLabels || []).length) return row.campusLabels.join('、')
  if (row.role === 'employee') return campusName(row.campusId) || '—'
  if (row.role === 'teacher') return teacherName(row.teacherId)
  return '—'
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

async function openProfile(row) {
  profileVisible.value = true
  profileLoading.value = true
  profileHistoryLoading.value = false
  profileUserId.value = row.id
  profileMonth.value = ''
  profileHistoryPage.value = 1
  Object.assign(profile, {
    firstClassDate: '',
    classCount: 0,
    classHistory: [],
    classHistoryTotal: 0,
    cards: [],
    user: row,
  })
  try {
    await loadProfile(true)
  } finally {
    profileLoading.value = false
  }
}

async function loadProfile(withCards = false) {
  if (!profileUserId.value) return
  const params = {
    page: profileHistoryPage.value,
    size: profileHistorySize,
  }
  if (profileMonth.value) params.month = profileMonth.value
  const res = await http.get(`/admin/users/${profileUserId.value}/profile`, { params })
  const data = res.data || {}
  profile.firstClassDate = data.firstClassDate || ''
  profile.classCount = data.classCount || 0
  profile.classHistory = data.classHistory || []
  profile.classHistoryTotal = data.classHistoryTotal || 0
  profile.user = data.user || profile.user
  if (withCards) {
    profile.cards = data.cards || []
  }
}

async function loadProfileHistory() {
  profileHistoryLoading.value = true
  try {
    await loadProfile(false)
  } finally {
    profileHistoryLoading.value = false
  }
}

function onProfileMonthChange() {
  profileHistoryPage.value = 1
  return loadProfileHistory()
}

function edit(row) {
  Object.assign(form, row)
  if (!form.role) form.role = 'student'
  if (!form.closedClassGroup) form.closedClassGroup = null
  // 业务标签按当前顶部校区生效；未选校区时不带入，避免把多校区标签合并保存到某一校区
  form.memberTags = campusId.value && Array.isArray(row.memberTags) ? [...row.memberTags] : []
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

.openid-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.openid-row {
  align-items: center;
  gap: 8px;
}

.openid-row .openid-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.profile-drawer h4 {
  margin: 20px 0 10px;
  font-size: 15px;
}

.profile-history-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
}

.profile-history-head h4 {
  margin: 12px 0;
}

.profile-pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.profile-summary {
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  background: #f7f7f9;
  border-radius: 8px;
  color: #4a4a55;
  font-size: 14px;
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

.claim-list {
  margin-top: 12px;
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

  .claim-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .claim-bar :deep(.el-input) {
    width: 100% !important;
  }

  .claim-bar .el-button {
    width: 100%;
    margin-left: 0 !important;
  }

  .claim-hint {
    font-size: 13px;
    line-height: 1.45;
  }
}
</style>
