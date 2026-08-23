<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索昵称" style="width: 260px" clearable @keyup.enter="load" />
      <el-button @click="load">查询</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="workLevel" label="勤工等级" width="100" />
      <el-table-column prop="workStage" label="勤工阶段" width="100" />
      <el-table-column prop="danceLevel" label="舞蹈等级" width="100" />
      <el-table-column prop="danceStage" label="舞蹈阶段" width="100" />
      <el-table-column prop="openid" label="微信ID" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button text type="primary" @click="edit(row)">编辑</el-button>
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
      <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="性别"><el-input v-model="form.gender" /></el-form-item>
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

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 15
const keyword = ref('')
const visible = ref(false)
const form = reactive({})

async function load() {
  const res = await http.get('/admin/users', { params: { keyword: keyword.value, page: page.value, size } })
  list.value = res.data.list || []
  total.value = res.data.total || 0
}
function edit(row) {
  Object.assign(form, row)
  visible.value = true
}
async function save() {
  await http.put(`/admin/users/${form.id}`, form)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}
onMounted(load)
</script>
