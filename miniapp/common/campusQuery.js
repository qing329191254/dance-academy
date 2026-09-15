export function parseQueryString(raw) {
  const out = {}
  String(raw || '')
    .replace(/^\?/, '')
    .split('&')
    .forEach((pair) => {
      if (!pair) return
      const eq = pair.indexOf('=')
      const k = decodeURIComponent((eq >= 0 ? pair.slice(0, eq) : pair).trim())
      const v = decodeURIComponent((eq >= 0 ? pair.slice(eq + 1) : '').trim())
      if (k) out[k] = v
    })
  return out
}

export function queryFromLaunchOptions(options) {
  const query = { ...(options?.query || {}) }
  if (query.campusId || query.campus_id) return query
  const path = String(options?.path || '')
  const qIndex = path.indexOf('?')
  if (qIndex >= 0) {
    Object.assign(query, parseQueryString(path.slice(qIndex + 1)))
  }
  return query
}

export function sharePathWithCampus(campusId, homePath = '/pages/home/home') {
  const id = String(campusId || '').trim()
  const query = id ? `campusId=${encodeURIComponent(id)}` : ''
  return query ? `${homePath}?${query}` : homePath
}

/** 同一张分享卡片反复 onShow 时 key 不变，避免把用户后来切换的校区冲回去。 */
export function shareEntryKey(options) {
  const query = queryFromLaunchOptions(options)
  const id = String(query.campusId || query.campus_id || '').trim()
  if (!id) return ''
  return `${options?.scene ?? ''}|${id}`
}

export function shouldApplyShareCampus(options, lastKey) {
  const key = shareEntryKey(options)
  if (!key) return { apply: false, key: lastKey || '' }
  if (key === lastKey) return { apply: false, key }
  return { apply: true, key }
}
