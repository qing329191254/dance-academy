<template>
  <div class="teachers-page page-card">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索姓名 / 风格 / 介绍"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select v-model="enabled" placeholder="启用状态" clearable @change="search">
          <el-option label="启用" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
        <el-button @click="search">查询</el-button>
      </div>
      <el-button type="primary" class="toolbar-add" @click="edit()">新增老师</el-button>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div v-for="row in list" :key="row.id" class="mobile-media-item">
        <img v-if="mediaSrc(row.avatar)" class="mobile-media-thumb mobile-avatar" :src="mediaSrc(row.avatar)" alt="" />
        <div v-else class="mobile-avatar-placeholder">{{ (row.name || '?').slice(0, 1) }}</div>
        <div class="mobile-media-body">
          <div class="mobile-feed-head">
            <span class="mobile-feed-title">{{ row.name || '—' }}</span>
            <span class="mobile-feed-status">{{ row.enabled ? '已启用' : '已停用' }}</span>
          </div>
          <div v-if="row.style" class="mobile-feed-main">{{ row.style }}</div>
          <div v-if="row.intro" class="mobile-feed-meta mobile-intro">{{ row.intro }}</div>
          <div class="mobile-feed-meta">
            <span>排序 {{ row.sortOrder ?? 0 }}</span>
            <span v-if="row.boundAccountNickname">已绑定 {{ row.boundAccountNickname }}</span>
            <el-button v-else link type="warning" class="inline-bind" @click="goBind(row)">待绑定</el-button>
          </div>
          <div class="table-actions">
            <el-button v-if="row.hasResume" link type="primary" @click="viewResume(row)">简历</el-button>
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </div>
        </div>
      </div>
      <div v-if="!list.length" class="mobile-feed-empty">暂无老师档案</div>
    </div>

    <el-table v-else :data="list">
      <el-table-column label="头像" width="80">
        <template #default="{ row }">
          <img v-if="mediaSrc(row.avatar)" class="thumb" :src="mediaSrc(row.avatar)" alt="" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="style" label="风格" width="120" />
      <el-table-column prop="intro" label="介绍" />
      <el-table-column label="简历" width="120">
        <template #default="{ row }">
          <el-button v-if="row.hasResume" link type="primary" @click="viewResume(row)">查看</el-button>
          <span v-else class="muted">未填写</span>
        </template>
      </el-table-column>
      <el-table-column label="绑定账号" width="150">
        <template #default="{ row }">
          <span v-if="row.boundAccountNickname">{{ row.boundAccountNickname }}</span>
          <el-button v-else link type="warning" @click="goBind(row)">待绑定</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
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

  <el-dialog v-model="visible" :title="form.id ? '编辑老师档案' : '新增老师档案'" width="560px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="风格"><el-input v-model="form.style" /></el-form-item>
      <el-form-item label="介绍"><el-input v-model="form.intro" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="头像"><ImageField v-model="form.avatar" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      <el-form-item v-if="form.id" label="简历">
        <el-button @click="viewResume(form)">打开简历管理</el-button>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">确定</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="resumeVisible" :size="resumeDrawerSize" :title="resumeTitle">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="老师在小程序自行填写与上传，此处仅供查看"
      style="margin-bottom: 16px"
    />
    <template v-if="resume">
      <h4>文字自我介绍</h4>
      <p class="resume-intro">{{ resume.resumeIntro || '暂无' }}</p>
      <h4>照片（{{ (resume.photos || []).length }}）</h4>
      <div class="media-grid">
        <el-image
          v-for="item in resume.photos || []"
          :key="item.id || item.url"
          class="photo"
          :src="mediaSrc(item.url)"
          :preview-src-list="(resume.photos || []).map((p) => mediaSrc(p.url))"
          fit="cover"
        />
        <span v-if="!(resume.photos || []).length" class="muted">暂无照片</span>
      </div>
      <h4>视频（{{ (resume.videos || []).length }}）</h4>
      <div class="video-list">
        <video
          v-for="item in resume.videos || []"
          :key="item.id || item.url"
          class="video"
          :src="mediaSrc(item.url)"
          controls
        />
        <span v-if="!(resume.videos || []).length" class="muted">暂无视频</span>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import ImageField from '../components/ImageField.vue'
import { mediaSrc } from '../utils/media'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'

const router = useRouter()
const { isMobile } = useBreakpoint()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const enabled = ref()
const visible = ref(false)
const form = reactive({})
const resumeVisible = ref(false)
const resume = ref(null)
const resumeTeacher = ref(null)

const resumeTitle = computed(() =>
  resumeTeacher.value ? `简历管理 · ${resumeTeacher.value.name || resumeTeacher.value.teacherName || ''}` : '简历管理',
)

const resumeDrawerSize = computed(() => (isMobile.value ? '100%' : '680px'))

function queryParams() {
  const params = {
    keyword: keyword.value,
    page: page.value,
    size: size.value,
    ...campusParams(),
  }
  if (enabled.value === true || enabled.value === false) {
    params.enabled = enabled.value
  }
  return params
}

async function load() {
  const res = await http.get('/admin/teachers', { params: queryParams() })
  list.value = res.data?.list || []
  total.value = res.data?.total || 0
}

function search() {
  page.value = 1
  return load()
}

function edit(row) {
  Object.assign(form, { id: null, name: '', style: '', intro: '', avatar: '', sortOrder: 0, enabled: true }, row || {})
  visible.value = true
}

function goBind(row) {
  router.push({
    path: '/users',
    query: {
      role: 'teacher',
      keyword: row.name || '',
    },
  })
}

async function viewResume(row) {
  const id = row.id || row.teacherId
  if (!id) return
  resumeTeacher.value = row
  const res = await http.get(`/admin/teachers/${id}/resume`)
  resume.value = res.data || null
  resumeVisible.value = true
}

async function save() {
  if (form.id) await http.put(`/admin/teachers/${form.id}`, form)
  else await http.post('/admin/teachers', form)
  visible.value = false
  ElMessage.success('已保存')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm('确认删除该老师？', '提示')
  await http.delete(`/admin/teachers/${row.id}`)
  ElMessage.success('已删除')
  if (list.value.length === 1 && page.value > 1) {
    page.value -= 1
  }
  await load()
}

const { campusParams } = useCampusScope(load)
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

.mobile-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
}

.mobile-avatar-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ebebef;
  color: #6b6b76;
  font-size: 18px;
  font-weight: 600;
}

.mobile-intro {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.45;
}

.inline-bind {
  padding: 0 !important;
  height: auto !important;
  min-height: unset !important;
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

.muted {
  color: #999;
}
.resume-intro {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #303133;
  margin: 8px 0 20px;
}
.media-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.photo {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}
.video-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.video {
  width: 100%;
  max-height: 280px;
  background: #000;
  border-radius: 8px;
}
h4 {
  margin: 0 0 8px;
}
</style>
