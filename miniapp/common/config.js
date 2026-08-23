const LOCAL_ORIGIN = 'http://127.0.0.1:8080'
const CLOUD_ORIGIN = 'https://springboot-1g7c-301404-6-1473444650.sh.run.tcloudbase.com'

/** 本地调试改成 false */
export const USE_CLOUD = true
export const API_ORIGIN = USE_CLOUD ? CLOUD_ORIGIN : LOCAL_ORIGIN
export const API_BASE = `${API_ORIGIN}/api/app`
export const USER_STORAGE_KEY = 'forget_user'

/** 预约提醒订阅消息模板 */
export const BOOKING_TMPL_ID = 'Nqnze6AWP7p9Jm7x5HtI3jioASw0MLp5DUtWHHBx1v8'

export function mediaUrl(url) {
  if (!url) return ''
  if (isPackagedDemoImage(url)) return ''
  if (/^https?:\/\//i.test(url) || url.startsWith('wxfile://')) return url
  if (url.startsWith('/uploads/') || url.startsWith('/logo.png')) return API_ORIGIN + url
  return url
}

/** 小程序包内的演示图，已改为后台上传 */
function isPackagedDemoImage(url) {
  const path = String(url).split('?')[0]
  if (/\/static\/(banners|brand)\//.test(path)) return true
  if (/\/static\/splash\.(jpg|jpeg|png|webp)$/i.test(path)) return true
  if (/\/static\/avatars\/(t\d+|default)\./i.test(path)) return true
  return false
}
