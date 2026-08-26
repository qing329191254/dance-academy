<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page" v-if="item">
    <view class="section">
      <view class="card">
        <text class="tag">{{ item.level }} · {{ meta.name }}</text>
        <text class="title">{{ item.title }}</text>
        <text class="muted summary">{{ item.summary }}</text>
        <view class="meta-row">
          <text>截止日期</text>
          <text>{{ item.deadline }}</text>
        </view>
        <view class="meta-row">
          <text>剩余名额</text>
          <text>{{ item.spots }}</text>
        </view>
        <view class="meta-row">
          <text>所需级别</text>
          <text>{{ item.level }}</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="card note">
        <text class="note-title">报名说明</text>
        <text class="muted">提交后进入机构后台名单，线上/线下筛选后通过小程序通知结果。</text>
      </view>
    </view>

    <view class="section">
      <view class="card resume-card">
        <view class="resume-head">
          <text class="note-title">个人简历</text>
          <text class="optional">选填</text>
        </view>
        <text class="muted resume-tip">可上传图片或 PDF，便于机构了解你的经历。</text>
        <view v-if="applied && item.resumeName" class="resume-file">
          <text class="resume-name">已提交：{{ item.resumeName }}</text>
        </view>
        <view v-else-if="resumeName" class="resume-file">
          <text class="resume-name">{{ resumeName }}</text>
          <text class="resume-clear" @tap.stop="clearResume">清除</text>
        </view>
        <view v-if="!applied" class="resume-actions">
          <view
            class="btn-ghost resume-btn"
            :class="{ disabled: uploading }"
            hover-class="none"
            @tap="pickImage"
          >
            {{ uploading ? '上传中...' : '相册/拍照' }}
          </view>
          <view
            class="btn-ghost resume-btn"
            :class="{ disabled: uploading }"
            hover-class="none"
            @tap="pickChatFile"
          >
            聊天文件
          </view>
        </view>
      </view>
    </view>

    <view class="actions">
      <view class="btn-ghost action-btn" @tap="share">复制报名链接</view>
      <view
        class="action-btn apply-btn"
        :class="applied ? 'btn-cancel' : 'btn-primary'"
        hover-class="none"
        @tap="toggleApply"
      >
        {{ applied ? '取消报名' : '立即报名' }}
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getOpportunities, toggleOpportunityApply, uploadResume } from '@/common/api.js'
import { trackMeta } from '@/common/mock.js'
import { ensureLogin } from '@/common/auth.js'
import { showSuccess, showToast, showError } from '@/common/toast.js'

const key = ref('parttime')
const id = ref('')
const applied = ref(false)
const item = ref(null)
const meta = computed(() => trackMeta[key.value] || { name: '成长' })
const resumeUrl = ref('')
const resumeName = ref('')
const uploading = ref(false)

async function loadItem() {
  try {
    const list = (await getOpportunities(key.value)) || []
    item.value = list.find((o) => String(o.id) === String(id.value)) || null
    applied.value = !!item.value?.applied
    if (applied.value) {
      resumeName.value = item.value.resumeName || resumeName.value
      resumeUrl.value = item.value.resumeUrl || resumeUrl.value
    }
  } catch (e) {
    item.value = null
  }
}

onLoad((query) => {
  key.value = query.key || 'parttime'
  id.value = query.id || ''
})

onShow(() => {
  if (id.value) loadItem()
})

async function toggleApply() {
  if (!ensureLogin()) return
  if (!item.value) return
  try {
    const payload = applied.value
      ? {}
      : { resumeUrl: resumeUrl.value, resumeName: resumeName.value }
    const result = await toggleOpportunityApply(item.value.id, payload)
    applied.value = !!result.applied
    if (applied.value) {
      showSuccess(result.message || '报名成功')
    } else {
      showToast(result.message || '已取消报名')
    }
  } catch (e) {
    showError(e.message || '操作失败')
  }
}

function clearResume() {
  if (uploading.value) return
  resumeUrl.value = ''
  resumeName.value = ''
}

function pickImage() {
  if (!ensureLogin()) return
  if (uploading.value) return
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success(res) {
      const path = res.tempFilePaths?.[0]
      if (path) uploadPicked(path, '简历.jpg')
    },
    fail(err) {
      if (err?.errMsg?.includes('cancel')) return
      showToast('无法打开相册，请重试')
    },
  })
}

function pickChatFile() {
  if (!ensureLogin()) return
  if (uploading.value) return
  if (typeof uni.chooseMessageFile === 'function') {
    uni.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['pdf', 'png', 'jpg', 'jpeg', 'webp'],
      success(res) {
        const file = res.tempFiles?.[0]
        if (!file?.path) return
        const name = file.name || '简历.pdf'
        if (!/\.(png|jpe?g|webp|pdf)$/i.test(name)) {
          showToast('请上传图片或 PDF')
          return
        }
        uploadPicked(file.path, name)
      },
      fail(err) {
        if (err?.errMsg?.includes('cancel')) return
        showToast('无法选择聊天文件，请改用相册/拍照')
      },
    })
    return
  }
  const picker = typeof wx !== 'undefined' ? wx.chooseMessageFile : null
  if (!picker) {
    showToast('当前环境不支持，请使用相册/拍照')
    return
  }
  picker({
    count: 1,
    type: 'file',
    extension: ['pdf', 'png', 'jpg', 'jpeg', 'webp'],
    success(res) {
      const file = res.tempFiles?.[0]
      if (!file?.path) return
      const name = file.name || '简历.pdf'
      if (!/\.(png|jpe?g|webp|pdf)$/i.test(name)) {
        showToast('请上传图片或 PDF')
        return
      }
      uploadPicked(file.path, name)
    },
    fail(err) {
      if (err?.errMsg?.includes('cancel')) return
      showToast('无法选择聊天文件，请改用相册/拍照')
    },
  })
}

async function uploadPicked(filePath, filename) {
  uploading.value = true
  try {
    const data = await uploadResume(filePath, filename)
    resumeUrl.value = data.url || ''
    resumeName.value = filename
    showToast('简历已上传')
  } catch (e) {
    showError(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

function share() {
  uni.setClipboardData({
    data: `/pages/growth/opportunity?key=${key.value}&id=${id.value}`,
    success: () => {
      showToast('链接已复制')
    },
  })
}
</script>

<style scoped>
.tag {
  display: inline-flex;
  margin-bottom: 16rpx;
}

.title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.summary {
  display: block;
  font-size: 28rpx;
  line-height: 1.7;
  margin-bottom: 28rpx;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  padding: 18rpx 0;
  border-top: 1rpx solid #2a2a2e;
  font-size: 26rpx;
  color: #ddd;
}

.note {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.note-title {
  font-size: 28rpx;
  font-weight: 600;
}

.resume-card {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.resume-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.optional {
  font-size: 22rpx;
  color: #8a74e5;
}

.resume-tip {
  font-size: 24rpx;
  line-height: 1.6;
}

.resume-file {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 16rpx 0 4rpx;
}

.resume-name {
  font-size: 26rpx;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resume-clear {
  color: #8a74e5;
  font-size: 24rpx;
  flex-shrink: 0;
}

.resume-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 8rpx;
}

.resume-btn {
  flex: 1;
  height: 72rpx;
  width: 100%;
}

.resume-btn.disabled {
  opacity: 0.6;
}

.actions {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 32rpx 48rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  border-radius: 999rpx;
  box-sizing: border-box;
  white-space: nowrap;
}

.apply-btn.btn-primary {
  padding: 0;
}

.btn-cancel {
  background: rgba(229, 115, 115, 0.15);
  color: #e57373;
  border: 1rpx solid rgba(229, 115, 115, 0.35);
}
</style>
