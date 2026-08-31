<script>
import { applyPageBackground, SPLASH_BG } from '@/common/pageTheme.js'
import { preloadTabPagesAsync } from '@/common/preloadTabs.js'
import { USE_CLOUD } from '@/common/config.js'
import { loadCampuses, selectedCampusId } from '@/common/campus.js'
import { getBrand } from '@/common/api.js'

const HOME_TAB = '/pages/home/home'

function goHomeTab() {
  uni.switchTab({
    url: HOME_TAB,
    fail() {
      uni.reLaunch({ url: HOME_TAB })
    },
  })
}

export default {
  onLaunch(options) {
    // #ifdef MP-WEIXIN
    if (USE_CLOUD && typeof wx !== 'undefined' && wx.cloud) {
      wx.cloud.init({
        env: 'prod-d0g3w5hfzfd320462',
        traceUser: true,
      })
    }
    // #endif
    loadCampuses()
    preloadTabPagesAsync()
    getBrand(selectedCampusId.value).catch(() => {})
    const path = options?.path || ''
    if (path.includes('splash/splash')) {
      goHomeTab()
      return
    }
    applyPageBackground()
  },
  onPageNotFound(res) {
    if (res?.path?.includes('splash/splash')) {
      goHomeTab()
      return
    }
    goHomeTab()
  },
}
</script>

<style lang="scss">
/* #ifdef H5 */
html,
body,
#app {
  background: #0a0a0a;
}

@media (min-width: 480px) {
  uni-app {
    max-width: 390px !important;
    margin: 0 auto !important;
    min-height: 100vh;
    box-shadow: 0 0 0 1px #222;
    background: #111;
    display: block !important;
  }
}
/* #endif */

page {
  background: #111111;
  background-color: #111111;
  color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', sans-serif;
  box-sizing: border-box;
}

/* #ifdef MP-WEIXIN */
uni-page-body,
uni-page-wrapper,
uni-page-head + uni-page-wrapper {
  background: #111111 !important;
  background-color: #111111 !important;
}
/* #endif */

view,
text,
image {
  box-sizing: border-box;
}

.page {
  min-height: 100vh;
  background: #111111;
  color: #ffffff;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.section {
  padding: 32rpx 32rpx 8rpx;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #ffffff;
}

.section-more {
  display: inline-flex;
  align-items: center;
  font-size: 24rpx;
  color: #a0a0a0;
  padding: 8rpx 20rpx;
  border: 1rpx solid #3a3a3a;
  border-radius: 999rpx;
}

.link-arrow {
  width: 10rpx;
  height: 10rpx;
  margin-left: 10rpx;
  border-top: 2rpx solid currentColor;
  border-right: 2rpx solid currentColor;
  transform: rotate(45deg);
  flex-shrink: 0;
}

.card {
  background: #1c1c1c;
  border-radius: 20rpx;
  padding: 28rpx;
}

.accent {
  color: #8a74e5;
}

.muted {
  color: #9a9a9a;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #8a74e5;
  color: #ffffff;
  border-radius: 999rpx;
  padding: 16rpx 40rpx;
  font-size: 28rpx;
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #2a2a2a;
  color: #ffffff;
  border-radius: 999rpx;
  padding: 16rpx 40rpx;
  font-size: 28rpx;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 14rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  background: rgba(138, 116, 229, 0.2);
  color: #8a74e5;
}
</style>
