<template>
  <div class="surveys-page">
    <el-alert
      v-if="!campusId"
      type="warning"
      :closable="false"
      show-icon
      class="campus-hint"
      title="请先在顶部选择校区，再管理该校区问卷"
    />
    <el-alert
      v-else-if="!isMobile"
      type="info"
      :closable="false"
      show-icon
      class="campus-hint"
      :title="`正在管理：${campusLabel}`"
    />

    <div class="page-card">
      <div class="toolbar">
        <el-button type="primary" class="toolbar-add" :disabled="!campusId" @click="editSurvey()">新建问卷</el-button>
      </div>

      <div v-if="isMobile" class="mobile-feed">
        <div v-for="row in list" :key="row.id" class="mobile-feed-item">
          <div class="mobile-feed-head">
            <span class="mobile-feed-title">{{ row.title || '—' }}</span>
            <span class="mobile-feed-status">{{ row.enabled ? '已启用' : '已停用' }}</span>
          </div>
          <div v-if="row.description" class="mobile-feed-main mobile-summary">{{ row.description }}</div>
          <div class="mobile-feed-meta">
            <span>{{ (row.questions || []).length }} 题</span>
            <span>{{ row.responseCount ?? 0 }} 份答卷</span>
            <span>排序 {{ row.sortOrder ?? 0 }}</span>
          </div>
          <div class="table-actions">
            <el-button link type="primary" @click="editSurvey(row)">编辑</el-button>
            <el-button link type="primary" @click="openResponses(row)">答卷</el-button>
            <el-button link type="danger" @click="removeSurvey(row)">删除</el-button>
          </div>
        </div>
        <div v-if="!list.length" class="mobile-feed-empty">暂无问卷</div>
      </div>

      <el-table v-else :data="list">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="题目数" width="90">
          <template #default="{ row }">{{ (row.questions || []).length }}</template>
        </el-table-column>
        <el-table-column prop="responseCount" label="答卷" width="90" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="启用" width="80">
          <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" class-name="col-actions" align="left" header-align="left">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="editSurvey(row)">编辑</el-button>
              <el-button link type="primary" @click="openResponses(row)">答卷</el-button>
              <el-button link type="danger" @click="removeSurvey(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

  <el-dialog v-model="visible" :title="form.id ? '编辑问卷' : '新建问卷'" width="760px" top="5vh">
    <el-form :model="form" label-width="90px">
      <el-form-item label="标题"><el-input v-model="form.title" maxlength="80" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      <el-form-item label="题目">
        <div class="question-list">
          <div v-for="(question, qIndex) in form.questions" :key="qIndex" class="question-card">
            <div class="question-head">
              <el-input v-model="question.title" placeholder="题目标题" />
              <el-select v-model="question.type" class="question-type" @change="onTypeChange(question)">
                <el-option label="填空" value="text" />
                <el-option label="单选" value="single" />
                <el-option label="多选" value="multi" />
              </el-select>
              <el-switch v-model="question.required" active-text="必填" />
              <el-button link type="danger" @click="form.questions.splice(qIndex, 1)">删除</el-button>
            </div>
            <div v-if="question.type !== 'text'" class="option-list">
              <div v-for="(option, oIndex) in question.options" :key="oIndex" class="option-row">
                <el-input v-model="option.label" placeholder="选项文案" />
                <el-button link type="danger" @click="question.options.splice(oIndex, 1)">删</el-button>
              </div>
              <el-button size="small" @click="question.options.push({ label: '' })">加选项</el-button>
            </div>
          </div>
          <el-button @click="addQuestion">新增题目</el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="saveSurvey">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="responseVisible" :size="responseDrawerSize" :title="responseTitle">
    <div class="response-toolbar">
      <el-input
        v-model="responseKeyword"
        placeholder="搜索学员昵称"
        clearable
        @keyup.enter="searchResponses"
        @clear="searchResponses"
      />
      <el-button @click="searchResponses">查询</el-button>
    </div>

    <div v-if="isMobile" class="mobile-feed">
      <div
        v-for="row in responses"
        :key="row.id"
        class="mobile-feed-item mobile-feed-clickable"
        @click="showResponseDetail(row)"
      >
        <div class="mobile-feed-head">
          <span class="mobile-feed-title">{{ row.nickname || '—' }}</span>
        </div>
        <div class="mobile-feed-meta">
          <span>{{ formatTime(row.createdAt) }}</span>
        </div>
        <div class="table-actions">
          <el-button link type="primary" @click.stop="showResponseDetail(row)">查看</el-button>
        </div>
      </div>
      <div v-if="!responses.length" class="mobile-feed-empty">暂无答卷</div>
    </div>

    <el-table v-else :data="responses" @row-click="showResponseDetail">
      <el-table-column prop="nickname" label="学员" width="120" />
      <el-table-column label="提交时间" min-width="160">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="showResponseDetail(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px"
      background
      layout="total, prev, pager, next"
      :total="responseTotal"
      v-model:current-page="responsePage"
      :page-size="responseSize"
      @current-change="loadResponses"
    />
  </el-drawer>

  <el-dialog v-model="detailVisible" title="答卷详情" width="560px">
    <div v-if="detail">
      <p><strong>{{ detail.nickname || '学员' }}</strong> · {{ formatTime(detail.createdAt) }}</p>
      <div v-for="(answer, index) in detail.answers || []" :key="index" class="answer-block">
        <div class="answer-q">{{ index + 1 }}. {{ answer.questionTitle }}</div>
        <div class="answer-a">
          <template v-if="answer.type === 'text'">{{ answer.textValue || '-' }}</template>
          <template v-else>{{ (answer.optionLabels || []).join('、') || '-' }}</template>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { campusName } from '../common/campuses'
import { useCampusScope } from '../composables/useCampusScope'
import { useBreakpoint } from '../composables/useBreakpoint'

const { isMobile } = useBreakpoint()
const list = ref([])
const visible = ref(false)
const responseVisible = ref(false)
const detailVisible = ref(false)
const currentSurvey = ref(null)
const responses = ref([])
const responseTotal = ref(0)
const responsePage = ref(1)
const responseSize = 20
const responseKeyword = ref('')
const detail = ref(null)
const form = reactive(emptyForm())

const campusLabel = computed(() => campusName(campusId.value))
const responseTitle = computed(() => (currentSurvey.value ? `答卷 · ${currentSurvey.value.title}` : '答卷'))
const responseDrawerSize = computed(() => (isMobile.value ? '100%' : '640px'))

function emptyForm() {
  return {
    id: null,
    title: '',
    description: '',
    enabled: true,
    sortOrder: 0,
    questions: [],
  }
}

function emptyQuestion() {
  return {
    title: '',
    type: 'text',
    required: true,
    options: [],
  }
}

function formatTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function load() {
  if (!campusId.value) {
    list.value = []
    return
  }
  const res = await http.get('/admin/surveys', { params: { campusId: campusId.value } })
  list.value = res.data || []
}

function editSurvey(row) {
  Object.assign(form, emptyForm(), row || {}, {
    questions: (row?.questions || []).map((item) => ({
      title: item.title || '',
      type: item.type || 'text',
      required: item.required !== false,
      options: (item.options || []).map((opt) => ({ label: opt.label || '' })),
    })),
  })
  if (!form.questions.length) {
    form.questions.push(emptyQuestion())
  }
  visible.value = true
}

function addQuestion() {
  form.questions.push(emptyQuestion())
}

function onTypeChange(question) {
  if (question.type === 'text') {
    question.options = []
  } else if (!question.options?.length) {
    question.options = [{ label: '' }, { label: '' }]
  }
}

async function saveSurvey() {
  if (!form.title?.trim()) {
    ElMessage.warning('请填写问卷标题')
    return
  }
  const questions = form.questions
    .map((item, index) => ({
      title: item.title?.trim() || '',
      type: item.type || 'text',
      required: item.required !== false,
      sortOrder: index,
      options: (item.options || [])
        .map((opt) => ({ label: (opt.label || '').trim() }))
        .filter((opt) => opt.label),
    }))
    .filter((item) => item.title)
  if (!questions.length) {
    ElMessage.warning('请至少添加一道题目')
    return
  }
  for (const question of questions) {
    if (question.type !== 'text' && !question.options.length) {
      ElMessage.warning(`「${question.title}」请至少添加一个选项`)
      return
    }
  }
  const payload = {
    ...form,
    title: form.title.trim(),
    description: form.description?.trim() || '',
    questions,
  }
  if (form.id) await http.put(`/admin/surveys/${form.id}`, payload, { params: { campusId: campusId.value } })
  else await http.post('/admin/surveys', payload, { params: { campusId: campusId.value } })
  visible.value = false
  ElMessage.success('已保存')
  await load()
}

async function removeSurvey(row) {
  await ElMessageBox.confirm(`确认删除问卷「${row.title}」及其全部答卷？`, '提示')
  await http.delete(`/admin/surveys/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

async function openResponses(row) {
  currentSurvey.value = row
  responseKeyword.value = ''
  responsePage.value = 1
  responseVisible.value = true
  await loadResponses()
}

async function loadResponses() {
  if (!currentSurvey.value?.id) return
  const res = await http.get(`/admin/surveys/${currentSurvey.value.id}/responses`, {
    params: {
      keyword: responseKeyword.value,
      page: responsePage.value,
      size: responseSize,
    },
  })
  responses.value = res.data?.list || []
  responseTotal.value = res.data?.total || 0
}

function searchResponses() {
  responsePage.value = 1
  return loadResponses()
}

function showResponseDetail(row) {
  detail.value = row
  detailVisible.value = true
}

const { campusId } = useCampusScope(load)
</script>

<style scoped>
.campus-hint {
  margin-bottom: 16px;
}

.mobile-summary {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.45;
}

.mobile-feed-clickable {
  cursor: pointer;
}

.response-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.response-toolbar :deep(.el-input) {
  width: 220px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.question-card {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}

.question-head {
  display: flex;
  gap: 8px;
  align-items: center;
}

.question-type {
  width: 120px;
  flex-shrink: 0;
}

.option-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.answer-block {
  margin: 14px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.answer-q {
  font-weight: 600;
  margin-bottom: 6px;
}

.answer-a {
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .campus-hint {
    margin-bottom: 10px;
  }

  .toolbar-add {
    width: 100%;
    margin-left: 0 !important;
  }

  .response-toolbar :deep(.el-input) {
    width: 100% !important;
  }

  .response-toolbar .el-button {
    width: 100%;
    margin-left: 0 !important;
  }

  .question-head {
    flex-direction: column;
    align-items: stretch;
  }

  .question-type {
    width: 100% !important;
  }

  .option-row :deep(.el-input) {
    flex: 1;
  }
}
</style>
