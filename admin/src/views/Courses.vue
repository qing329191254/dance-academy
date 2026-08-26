<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索名称 / 级别 / 介绍"
          style="width: 240px"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="enabled" placeholder="启用状态" clearable style="width: 140px" @change="search">
          <el-option label="启用" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
      <el-button type="primary" @click="edit()">新增课程</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="level" label="级别" width="100" />
      <el-table-column prop="description" label="介绍" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
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
  <el-dialog v-model="visible" :title="form.id ? '编辑课程' : '新增课程'" width="560px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" /></el-form-item>
      <el-form-item label="级别"><el-input v-model="form.level" /></el-form-item>
      <el-form-item label="介绍"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const enabled = ref()
const visible = ref(false)
const form = reactive({})

function queryParams() {
  const params = { keyword: keyword.value, page: page.value, size: size.value }
  if (enabled.value === true || enabled.value === false) params.enabled = enabled.value
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
function edit(row) {
  Object.assign(form, { id: null, name: '', price: 0, level: '零基础', description: '', cover: '', sortOrder: 0, enabled: true }, row || {})
  visible.value = true
}
async function save() {
  if (form.id) await http.put(`/admin/courses/${form.id}`, form)
  else await http.post('/admin/courses', form)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除该课程？', '提示')
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
  gap: 12px;
  align-items: center;
}
</style>
