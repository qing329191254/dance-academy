import { computed, ref } from 'vue'
import { getSchools } from './api.js'

/** 兜底：接口失败时仍可用；启动后用「校区管理员」列表覆盖 */
const FALLBACK_CAMPUSES = [
  { id: '1', name: '四川师范大学', shortName: '川师大' },
]

export const CAMPUSES = ref([...FALLBACK_CAMPUSES])

export const DEFAULT_CAMPUS_ID = '1'

const STORAGE_KEY = 'selectedCampusId'

function shortNameOf(name) {
  if (!name) return ''
  if (name.length <= 6) return name
  const shortName = name
    .replace('（珠海）', '')
    .replace('(珠海)', '')
    .replace('师范大学', '师大')
    .replace('大学', '')
  return shortName.length > 8 ? name.slice(0, 6) : shortName
}

function readCampusId() {
  try {
    const id = String(uni.getStorageSync(STORAGE_KEY) || '')
    if (CAMPUSES.value.some((item) => item.id === id)) return id
  } catch (e) {}
  return CAMPUSES.value[0]?.id || DEFAULT_CAMPUS_ID
}

export const selectedCampusId = ref(readCampusId())

export const currentCampus = computed(
  () => CAMPUSES.value.find((item) => item.id === selectedCampusId.value) || CAMPUSES.value[0] || FALLBACK_CAMPUSES[0],
)

export function selectCampus(id) {
  const key = String(id)
  if (!CAMPUSES.value.some((item) => item.id === key)) return
  selectedCampusId.value = key
  try {
    uni.setStorageSync(STORAGE_KEY, key)
  } catch (e) {}
}

function applyCampusList(list) {
  if (!Array.isArray(list) || !list.length) return
  CAMPUSES.value = list.map((item) => ({
    id: String(item.id),
    name: item.name,
    shortName: shortNameOf(item.name),
  }))
  if (!CAMPUSES.value.some((item) => item.id === selectedCampusId.value)) {
    selectCampus(CAMPUSES.value[0].id)
  }
}

let loadPromise = null

export function loadCampuses(force = false) {
  if (loadPromise && !force) return loadPromise
  loadPromise = getSchools()
    .then((list) => {
      applyCampusList(list)
      return CAMPUSES.value
    })
    .catch(() => CAMPUSES.value)
    .finally(() => {
      loadPromise = null
    })
  return loadPromise
}
