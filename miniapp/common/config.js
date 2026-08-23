const LOCAL_ORIGIN = 'http://127.0.0.1:8080'
const CLOUD_ORIGIN = 'https://springboot-1g7c-301404-6-1473444650.sh.run.tcloudbase.com'

/** 本地调试改成 false */
export const USE_CLOUD = true
export const API_ORIGIN = USE_CLOUD ? CLOUD_ORIGIN : LOCAL_ORIGIN
export const API_BASE = `${API_ORIGIN}/api/app`

export function mediaUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url) || url.startsWith('wxfile://')) return url
  if (url.startsWith('/uploads/') || url.startsWith('/logo.png')) return API_ORIGIN + url
  return url
}
