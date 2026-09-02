<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card doc">
        <view v-if="ready">
          <view v-if="lines.length" class="notice-body">
            <view v-for="(line, index) in lines" :key="index" class="line">
              <text v-if="line.empty" class="line-text empty">&nbsp;</text>
              <text v-else class="line-text">
                <text
                  v-for="(part, partIndex) in line.parts"
                  :key="partIndex"
                  :class="{ bold: part.bold }"
                >{{ part.text }}</text>
              </text>
            </view>
          </view>
          <text v-else class="empty-tip muted">暂未配置学员须知</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { parseStudentNotice } from '@/common/studentNotice.js'
import { selectedCampusId } from '@/common/campus.js'
import { getBrand } from '@/common/api.js'

const noticeText = ref('')
const ready = ref(false)

const lines = computed(() => parseStudentNotice(noticeText.value))

async function loadNotice() {
  ready.value = false
  try {
    const data = await getBrand(selectedCampusId.value)
    noticeText.value = data.studio?.studentNotice || ''
  } catch {
    noticeText.value = ''
  } finally {
    ready.value = true
  }
}

onShow(() => {
  loadNotice()
})
</script>

<style scoped>
.doc {
  display: flex;
  flex-direction: column;
}

.notice-body {
  display: flex;
  flex-direction: column;
}

.line {
  display: block;
}

.line-text {
  display: inline;
  font-size: 26rpx;
  line-height: 1.8;
  color: #cccccc;
  white-space: pre-wrap;
  word-break: break-word;
}

.line-text.bold,
.bold {
  font-weight: 700;
  color: #eeeeee;
}

.line-text.empty {
  display: block;
  min-height: 0.9em;
}

.empty-tip {
  display: block;
  font-size: 26rpx;
  line-height: 1.8;
  text-align: center;
  padding: 48rpx 0;
}
</style>
