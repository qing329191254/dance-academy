<template>
  <div>
    <el-alert
      v-if="!campusId"
      type="warning"
      :closable="false"
      show-icon
      class="campus-hint"
      title="请先在顶部选择校区，再编辑该校区门店信息"
    />
    <el-alert
      v-else
      type="info"
      :closable="false"
      show-icon
      class="campus-hint"
      :title="`正在编辑：${campusLabel}`"
    />
    <div class="page-card">
      <el-form :model="form" label-width="108px" style="max-width: 720px" :disabled="!campusId">
        <el-form-item label="机构名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="位置描述"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="form.businessHours" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="电话展示"><el-input v-model="form.phoneDisplay" /></el-form-item>
        <el-form-item label="运营主体">
          <el-input v-model="form.company" maxlength="120" placeholder="隐私政策/用户协议中的公司名称" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.email" maxlength="120" placeholder="用于协议与学员须知展示" />
        </el-form-item>
        <el-form-item label="协议更新日期">
          <el-input v-model="form.legalUpdateDate" maxlength="32" placeholder="如 2026年8月31日" />
        </el-form-item>
        <el-form-item label="Logo">
          <ImageField v-model="form.logo" auto-persist @uploaded="onLogoUploaded" />
        </el-form-item>
        <el-form-item label="开屏图">
          <ImageField v-model="form.splashImage" auto-persist @uploaded="onSplashUploaded" />
        </el-form-item>
        <el-form-item label="分享标题">
          <el-input v-model="form.shareTitle" maxlength="32" show-word-limit placeholder="转发好友、朋友圈共用，如：高校FOR-GET舞室" />
        </el-form-item>
        <el-form-item label="分享封面">
          <ImageField v-model="form.shareImage" auto-persist @uploaded="onShareImageUploaded" />
          <div class="form-tip">建议 5:4（如 500×400），好友转发与朋友圈共用。不上传则使用微信默认截图。</div>
        </el-form-item>
        <el-form-item label="品牌介绍"><el-input v-model="form.intro" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="业务"><el-input v-model="form.business" /></el-form-item>
        <el-form-item label="理念"><el-input v-model="form.slogan" /></el-form-item>
        <el-form-item label="课程产品引导">
          <el-input v-model="form.courseSystemLead" type="textarea" :rows="2" placeholder="课程产品列表页顶部说明" />
        </el-form-item>
        <el-form-item label="首页产品摘要">
          <el-input
            v-model="form.courseSystemHomeSummary"
            type="textarea"
            :rows="2"
            placeholder="首页「课程产品」卡片副标题，回车可换行"
          />
        </el-form-item>
        <el-form-item label="学员须知">
          <el-input
            v-model="form.studentNotice"
            type="textarea"
            :rows="14"
            maxlength="8000"
            show-word-limit
            placeholder="按校区配置全文，换行与空格按原文展示；**文字** 为加粗。留空则小程序显示「暂未配置」。"
          />
          <div class="form-tip">
            示例：第一行写标题，空一行后写正文；<code>**重点条款**</code> 会加粗。小程序「我的 → 学员须知」原样展示，不含固定页眉或联系方式。
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" :disabled="!campusId" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import ImageField from '../components/ImageField.vue'
import { campusName } from '../common/campuses'
import { useCampusScope } from '../composables/useCampusScope'

const form = reactive({})
const saving = ref(false)

const campusLabel = computed(() => campusName(campusId.value))

async function load() {
  if (!campusId.value) {
    Object.keys(form).forEach((key) => delete form[key])
    return
  }
  const res = await http.get('/admin/studio', { params: { campusId: campusId.value } })
  Object.assign(form, { shareTitle: '', shareImage: '' }, res.data || {})
}

const { campusId } = useCampusScope(load)

async function save() {
  if (!campusId.value) {
    ElMessage.warning('请先选择顶部校区')
    return
  }
  saving.value = true
  try {
    await http.put('/admin/studio', form, { params: { campusId: campusId.value } })
    ElMessage.success('已保存')
  } finally {
    saving.value = false
  }
}

async function onLogoUploaded(url) {
  form.logo = url
  await save()
}

async function onSplashUploaded(url) {
  form.splashImage = url
  await save()
}

async function onShareImageUploaded(url) {
  form.shareImage = url
  await save()
}
</script>

<style scoped>
.campus-hint {
  margin-bottom: 16px;
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>
