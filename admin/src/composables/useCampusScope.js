import { computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useCampusStore } from '../stores/campus'

export function useCampusScope(reload) {
  const campusStore = useCampusStore()
  const { campusId } = storeToRefs(campusStore)
  const campusFiltered = computed(() => campusStore.filtered)
  const campusParams = () => campusStore.campusParams

  if (reload) {
    watch(campusId, reload, { immediate: true })
  }

  return { campusId, campusFiltered, campusParams, campusStore }
}
