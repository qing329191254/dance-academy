import { API_BASE, USE_CLOUD } from './config.js'

const USER_KEY = 'forget_user'
const CLOUD_ENV_ID = 'prod-d0g3w5hfzfd320462'
const CLOUD_SERVICE = 'springboot-1g7c'

function getToken() {
  try {
    return uni.getStorageSync(USER_KEY)?.token || ''
  } catch (e) {
    return ''
  }
}

function unwrap(res, reject, resolve) {
  const body = res.data || {}
  const status = res.statusCode
  if (status === 401 || body.code === 401) {
    try {
      uni.removeStorageSync(USER_KEY)
    } catch (e) {}
    reject(new Error(body.message || '请先登录'))
    return
  }
  if (status >= 400 || (typeof body.code === 'number' && body.code !== 0)) {
    reject(new Error(body.message || '请求失败'))
    return
  }
  resolve(body.data)
}

function httpRequest({ url, method = 'GET', data } = {}) {
  return new Promise((resolve, reject) => {
    const header = {
      'Content-Type': 'application/json',
    }
    const token = getToken()
    if (token) {
      header.Authorization = `Bearer ${token}`
    }
    uni.request({
      url: `${API_BASE}${url}`,
      method,
      data,
      header,
      timeout: 15000,
      success(res) {
        unwrap(res, reject, resolve)
      },
      fail() {
        reject(new Error('网络异常，请确认后端已启动'))
      },
    })
  })
}

function containerRequest({ url, method = 'GET', data } = {}) {
  return new Promise((resolve, reject) => {
    const wxApi = typeof wx !== 'undefined' ? wx : null
    if (!wxApi?.cloud?.callContainer) {
      httpRequest({ url, method, data }).then(resolve).catch(reject)
      return
    }
    const header = {
      'content-type': 'application/json',
      'X-WX-SERVICE': CLOUD_SERVICE,
    }
    const token = getToken()
    if (token) {
      header.Authorization = `Bearer ${token}`
    }
    wxApi.cloud.callContainer({
      config: { env: CLOUD_ENV_ID },
      path: `/api/app${url}`,
      method,
      data: data || {},
      header,
      success(res) {
        unwrap(res, reject, resolve)
      },
      fail() {
        httpRequest({ url, method, data }).then(resolve).catch(reject)
      },
    })
  })
}

export function request(opts) {
  // #ifdef MP-WEIXIN
  if (USE_CLOUD) {
    return containerRequest(opts)
  }
  // #endif
  return httpRequest(opts)
}
