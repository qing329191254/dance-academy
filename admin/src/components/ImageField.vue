<template>
  <div class="image-field">
    <div class="image-field-row">
      <el-input v-model="model" placeholder="图片地址" />
      <el-upload
        :show-file-list="false"
        accept="image/*"
        :http-request="upload"
        :before-upload="beforeUpload"
      >
        <el-button :loading="uploading">上传</el-button>
      </el-upload>
    </div>
    <div class="hint">{{ hintText }}</div>
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
import { compressImageFile, getImagePreset } from '../utils/compressImage'

const props = defineProps({
  modelValue: { type: String, default: '' },
  autoPersist: { type: Boolean, default: false },
  /** avatar | hero | share | default */
  purpose: { type: String, default: 'default' },
})
const emit = defineEmits(['update:modelValue', 'uploaded'])
const uploading = ref(false)

const model = computed({
  get: () => props.modelValue || '',
  set: (v) => emit('update:modelValue', v),
})

const preview = computed(() => mediaSrc(model.value))
const preset = computed(() => getImagePreset(props.purpose))
const hintText = computed(() => {
  const kb = Math.round(preset.value.maxBytes / 1024)
  return `上传前自动压缩：最长边 ≤ ${preset.value.maxEdge}px，建议 ≤ ${kb}KB`
})

function beforeUpload(file) {
  if (!file.type?.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return false
  }
  // Hard reject absurd originals before even trying canvas (40MB spring limit exists,
  // but 25MB+ still freezes browser compress).
  if (file.size > 25 * 1024 * 1024) {
    ElMessage.error('原图过大（超过 25MB），请先在本地缩小后再上传')
    return false
  }
  return true
}

async function upload({ file }) {
  uploading.value = true
  try {
    const compressed = await compressImageFile(file, props.purpose)
    const form = new FormData()
    form.append('file', compressed)
    const res = await http.post('/admin/upload', form)
    model.value = res.data.url
    emit('uploaded', res.data.url)
    const before = Math.round(file.size / 1024)
    const after = Math.round(compressed.size / 1024)
    ElMessage.success(
      props.autoPersist
        ? `已上传（${before}KB → ${after}KB）`
        : `上传成功（${before}KB → ${after}KB），请点击保存`,
    )
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
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
.hint {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
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
