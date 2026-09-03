<template>
  <div class="cards-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索卡名 / 学员ID / 昵称"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="type" placeholder="类型" clearable @change="search">
          <el-option label="团课" value="团课" />
          <el-option label="私教" value="私教" />
          <el-option label="固定班" value="固定班" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
      <el-button type="primary" class="toolbar-add" @click="edit()">发卡</el-button>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div v-for="row in list" :key="row.id" class="mobile-feed-item">
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.name || '—' }}</span>
          <span class="mobile-feed-status">{{ row.type || '—' }}</span>
        </div>
        <div class="mobile-feed-main">学员 ID {{ row.userId ?? '—' }}</div>
        <div class="mobile-feed-meta">
          <span>{{ row.sectionName || '通用板块' }}</span>
          <span>剩余 {{ row.remain ?? 0 }}/{{ row.total ?? 0 }}</span>
          <span>{{ row.activatedAt ? '已开卡' : '未开卡' }}</span>
        </div>
        <div class="mobile-feed-meta">{{ expireLabel(row) }}</div>
        <div class="table-actions">
          <el-button link type="primary" @click="edit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无卡包记录</div>
    </div>

    <el-table v-else :data="list">
      <el-table-column prop="userId" label="学员ID" width="90" />
      <el-table-column prop="name" label="卡名" />
      <el-table-column prop="type" label="类型" width="90" />
      <el-table-column label="适用板块" width="140">
        <template #default="{ row }">{{ row.sectionName || '通用' }}</template>
      </el-table-column>
      <el-table-column label="次数" width="120">
        <template #default="{ row }">{{ row.remain }}/{{ row.total }}</template>
      </el-table-column>
      <el-table-column label="开卡" width="100">
        <template #default="{ row }">{{ row.activatedAt ? '已开卡' : '未开卡' }}</template>
      </el-table-column>
      <el-table-column label="有效期" min-width="180">
        <template #default="{ row }">{{ expireLabel(row) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
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

  <el-dialog v-model="visible" :title="form.id ? '编辑卡' : '发放卡包'" width="560px">
    <el-form :model="form" label-width="110px">
      <el-form-item label="微信ID"><el-input v-model="form.openid" /></el-form-item>
      <el-form-item label="卡名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.type" style="width: 100%">
          <el-option label="团课" value="团课" />
          <el-option label="私教" value="私教" />
          <el-option label="固定班" value="固定班" />
        </el-select>
      </el-form-item>
      <el-form-item label="适用板块">
        <el-select v-model="form.sectionId" clearable placeholder="不选=通用" style="width: 100%">
          <el-option v-for="s in sections" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="总次数"><el-input-number v-model="form.total" :min="1" /></el-form-item>
      <el-form-item label="剩余次数"><el-input-number v-model="form.remain" :min="0" /></el-form-item>
      <el-form-item label="有效期模式">
        <el-radio-group v-model="form.expireMode" @change="onExpireModeChange">
          <el-radio value="from_activation">首次到课起算</el-radio>
          <el-radio value="fixed_deadline">固定截止日期</el-radio>
        </el-radio-group>
        <div class="form-tip">二选一。小次卡常用「首次到课起算」；学期通/大次卡/年通用「固定截止日期」。</div>
      </el-form-item>
      <el-form-item v-if="form.expireMode === 'from_activation'" label="有效天数">
        <el-input-number v-model="form.validDays" :min="0" :max="3650" />
        <div class="form-tip">留空或 0 = 开卡后不过期；有天数则首次到课后起算</div>
      </el-form-item>
      <el-form-item v-if="form.expireMode === 'fixed_deadline'" label="到期日" required>
        <el-date-picker v-model="form.expireDate" value-format="YYYY-MM-DD" placeholder="学期末等截止日期" clearable />
        <div class="form-tip">过此日期无论开没开卡都作废（期期清）</div>
      </el-form-item>
      <el-form-item v-if="form.id" label="开卡日期">
        <el-date-picker v-model="form.activatedAt" value-format="YYYY-MM-DD" placeholder="空=未开卡" clearable />
      </el-form-item>
      <el-form-item
        v-if="form.id && form.expireMode === 'from_activation'"
        label="到期日"
      >
        <el-date-picker v-model="form.expireDate" value-format="YYYY-MM-DD" clearable />
        <div class="form-tip">一般由首次到课自动填写，也可手动改</div>
      </el-form-item>
      <el-form-item label="封面"><ImageField v-model="form.cover" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import ImageField from '../components/ImageField.vue'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'

const MODE_FROM = 'from_activation'
const MODE_FIXED = 'fixed_deadline'

const list = ref([])
const { isMobile } = useBreakpoint()
const sections = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const type = ref('')
const visible = ref(false)
const form = reactive({})

function resolveMode(row) {
  if (row?.expireMode === MODE_FIXED || row?.expireMode === MODE_FROM) return row.expireMode
  return MODE_FROM
}

function expireLabel(row) {
  const mode = resolveMode(row)
  if (mode === MODE_FIXED) {
    if (!row.expireDate) return '固定截止 · 未设到期日'
    if (!row.activatedAt) return `未开卡 · 须在 ${row.expireDate} 前`
    return `至 ${row.expireDate}`
  }
  if (!row.activatedAt) {
    if (row.validDays) return `未开卡 · 首次到课后 ${row.validDays} 天`
    return '未开卡 · 不过期'
  }
  return row.expireDate ? `至 ${row.expireDate}` : '已开卡 · 不过期'
}

function onExpireModeChange() {
  if (form.expireMode === MODE_FIXED) {
    form.validDays = null
  } else if (!form.activatedAt) {
    form.expireDate = ''
  }
}

function queryParams() {
  const params = { keyword: keyword.value, page: page.value, size: size.value, ...campusParams() }
  if (type.value) params.type = type.value
  return params
}

async function loadSections() {
  const res = await http.get('/admin/dance-sections', { params: { enabledOnly: false } })
  sections.value = res.data || []
}

async function load() {
  const res = await http.get('/admin/cards', { params: queryParams() })
  list.value = res.data?.list || []
  total.value = res.data?.total || 0
}

function search() {
  page.value = 1
  return load()
}
function edit(row) {
  Object.assign(form, {
    id: null,
    userId: null,
    openid: '',
    name: '团课 10 次卡',
    type: '团课',
    remain: 10,
    total: 10,
    sectionId: null,
    expireMode: MODE_FROM,
    validDays: null,
    activatedAt: null,
    expireDate: '',
    cover: '',
  }, row || {})
  form.expireMode = resolveMode(form)
  if (form.validDays === 0) form.validDays = null
  if (form.expireMode === MODE_FIXED) form.validDays = null
  visible.value = true
}
async function save() {
  const openid = String(form.openid || '').trim()
  if (!openid) {
    ElMessage.warning('请填写微信ID')
    return
  }
  const expireMode = form.expireMode === MODE_FIXED ? MODE_FIXED : MODE_FROM
  if (expireMode === MODE_FIXED && !form.expireDate) {
    ElMessage.warning('固定截止日期模式下请填写到期日')
    return
  }
  const payload = {
    ...form,
    userId: null,
    openid,
    sectionId: form.sectionId || null,
    expireMode,
    validDays: expireMode === MODE_FROM ? (form.validDays || null) : null,
    expireDate: form.expireDate || null,
  }
  if (expireMode === MODE_FROM && !form.activatedAt) {
    payload.expireDate = null
  }
  if (form.id) await http.put(`/admin/cards/${form.id}`, payload)
  else await http.post('/admin/cards', payload)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除该卡？', '提示')
  await http.delete(`/admin/cards/${row.id}`)
  ElMessage.success('已删除')
  if (list.value.length === 1 && page.value > 1) page.value -= 1
  await load()
}

const { campusParams } = useCampusScope(load)
onMounted(loadSections)
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
  width: 260px;
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

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}
</style>
