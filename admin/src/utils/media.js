export function mediaSrc(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) return url
  if (url.startsWith('/uploads/') || url.startsWith('/logo.png')) return url
  if (url.startsWith('/static/')) return ''
  return url
}
