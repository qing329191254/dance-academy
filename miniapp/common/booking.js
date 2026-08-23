const STORAGE_KEY = 'forget_course_bookings'

function readList() {
  try {
    const raw = uni.getStorageSync(STORAGE_KEY) || []
    if (!Array.isArray(raw)) return []
    if (raw.length && typeof raw[0] === 'string') return []
    return raw
  } catch (e) {
    return []
  }
}

function writeList(list) {
  uni.setStorageSync(STORAGE_KEY, list)
}

export function buildBookingKey(tab, item, date) {
  return `${tab}:${date || 'default'}:${item.id}`
}

export function getBookings() {
  return readList()
}

export function getBookingKeys() {
  return getBookings().map((item) => item.key)
}

export function isBooked(key) {
  return getBookings().some((item) => item.key === key)
}

export function addBooking(record) {
  const list = getBookings().filter((item) => item.key !== record.key)
  list.push(record)
  writeList(list)
}

export function removeBooking(key) {
  writeList(getBookings().filter((item) => item.key !== key))
}
