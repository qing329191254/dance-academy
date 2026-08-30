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
            <view class="label-row">
              <text class="q">{{ index + 1 }}. {{ answer.questionTitle }}</text>
              <text class="type-tag">{{ typeLabel(answer.type) }}</text>
            </view>
            <text class="a muted">
              {{ answer.type === 'text' ? (answer.textValue || '-') : ((answer.optionLabels || []).join('、') || '-') }}
            </text>
          </view>
        </view>

        <view v-else>
          <view v-for="(question, index) in questions" :key="question.id" class="field">
            <view class="label-row">
              <text class="label">{{ index + 1 }}. {{ question.title }}</text>
              <view class="meta-tags">
                <text class="type-tag">{{ typeLabel(question.type) }}</text>
                <text class="req-tag" :class="question.required ? 'required-tag' : 'optional-tag'">
                  {{ question.required ? '必填' : '选填' }}
                </text>
              </view>
            </view>
            <text v-if="question.type === 'single'" class="hint muted">请选择一项</text>
            <text v-else-if="question.type === 'multi'" class="hint muted">可多选</text>
            <text v-else class="hint muted">最多 500 字</text>

            <view v-if="question.type === 'text'" class="text-wrap">
              <textarea
                class="input"
                :value="answers[question.id]?.textValue || ''"
                maxlength="500"
                placeholder="请填写"
                placeholder-class="placeholder"
                @input="onTextInput(question.id, $event)"
              />
              <text class="count muted">{{ textLength(question.id) }}/500</text>
            </view>

            <view v-else-if="question.type === 'single'" class="options">
              <view
                v-for="option in question.options || []"
                :key="option.id"
                class="option"
                :class="{ active: isSelected(question.id, option.id) }"
                @click="selectSingle(question.id, option.id)"
              >
                <view class="mark radio" :class="{ on: isSelected(question.id, option.id) }">
                  <view v-if="isSelected(question.id, option.id)" class="radio-dot" />
                </view>
                <text class="option-text">{{ option.label }}</text>
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
                <view class="mark check" :class="{ on: isSelected(question.id, option.id) }">
                  <text v-if="isSelected(question.id, option.id)" class="check-icon">✓</text>
                </view>
                <text class="option-text">{{ option.label }}</text>
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

function typeLabel(type) {
  if (type === 'single') return '单选'
  if (type === 'multi') return '多选'
  return '简答'
}

function textLength(questionId) {
  return String(answers[questionId]?.textValue || '').length
}

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
  margin-bottom: 36rpx;
}

.label-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.label,
.q {
  flex: 1;
  font-size: 28rpx;
  font-weight: 600;
  line-height: 1.5;
}

.meta-tags {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8rpx;
  padding-top: 4rpx;
}

.type-tag,
.req-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  line-height: 1.4;
}

.type-tag {
  background: rgba(138, 116, 229, 0.18);
  color: #cbbdff;
}

.required-tag {
  background: rgba(229, 115, 115, 0.18);
  color: #ef9a9a;
}

.optional-tag {
  background: rgba(255, 255, 255, 0.08);
  color: #8a8a8a;
}

.hint {
  display: block;
  font-size: 22rpx;
  margin-bottom: 14rpx;
}

.text-wrap {
  position: relative;
}

.input {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx 20rpx 48rpx;
  border-radius: 16rpx;
  background: #242424;
  color: #ffffff;
  font-size: 28rpx;
  line-height: 1.6;
  box-sizing: border-box;
}

.count {
  position: absolute;
  right: 20rpx;
  bottom: 14rpx;
  font-size: 22rpx;
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
  display: flex;
  align-items: center;
  gap: 16rpx;
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

.option-text {
  flex: 1;
  line-height: 1.4;
}

.mark {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
  border: 2rpx solid #6a6a6a;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mark.radio {
  border-radius: 50%;
}

.mark.check {
  border-radius: 6rpx;
}

.mark.on {
  border-color: #8a74e5;
  background: #8a74e5;
}

.radio-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #ffffff;
}

.check-icon {
  font-size: 20rpx;
  color: #ffffff;
  line-height: 1;
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

.a {
  display: block;
  font-size: 26rpx;
  line-height: 1.5;
  margin-top: 8rpx;
}
</style>
