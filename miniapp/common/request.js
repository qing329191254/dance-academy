import { API_BASE, USE_CLOUD, USER_STORAGE_KEY } from './config.js'

const CLOUD_ENV_ID = 'prod-d0g3w5hfzfd320462'
const CLOUD_SERVICE = 'springboot-1g7c'

function getToken() {
  try {
    return uni.getStorageSync(USER_STORAGE_KEY)?.token || ''
  } catch (e) {
    return ''
  }
}

function parseBody(raw) {
  if (raw == null || raw === '') return {}
  if (typeof raw === 'string') {
    try {
      return JSON.parse(raw)
    } catch (e) {
      return { message: raw }
    }
  }
  return raw
}

function authHeader() {
  const header = {}
  const token = getToken()
  if (token) {
    header.Authorization = `Bearer ${token}`
    header['X-App-Token'] = token
    header['X-Token'] = token
  }
  return header
}

function failMessage(body, fallback) {
  return body?.message || body?.msg || body?.error || fallback
}

function unwrap(res, reject, resolve) {
  const body = parseBody(res.data)
  const status = res.statusCode
  if (status === 401 || body.code === 401) {
    try {
      uni.removeStorageSync(USER_STORAGE_KEY)
    } catch (e) {}
    reject(new Error(failMessage(body, '请先登录')))
    return
  }
  if (status >= 400 || (typeof body.code === 'number' && body.code !== 0)) {
    reject(new Error(failMessage(body, '请求失败')))
    return
  }
  resolve(body.data)
}

function isUnauthorized(res) {
  const body = parseBody(res?.data)
  return res?.statusCode === 401 || body.code === 401
}

function httpRequest({ url, method = 'GET', data } = {}) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...authHeader(),
      },
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
    let settled = false
    const finish = (fn, value) => {
      if (settled) return
      settled = true
      clearTimeout(timer)
      fn(value)
    }
    const goHttp = () => {
      httpRequest({ url, method, data }).then((v) => finish(resolve, v)).catch((e) => finish(reject, e))
    }
    const timer = setTimeout(goHttp, 8000)
    const wxApi = typeof wx !== 'undefined' ? wx : null
    if (!wxApi?.cloud?.callContainer) {
      goHttp()
      return
    }
    wxApi.cloud.callContainer({
      config: { env: CLOUD_ENV_ID },
      path: `/api/app${url}`,
      method,
      data: data || {},
      header: {
        'content-type': 'application/json',
        'X-WX-SERVICE': CLOUD_SERVICE,
        ...authHeader(),
      },
      success(res) {
        if (isUnauthorized(res) && getToken()) {
          goHttp()
          return
        }
        unwrap(res, (err) => finish(reject, err), (data) => finish(resolve, data))
      },
      fail() {
        goHttp()
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
