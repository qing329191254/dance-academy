export const CAMPUSES = [
  { id: 'shizishan', name: '川师大狮子山校区' },
  { id: 'chenglong', name: '川师大成龙校区' },
  { id: 'bnu-zhuhai', name: '北京师范大学珠海校区' },
  { id: 'uic', name: '北师香港浸会大学校区' },
  { id: 'cdu', name: '成都大学校区' },
  { id: 'swpu', name: '西南石油大学成都校区' },
]

export function campusName(id) {
  return CAMPUSES.find((item) => item.id === id)?.name || id || '-'
}
