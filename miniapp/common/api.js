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

function mapStudio(item) {
  if (!item) return {}
  return {
    ...item,
    logo: mediaUrl(item.logo),
    splashImage: mediaUrl(item.splashImage),
  }
}

export function getHome() {
  return request({ url: '/home' }).then((data) => ({
    studio: mapStudio(data.studio),
    banners: (data.banners || []).map(mediaUrl),
    teachers: (data.teachers || []).map(mapTeacher),
    courses: (data.courses || []).map(mapCourse),
  }))
}

export function getBrand() {
  return request({ url: '/brand' }).then((data) => ({
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

export function getSchedules(type, date) {
  const query = date ? `?type=${type}&date=${date}` : `?type=${type}`
  return request({ url: `/schedules${query}` })
}

export function loginByCode(code) {
  return request({ url: '/auth/login', method: 'POST', data: { code } })
}

export function saveProfile(payload) {
  return request({ url: '/auth/profile', method: 'POST', data: payload })
}

export function getMine() {
  return request({ url: '/mine' })
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

export function getGrowth() {
  return request({ url: '/growth' })
}

export function getOpportunities(trackKey) {
  return request({ url: `/opportunities?trackKey=${encodeURIComponent(trackKey)}` }).then((list) =>
    (list || []).map((item) => ({
      ...item,
      deadline: formatDate(item.deadline),
    })),
  )
}

export function toggleOpportunityApply(id) {
  return request({ url: `/opportunities/${id}/apply`, method: 'POST' })
}
