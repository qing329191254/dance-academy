<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="hint">超级管理员可创建管理员账号，并为管理员分配可管理的校区。</div>
      <el-button type="primary" @click="edit()">新增管理员</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="username" label="账号" width="140" />
      <el-table-column prop="name" label="姓名" width="140" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">{{ roleLabel(row.role) }}</template>
      </el-table-column>
      <el-table-column label="管理校区">
        <template #default="{ row }">
          <span v-if="row.superAdmin">全部校区</span>
          <span v-else>{{ (row.campusIds || []).map(campusName).join('、') || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="danger" :disabled="row.id === auth.profile?.id" @click="remove(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="visible" :title="form.id ? '编辑管理员' : '新增管理员'" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="账号">
        <el-input v-model="form.username" :disabled="!!form.id" />
      </el-form-item>
      <el-form-item :label="form.id ? '新密码' : '密码'">
        <el-input v-model="form.password" type="password" show-password :placeholder="form.id ? '留空则不修改' : '请输入密码'" />
      </el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item v-if="form.id && form.superAdmin" label="角色">
        <el-input model-value="超级管理员" disabled />
      </el-form-item>
      <el-form-item v-else-if="form.id" label="角色">
        <el-input model-value="管理员" disabled />
      </el-form-item>
      <el-form-item v-if="!form.superAdmin" label="校区">
        <el-select v-model="form.campusIds" multiple placeholder="选择可管理校区" style="width: 100%">
          <el-option v-for="item in CAMPUSES" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
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
import { CAMPUSES, campusName } from '../common/campuses'
import { roleLabel } from '../common/adminAccess'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const list = ref([])
const visible = ref(false)
const form = reactive({
  id: null,
  username: '',
  password: '',
  name: '',
  superAdmin: false,
  campusIds: [],
})

async function load() {
  const res = await http.get('/admin/admins')
  list.value = res.data || []
}

function edit(row) {
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    name: '',
    superAdmin: false,
    campusIds: [],
  }, row ? {
    id: row.id,
    username: row.username,
    password: '',
    name: row.name,
    superAdmin: !!row.superAdmin,
    campusIds: [...(row.campusIds || [])],
  } : {})
  visible.value = true
}

async function save() {
  if (!form.superAdmin && !form.campusIds.length) {
    ElMessage.warning('请为管理员选择至少一个校区')
    return
  }
  const payload = {
    username: form.username,
    password: form.password,
    name: form.name,
    campusIds: form.superAdmin ? [] : form.campusIds,
  }
  if (form.id) {
    if (!payload.password) delete payload.password
    await http.put(`/admin/admins/${form.id}`, payload)
  } else {
    await http.post('/admin/admins', payload)
  }
  visible.value = false
  ElMessage.success('已保存')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除管理员「${row.name || row.username}」？`, '提示')
  await http.delete(`/admin/admins/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.hint {
  color: #6b6b76;
  font-size: 13px;
}
</style>
