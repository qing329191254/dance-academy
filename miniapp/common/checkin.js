const CHECKIN_TYPE = 'checkin'
const STORAGE_KEY = 'forget_practice_records'

export const checkInSessions = {
  101: {
    id: '101',
    className: 'HIPHOP',
    date: '2026-08-22',
    time: '16:00-17:15',
    teacher: '金大铭',
    room: '二楼 Room B',
    duration: '75分钟',
  },
  102: {
    id: '102',
    className: 'JAZZ二星课',
    date: '2026-08-22',
    time: '18:00-19:15',
    teacher: '龙龙',
    room: '二楼 Room A',
    duration: '75分钟',
  },
}

export function buildCheckInPayload(sessionId) {
  const session = checkInSessions[sessionId]
  if (!session) return ''
  return JSON.stringify({
    t: CHECKIN_TYPE,
    id: session.id,
    className: session.className,
    date: session.date,
    time: session.time,
    teacher: session.teacher,
    room: session.room,
    duration: session.duration,
  })
}

export function parseCheckInCode(raw) {
  if (!raw || typeof raw !== 'string') return null

  const text = raw.trim()

  try {
    const data = JSON.parse(text)
    if (data.t === CHECKIN_TYPE && data.id) {
      return normalizeSession(data)
    }
  } catch (e) {
    // not json
  }

  const prefixMatch = text.match(/^FORGET_CHECKIN:(\d+)$/i)
  if (prefixMatch) {
    const session = checkInSessions[prefixMatch[1]]
    return session ? { ...session } : null
  }

  return null
}

function normalizeSession(data) {
  const preset = checkInSessions[data.id]
  return {
    id: String(data.id),
    className: data.className || preset?.className || '课程',
    date: data.date || preset?.date || formatToday(),
    time: data.time || preset?.time || '',
    teacher: data.teacher || preset?.teacher || '',
    room: data.room || preset?.room || '',
    duration: data.duration || preset?.duration || '60分钟',
  }
}

function formatToday() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function getPracticeRecords() {
  try {
    return uni.getStorageSync(STORAGE_KEY) || []
  } catch (e) {
    return []
  }
}

export function hasCheckedIn(sessionId, date) {
  return getPracticeRecords().some((item) => item.sessionId === String(sessionId) && item.date === date)
}

export function addPracticeRecord(session) {
  const record = {
    id: `${session.id}-${session.date}-${Date.now()}`,
    sessionId: String(session.id),
    name: session.className,
    date: session.date,
    time: session.time,
    duration: session.duration,
    teacher: session.teacher,
    room: session.room,
    checkedAt: Date.now(),
  }

  if (hasCheckedIn(session.id, session.date)) {
    return { ok: false, reason: 'duplicate', record }
  }

  const list = [record, ...getPracticeRecords()]
  uni.setStorageSync(STORAGE_KEY, list)
  return { ok: true, record }
}

export function handleCheckInScan(result) {
  const session = parseCheckInCode(result)
  if (!session) {
    return { ok: false, message: '无法识别的签到码，请扫描教室二维码' }
  }

  const saved = addPracticeRecord(session)
  if (!saved.ok) {
    return { ok: false, message: '今日该课程已签到，请勿重复扫描' }
  }

  return {
    ok: true,
    message: `${session.className}\n${session.date} ${session.time}\n${session.room}`,
    record: saved.record,
  }
}

async function handleCheckInScanRemote(result) {
  const { checkin } = await import('./api.js')
  try {
    const data = await checkin(result)
    return {
      ok: true,
      message: data.message || '签到成功',
      record: data.record,
    }
  } catch (e) {
    return { ok: false, message: e.message || '签到失败' }
  }
}

export function startCheckInScan(callbacks = {}) {
  uni.scanCode({
    onlyFromCamera: true,
    scanType: ['qrCode'],
    success(res) {
      handleCheckInScanRemote(res.result).then((outcome) => {
        callbacks.onResult?.(outcome)
      })
    },
    fail(err) {
      if (err.errMsg && err.errMsg.includes('cancel')) {
        callbacks.onCancel?.()
        return
      }
      callbacks.onResult?.({
        ok: false,
        message: '请检查相机权限，或在真机上重试',
      })
    },
  })
}
