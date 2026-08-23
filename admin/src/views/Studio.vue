<template>
  <div class="page-card">
    <el-form :model="form" label-width="108px" style="max-width: 720px">
      <el-form-item label="机构名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="位置描述"><el-input v-model="form.location" /></el-form-item>
      <el-form-item label="城市"><el-input v-model="form.city" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="营业时间"><el-input v-model="form.businessHours" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="电话展示"><el-input v-model="form.phoneDisplay" /></el-form-item>
      <el-form-item label="Logo">
        <ImageField v-model="form.logo" auto-persist @uploaded="onLogoUploaded" />
      </el-form-item>
      <el-form-item label="开屏图">
        <ImageField v-model="form.splashImage" auto-persist @uploaded="onSplashUploaded" />
      </el-form-item>
      <el-form-item label="品牌介绍"><el-input v-model="form.intro" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="业务"><el-input v-model="form.business" /></el-form-item>
      <el-form-item label="理念"><el-input v-model="form.slogan" /></el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import ImageField from '../components/ImageField.vue'

const form = reactive({})
const saving = ref(false)

onMounted(async () => {
  const res = await http.get('/admin/studio')
  Object.assign(form, res.data || {})
})

async function save() {
  saving.value = true
  try {
    await http.put('/admin/studio', form)
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
</script>
