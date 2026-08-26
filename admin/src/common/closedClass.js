export const CLOSED_CLASS_GROUPS = [
  { value: '', label: '普通（不可约闭门课）' },
  { value: 'advanced', label: '高潜闭门' },
  { value: 'foundation', label: '基础闭门' },
]

export function closedClassGroupLabel(value) {
  const item = CLOSED_CLASS_GROUPS.find((g) => g.value === (value || ''))
  return item ? item.label : '普通'
}
