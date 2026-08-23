const STORAGE_KEY = 'forget_opportunity_applies'

function readList() {
  try {
    const raw = uni.getStorageSync(STORAGE_KEY) || []
    return Array.isArray(raw) ? raw : []
  } catch (e) {
    return []
  }
}

function writeList(list) {
  uni.setStorageSync(STORAGE_KEY, list)
}

export function buildApplyKey(trackKey, opportunityId) {
  return `${trackKey}:${opportunityId}`
}

export function getApplies() {
  return readList()
}

export function isApplied(key) {
  return getApplies().some((item) => item.key === key)
}

export function addApply(record) {
  const list = getApplies().filter((item) => item.key !== record.key)
  list.push(record)
  writeList(list)
}

export function removeApply(key) {
  writeList(getApplies().filter((item) => item.key !== key))
}
