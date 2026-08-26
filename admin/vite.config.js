import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget = env.VITE_API_PROXY_TARGET || 'http://127.0.0.1:8080'
  const useCloud = /^https:\/\//i.test(apiTarget)

  const proxy = {
    target: apiTarget,
    changeOrigin: true,
    secure: useCloud,
  }

  return {
    plugins: [vue()],
    build: {
      outDir: '../server/src/main/resources/static',
      emptyOutDir: true,
    },
    server: {
      port: 5173,
      strictPort: false,
      proxy: {
        '/api': proxy,
        '/uploads': proxy,
      },
    },
  }
})
