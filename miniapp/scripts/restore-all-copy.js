const fs = require('fs')
const path = require('path')

const ROOT = path.join(__dirname, '..')

function write(rel, content) {
  const file = path.join(ROOT, rel)
  fs.writeFileSync(file, content, 'utf8')
  console.log('wrote', rel)
}

write('pages/growth/index.vue', `<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <page-brand-header />

    <view class="section">
      <view class="intro card">
        <text class="title">成长中心</text>
        <text class="muted body">{{ growthIntro }}</text>
        <text class="tip">新学员默认享有 T1 权益，可通过消费、年限、考核等途径升级至 T2 / T3。</text>
      </view>
    </view>

    <view class="section">
      <view class="module card work" @click="go('/pages/growth/work')">
        <view class="module-top">
          <text class="module-name">勤工俭学</text>
          <view class="module-arrow">
            <text>进入</text>
            <view class="link-arrow" />
          </view>
        </view>
        <text class="muted">兼职 → 实习 → 管理（T1-T3）</text>
        <view class="chips">
          <text class="chip">兼职 T1</text>
          <text class="chip">实习 T2</text>
          <text class="chip">管理 T3</text>
        </view>
      </view>

      <view class="module card dance" @click="go('/pages/growth/dance')">
        <view class="module-top">
          <text class="module-name">舞蹈发展</text>
          <view class="module-arrow">
            <text>进入</text>
            <view class="link-arrow" />
          </view>
        </view>
        <text class="muted">演出 → 商演 → 教师（T1-T3）</text>
        <view class="chips">
          <text class="chip">演出 T1</text>
          <text class="chip">商演 T2</text>
          <text class="chip">教师 T3</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { growthIntro } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

function go(url) {
  openPage(url)
}
</script>

<style scoped>
.intro {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.title {
  font-size: 40rpx;
  font-weight: 700;
}

.body {
  font-size: 28rpx;
  line-height: 1.7;
}

.tip {
  font-size: 24rpx;
  color: #8a74e5;
  line-height: 1.6;
}

.module {
  margin-bottom: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.work {
  background: linear-gradient(145deg, #1c1c1c, #242038);
}

.dance {
  background: linear-gradient(145deg, #1c1c1c, #2a2030);
}

.module-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-name {
  font-size: 34rpx;
  font-weight: 700;
}

.module-arrow {
  display: inline-flex;
  align-items: center;
  color: #8a74e5;
  font-size: 26rpx;
}

.chips {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-top: 8rpx;
}

.chip {
  font-size: 22rpx;
  color: #d7d0ff;
  background: rgba(138, 116, 229, 0.18);
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
}
</style>
`)

write('pages/growth/track.vue', `<template>
  <page-meta root-background-color="#111111" background-color="#111111" page-style="background-color:#111111;" />
  <view class="page">
    <view class="section">
      <view class="header card">
        <text class="line">{{ meta.line }}</text>
        <text class="title">{{ meta.name }} · {{ meta.level }}</text>
        <text class="muted">以下为近期机会，感兴趣可报名；机构可把报名链接发给学员。</text>
      </view>
    </view>

    <view class="section">
      <view v-if="!list.length" class="empty muted">暂无机会，敬请期待</view>
      <view
        v-for="item in list"
        :key="item.id"
        class="card opp"
        @click="open(item.id)"
      >
        <view class="top">
          <text class="name">{{ item.title }}</text>
          <text class="tag">{{ item.level }}</text>
        </view>
        <text class="muted summary">{{ item.summary }}</text>
        <view class="bottom">
          <text class="muted">截止 {{ item.deadline }} · 名额 {{ item.spots }}</text>
          <view class="accent link-with-arrow">
            <text>报名</text>
            <view class="link-arrow" />
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { opportunities, trackMeta } from '@/common/mock.js'
import { openPage } from '@/common/navigate.js'

const key = ref('parttime')
const meta = computed(() => trackMeta[key.value] || { line: '成长', name: '路径', level: 'T1' })
const list = computed(() => opportunities[key.value] || [])

onLoad((query) => {
  key.value = query.key || 'parttime'
  uni.setNavigationBarTitle({
    title: \`\${meta.value.name}机会\`,
  })
})

function open(id) {
  openPage(\`/pages/growth/opportunity?id=\${id}&key=\${key.value}\`)
}
</script>

<style scoped>
.header {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.line {
  color: #8a74e5;
  font-size: 24rpx;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
}

.opp {
  margin-bottom: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.name {
  font-size: 30rpx;
  font-weight: 600;
  flex: 1;
}

.summary {
  font-size: 26rpx;
  line-height: 1.6;
}

.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
}

.link-with-arrow {
  display: inline-flex;
  align-items: center;
}

.empty {
  text-align: center;
  padding: 80rpx 0;
}
</style>
`)
