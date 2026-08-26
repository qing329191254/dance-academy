import { defineStore } from 'pinia'
import http from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    profile: JSON.parse(localStorage.getItem('admin_profile') || 'null'),
  }),
  getters: {
    isSuperAdmin: (state) => !!state.profile?.superAdmin,
  },
  actions: {
    async login(username, password) {
      const res = await http.post('/admin/auth/login', { username, password })
      this.token = res.data.token
      const { token, ...profile } = res.data
      this.profile = profile
      localStorage.setItem('admin_token', this.token)
      localStorage.setItem('admin_profile', JSON.stringify(this.profile))
    },
    async fetchMe() {
      const res = await http.get('/admin/auth/me')
      this.profile = res.data
      localStorage.setItem('admin_profile', JSON.stringify(this.profile))
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_profile')
    },
  },
})
