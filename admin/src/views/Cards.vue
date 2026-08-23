<template>
  <div class="page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索卡名 / 学员ID / 昵称"
          style="width: 260px"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="type" placeholder="类型" clearable style="width: 130px" @change="search">
          <el-option label="团课" value="团课" />
          <el-option label="私教" value="私教" />
          <el-option label="固定班" value="固定班" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
      <el-button type="primary" @click="edit()">发卡</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="userId" label="学员ID" width="90" />
      <el-table-column prop="name" label="卡名" />
      <el-table-column prop="type" label="类型" width="90" />
      <el-table-column label="次数" width="120">
        <template #default="{ row }">{{ row.remain }}/{{ row.total }}</template>
      </el-table-column>
      <el-table-column prop="expireDate" label="有效期" width="130" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button text type="primary" @click="edit(row)">编辑</el-button>
          <el-button text type="danger" @click="remove(row)">删除</el-button>
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
    <el-form :model="form" label-width="90px">
      <el-form-item label="学员ID"><el-input-number v-model="form.userId" :min="1" /></el-form-item>
      <el-form-item label="卡名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.type">
          <el-option label="团课" value="团课" />
          <el-option label="私教" value="私教" />
          <el-option label="固定班" value="固定班" />
        </el-select>
      </el-form-item>
      <el-form-item label="总次数"><el-input-number v-model="form.total" :min="1" /></el-form-item>
      <el-form-item label="剩余次数"><el-input-number v-model="form.remain" :min="0" /></el-form-item>
      <el-form-item label="有效期"><el-date-picker v-model="form.expireDate" value-format="YYYY-MM-DD" /></el-form-item>
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

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const type = ref('')
const visible = ref(false)
const form = reactive({})

function queryParams() {
  const params = { keyword: keyword.value, page: page.value, size: size.value }
  if (type.value) params.type = type.value
  return params
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
  Object.assign(form, { id: null, userId: 1, name: '团课 10 次卡', type: '团课', remain: 10, total: 10, expireDate: '', cover: '' }, row || {})
  visible.value = true
}
async function save() {
  if (form.id) await http.put(`/admin/cards/${form.id}`, form)
  else await http.post('/admin/cards', form)
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
onMounted(load)
</script>

<style scoped>
.filters {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
