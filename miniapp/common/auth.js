import { USER_STORAGE_KEY } from './config.js'
import { loginByCode, saveProfile } from './api.js'

const STORAGE_KEY = USER_STORAGE_KEY

export function getUser() {
  try {
    return uni.getStorageSync(STORAGE_KEY) || null
  } catch (e) {
    return null
  }
}

export function isLoggedIn() {
  return !!getUser()?.token
}

export function isProfileComplete() {
  return !!getUser()?.profileComplete
}

export function ensureLoggedIn() {
  if (!isLoggedIn()) {
    uni.navigateTo({ url: '/pages/login/login' })
    return false
  }
  return true
}

export function ensureLogin() {
  if (!ensureLoggedIn()) return false
  if (!isProfileComplete()) {
    uni.navigateTo({ url: '/pages/login/profile' })
    return false
  }
  return true
}

export function navigateAfterLogin() {
  if (!isProfileComplete()) {
    uni.redirectTo({
      url: '/pages/login/profile',
      fail() {
        uni.navigateTo({ url: '/pages/login/profile' })
      },
    })
    return
  }
  uni.switchTab({ url: '/pages/mine/mine' })
}

export function saveUser(next) {
  uni.setStorageSync(STORAGE_KEY, next)
  return next
}

export function updateProfile(data) {
  const user = getUser()
  if (!user) return null
  const next = { ...user, ...data }
  return saveUser(next)
}

export function logout() {
  uni.removeStorageSync(STORAGE_KEY)
}

export async function completeProfile(data) {
  const payload = {
    nickname: data.nickname?.trim() || '学员',
    gender: data.gender || '女',
  }
  const avatar = String(data.avatar || '').trim()
  if (avatar && !isLocalFile(avatar)) {
    payload.avatar = avatar
  }
  const birthday = String(data.birthday || '').trim()
  if (birthday) {
    payload.birthday = birthday
  }
  const profile = await saveProfile(payload)
  const user = getUser() || {}
  return saveUser({
    ...user,
    ...profile,
    profileComplete: true,
  })
}

function isLocalFile(url) {
  const value = String(url).toLowerCase()
  return value.startsWith('wxfile://')
    || value.startsWith('http://tmp/')
    || value.startsWith('https://tmp/')
    || value.startsWith('file://')
}

export function weixinOneTapLogin() {
  const login = new Promise((resolve, reject) => {
    const onCode = (code) => {
      if (!code) {
        reject(new Error('微信登录失败'))
        return
      }
      loginByCode(code)
        .then((data) => {
          const user = {
            token: data.token,
            ...(data.user || {}),
            loginAt: Date.now(),
          }
          saveUser(user)
          resolve(user)
        })
        .catch(reject)
    }
    const onFail = (err) => {
      reject(new Error(err?.errMsg || err?.message || '微信登录失败'))
    }
    // 小程序里直接走 wx.login，避免 uni.login + provider 在部分基础库里不回调
    if (typeof wx !== 'undefined' && typeof wx.login === 'function') {
      wx.login({
        timeout: 8000,
        success: (res) => onCode(res.code),
        fail: onFail,
      })
      return
    }
    uni.login({
      timeout: 8000,
      success: (res) => onCode(res.code),
      fail: onFail,
    })
  })
  const timeout = new Promise((_, reject) => {
    setTimeout(() => reject(new Error('登录超时，请重试')), 12000)
  })
  return Promise.race([login, timeout])
}
