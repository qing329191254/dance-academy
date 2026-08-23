import { createSSRApp } from 'vue'
import App from './App.vue'
import { applyPageBackground } from '@/common/pageTheme.js'

export function createApp() {
  const app = createSSRApp(App)

  app.mixin({
    onLoad() {
      applyPageBackground()
    },
    onShow() {
      applyPageBackground()
    },
  })

  return {
    app,
  }
}
