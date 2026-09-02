<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card doc">
        <text class="doc-title">学员须知</text>
        <text class="doc-meta muted">{{ brandName }} · 请在上课与报名前提前阅读</text>

        <view v-for="(section, index) in sections" :key="index" class="section-block">
          <text class="h">{{ section.title }}</text>
          <text v-for="(para, pIndex) in section.paragraphs" :key="pIndex" class="p">{{ para }}</text>
        </view>

        <view class="section-block">
          <text class="h">七、联系我们</text>
          <text class="p">运营主体：{{ info.company }}</text>
          <text class="p">联系电话：{{ contactPhone }}</text>
          <text class="p">联系邮箱：{{ info.email }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getBrand } from '@/common/api.js'
import { legalInfo } from '@/common/legal.js'
import { parseStudentNotice } from '@/common/studentNotice.js'
import { selectedCampusId } from '@/common/campus.js'

const info = legalInfo
const brandName = ref(info.brand)
const contactPhone = ref(info.phone)
const noticeText = ref('')

const sections = computed(() => parseStudentNotice(noticeText.value))

async function loadNotice() {
  try {
    const data = await getBrand(selectedCampusId.value)
    const studio = data.studio || {}
    if (studio.name) brandName.value = studio.name
    if (studio.phoneDisplay || studio.phone) {
      contactPhone.value = studio.phoneDisplay || studio.phone
    }
    noticeText.value = studio.studentNotice || ''
  } catch {
    noticeText.value = ''
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
  gap: 16rpx;
}

.doc-title {
  font-size: 40rpx;
  font-weight: 700;
}

.doc-meta {
  font-size: 24rpx;
  margin-bottom: 12rpx;
}

.section-block {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.h {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  margin-top: 20rpx;
}

.p {
  display: block;
  font-size: 26rpx;
  line-height: 1.8;
  color: #cccccc;
}
</style>
