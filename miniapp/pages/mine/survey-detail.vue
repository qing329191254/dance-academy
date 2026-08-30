<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view v-if="survey" class="section">
      <view class="card">
        <text class="title">{{ survey.title }}</text>
        <text v-if="survey.description" class="muted tip">{{ survey.description }}</text>

        <view v-if="survey.submitted" class="done-box">
          <text class="done-title">你已提交过该问卷</text>
          <view v-for="(answer, index) in myAnswers" :key="index" class="answer-block">
            <text class="q">{{ index + 1 }}. {{ answer.questionTitle }}</text>
            <text class="a muted">
              {{ answer.type === 'text' ? (answer.textValue || '-') : ((answer.optionLabels || []).join('、') || '-') }}
            </text>
          </view>
        </view>

        <view v-else>
          <view v-for="(question, index) in questions" :key="question.id" class="field">
            <text class="label">
              {{ index + 1 }}. {{ question.title }}
              <text v-if="question.required" class="required">*</text>
            </text>

            <textarea
              v-if="question.type === 'text'"
              class="input"
              :value="answers[question.id]?.textValue || ''"
              maxlength="500"
              placeholder="请填写"
              placeholder-class="placeholder"
              @input="onTextInput(question.id, $event)"
            />

            <view v-else-if="question.type === 'single'" class="options">
              <view
                v-for="option in question.options || []"
                :key="option.id"
                class="option"
                :class="{ active: isSelected(question.id, option.id) }"
                @click="selectSingle(question.id, option.id)"
              >
                {{ option.label }}
              </view>
            </view>

            <view v-else class="options">
              <view
                v-for="option in question.options || []"
                :key="option.id"
                class="option"
                :class="{ active: isSelected(question.id, option.id) }"
                @click="toggleMulti(question.id, option.id)"
              >
                {{ option.label }}
              </view>
            </view>
          </view>

          <view class="btn-primary submit" :class="{ disabled: submitting }" @click="submit">
            {{ submitting ? '提交中...' : '提交问卷' }}
          </view>
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getSurveyDetail, submitSurvey } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { showError, showSuccess, showToast } from '@/common/toast.js'

const surveyId = ref(null)
const survey = ref(null)
const answers = reactive({})
const submitting = ref(false)

const questions = computed(() => survey.value?.questions || [])
const myAnswers = computed(() => survey.value?.myResponse?.answers || [])

function ensureAnswer(questionId) {
  if (!answers[questionId]) {
    answers[questionId] = { textValue: '', optionIds: [] }
  }
  return answers[questionId]
}

function onTextInput(questionId, e) {
  ensureAnswer(questionId).textValue = e.detail.value || ''
}

function isSelected(questionId, optionId) {
  return (ensureAnswer(questionId).optionIds || []).includes(optionId)
}

function selectSingle(questionId, optionId) {
  ensureAnswer(questionId).optionIds = [optionId]
}

function toggleMulti(questionId, optionId) {
  const row = ensureAnswer(questionId)
  const set = new Set(row.optionIds || [])
  if (set.has(optionId)) set.delete(optionId)
  else set.add(optionId)
  row.optionIds = Array.from(set)
}

async function load() {
  try {
    survey.value = await getSurveyDetail(surveyId.value)
    for (const question of survey.value?.questions || []) {
      ensureAnswer(question.id)
    }
  } catch (e) {
    showError(e.message || '加载失败')
  }
}

async function submit() {
  if (!ensureLogin()) return
  for (const question of questions.value) {
    const row = ensureAnswer(question.id)
    if (!question.required) continue
    if (question.type === 'text' && !String(row.textValue || '').trim()) {
      showToast(`请填写：${question.title}`)
      return
    }
    if (question.type !== 'text' && !(row.optionIds || []).length) {
      showToast(`请选择：${question.title}`)
      return
    }
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await submitSurvey(surveyId.value, {
      answers: questions.value.map((question) => ({
        questionId: question.id,
        textValue: answers[question.id]?.textValue || '',
        optionIds: answers[question.id]?.optionIds || [],
      })),
    })
    showSuccess('提交成功')
    await load()
  } catch (e) {
    showError(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onLoad((query) => {
  surveyId.value = Number(query?.id || 0)
  if (!ensureLogin()) return
  if (!surveyId.value) {
    showToast('问卷不存在')
    return
  }
  load()
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
  margin-bottom: 32rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
  line-height: 1.5;
}

.required {
  color: #e57373;
  margin-left: 4rpx;
}

.input {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #242424;
  color: #ffffff;
  font-size: 28rpx;
  line-height: 1.6;
  box-sizing: border-box;
}

.placeholder {
  color: #6a6a6a;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.option {
  padding: 22rpx 24rpx;
  border-radius: 16rpx;
  background: #242424;
  color: #ffffff;
  font-size: 28rpx;
  border: 1rpx solid transparent;
}

.option.active {
  border-color: #8a74e5;
  background: rgba(138, 116, 229, 0.16);
  color: #cbbdff;
}

.submit {
  margin-top: 16rpx;
  height: 88rpx;
  width: 100%;
}

.submit.disabled {
  opacity: 0.6;
}

.done-box {
  margin-top: 8rpx;
}

.done-title {
  display: block;
  font-size: 28rpx;
  color: #8a74e5;
  margin-bottom: 24rpx;
}

.answer-block {
  margin-bottom: 24rpx;
}

.q {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.a {
  display: block;
  font-size: 26rpx;
  line-height: 1.5;
}
</style>
