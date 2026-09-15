import { computed, ref } from 'vue'
import { getSchools } from './api.js'
import { queryFromLaunchOptions, shareEntryKey } from './campusQuery.js'

export { queryFromLaunchOptions } from './campusQuery.js'

/** 兜底：接口失败时仍可用；启动后用校区列表覆盖。勿用它校验本地已选校区。 */
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

function readStoredCampusId() {
  try {
    return String(uni.getStorageSync(STORAGE_KEY) || '').trim()
  } catch (e) {
    return ''
  }
}

function persistCampusId(id) {
  const key = String(id || '').trim()
  if (!key) return
  try {
    uni.setStorageSync(STORAGE_KEY, key)
  } catch (e) {}
}

function hasCampus(id) {
  const key = String(id || '')
  return CAMPUSES.value.some((item) => item.id === key)
}

/** 先信任本地已选校区，等正式列表加载后再校验；避免冷启动被 fallback 冲成默认校区。 */
function readCampusId() {
  const stored = readStoredCampusId()
  if (stored) return stored
  return CAMPUSES.value[0]?.id || DEFAULT_CAMPUS_ID
}

export const selectedCampusId = ref(readCampusId())

export const currentCampus = computed(() => {
  const found = CAMPUSES.value.find((item) => item.id === selectedCampusId.value)
  if (found) return found
  // 列表未到齐时不要回落到第一个校区，否则会出现「名字是川师、课表是别的校」
  const id = selectedCampusId.value || DEFAULT_CAMPUS_ID
  return { id, name: '', shortName: '' }
})

export function selectCampus(id) {
  const key = String(id || '').trim()
  if (!key) return
  if (CAMPUSES.value.length && !hasCampus(key)) return
  selectedCampusId.value = key
  persistCampusId(key)
}

/** 从分享/启动参数切换校区；校区列表未加载完时先记下待应用 id */
let pendingShareCampusId = ''

export function applyCampusFromQuery(query) {
  const raw = query?.campusId ?? query?.campus_id ?? ''
  const id = String(raw || '').trim()
  if (!id) return false
  pendingShareCampusId = id
  selectedCampusId.value = id
  persistCampusId(id)
  if (hasCampus(id)) {
    pendingShareCampusId = ''
  }
  return true
}

let lastShareEntryKey = ''

/** 冷启动 / 点开新分享卡片时切校区；从后台回到前台不重复套用同一张卡片。 */
export function applyCampusFromLaunchOptions(options) {
  const query = queryFromLaunchOptions(options)
  const key = shareEntryKey(options)
  if (!key) return false
  if (key === lastShareEntryKey) return false
  lastShareEntryKey = key
  return applyCampusFromQuery(query)
}

function flushPendingShareCampus() {
  if (!pendingShareCampusId) return
  if (hasCampus(pendingShareCampusId)) {
    selectedCampusId.value = pendingShareCampusId
    persistCampusId(pendingShareCampusId)
    pendingShareCampusId = ''
  }
}

function applyCampusList(list) {
  if (!Array.isArray(list) || !list.length) return
  CAMPUSES.value = list.map((item) => ({
    id: String(item.id),
    name: item.name,
    shortName: shortNameOf(item.name),
  }))
  flushPendingShareCampus()
  if (hasCampus(selectedCampusId.value)) return
  const stored = readStoredCampusId()
  if (hasCampus(stored)) {
    selectedCampusId.value = stored
    return
  }
  selectedCampusId.value = CAMPUSES.value[0].id
  persistCampusId(selectedCampusId.value)
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
