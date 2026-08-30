<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="card">
        <text class="title">教师简历</text>
        <text class="muted tip">填写自我介绍，并上传照片、视频。后台老师档案可查看你提交的内容。</text>

        <view class="field">
          <text class="label">文字自我介绍</text>
          <textarea
            class="input"
            v-model="resumeIntro"
            maxlength="2000"
            placeholder="介绍你的教学风格、经历与擅长方向"
            placeholder-class="placeholder"
          />
          <text class="count muted">{{ resumeIntro.length }}/2000</text>
        </view>

        <view class="field">
          <text class="label">照片（最多 12 张）</text>
          <view class="photo-grid">
            <view v-for="(item, index) in photos" :key="item.url + index" class="photo-item">
              <image class="photo" :src="item.displayUrl" mode="aspectFill" @click="previewPhoto(index)" />
              <view class="remove" @click="photos.splice(index, 1)">×</view>
            </view>
            <view v-if="photos.length < 12" class="add-box" @click="addPhoto">
              <text class="add-plus">+</text>
              <text class="add-text">添加照片</text>
            </view>
          </view>
        </view>

        <view class="field">
          <text class="label">视频（最多 6 个）</text>
          <view v-for="(item, index) in videos" :key="item.url + index" class="video-card">
            <video class="video" :src="item.displayUrl" controls />
            <view class="video-remove" @click="videos.splice(index, 1)">删除视频</view>
          </view>
          <view v-if="videos.length < 6" class="btn-ghost add-video" @click="addVideo">添加视频</view>
          <text class="muted tip-sm">建议上传压缩后的 mp4，单文件不超过 35MB</text>
        </view>

        <view class="btn-primary submit" :class="{ disabled: saving }" @click="save">
          {{ saving ? '保存中...' : '保存简历' }}
        </view>
      </view>
    </view>
    <app-toast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getTeacherResume, saveTeacherResume, uploadMediaFile } from '@/common/api.js'
import { ensureLogin } from '@/common/auth.js'
import { mediaUrl } from '@/common/config.js'
import { showError, showSuccess, showToast } from '@/common/toast.js'

const resumeIntro = ref('')
const photos = ref([])
const videos = ref([])
const saving = ref(false)

function mapMedia(list) {
  return (list || []).map((item) => ({
    url: item.url,
    displayUrl: mediaUrl(item.url),
  }))
}

async function load() {
  try {
    const data = await getTeacherResume()
    resumeIntro.value = data.resumeIntro || ''
    photos.value = mapMedia(data.photos)
    videos.value = mapMedia(data.videos)
  } catch (e) {
    showError(e.message || '加载失败')
  }
}

function previewPhoto(index) {
  uni.previewImage({
    current: index,
    urls: photos.value.map((item) => item.displayUrl),
  })
}

function addPhoto() {
  uni.chooseImage({
    count: Math.min(9, 12 - photos.value.length),
    sizeType: ['compressed'],
    success: async (res) => {
      const paths = res.tempFilePaths || []
      for (const path of paths) {
        try {
          showToast('上传中...')
          const uploaded = await uploadMediaFile(path, `photo-${Date.now()}.jpg`)
          const url = uploaded?.url
          if (url) {
            photos.value.push({ url, displayUrl: mediaUrl(url) })
          }
        } catch (e) {
          showError(e.message || '照片上传失败')
        }
      }
    },
  })
}

function addVideo() {
  uni.chooseVideo({
    sourceType: ['album', 'camera'],
    compressed: true,
    maxDuration: 60,
    success: async (res) => {
      const path = res.tempFilePath
      if (!path) return
      try {
        showToast('视频上传中...')
        const name = `video-${Date.now()}.mp4`
        const uploaded = await uploadMediaFile(path, name)
        const url = uploaded?.url
        if (url) {
          videos.value.push({ url, displayUrl: mediaUrl(url) })
        }
      } catch (e) {
        showError(e.message || '视频上传失败')
      }
    },
  })
}

async function save() {
  if (!ensureLogin()) return
  if (saving.value) return
  saving.value = true
  try {
    await saveTeacherResume({
      resumeIntro: resumeIntro.value.trim(),
      photos: photos.value.map((item) => ({ url: item.url })),
      videos: videos.value.map((item) => ({ url: item.url })),
    })
    showSuccess('简历已保存')
    await load()
  } catch (e) {
    showError(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onLoad(() => {
  if (!ensureLogin()) return
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

.tip-sm {
  display: block;
  font-size: 22rpx;
  margin-top: 12rpx;
}

.field {
  margin-bottom: 32rpx;
}

.label {
  display: block;
  font-size: 26rpx;
  margin-bottom: 12rpx;
}

.input {
  width: 100%;
  min-height: 220rpx;
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

.count {
  display: block;
  text-align: right;
  font-size: 22rpx;
  margin-top: 8rpx;
}

.photo-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.photo-item,
.add-box {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
  position: relative;
  background: #242424;
}

.photo {
  width: 100%;
  height: 100%;
}

.remove {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 999rpx;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  text-align: center;
  line-height: 40rpx;
  font-size: 28rpx;
}

.add-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1rpx dashed #555;
}

.add-plus {
  font-size: 48rpx;
  color: #8a74e5;
  line-height: 1;
}

.add-text {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8a8a8a;
}

.video-card {
  margin-bottom: 16rpx;
}

.video {
  width: 100%;
  height: 360rpx;
  border-radius: 16rpx;
  background: #000;
}

.video-remove {
  margin-top: 12rpx;
  text-align: center;
  color: #e57373;
  font-size: 26rpx;
}

.add-video {
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  border-radius: 999rpx;
  border: 1rpx solid rgba(138, 116, 229, 0.45);
  color: #cbbdff;
  font-size: 26rpx;
}

.submit {
  margin-top: 12rpx;
  height: 88rpx;
  width: 100%;
}

.submit.disabled {
  opacity: 0.6;
}
</style>
