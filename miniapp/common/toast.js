import { reactive } from 'vue'

const state = reactive({
  visible: false,
  title: '',
  type: 'info',
  offsetTop: 'calc(env(safe-area-inset-top) + 100rpx)',
})

const DEFAULT_OFFSET = state.offsetTop

let timer = null

export function useToastState() {
  return state
}

export function hideToast() {
  state.visible = false
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

export function showToast(title, options = {}) {
  const { type = 'info', duration = 2000, offsetTop = DEFAULT_OFFSET } = options
  if (!title) return

  hideToast()
  state.title = title
  state.type = type
  state.offsetTop = offsetTop
  state.visible = true

  timer = setTimeout(() => {
    hideToast()
  }, duration)

  return new Promise((resolve) => {
    setTimeout(resolve, duration)
  })
}

export function showSuccess(title, options = {}) {
  const opts = typeof options === 'number' ? { duration: options } : options
  return showToast(title, { type: 'success', ...opts })
}

export function showError(title, options = {}) {
  const opts = typeof options === 'number' ? { duration: options } : options
  return showToast(title, { type: 'error', ...opts })
}
