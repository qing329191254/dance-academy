/** 成长机会 / 报名审核：内部 trackKey → 展示文案 */
export const trackLabel = {
  parttime: '兼职',
  intern: '实习',
  manage: '管理',
  show: '演出',
  commercial: '商演',
  teacher: '教师',
}

/** 报名审核 status → 展示文案 */
export const applyStatusLabel = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝',
  cancelled: '已取消',
}

export function trackLabelOf(key) {
  return trackLabel[key] || key || '—'
}

export function applyStatusLabelOf(status) {
  return applyStatusLabel[status] || status || '—'
}

export function applyStatusTagType(status) {
  if (status === 'pending') return 'warning'
  if (status === 'approved') return 'success'
  if (status === 'rejected') return 'danger'
  if (status === 'cancelled') return 'info'
  return ''
}
