<template>
  <div class="image-field">
    <div class="image-field-row">
      <el-input v-model="model" placeholder="图片地址" />
      <el-upload :show-file-list="false" accept="image/*" :http-request="upload">
        <el-button :loading="uploading">上传</el-button>
      </el-upload>
    </div>
    <el-image
      v-if="preview"
      :src="preview"
      fit="contain"
      class="preview"
      preview-teleported
      :preview-src-list="[preview]"
    >
      <template #error>
        <div class="preview-error">无预览</div>
      </template>
    </el-image>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import http from '../api/http'
import { ElMessage } from 'element-plus'
import { mediaSrc } from '../utils/media'

const props = defineProps({
  modelValue: { type: String, default: '' },
  autoPersist: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'uploaded'])
const uploading = ref(false)

const model = computed({
  get: () => props.modelValue || '',
  set: (v) => emit('update:modelValue', v),
})

const preview = computed(() => mediaSrc(model.value))

async function upload({ file }) {
  uploading.value = true
  try {
    const form = new FormData()
    form.append('file', file)
    const res = await http.post('/admin/upload', form)
    model.value = res.data.url
    emit('uploaded', res.data.url)
    ElMessage.success(props.autoPersist ? '已上传' : '上传成功，请点击保存')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.image-field {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  width: 100%;
}
.image-field-row {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
  min-width: 0;
}
.image-field-row :deep(.el-input) {
  flex: 1;
  min-width: 0;
}
.image-field-row :deep(.el-upload) {
  flex-shrink: 0;
}
.preview {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  background: #f6f6f8;
  border: 1px solid #eee;
  overflow: hidden;
}
.preview :deep(.el-image__inner) {
  object-fit: contain;
}
.preview-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}
</style>
