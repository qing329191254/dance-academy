import { createSSRApp } from 'vue'
import App from './App.vue'
import { applyPageBackground } from '@/common/pageTheme.js'
import { getShareMessage, getShareTimeline, syncShareMenu } from '@/common/share.js'

export function createApp() {
  const app = createSSRApp(App)

  app.mixin({
    onLoad() {
      applyPageBackground()
    },
    onShow() {
      applyPageBackground()
      syncShareMenu()
    },
    onShareAppMessage() {
      return getShareMessage()
    },
    onShareTimeline() {
      return getShareTimeline()
    },
  })

  return {
    app,
  }
}
