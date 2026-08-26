import { computed, ref } from 'vue'

export const CAMPUSES = [
  { id: 'shizishan', name: '川师大狮子山校区', shortName: '狮子山' },
  { id: 'chenglong', name: '川师大成龙校区', shortName: '成龙' },
  { id: 'bnu-zhuhai', name: '北京师范大学珠海校区', shortName: '北师珠海' },
  { id: 'uic', name: '北师香港浸会大学校区', shortName: '北师浸会' },
  { id: 'cdu', name: '成都大学校区', shortName: '成都大学' },
  { id: 'swpu', name: '西南石油大学成都校区', shortName: '西南石油' },
]

export const DEFAULT_CAMPUS_ID = CAMPUSES[0].id

const STORAGE_KEY = 'selectedCampusId'

function readCampusId() {
  try {
    const id = uni.getStorageSync(STORAGE_KEY)
    if (CAMPUSES.some((item) => item.id === id)) return id
  } catch (e) {}
  return DEFAULT_CAMPUS_ID
}

export const selectedCampusId = ref(readCampusId())

export const currentCampus = computed(
  () => CAMPUSES.find((item) => item.id === selectedCampusId.value) || CAMPUSES[0],
)

export function selectCampus(id) {
  if (!CAMPUSES.some((item) => item.id === id)) return
  selectedCampusId.value = id
  try {
    uni.setStorageSync(STORAGE_KEY, id)
  } catch (e) {}
}
