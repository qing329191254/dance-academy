import { mediaUrl } from './config.js'
import { request } from './request.js'

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

function mapCourse(item) {
  if (!item) return null
  return {
    ...item,
    cover: mediaUrl(item.cover),
    desc: item.desc || item.description || '',
  }
}

function mapCourseModule(item) {
  if (!item) return null
  return {
    ...item,
    key: item.moduleKey || item.key || '',
    cover: mediaUrl(item.cover),
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
  }
}

export function getHome(campusId) {
  const query = campusId ? `?campusId=${encodeURIComponent(campusId)}` : ''
  return request({ url: `/home${query}` }).then((data) => ({
    studio: mapStudio(data.studio),
    banners: (data.banners || []).map(mediaUrl),
    teachers: (data.teachers || []).map(mapTeacher),
    courses: (data.courses || []).map(mapCourse),
  }))
}

export function getBrand(campusId) {
  const query = campusId ? `?campusId=${encodeURIComponent(campusId)}` : ''
  return request({ url: `/brand${query}` }).then((data) => ({
    studio: mapStudio(data.studio),
    photos: (data.photos || []).map(mediaUrl),
  }))
}

export function getTeachers() {
  return request({ url: '/teachers' }).then((list) => (list || []).map(mapTeacher))
}

export function getTeacher(id) {
  return request({ url: `/teachers/${id}` }).then(mapTeacher)
}

export function getCourses() {
  return request({ url: '/courses' }).then((list) => (list || []).map(mapCourse))
}

export function getCourse(id) {
  return request({ url: `/courses/${id}` }).then(mapCourse)
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

export function checkin(payload) {
  return request({ url: '/checkin', method: 'POST', data: { payload } })
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
  const params = new URLSearchParams({ trackKey })
  if (campusId) params.set('campusId', campusId)
  return request({ url: `/opportunities?${params.toString()}` }).then((list) =>
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
