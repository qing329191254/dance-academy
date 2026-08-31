const STORAGE_KEY = 'forget_share_config'
const DEFAULT_TITLE = '高校FOR-GET舞室'
const SHARE_PATH = '/pages/home/home'

const HIDE_SHARE_PAGES = new Set([
  'pages/login/login',
  'pages/login/profile',
  'pages/employee/checkin',
])

let cached = {
  title: DEFAULT_TITLE,
  imageUrl: '',
}

try {
  const saved = uni.getStorageSync(STORAGE_KEY)
  if (saved && typeof saved === 'object') {
    if (saved.title) cached.title = String(saved.title)
    if (saved.imageUrl) cached.imageUrl = String(saved.imageUrl)
  }
} catch (e) {}

function persist() {
  try {
    uni.setStorageSync(STORAGE_KEY, cached)
  } catch (e) {}
}

export function applyShareFromStudio(studio) {
  if (!studio) return
  const title = String(studio.shareTitle || '').trim()
  const imageUrl = String(studio.shareImage || '').trim()
  if (title) cached.title = title
  if (imageUrl) cached.imageUrl = imageUrl
  persist()
}

export function getShareMessage() {
  const payload = {
    title: cached.title || DEFAULT_TITLE,
    path: SHARE_PATH,
  }
  if (cached.imageUrl) payload.imageUrl = cached.imageUrl
  return payload
}

export function getShareTimeline() {
  const payload = {
    title: cached.title || DEFAULT_TITLE,
  }
  if (cached.imageUrl) payload.imageUrl = cached.imageUrl
  return payload
}

function currentPageRoute() {
  try {
    const pages = getCurrentPages()
    const last = pages[pages.length - 1]
    return last?.route || ''
  } catch (e) {
    return ''
  }
}

export function shouldHideShare(route = currentPageRoute()) {
  return HIDE_SHARE_PAGES.has(String(route || ''))
}

export function syncShareMenu() {
  if (shouldHideShare()) {
    try {
      uni.hideShareMenu({})
    } catch (e) {}
    return
  }
  try {
    uni.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline'],
    })
  } catch (e) {}
}
