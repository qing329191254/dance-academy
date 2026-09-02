<template>
  <div class="courses-page">
    <OrgWideNotice />
    <div class="page-card">
      <div class="toolbar">
        <div class="filters">
          <el-input
            v-model="keyword"
            placeholder="搜索名称 / 介绍"
            clearable
            @keyup.enter="search"
            @clear="search"
          />
          <el-select v-model="moduleType" placeholder="模块类型" clearable @change="search">
            <el-option v-for="opt in moduleTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-select v-model="enabled" placeholder="启用状态" clearable @change="search">
            <el-option label="启用" :value="true" />
            <el-option label="停用" :value="false" />
          </el-select>
          <el-button @click="search">查询</el-button>
        </div>
        <el-button type="primary" class="toolbar-add" @click="edit()">新增</el-button>
      </div>

      <div v-if="isMobile" class="mobile-feed">
        <div v-for="row in list" :key="row.id" class="mobile-media-item">
          <img v-if="mediaSrc(row.cover)" class="mobile-media-thumb" :src="mediaSrc(row.cover)" alt="" />
          <div class="mobile-media-body" :class="{ 'is-full': !mediaSrc(row.cover) }">
            <div class="mobile-feed-head">
              <span class="mobile-feed-title">{{ row.name || '—' }}</span>
              <span class="mobile-feed-status">{{ row.enabled ? '已启用' : '已停用' }}</span>
            </div>
            <div class="mobile-feed-main">
              {{ moduleTypeLabel(row.moduleType) }}
              <template v-if="row.moduleKey"> · {{ row.moduleKey }}</template>
            </div>
            <div class="mobile-feed-meta">
              <span>价格 {{ displayPrice(row) }}{{ row.priceUnit ? ` / ${row.priceUnit}` : '' }}</span>
              <span>排序 {{ row.sortOrder ?? 0 }}</span>
            </div>
            <div v-if="row.summary" class="mobile-feed-meta mobile-summary">{{ row.summary }}</div>
            <div class="table-actions">
              <el-button link type="primary" @click="edit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </div>
          </div>
        </div>
        <div v-if="!list.length" class="mobile-feed-empty">暂无课程产品</div>
      </div>

      <el-table v-else :data="list">
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ moduleTypeLabel(row.moduleType) }}</template>
      </el-table-column>
      <el-table-column prop="moduleKey" label="标识" width="90" />
      <el-table-column label="价格" width="100">
        <template #default="{ row }">{{ displayPrice(row) }}</template>
      </el-table-column>
      <el-table-column prop="summary" label="摘要" min-width="160" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column label="启用" width="70">
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

  <el-dialog v-model="visible" :title="form.id ? '编辑' : '新增'" width="640px">
    <el-form :model="form" label-width="96px">
      <el-form-item label="模块类型" required>
        <el-select v-model="form.moduleType" style="width: 100%">
          <el-option v-for="opt in moduleTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.moduleType === 'system'" label="模块标识" required>
        <el-input v-model="form.moduleKey" placeholder="如 fixed / pass / private / custom" />
      </el-form-item>
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="展示价格">
        <div class="price-row">
          <el-input v-model="form.priceDisplay" placeholder="如 9.9，留空则不展示" class="price-value" />
          <el-input v-model="form.priceUnit" placeholder="单位" class="price-unit" />
        </div>
      </el-form-item>
      <el-form-item v-if="form.moduleType === 'trial'" label="标签">
        <el-input v-model="form.tag" placeholder="如 新人专享" />
      </el-form-item>
      <el-form-item label="列表摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="详细介绍">
        <el-input v-model="form.description" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="亮点">
        <el-input
          v-model="form.highlights"
          type="textarea"
          :rows="4"
          placeholder="每行一条，如：&#10;一节团课体验&#10;到店即可上课"
        />
      </el-form-item>
      <template v-if="form.moduleType === 'system'">
        <el-form-item label="按钮文案">
          <el-input v-model="form.actionLabel" placeholder="如 查看固定班课表" />
        </el-form-item>
        <el-form-item label="按钮动作">
          <el-select v-model="form.actionTab" clearable placeholder="留空则展示客服二维码" style="width: 100%">
            <el-option label="展示客服二维码" value="" />
            <el-option label="跳转团课约课" value="group" />
            <el-option label="跳转固定班" value="fixed" />
            <el-option label="跳转私教" value="private" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!form.actionTab" label="客服二维码">
          <ImageField v-model="form.customerServiceQr" />
          <div class="form-tip">无课程预约入口时，小程序详情页会直接展示此二维码。</div>
        </el-form-item>
      </template>
      <el-form-item label="封面"><ImageField v-model="form.cover" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
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
import ImageField from '../components/ImageField.vue'
import OrgWideNotice from '../components/OrgWideNotice.vue'
import { useBreakpoint } from '../composables/useBreakpoint'
import { mediaSrc } from '../utils/media'

const moduleTypeOptions = [
  { value: 'trial', label: '体验课' },
  { value: 'system', label: '课程产品' },
]

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const moduleType = ref()
const enabled = ref()
const visible = ref(false)
const form = reactive({})
const { isMobile } = useBreakpoint()

function moduleTypeLabel(type) {
  return moduleTypeOptions.find((item) => item.value === type)?.label || type || '—'
}

function displayPrice(row) {
  if (row.priceDisplay) return row.priceDisplay
  if (row.price != null) return row.price
  return '-'
}

function queryParams() {
  const params = { keyword: keyword.value, page: page.value, size: size.value }
  if (enabled.value === true || enabled.value === false) params.enabled = enabled.value
  if (moduleType.value) params.moduleType = moduleType.value
  return params
}

async function load() {
  const res = await http.get('/admin/courses', { params: queryParams() })
  list.value = res.data?.list || []
  total.value = res.data?.total || 0
}

function search() {
  page.value = 1
  return load()
}

function defaultForm() {
  return {
    id: null,
    moduleType: 'trial',
    moduleKey: '',
    name: '',
    price: null,
    priceDisplay: '',
    priceUnit: '节',
    summary: '',
    tag: '',
    description: '',
    highlights: '',
    actionLabel: '',
    actionTab: '',
    customerServiceQr: '',
    cover: '',
    sortOrder: 0,
    enabled: true,
  }
}

function edit(row) {
  Object.assign(form, defaultForm(), row || {})
  if (!form.moduleType || form.moduleType === 'product') form.moduleType = 'trial'
  visible.value = true
}

async function save() {
  const payload = { ...form }
  if (payload.moduleType === 'system' && !payload.actionTab && !payload.customerServiceQr) {
    ElMessage.warning('无预约入口的课程产品请上传客服二维码')
    return
  }
  if (payload.moduleType !== 'system') {
    payload.moduleKey = null
    payload.actionLabel = null
    payload.actionTab = null
    payload.customerServiceQr = null
  }
  payload.price = null
  if (form.id) await http.put(`/admin/courses/${form.id}`, payload)
  else await http.post('/admin/courses', payload)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm('确认删除该条目？', '提示')
  await http.delete(`/admin/courses/${row.id}`)
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

.mobile-media-body.is-full {
  width: 100%;
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

.form-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
.price-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  width: 100%;
}
.price-value {
  flex: 1 1 160px;
  min-width: 0;
}
.price-unit {
  flex: 0 1 100px;
  width: 100px;
}
</style>
