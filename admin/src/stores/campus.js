import { defineStore } from 'pinia'
import { allowedCampuses, defaultCampusId } from '../common/adminAccess'

export const useCampusStore = defineStore('campus', {
  state: () => ({
    campusId: localStorage.getItem('admin_campus_id') || '',
  }),
  getters: {
    filtered(state) {
      return !!state.campusId
    },
    campusParams(state) {
      return state.campusId ? { campusId: state.campusId } : {}
    },
  },
  actions: {
    syncWithProfile(profile) {
      const allowed = allowedCampuses(profile).map((item) => item.id)
      if (this.campusId && !allowed.includes(this.campusId)) {
        this.setCampus('')
      }
      if (!this.campusId && allowed.length === 1) {
        this.setCampus(allowed[0])
      } else if (!this.campusId && defaultCampusId(profile)) {
        this.setCampus(defaultCampusId(profile))
      }
    },
    setCampus(id) {
      this.campusId = id || ''
      if (this.campusId) localStorage.setItem('admin_campus_id', this.campusId)
      else localStorage.removeItem('admin_campus_id')
    },
  },
})
