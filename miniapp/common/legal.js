const FALLBACK = {
  company: '四川佳贝行教育咨询有限公司',
  brand: '高校FOR-GET舞室',
  phone: '18200407710',
  email: 'lhno1111@163.com',
  address: '四川省成都市锦江区静逸路76号8栋2单元601',
  updateDate: '2026年8月31日',
}

let cached = { ...FALLBACK }

function pickText(...values) {
  for (const value of values) {
    const text = value == null ? '' : String(value).trim()
    if (text) return text
  }
  return ''
}

export function applyLegalFromStudio(studio) {
  if (!studio) return { ...cached }
  cached = {
    company: pickText(studio.company, FALLBACK.company),
    brand: pickText(studio.name, FALLBACK.brand),
    phone: pickText(studio.phoneDisplay, studio.phone, FALLBACK.phone),
    email: pickText(studio.email, FALLBACK.email),
    address: pickText(studio.address, studio.location, studio.city, FALLBACK.address),
    updateDate: pickText(studio.legalUpdateDate, FALLBACK.updateDate),
  }
  return { ...cached }
}

export function getLegalInfo() {
  return { ...cached }
}

export async function loadLegalInfo(campusId, loader) {
  const fetchBrand = loader || (async (id) => {
    const { getBrand } = await import('./api.js')
    return getBrand(id)
  })
  try {
    const data = await fetchBrand(campusId)
    return applyLegalFromStudio(data?.studio)
  } catch {
    return getLegalInfo()
  }
}

/** @deprecated 请使用 getLegalInfo()，会在 getBrand 后自动更新 */
export const legalInfo = cached
