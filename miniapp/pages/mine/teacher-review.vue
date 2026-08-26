<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card">
        <text class="title">反馈老师</text>
        <text class="muted tip">你的评价仅机构内部可见，不会公开展示给其他学员。</text>

        <view class="field">
          <text class="label">选择老师</text>
          <picker mode="selector" :range="teacherNames" :value="teacherIndex" @change="onTeacherChange">
            <view class="picker-value">
              <text :class="teacherId ? '' : 'placeholder'">{{ selectedTeacherName || '请选择老师' }}</text>
              <text class="arrow">›</text>
            </view>
          </picker>
        </view>

        <textarea
          class="input"
          v-model="content"
          maxlength="500"
          placeholder="请填写对老师的评价或建议（至少 5 个字）"
          placeholder-class="placeholder"
        />
        <text class="count muted">{{ content.length }}/500</text>

        <view class="btn-primary submit" :class="{ disabled: submitting }" @click="submit">
          {{ submitting ? '提交中...' : '提交评价' }}
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getTeachers, submitTeacherReview } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { showError, showSuccess, showToast } from '@/common/toast.js'

const teachers = ref([])
const teacherIndex = ref(0)
const content = ref('')
const submitting = ref(false)

const teacherNames = computed(() => teachers.value.map((item) => item.name))
const teacherId = computed(() => teachers.value[teacherIndex.value]?.id || null)
const selectedTeacherName = computed(() => teachers.value[teacherIndex.value]?.name || '')

function onTeacherChange(e) {
  const index = Number(e.detail.value)
  teacherIndex.value = Number.isFinite(index) ? index : 0
}

async function loadTeachers() {
  try {
    teachers.value = await getTeachers()
  } catch {
    teachers.value = []
  }
}

async function submit() {
  if (!ensureLogin()) return
  if (!teacherId.value) {
    showToast('请选择老师')
    return
  }
  const text = content.value.trim()
  if (text.length < 5) {
    showToast('请至少填写 5 个字')
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await submitTeacherReview({ teacherId: teacherId.value, content: text })
    content.value = ''
    showSuccess('已提交，感谢反馈')
  } catch (e) {
    showError(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onShow(async () => {
  if (!ensureLogin()) return
  await loadTeachers()
})
</script>

<style scoped>
.title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.tip {
  display: block;
  font-size: 24rpx;
  line-height: 1.6;
  margin-bottom: 28rpx;
}

.field {
  margin-bottom: 24rpx;
}

.label {
  display: block;
  font-size: 26rpx;
  margin-bottom: 12rpx;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background: #242424;
  color: #ffffff;
  font-size: 28rpx;
}

.placeholder {
  color: #6a6a6a;
}

.arrow {
  color: #6a6a6a;
}

.input {
  width: 100%;
  min-height: 240rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #242424;
  color: #ffffff;
  font-size: 28rpx;
  line-height: 1.6;
  box-sizing: border-box;
}

.count {
  display: block;
  text-align: right;
  font-size: 22rpx;
  margin-top: 12rpx;
}

.submit {
  margin-top: 40rpx;
  height: 88rpx;
  width: 100%;
}

.submit.disabled {
  opacity: 0.6;
}
</style>
