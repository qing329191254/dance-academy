const BASE = process.env.API_BASE || 'https://springboot-1g7c-301404-6-1473444650.sh.run.tcloudbase.com'
const ADMIN_USER = process.env.ADMIN_USERNAME || 'admin'
const ADMIN_PASS = process.env.ADMIN_PASSWORD || 'admin123'

const results = []

async function request(method, path, { token, body, label } = {}) {
  const url = `${BASE}${path}`
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`
  const opts = { method, headers }
  if (body) opts.body = JSON.stringify(body)
  const started = Date.now()
  try {
    const res = await fetch(url, opts)
    const text = await res.text()
    let json
    try {
      json = JSON.parse(text)
    } catch {
      json = { raw: text.slice(0, 200) }
    }
    const ok = res.ok && json?.code === 0
    results.push({
      label: label || `${method} ${path}`,
      ok,
      status: res.status,
      code: json?.code,
      message: json?.message,
      ms: Date.now() - started,
      detail: ok ? summarize(json.data) : (json?.message || text.slice(0, 120)),
    })
    return { ok, json, status: res.status }
  } catch (e) {
    results.push({
      label: label || `${method} ${path}`,
      ok: false,
      status: 0,
      message: e.message,
      ms: Date.now() - started,
      detail: e.message,
    })
    return { ok: false, error: e }
  }
}

function summarize(data) {
  if (data == null) return 'null'
  if (Array.isArray(data)) return `array(${data.length})`
  if (typeof data === 'object') {
    if (data.list && typeof data.total === 'number') return `page total=${data.total}`
    if (data.token) return 'token ok'
    if (data.username) return `user=${data.username}`
    return `keys=${Object.keys(data).slice(0, 6).join(',')}`
  }
  return String(data).slice(0, 60)
}

async function main() {
  console.log(`\n=== 云环境 API 冒烟测试 ===`)
  console.log(`BASE: ${BASE}\n`)

  const login = await request('POST', '/api/admin/auth/login', {
    body: { username: ADMIN_USER, password: ADMIN_PASS },
    label: '管理员登录',
  })
  if (!login.ok) {
    printReport()
    process.exit(1)
  }
  const token = login.json.data?.token
  const profile = login.json.data?.profile || login.json.data

  await request('GET', '/api/admin/auth/me', { token, label: '管理员 /me' })
  await request('GET', '/api/admin/dashboard', { token, label: '工作台' })
  await request('GET', '/api/admin/users?page=1&size=5', { token, label: '小程序用户-全部' })
  await request('GET', '/api/admin/users?page=1&size=5&role=employee', { token, label: '小程序用户-员工筛选' })
  await request('GET', '/api/admin/users?page=1&size=5&role=student', { token, label: '小程序用户-学员筛选' })
  await request('GET', '/api/admin/users?page=1&size=5&role=teacher', { token, label: '小程序用户-老师筛选' })
  await request('GET', '/api/admin/teachers?page=1&size=5', { token, label: '老师档案列表' })
  await request('GET', '/api/admin/courses?page=1&size=5', { token, label: '课程产品' })
  await request('GET', '/api/admin/schedules?page=1&size=5', { token, label: '课表管理' })
  await request('GET', '/api/admin/bookings?page=1&size=5', { token, label: '预约管理' })
  await request('GET', '/api/admin/checkin-pending?page=1&size=5', { token, label: '待确认签到' })
  await request('GET', '/api/admin/practice?page=1&size=5', { token, label: '签到记录' })
  await request('GET', '/api/admin/teacher-attendance?page=1&size=5', { token, label: '教师考勤' })
  await request('GET', '/api/admin/employee-duty?page=1&size=5', { token, label: '员工值班' })
  await request('GET', '/api/admin/teacher-reviews?page=1&size=5', { token, label: '学员评价' })
  await request('GET', '/api/admin/schools', { token, label: '学校管理' })
  await request('GET', '/api/admin/cards?page=1&size=5', { token, label: '卡包发放' })
  await request('GET', '/api/admin/opportunities?page=1&size=5', { token, label: '成长机会' })
  await request('GET', '/api/admin/applies?page=1&size=5', { token, label: '报名审核' })
  await request('GET', '/api/admin/feedbacks?page=1&size=5', { token, label: '意见反馈' })
  await request('GET', '/api/admin/class-archives?page=1&size=5', { token, label: '课堂档案' })
  await request('GET', '/api/admin/studio?campusId=shizishan', { token, label: '门店信息' })
  await request('GET', '/api/admin/banners?campusId=shizishan', { token, label: '轮播图' })

  // 小程序公开接口
  await request('GET', '/api/app/home?campusId=shizishan', { label: '小程序首页' })
  await request('GET', '/api/app/schools', { label: '小程序学校列表' })
  await request('GET', '/api/app/schedules?type=group', { label: '小程序课表' })
  await request('GET', '/api/app/teachers', { label: '小程序老师列表' })
  await request('GET', '/api/app/course-intro', { label: '小程序课程介绍' })
  await request('GET', '/api/app/growth-content?campusId=shizishan', { label: '小程序成长文案' })
  await request('GET', '/api/admin/growth-tracks?campusId=shizishan', { token, label: '成长赛道配置' })

  // 角色筛选逻辑校验
  const emp = results.find((r) => r.label === '小程序用户-员工筛选')
  const all = results.find((r) => r.label === '小程序用户-全部')
  if (emp?.ok && all?.ok) {
    const empRes = await request('GET', '/api/admin/users?page=1&size=50&role=employee', { token, label: '_emp_detail' })
    if (empRes.ok && Array.isArray(empRes.json.data?.list)) {
      const bad = empRes.json.data.list.filter((u) => u.role !== 'employee')
      results.push({
        label: '员工筛选数据校验',
        ok: bad.length === 0,
        status: 200,
        code: bad.length === 0 ? 0 : -1,
        message: bad.length === 0 ? 'ok' : `含非员工 ${bad.length} 条`,
        ms: 0,
        detail: bad.length ? bad.map((u) => `${u.nickname || u.id}:${u.role || 'null'}`).join(', ') : '全部 role=employee',
      })
    }
  }

  printReport(profile)
  const failed = results.filter((r) => !r.ok && !r.label.startsWith('_'))
  process.exit(failed.length ? 1 : 0)
}

function printReport(profile) {
  const failed = results.filter((r) => !r.ok && !r.label.startsWith('_'))
  const passed = results.filter((r) => r.ok && !r.label.startsWith('_'))
  console.log('结果:')
  for (const r of results.filter((x) => !x.label.startsWith('_'))) {
    const mark = r.ok ? 'PASS' : 'FAIL'
    console.log(`  [${mark}] ${r.label} (${r.ms}ms) — ${r.detail}`)
  }
  console.log(`\n通过 ${passed.length} / 失败 ${failed.length}`)
  if (profile?.name || profile?.username) {
    console.log(`登录账号: ${profile.username || ADMIN_USER} (${profile.name || profile.role || 'admin'})`)
  }
  if (failed.length) {
    console.log('\n失败项:')
    failed.forEach((r) => console.log(`  - ${r.label}: ${r.detail || r.message}`))
  }
  console.log('')
}

main()
