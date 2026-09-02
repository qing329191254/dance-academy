<template>
  <div class="opportunities-page">
    <OrgWideNotice />
    <div class="page-card">
      <div class="toolbar">
        <div class="filters">
          <el-input
            v-model="keyword"
            placeholder="搜索标题 / 编码 / 摘要"
            clearable
            @keyup.enter="search"
            @clear="search"
          />
          <el-select v-model="trackKey" placeholder="赛道" clearable @change="search">
            <el-option v-for="(label, key) in trackLabel" :key="key" :label="label" :value="key" />
          </el-select>
          <el-select v-model="enabled" placeholder="启用状态" clearable @change="search">
            <el-option label="启用" :value="true" />
            <el-option label="停用" :value="false" />
          </el-select>
          <el-button @click="search">查询</el-button>
        </div>
        <el-button type="primary" class="toolbar-add" @click="edit()">新增机会</el-button>
      </div>

      <div v-if="isMobile" class="mobile-feed">
        <div v-for="row in list" :key="row.id" class="mobile-feed-item">
          <div class="mobile-feed-head">
            <span class="mobile-feed-title">{{ row.title || '—' }}</span>
            <span class="mobile-feed-status">{{ row.enabled ? '已启用' : '已停用' }}</span>
          </div>
          <div class="mobile-feed-main">
            {{ trackLabel[row.trackKey] || row.trackKey || '—' }}
            <template v-if="row.level"> · {{ row.level }}</template>
          </div>
          <div class="mobile-feed-meta">
            <span>名额 {{ row.spots ?? '—' }}</span>
            <span v-if="row.deadline">截止 {{ row.deadline }}</span>
          </div>
          <div v-if="row.summary" class="mobile-feed-meta mobile-summary">{{ row.summary }}</div>
          <div class="table-actions">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </div>
        </div>
        <div v-if="!list.length" class="mobile-feed-empty">暂无成长机会</div>
      </div>

      <el-table v-else :data="list">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="trackKey" label="赛道" width="120">
          <template #default="{ row }">{{ trackLabel[row.trackKey] || row.trackKey }}</template>
        </el-table-column>
        <el-table-column prop="level" label="级别" width="80" />
        <el-table-column prop="spots" label="名额" width="80" />
        <el-table-column prop="deadline" label="截止" width="130" />
        <el-table-column label="启用" width="80">
          <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
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
  </div>

  <el-dialog v-model="visible" :title="form.id ? '编辑机会' : '新增机会'" width="560px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="赛道">
        <el-select v-model="form.trackKey">
          <el-option v-for="(label, key) in trackLabel" :key="key" :label="label" :value="key" />
        </el-select>
      </el-form-item>
      <el-form-item label="级别">
        <el-select v-model="form.level">
          <el-option label="T1" value="T1" /><el-option label="T2" value="T2" /><el-option label="T3" value="T3" />
        </el-select>
      </el-form-item>
      <el-form-item label="名额"><el-input-number v-model="form.spots" :min="0" /></el-form-item>
      <el-form-item label="截止"><el-date-picker v-model="form.deadline" value-format="YYYY-MM-DD" /></el-form-item>
      <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
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
import OrgWideNotice from '../components/OrgWideNotice.vue'
import { trackLabel } from '../common/growth'
import { useBreakpoint } from '../composables/useBreakpoint'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const trackKey = ref('')
const enabled = ref()
const visible = ref(false)
const form = reactive({})
const { isMobile } = useBreakpoint()

function queryParams() {
  const params = { keyword: keyword.value, page: page.value, size: size.value }
  if (trackKey.value) params.trackKey = trackKey.value
  if (enabled.value === true || enabled.value === false) params.enabled = enabled.value
  return params
}

async function load() {
  const res = await http.get('/admin/opportunities', { params: queryParams() })
  list.value = res.data?.list || []
  total.value = res.data?.total || 0
}

function search() {
  page.value = 1
  return load()
}

function edit(row) {
  Object.assign(form, { id: null, title: '', trackKey: 'parttime', level: 'T1', spots: 4, deadline: '', summary: '', enabled: true }, row || {})
  visible.value = true
}

async function save() {
  if (form.id) await http.put(`/admin/opportunities/${form.id}`, form)
  else await http.post('/admin/opportunities', form)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm('确认删除该机会？', '提示')
  await http.delete(`/admin/opportunities/${row.id}`)
  ElMessage.success('已删除')
  if (list.value.length === 1 && page.value > 1) page.value -= 1
  await load()
}

onMounted(load)
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

.mobile-summary {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.45;
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
</style>
