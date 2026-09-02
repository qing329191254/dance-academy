<template>
  <div class="dance-categories-page page-card">
    <div class="toolbar">
      <div class="hint muted">板块用于发卡与约课匹配；舞种挂在板块下，排课可选填，仅作展示。</div>
      <el-button type="primary" class="toolbar-add" @click="editSection()">新增板块</el-button>
    </div>

    <div v-if="isMobile" class="mobile-category-list">
      <div v-for="section in tree" :key="section.id" class="category-section-card">
        <div class="mobile-feed-item">
          <div class="mobile-feed-head">
            <span class="mobile-feed-title">{{ section.name }}</span>
            <span class="mobile-feed-status">板块</span>
          </div>
          <div class="mobile-feed-meta">
            <span v-if="section.code">标识 {{ section.code }}</span>
            <span>排序 {{ section.sortOrder ?? 0 }}</span>
            <span>{{ section.enabled ? '已启用' : '已停用' }}</span>
          </div>
          <div class="table-actions">
            <el-button link type="primary" @click="editStyle(section)">加舞种</el-button>
            <el-button link type="primary" @click="edit(section)">编辑</el-button>
            <el-button link type="danger" @click="remove(section)">删除</el-button>
          </div>
        </div>

        <div v-for="child in section.children || []" :key="child.id" class="mobile-feed-item category-style-item">
          <div class="mobile-feed-head">
            <span class="mobile-feed-title">{{ child.name }}</span>
            <span class="mobile-feed-status is-style">舞种</span>
          </div>
          <div class="mobile-feed-meta">
            <span v-if="child.code">标识 {{ child.code }}</span>
            <span>排序 {{ child.sortOrder ?? 0 }}</span>
            <span>{{ child.enabled ? '已启用' : '已停用' }}</span>
          </div>
          <div class="table-actions">
            <el-button link type="primary" @click="edit(child)">编辑</el-button>
            <el-button link type="danger" @click="remove(child)">删除</el-button>
          </div>
        </div>

        <div v-if="!(section.children || []).length" class="category-empty">暂无舞种，可点「加舞种」</div>
      </div>
      <div v-if="!tree.length" class="mobile-feed-empty">暂无板块</div>
    </div>

    <el-table v-else :data="tree" row-key="id" default-expand-all>
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="code" label="标识" width="140" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ row.section ? '板块' : '舞种' }}</template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="启用" width="90">
        <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="row.section" link type="primary" @click="editStyle(row)">加舞种</el-button>
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="visible" :title="dialogTitle" width="480px">
    <el-form :model="form" label-width="90px">
      <el-form-item v-if="form.parentId" label="所属板块">
        <el-select v-model="form.parentId" style="width: 100%">
          <el-option v-for="s in sections" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称"><el-input v-model="form.name" maxlength="64" /></el-form-item>
      <el-form-item label="标识">
        <el-input v-model="form.code" maxlength="64" placeholder="如 street / yoga，可留空自动生成" />
      </el-form-item>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { useBreakpoint } from '../composables/useBreakpoint'

const tree = ref([])
const visible = ref(false)
const form = reactive({})
const { isMobile } = useBreakpoint()

const sections = computed(() => tree.value.filter((item) => item.section))

const dialogTitle = computed(() => {
  if (form.id) return form.parentId ? '编辑舞种' : '编辑板块'
  return form.parentId ? '新增舞种' : '新增板块'
})

function flattenChildren(list) {
  return (list || []).map((section) => ({
    ...section,
    children: (section.children || []).map((child) => ({ ...child, section: false })),
  }))
}

async function load() {
  const res = await http.get('/admin/dance-categories', { params: { all: true } })
  tree.value = flattenChildren(res.data || [])
}

function editSection() {
  Object.assign(form, { id: null, parentId: null, name: '', code: '', sortOrder: 0, enabled: true })
  visible.value = true
}

function editStyle(section) {
  Object.assign(form, {
    id: null,
    parentId: section.id,
    name: '',
    code: '',
    sortOrder: (section.children || []).length + 1,
    enabled: true,
  })
  visible.value = true
}

function edit(row) {
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || null,
    name: row.name,
    code: row.code,
    sortOrder: row.sortOrder ?? 0,
    enabled: row.enabled !== false,
  })
  visible.value = true
}

async function save() {
  if (!form.name?.trim()) {
    ElMessage.warning('请填写名称')
    return
  }
  const payload = {
    parentId: form.parentId || null,
    name: form.name.trim(),
    code: form.code?.trim() || '',
    sortOrder: form.sortOrder,
    enabled: form.enabled,
  }
  if (form.id) await http.put(`/admin/dance-categories/${form.id}`, payload)
  else await http.post('/admin/dance-categories', payload)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '提示')
  await http.delete(`/admin/dance-categories/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.hint {
  font-size: 13px;
  line-height: 1.5;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.muted {
  color: #999;
}

.mobile-category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-section-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  border-radius: 10px;
  background: #f8f8fa;
  border: 1px solid #ececf0;
}

.category-style-item {
  margin-left: 8px;
  border-left: 3px solid #d8d8e0;
}

.mobile-feed-status.is-style {
  color: #8a74e5;
}

.category-empty {
  margin-left: 8px;
  padding: 8px 10px;
  font-size: 12px;
  color: #b0b0b8;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
    margin-bottom: 12px;
  }

  .hint {
    font-size: 12px;
  }

  .toolbar-add {
    width: 100%;
    margin-left: 0 !important;
  }
}
</style>
