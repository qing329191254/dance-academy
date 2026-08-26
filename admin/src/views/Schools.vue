<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="edit()">新增学校</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="name" label="学校名称" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="启用" width="100">
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
  </div>

  <el-dialog v-model="visible" :title="form.id ? '编辑学校' : '新增学校'" width="480px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="学校名称"><el-input v-model="form.name" maxlength="80" /></el-form-item>
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

const list = ref([])
const visible = ref(false)
const form = reactive({})

async function load() {
  const res = await http.get('/admin/schools', { params: { all: true } })
  list.value = res.data || []
}

function edit(row) {
  Object.assign(form, { id: null, name: '', sortOrder: 0, enabled: true }, row || {})
  visible.value = true
}

async function save() {
  if (!form.name?.trim()) {
    ElMessage.warning('请填写学校名称')
    return
  }
  const payload = { name: form.name.trim(), sortOrder: form.sortOrder, enabled: form.enabled }
  if (form.id) await http.put(`/admin/schools/${form.id}`, payload)
  else await http.post('/admin/schools', payload)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除学校「${row.name}」？`, '提示')
  await http.delete(`/admin/schools/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>
