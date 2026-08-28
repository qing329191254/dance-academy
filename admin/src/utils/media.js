export function mediaSrc(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) return url
  if (url.startsWith('/uploads/') || url.startsWith('/logo.png') || url.startsWith('/customer-service-qr.jpg')) return url
  if (url.startsWith('/static/')) return ''
  return url
}
