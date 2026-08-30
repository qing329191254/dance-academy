import { ref } from 'vue'
import http from '../api/http'

/** 与小程序左上角切换同源：后台「校区管理」配置 */
export const CAMPUSES = ref([])

let loading = null

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

export async function loadCampuses(force = false) {
  if (loading && !force) return loading
  loading = (async () => {
    try {
      const res = await http.get('/admin/schools')
      const list = (res.data || [])
        .filter((item) => item.enabled !== false)
        .map((item) => ({
          id: String(item.id),
          name: item.name,
          shortName: shortNameOf(item.name),
        }))
      CAMPUSES.value = list
    } catch (e) {
      // 保持已有列表
    } finally {
      loading = null
    }
  })()
  return loading
}

export function campusName(id) {
  return CAMPUSES.value.find((item) => item.id === String(id))?.name || id || '-'
}
