<template>
  <div>
    <el-alert
      v-if="!campusId"
      type="warning"
      :closable="false"
      show-icon
      class="campus-hint"
      title="请先在顶部选择校区，再编辑该校区轮播与相册"
    />
    <el-alert
      v-else
      type="info"
      :closable="false"
      show-icon
      class="campus-hint"
      :title="`正在编辑：${campusLabel}`"
    />
    <div class="page-card" style="margin-bottom: 16px">
    <div class="toolbar">
      <h3>首页轮播</h3>
      <el-button type="primary" :disabled="!campusId" @click="editBanner()">新增轮播</el-button>
    </div>
    <el-table :data="banners">
      <el-table-column label="图片" width="120">
        <template #default="{ row }"><img class="thumb" :src="mediaSrc(row.imageUrl)" /></template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="启用" width="90">
        <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" @click="editBanner(row)">编辑</el-button>
            <el-button link type="danger" @click="remove('/admin/banners', row.id, loadBanners)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <div class="page-card">
    <div class="toolbar">
      <h3>品牌相册</h3>
      <el-button type="primary" :disabled="!campusId" @click="editPhoto()">新增照片</el-button>
    </div>
    <el-table :data="photos">
      <el-table-column label="图片" width="120">
        <template #default="{ row }"><img class="thumb" :src="mediaSrc(row.imageUrl)" /></template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="120" class-name="col-actions" label-class-name="col-actions" align="left" header-align="left">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" @click="editPhoto(row)">编辑</el-button>
            <el-button link type="danger" @click="remove('/admin/brand-photos', row.id, loadPhotos)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    </div>
  </div>

  <el-dialog v-model="visible" :title="dialogTitle" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="图片"><ImageField v-model="form.imageUrl" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      <el-form-item v-if="kind === 'banner'" label="启用">
        <el-switch v-model="form.enabled" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import ImageField from '../components/ImageField.vue'
import { campusName } from '../common/campuses'
import { useCampusScope } from '../composables/useCampusScope'
import { mediaSrc } from '../utils/media'

const banners = ref([])
const photos = ref([])
const visible = ref(false)
const kind = ref('banner')
const dialogTitle = ref('')
const form = reactive({})

const campusLabel = computed(() => campusName(campusId.value))

async function loadBanners() {
  if (!campusId.value) {
    banners.value = []
    return
  }
  banners.value = (await http.get('/admin/banners', { params: { campusId: campusId.value } })).data || []
}

async function loadPhotos() {
  if (!campusId.value) {
    photos.value = []
    return
  }
  photos.value = (await http.get('/admin/brand-photos', { params: { campusId: campusId.value } })).data || []
}

async function load() {
  await Promise.all([loadBanners(), loadPhotos()])
}

const { campusId } = useCampusScope(load)

function editBanner(row) {
  if (!campusId.value) {
    ElMessage.warning('请先选择顶部校区')
    return
  }
  kind.value = 'banner'
  dialogTitle.value = row ? '编辑轮播' : '新增轮播'
  Object.assign(form, { id: null, imageUrl: '', sortOrder: 0, enabled: true }, row || {})
  visible.value = true
}

function editPhoto(row) {
  if (!campusId.value) {
    ElMessage.warning('请先选择顶部校区')
    return
  }
  kind.value = 'photo'
  dialogTitle.value = row ? '编辑照片' : '新增照片'
  Object.assign(form, { id: null, imageUrl: '', sortOrder: 0 }, row || {})
  visible.value = true
}

async function save() {
  const path = kind.value === 'banner' ? '/admin/banners' : '/admin/brand-photos'
  const params = { campusId: campusId.value }
  if (form.id) await http.put(`${path}/${form.id}`, form, { params })
  else await http.post(path, form, { params })
  visible.value = false
  ElMessage.success('已保存')
  if (kind.value === 'banner') await loadBanners()
  else await loadPhotos()
}

async function remove(path, id, reload) {
  await ElMessageBox.confirm('确认删除？', '提示')
  await http.delete(`${path}/${id}`, { params: { campusId: campusId.value } })
  ElMessage.success('已删除')
  await reload()
}
</script>

<style scoped>
.campus-hint {
  margin-bottom: 16px;
}
</style>
