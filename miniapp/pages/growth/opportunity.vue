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
        <text class="muted">提交后进入机构后台名单，线上/线下筛选后通过小程序通知结果。成长中心不涉及收费支付。</text>
      </view>
    </view>

    <view class="actions">
      <view class="btn-ghost action-btn" @click="share">复制报名链接</view>
      <view
        class="action-btn apply-btn"
        :class="applied ? 'btn-cancel' : 'btn-primary'"
        @click="toggleApply"
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
import { getOpportunities, toggleOpportunityApply } from '@/common/api.js'
import { trackMeta } from '@/common/mock.js'
import { ensureLogin } from '@/common/auth.js'
import { showSuccess, showToast, showError } from '@/common/toast.js'

const key = ref('parttime')
const id = ref('')
const applied = ref(false)
const item = ref(null)
const meta = computed(() => trackMeta[key.value] || { name: '成长' })

async function loadItem() {
  try {
    const list = (await getOpportunities(key.value)) || []
    item.value = list.find((o) => String(o.id) === String(id.value)) || null
    applied.value = !!item.value?.applied
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
    const result = await toggleOpportunityApply(item.value.id)
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
