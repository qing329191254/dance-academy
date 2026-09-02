<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="hint muted">板块用于发卡与约课匹配；舞种挂在板块下，排课可选填，仅作展示。</div>
      <el-button type="primary" @click="editSection()">新增板块</el-button>
    </div>

    <el-table :data="tree" row-key="id" default-expand-all>
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

const tree = ref([])
const visible = ref(false)
const form = reactive({})

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
</style>
