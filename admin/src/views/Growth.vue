<template>
  <div class="growth-page">
    <el-alert
      v-if="!campusId"
      type="warning"
      :closable="false"
      show-icon
      class="campus-hint"
      title="请先在顶部选择校区，再编辑该校区成长文案与赛道"
    />
    <el-alert
      v-else-if="!isCompact"
      type="info"
      :closable="false"
      show-icon
      class="campus-hint"
      :title="`正在编辑：${campusLabel}`"
    />

    <div class="page-card">
      <h3 class="section-title">成长中心文案</h3>
      <el-form
        :model="copyForm"
        :label-position="formLabelPosition"
        label-width="120px"
        class="growth-form"
        :disabled="!campusId"
      >
        <el-form-item label="首页介绍">
          <el-input v-model="copyForm.growthIntro" type="textarea" :rows="introRows" />
        </el-form-item>
        <el-form-item label="等级提示">
          <el-input v-model="copyForm.growthLevelTip" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="勤工俭学引导">
          <el-input v-model="copyForm.workLead" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="舞蹈发展引导">
          <el-input v-model="copyForm.danceLead" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="勤工俭学摘要">
          <el-input v-model="copyForm.workModuleSummary" placeholder="成长中心卡片副标题" />
        </el-form-item>
        <el-form-item label="舞蹈发展摘要">
          <el-input v-model="copyForm.danceModuleSummary" placeholder="成长中心卡片副标题" />
        </el-form-item>
        <el-form-item v-if="!isMobile">
          <el-button type="primary" :loading="copySaving" :disabled="!campusId" @click="saveCopy">保存文案</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="isMobile" class="growth-save-bar">
      <el-button
        type="primary"
        class="growth-save-btn"
        :loading="copySaving"
        :disabled="!campusId"
        @click="saveCopy"
      >
        保存文案
      </el-button>
    </div>

    <div class="page-card tracks-card">
      <div class="toolbar">
        <h3 class="section-title">成长赛道</h3>
        <el-button type="primary" class="toolbar-add" :disabled="!campusId" @click="edit()">新增赛道</el-button>
      </div>

      <div v-if="isMobile" class="mobile-feed">
        <div v-for="row in list" :key="row.id" class="mobile-feed-item">
          <div class="mobile-feed-head">
            <span class="mobile-feed-title">{{ row.name || '—' }}</span>
            <span class="mobile-feed-status">{{ row.enabled ? '已启用' : '已停用' }}</span>
          </div>
          <div class="mobile-feed-main">
            {{ lineLabel[row.lineKey] || row.lineKey || '—' }}
            <template v-if="row.trackKey"> · {{ row.trackKey }}</template>
            <template v-if="row.level"> · {{ row.level }}</template>
          </div>
          <div class="mobile-feed-meta">
            <span>排序 {{ row.sortOrder ?? 0 }}</span>
          </div>
          <div v-if="row.description" class="mobile-feed-meta mobile-summary">{{ row.description }}</div>
          <div class="table-actions">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </div>
        </div>
        <div v-if="!list.length" class="mobile-feed-empty">暂无成长赛道</div>
      </div>

      <el-table v-else :data="list">
        <el-table-column prop="lineKey" label="成长线" width="100">
          <template #default="{ row }">{{ lineLabel[row.lineKey] || row.lineKey }}</template>
        </el-table-column>
        <el-table-column prop="trackKey" label="标识" width="110" />
        <el-table-column prop="name" label="名称" width="100" />
        <el-table-column prop="level" label="等级" width="70" />
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
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
    </div>
  </div>

  <el-dialog v-model="visible" :title="form.id ? '编辑赛道' : '新增赛道'" width="560px">
    <el-form :model="form" label-width="96px">
      <el-form-item label="成长线" required>
        <el-select v-model="form.lineKey" style="width: 100%">
          <el-option label="勤工俭学" value="work" />
          <el-option label="舞蹈发展" value="dance" />
        </el-select>
      </el-form-item>
      <el-form-item label="成长线名称">
        <el-input v-model="form.lineName" placeholder="如 勤工俭学 / 舞蹈发展" />
      </el-form-item>
      <el-form-item label="赛道标识" required>
        <el-input v-model="form.trackKey" placeholder="如 parttime / show，需与成长机会一致" />
      </el-form-item>
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="等级"><el-input v-model="form.level" placeholder="T1 / T2 / T3" /></el-form-item>
      <el-form-item label="说明">
        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="支持换行，多行会在小程序展示" />
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
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { campusName } from '../common/campuses'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'

const lineLabel = { work: '勤工俭学', dance: '舞蹈发展' }
const list = ref([])
const visible = ref(false)
const copySaving = ref(false)
const copyForm = reactive({})
const form = reactive({})

const { isCompact, isMobile } = useBreakpoint()
const formLabelPosition = computed(() => (isCompact.value ? 'top' : 'right'))
const introRows = computed(() => (isMobile.value ? 5 : 4))

const campusLabel = computed(() => campusName(campusId.value))

function defaultTrack() {
  return {
    id: null,
    lineKey: 'work',
    lineName: '勤工俭学',
    trackKey: '',
    name: '',
    level: 'T1',
    description: '',
    sortOrder: 0,
    enabled: true,
  }
}

watch(() => form.lineKey, (value) => {
  if (!form.lineName || form.lineName === lineLabel.work || form.lineName === lineLabel.dance) {
    form.lineName = lineLabel[value] || ''
  }
})

async function loadCopy() {
  if (!campusId.value) {
    Object.keys(copyForm).forEach((key) => delete copyForm[key])
    return
  }
  const res = await http.get('/admin/studio', { params: { campusId: campusId.value } })
  Object.assign(copyForm, res.data || {})
}

async function loadTracks() {
  if (!campusId.value) {
    list.value = []
    return
  }
  const res = await http.get('/admin/growth-tracks', { params: { campusId: campusId.value } })
  list.value = res.data || []
}

async function load() {
  await Promise.all([loadCopy(), loadTracks()])
}

const { campusId } = useCampusScope(load)

async function saveCopy() {
  if (!campusId.value) {
    ElMessage.warning('请先选择顶部校区')
    return
  }
  copySaving.value = true
  try {
    await http.put('/admin/studio', copyForm, { params: { campusId: campusId.value } })
    ElMessage.success('文案已保存')
  } finally {
    copySaving.value = false
  }
}

function edit(row) {
  if (!campusId.value) {
    ElMessage.warning('请先选择顶部校区')
    return
  }
  Object.assign(form, defaultTrack(), row || {})
  visible.value = true
}

async function save() {
  const payload = { ...form }
  const params = { campusId: campusId.value }
  if (form.id) await http.put(`/admin/growth-tracks/${form.id}`, payload, { params })
  else await http.post('/admin/growth-tracks', payload, { params })
  visible.value = false
  ElMessage.success('已保存')
  await loadTracks()
}

async function remove(row) {
  await ElMessageBox.confirm('确认删除该赛道？', '提示')
  await http.delete(`/admin/growth-tracks/${row.id}`, { params: { campusId: campusId.value } })
  ElMessage.success('已删除')
  await loadTracks()
}
</script>

<style scoped>
.campus-hint {
  margin-bottom: 16px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
}

.growth-form {
  max-width: 760px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar .section-title {
  margin-bottom: 0;
}

.tracks-card {
  margin-top: 16px;
}

.mobile-summary {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.growth-save-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 30;
  padding: 10px 12px calc(10px + env(safe-area-inset-bottom, 0px));
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid #ececf0;
  box-shadow: 0 -4px 16px rgba(22, 22, 28, 0.06);
  backdrop-filter: blur(8px);
}

.growth-save-btn {
  width: 100%;
}

@media (max-width: 768px) {
  .growth-page {
    padding-bottom: calc(64px + env(safe-area-inset-bottom, 0px));
  }

  .campus-hint {
    margin-bottom: 10px;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .toolbar-add {
    width: 100%;
    margin-left: 0 !important;
  }
}
</style>
