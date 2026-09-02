import { API_BASE, USER_STORAGE_KEY, mediaUrl } from './config.js'
import { request } from './request.js'
import { applyShareFromStudio } from './share.js'
import { applyLegalFromStudio } from './legal.js'

function formatDate(value) {
  if (!value) return ''
  if (Array.isArray(value)) {
    const [y, m, d] = value
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  }
  return String(value).slice(0, 10)
}

function mapTeacher(item) {
  if (!item) return null
  return {
    ...item,
    avatar: mediaUrl(item.avatar),
  }
}

function mapCourseModule(item) {
  if (!item) return null
  return {
    ...item,
    key: item.moduleKey || item.key || '',
    cover: mediaUrl(item.cover),
    customerServiceQr: mediaUrl(item.customerServiceQr),
    desc: item.desc || item.description || '',
    price: item.price ?? item.priceDisplay ?? '',
    unit: item.unit || item.priceUnit || '节',
    highlights: Array.isArray(item.highlights) ? item.highlights : [],
  }
}

function mapStudio(item) {
  if (!item) return {}
  return {
    ...item,
    logo: mediaUrl(item.logo),
    splashImage: mediaUrl(item.splashImage),
    shareImage: mediaUrl(item.shareImage),
  }
}

export function getHome(campusId) {
  const query = campusId ? `?campusId=${encodeURIComponent(campusId)}` : ''
  return request({ url: `/home${query}` }).then((data) => {
    const studio = mapStudio(data.studio)
    applyShareFromStudio(studio)
    applyLegalFromStudio(studio)
    return {
      studio,
      banners: (data.banners || []).map(mediaUrl),
      teachers: (data.teachers || []).map(mapTeacher),
    }
  })
}

export function getBrand(campusId) {
  const query = campusId ? `?campusId=${encodeURIComponent(campusId)}` : ''
  return request({ url: `/brand${query}` }).then((data) => {
    const studio = mapStudio(data.studio)
    applyShareFromStudio(studio)
    applyLegalFromStudio(studio)
    return {
      studio,
      photos: (data.photos || []).map(mediaUrl),
    }
  })
}

export function getTeachers() {
  return request({ url: '/teachers' }).then((list) => (list || []).map(mapTeacher))
}

export function getTeacher(id) {
  return request({ url: `/teachers/${id}` }).then(mapTeacher)
}

export function getCourseIntro(campusId) {
  const query = campusId ? `?campusId=${encodeURIComponent(campusId)}` : ''
  return request({ url: `/course-intro${query}` }).then((data) => ({
    trial: mapCourseModule(data.trial),
    systemModules: (data.systemModules || []).map(mapCourseModule),
    systemLead: data.systemLead || '',
    systemHomeSummary: data.systemHomeSummary || '',
  }))
}

export function getCourseModule(id) {
  return request({ url: `/course-modules/${id}` }).then(mapCourseModule)
}

export function getSchedules(type, date, campusId) {
  const params = [`type=${type}`]
  if (date) params.push(`date=${date}`)
  if (campusId) params.push(`campusId=${encodeURIComponent(campusId)}`)
  return request({ url: `/schedules?${params.join('&')}` })
}

export function getLeaderboard(period, campusId) {
  const params = [`period=${period || 'month'}`]
  if (campusId) params.push(`campusId=${encodeURIComponent(campusId)}`)
  return request({ url: `/leaderboard?${params.join('&')}` }).then((data) => ({
    ...data,
    list: (data.list || []).map((item) => ({
      ...item,
      avatar: mediaUrl(item.avatar),
    })),
  }))
}

export function getSchools() {
  return request({ url: '/schools' })
}

export function loginByCode(code) {
  return request({ url: '/auth/login', method: 'POST', data: { code } })
}

export function saveProfile(payload) {
  return request({ url: '/auth/profile', method: 'POST', data: payload })
}

export function uploadAvatar(filePath) {
  return compressImage(filePath)
    .then(readFileBase64)
    .then((imageBase64) => request({
      url: '/upload',
      method: 'POST',
      data: {
        imageBase64,
        filename: 'avatar.jpg',
      },
    }))
}

export function uploadResume(filePath, filename) {
  const name = filename || 'resume.jpg'
  const isImage = /\.(png|jpe?g|webp|gif)$/i.test(name)
  const prepare = isImage ? compressImage(filePath) : Promise.resolve(filePath)
  return prepare
    .then(readFileBase64)
    .then((fileBase64) => request({
      url: '/upload',
      method: 'POST',
      data: {
        fileBase64,
        filename: name,
      },
    }))
}

const UPLOAD_TOO_LARGE_MSG = '文件过大，请压缩后再上传（视频建议 35MB 以内）'

function parseUploadBody(raw) {
  if (raw == null || raw === '') return {}
  if (typeof raw !== 'string') return raw
  try {
    return JSON.parse(raw)
  } catch {
    return {}
  }
}

function isOversizedMessage(text) {
  if (!text) return false
  return /too large|max upload|size exceeded|文件过大|超出|413|payload too large/i.test(String(text))
}

function uploadErrorMessage(statusCode, body) {
  if (statusCode === 413) return UPLOAD_TOO_LARGE_MSG
  const msg = body?.message || body?.msg || body?.error
  if (msg && isOversizedMessage(msg)) return UPLOAD_TOO_LARGE_MSG
  if (statusCode === 401 || body?.code === 401) return msg || '请先登录'
  if (msg) return msg
  if (statusCode === 502 || statusCode === 504) return '上传超时，请压缩视频后重试'
  if (statusCode >= 500) return '服务器繁忙，请稍后重试'
  if (statusCode >= 400) return '上传失败，请稍后重试'
  return '上传失败'
}

export function uploadMediaFile(filePath, filename, options = {}) {
  const { onProgress } = options
  return new Promise((resolve, reject) => {
    const token = (() => {
      try {
        return uni.getStorageSync(USER_STORAGE_KEY)?.token || ''
      } catch (e) {
        return ''
      }
    })()
    const uploadTask = uni.uploadFile({
      url: `${API_BASE}/upload-media`,
      filePath,
      name: 'file',
      formData: {
        filename: filename || 'media.bin',
      },
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'X-App-Token': token,
        'X-Token': token,
      },
      success(res) {
        const body = parseUploadBody(res.data)
        const statusCode = res.statusCode || 0
        if (statusCode === 401 || body?.code === 401) {
          reject(new Error(uploadErrorMessage(statusCode, body)))
          return
        }
        if (statusCode >= 400 || (typeof body?.code === 'number' && body.code !== 0)) {
          reject(new Error(uploadErrorMessage(statusCode, body)))
          return
        }
        if (!body || (typeof body?.code === 'number' && body.code !== 0)) {
          reject(new Error('上传失败'))
          return
        }
        resolve(body?.data ?? body)
      },
      fail(err) {
        const msg = err?.errMsg || ''
        if (/timeout|timed out/i.test(msg)) {
          reject(new Error('上传超时，请压缩视频后重试'))
          return
        }
        reject(new Error('网络异常，上传失败'))
      },
    })
    if (onProgress && uploadTask?.onProgressUpdate) {
      uploadTask.onProgressUpdate((res) => {
        onProgress(res.progress || 0)
      })
    }
  })
}

export function getTeacherResume() {
  return request({ url: '/teacher/resume' })
}

export function saveTeacherResume(payload) {
  return request({ url: '/teacher/resume', method: 'PUT', data: payload })
}

function compressImage(filePath) {
  return new Promise((resolve) => {
    uni.compressImage({
      src: filePath,
      quality: 80,
      success(res) {
        resolve(res.tempFilePath || filePath)
      },
      fail() {
        resolve(filePath)
      },
    })
  })
}

function readFileBase64(filePath) {
  return new Promise((resolve, reject) => {
    const fs = uni.getFileSystemManager()
    fs.readFile({
      filePath,
      encoding: 'base64',
      success(res) {
        resolve(res.data)
      },
      fail() {
        reject(new Error('读取文件失败'))
      },
    })
  })
}

export function getMine(campusId) {
  const query = campusId ? `?campusId=${encodeURIComponent(campusId)}` : ''
  return request({ url: `/mine${query}` })
}

export function getCards() {
  return request({ url: '/cards' }).then((list) =>
    (list || []).map((item) => ({
      ...item,
      cover: mediaUrl(item.cover),
      expire: formatDate(item.expire),
      sectionName: item.sectionName || '',
      validDays: item.validDays || null,
      activatedAt: item.activatedAt || null,
    })),
  )
}

export function getMyCourses() {
  return request({ url: '/my-courses' })
}

export function getBookings() {
  return request({ url: '/bookings' }).then((list) =>
    (list || []).map((item) => ({
      ...item,
      dateText: item.date ? String(item.date).replace(/-/g, '.') : '',
      time: item.time,
      teacher: item.teacher,
    })),
  )
}

export function getWaitlist() {
  return request({ url: '/waitlist' }).then((list) =>
    (list || []).map((item) => ({
      ...item,
      dateText: item.date ? String(item.date).replace(/-/g, '.') : '',
      time: item.time,
      teacher: item.teacher,
    })),
  )
}

export function toggleBooking(scheduleId, date) {
  return request({
    url: '/bookings',
    method: 'POST',
    data: { scheduleId, date },
  })
}

export function getPractice() {
  return request({ url: '/practice' })
}

export function getPracticeRooms(campusId) {
  const query = campusId ? `?campusId=${encodeURIComponent(campusId)}` : ''
  return request({ url: `/practice-rooms${query}` })
}

export function getPracticeRoomSlots(classroomId, date) {
  return request({
    url: `/practice-rooms/${classroomId}/slots?date=${encodeURIComponent(date)}`,
  })
}

export function getPracticeRoomBookings(page = 1, size = 20) {
  return request({ url: `/practice-room-bookings?page=${page}&size=${size}` }).then((data) => {
    const list = (data?.list || []).map((item) => ({
      ...item,
      classDate: formatDate(item.classDate),
      timeText: item.timeText || `${item.startTime || ''}-${item.endTime || ''}`,
      statusLabel: item.statusLabel || item.status,
    }))
    return {
      list,
      total: data?.total || 0,
      page: data?.page || page,
      size: data?.size || size,
    }
  })
}

export function createPracticeRoomBooking(payload) {
  return request({ url: '/practice-room-bookings', method: 'POST', data: payload })
}

export function cancelPracticeRoomBooking(id) {
  return request({ url: `/practice-room-bookings/${id}/cancel`, method: 'POST' })
}

export function checkin(payload, mode) {
  const data = { payload }
  if (mode) data.mode = mode
  return request({ url: '/checkin', method: 'POST', data })
}

export function getGrowthContent(campusId) {
  const query = campusId ? `?campusId=${encodeURIComponent(campusId)}` : ''
  return request({ url: `/growth-content${query}` }).then((data) => ({
    intro: data.intro || '',
    levelTip: data.levelTip || '',
    workLead: data.workLead || '',
    danceLead: data.danceLead || '',
    workModuleSummary: data.workModuleSummary || '',
    danceModuleSummary: data.danceModuleSummary || '',
    workTracks: (data.workTracks || []).map((item) => ({
      ...item,
      key: item.key || item.trackKey,
      desc: item.desc || item.description || '',
    })),
    danceTracks: (data.danceTracks || []).map((item) => ({
      ...item,
      key: item.key || item.trackKey,
      desc: item.desc || item.description || '',
    })),
    trackMeta: data.trackMeta || {},
  }))
}

export function getGrowth() {
  return request({ url: '/growth' })
}

export function getOpportunities(trackKey, campusId) {
  const q = [`trackKey=${encodeURIComponent(trackKey || '')}`]
  if (campusId) q.push(`campusId=${encodeURIComponent(campusId)}`)
  return request({ url: `/opportunities?${q.join('&')}` }).then((list) =>
    (list || []).map((item) => ({
      ...item,
      deadline: formatDate(item.deadline),
    })),
  )
}

export function toggleOpportunityApply(id, payload = {}) {
  return request({ url: `/opportunities/${id}/apply`, method: 'POST', data: payload })
}

export function submitFeedback(payload) {
  return request({ url: '/feedback', method: 'POST', data: payload })
}

export function getSurveys(campusId) {
  const query = campusId ? `?campusId=${encodeURIComponent(campusId)}` : ''
  return request({ url: `/surveys${query}` })
}

export function getSurveyDetail(id) {
  return request({ url: `/surveys/${id}` })
}

export function submitSurvey(id, payload) {
  return request({ url: `/surveys/${id}/submit`, method: 'POST', data: payload })
}

export function submitTeacherReview(payload) {
  return request({ url: '/teacher-reviews', method: 'POST', data: payload })
}

export function getTeacherSchedules(date) {
  const params = date ? `?date=${date}` : ''
  return request({ url: `/teacher/schedules${params}` })
}

export function getTeacherStats() {
  return request({ url: '/teacher/stats' })
}

export function getTeacherArchives(page = 1, size = 20) {
  return request({ url: `/teacher/archives?page=${page}&size=${size}` })
}

export function getTeacherArchiveDetail(id) {
  return request({ url: `/teacher/archives/${id}` })
}

export function getTeacherReviews(page = 1, size = 20) {
  return request({ url: `/teacher/reviews?page=${page}&size=${size}` })
}

export function deleteTeacherReview(id) {
  return request({ url: `/teacher/reviews/${id}`, method: 'DELETE' })
}

export function getEmployeeProfile() {
  return request({ url: '/employee/profile' })
}

export function getEmployeeWeeklyReports() {
  return request({ url: '/employee/weekly-reports' })
}

export function submitEmployeeWeeklyReport(payload) {
  return request({ url: '/employee/weekly-reports', method: 'POST', data: payload })
}

export function getEmployeePerformance() {
  return request({ url: '/employee/performance' })
}

export function getTeacherRoster(scheduleId, date) {
  return request({ url: `/teacher/roster?scheduleId=${scheduleId}&date=${encodeURIComponent(date)}` })
}

export function manualTeacherCheckin(payload) {
  return request({ url: '/teacher/roster/checkin', method: 'POST', data: payload })
}

export function teacherCheckin(payload) {
  return request({ url: '/teacher/checkin', method: 'POST', data: { payload } })
}

export function getEmployeeCheckinSchedules(date) {
  const q = date ? `?date=${encodeURIComponent(date)}` : ''
  return request({ url: `/employee/checkin/schedules${q}` })
}

export function openEmployeeCheckinSession(payload) {
  return request({ url: '/employee/checkin/sessions', method: 'POST', data: payload })
}

export function closeEmployeeCheckinSession(sessionId) {
  return request({ url: `/employee/checkin/sessions/${sessionId}/close`, method: 'POST' })
}

export function getEmployeeCheckinSessionPayload(sessionId) {
  return request({ url: `/employee/checkin/sessions/${sessionId}/payload` })
}

export function getEmployeeCheckinPending(scheduleId, date) {
  return request({
    url: `/employee/checkin/pending?scheduleId=${scheduleId}&date=${encodeURIComponent(date)}&status=pending`,
  })
}

export function confirmEmployeeCheckinPending(id) {
  return request({ url: `/employee/checkin/pending/${id}/confirm`, method: 'POST' })
}

export function rejectEmployeeCheckinPending(id) {
  return request({ url: `/employee/checkin/pending/${id}/reject`, method: 'POST' })
}
