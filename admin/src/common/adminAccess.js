import { CAMPUSES } from './campuses'

export function isSuperAdmin(profile) {
  return !!profile?.superAdmin
}

export function allowedCampuses(profile) {
  if (!profile) return []
  const all = CAMPUSES.value || []
  if (profile.superAdmin) return all
  const ids = profile.campusIds || []
  return all.filter((item) => ids.includes(item.id))
}

export function defaultCampusId(profile) {
  const list = allowedCampuses(profile)
  return list.length === 1 ? list[0].id : ''
}

export function roleLabel(role) {
  return role === 'SUPER_ADMIN' ? '超级管理员' : '管理员'
}
