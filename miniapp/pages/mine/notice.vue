<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card doc">
        <text class="doc-title">学员须知</text>
        <text class="doc-meta muted">{{ info.brand }} · 请在上课与报名前提前阅读</text>

        <view v-for="(section, index) in sections" :key="index" class="section-block">
          <text class="h">{{ section.title }}</text>
          <text v-for="(para, pIndex) in section.paragraphs" :key="pIndex" class="p">{{ para }}</text>
        </view>

        <view class="section-block">
          <text class="h">七、联系我们</text>
          <text class="p">运营主体：{{ info.company }}</text>
          <text class="p">联系电话：{{ info.phone }}</text>
          <text class="p">联系邮箱：{{ info.email }}</text>
          <text v-if="info.address" class="p">地址：{{ info.address }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getLegalInfo, loadLegalInfo } from '@/common/legal.js'
import { parseStudentNotice } from '@/common/studentNotice.js'
import { selectedCampusId } from '@/common/campus.js'
import { getBrand } from '@/common/api.js'

const info = ref(getLegalInfo())
const noticeText = ref('')

const sections = computed(() => parseStudentNotice(noticeText.value))

async function loadNotice() {
  try {
    const data = await getBrand(selectedCampusId.value)
    info.value = getLegalInfo()
    noticeText.value = data.studio?.studentNotice || ''
  } catch {
    await loadLegalInfo(selectedCampusId.value)
    info.value = getLegalInfo()
    noticeText.value = ''
  }
}

onShow(() => {
  loadNotice()
})
</script>
