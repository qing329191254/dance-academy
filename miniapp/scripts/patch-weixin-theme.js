/**
 * 将 backgroundColorContent、lazyCodeLoading 等写入微信小程序编译产物。
 * HBuilderX 部分版本不会把 pages.json 里的该字段输出到 page.json。
 *
 * 用法（编译到微信后执行一次）：
 *   node scripts/patch-weixin-theme.js
 */
const fs = require('fs')
const path = require('path')

const THEME = '#111111'
const SPLASH_BG = '#000000'
const TAB_PAGES = new Set([
  'pages/home/home',
  'pages/book/book',
  'pages/growth/index',
  'pages/mine/mine',
])

function findMpWeixinDirs() {
  const distRoot = path.join(__dirname, '..', 'unpackage', 'dist')
  if (!fs.existsSync(distRoot)) return []

  const dirs = []
  for (const name of fs.readdirSync(distRoot)) {
    const candidate = path.join(distRoot, name, 'mp-weixin')
    if (fs.existsSync(path.join(candidate, 'app.json'))) {
      dirs.push(candidate)
    }
  }
  return dirs
}

function walkJsonFiles(dir, list = []) {
  if (!fs.existsSync(dir)) return list
  for (const name of fs.readdirSync(dir)) {
    const full = path.join(dir, name)
    const stat = fs.statSync(full)
    if (stat.isDirectory()) {
      walkJsonFiles(full, list)
      continue
    }
    if (name.endsWith('.json') && !name.includes('project')) {
      list.push(full)
    }
  }
  return list
}

function patchPageJson(distRoot, filePath) {
  const raw = fs.readFileSync(filePath, 'utf8')
  let json
  try {
    json = JSON.parse(raw)
  } catch {
    return false
  }

  if (!json.navigationBarTitleText && !json.navigationStyle && !json.backgroundColor) {
    return false
  }

  let changed = false
  const rel = path.relative(distRoot, filePath).replace(/\\/g, '/').replace(/\.json$/, '')

  if (json.backgroundColorContent !== (rel === 'pages/home/home' ? SPLASH_BG : THEME)) {
    json.backgroundColorContent = rel === 'pages/home/home' ? SPLASH_BG : THEME
    changed = true
  }

  if (rel === 'pages/home/home' && json.backgroundColor !== SPLASH_BG) {
    json.backgroundColor = SPLASH_BG
    changed = true
  }

  if (TAB_PAGES.has(rel) && json.initialRenderingCache) {
    delete json.initialRenderingCache
    changed = true
  }

  if (rel === 'pages/home/home' && json.usingComponents) {
    delete json.usingComponents
    changed = true
  }

  if (json.usingComponents?.['page-brand-header']) {
    delete json.usingComponents['page-brand-header']
    changed = true
  }

  if (json.usingComponents && !Object.keys(json.usingComponents).length) {
    delete json.usingComponents
    changed = true
  }

  if (json.componentPlaceholder?.['page-brand-header']) {
    delete json.componentPlaceholder['page-brand-header']
    if (!Object.keys(json.componentPlaceholder).length) {
      delete json.componentPlaceholder
    }
    changed = true
  }

  if (json.usingComponents && Object.keys(json.usingComponents).length) {
    const placeholder = json.componentPlaceholder || {}
    let placeholderChanged = false
    for (const name of Object.keys(json.usingComponents)) {
      if (!placeholder[name]) {
        placeholder[name] = 'view'
        placeholderChanged = true
      }
    }
    if (placeholderChanged) {
      json.componentPlaceholder = placeholder
      changed = true
    }
  }

  if (changed) {
    fs.writeFileSync(filePath, `${JSON.stringify(json, null, 2)}\n`, 'utf8')
  }
  return changed
}

function patchAppJson(distRoot) {
  const appJsonPath = path.join(distRoot, 'app.json')
  if (!fs.existsSync(appJsonPath)) {
    return { ok: false, appChanged: false, pageChanged: 0 }
  }

  const json = JSON.parse(fs.readFileSync(appJsonPath, 'utf8'))
  json.window = json.window || {}
  let changed = false

  for (const key of ['backgroundColor', 'backgroundColorTop', 'backgroundColorBottom', 'backgroundColorContent']) {
    if (json.window[key] !== THEME) {
      json.window[key] = THEME
      changed = true
    }
  }

  if (json.tabBar && json.tabBar.backgroundColor !== THEME) {
    json.tabBar.backgroundColor = THEME
    changed = true
  }

  if (json.lazyCodeLoading !== 'requiredComponents') {
    json.lazyCodeLoading = 'requiredComponents'
    changed = true
  }

  if (json.usingComponents) {
    delete json.usingComponents
    changed = true
  }

  if (Array.isArray(json.pages)) {
    const splashPage = 'pages/splash/splash'
    const homePage = 'pages/home/home'
    const pages = json.pages.filter((page) => page !== splashPage)
    let pagesChanged = pages.length !== json.pages.length
    const homeIndex = pages.indexOf(homePage)
    if (homeIndex > 0) {
      pages.splice(homeIndex, 1)
      pages.unshift(homePage)
      pagesChanged = true
    }
    if (pagesChanged) {
      json.pages = pages
      changed = true
    }
  }

  if (changed) {
    fs.writeFileSync(appJsonPath, `${JSON.stringify(json, null, 2)}\n`, 'utf8')
  }

  const pageFiles = walkJsonFiles(path.join(distRoot, 'pages'))
  let pageChanged = 0
  for (const file of pageFiles) {
    if (patchPageJson(distRoot, file)) pageChanged += 1
  }

  return { ok: true, appChanged: changed, pageChanged }
}

function cleanupStaleDist(distRoot) {
  const staleComponentDir = path.join(distRoot, 'components', 'page-brand-header')
  if (fs.existsSync(staleComponentDir)) {
    fs.rmSync(staleComponentDir, { recursive: true, force: true })
    console.log(`[patch-weixin-theme] 已删除过时组件目录 components/page-brand-header`)
  }

  const distName = path.basename(path.dirname(distRoot))
  const staleSourcemapDir = path.join(
    path.dirname(distRoot),
    '.sourcemap',
    'mp-weixin',
    'components',
    'page-brand-header',
  )
  if (fs.existsSync(staleSourcemapDir)) {
    fs.rmSync(staleSourcemapDir, { recursive: true, force: true })
    console.log(`[patch-weixin-theme] 已删除过时 sourcemap (${distName})`)
  }

  const jsonFiles = walkJsonFiles(distRoot)
  for (const file of jsonFiles) {
    let json
    try {
      json = JSON.parse(fs.readFileSync(file, 'utf8'))
    } catch {
      continue
    }

    let changed = false
    if (json.usingComponents?.['page-brand-header']) {
      delete json.usingComponents['page-brand-header']
      changed = true
    }
    if (json.usingComponents && !Object.keys(json.usingComponents).length) {
      delete json.usingComponents
      changed = true
    }
    if (json.componentPlaceholder?.['page-brand-header']) {
      delete json.componentPlaceholder['page-brand-header']
      if (!Object.keys(json.componentPlaceholder).length) {
        delete json.componentPlaceholder
      }
      changed = true
    }
    if (changed) {
      fs.writeFileSync(file, `${JSON.stringify(json, null, 2)}\n`, 'utf8')
    }
  }
}

function verifyDist(distRoot) {
  const appJsonPath = path.join(distRoot, 'app.json')
  const json = JSON.parse(fs.readFileSync(appJsonPath, 'utf8'))
  const rel = path.relative(path.join(__dirname, '..'), distRoot)
  const errors = []

  if (!Array.isArray(json.pages) || !json.pages.length) {
    errors.push('app.json 缺少 pages 配置')
  } else {
    if (json.pages.includes('pages/splash/splash')) {
      errors.push('app.json 仍包含 pages/splash/splash（该页面已删除，不能作为入口）')
    }
    if (json.pages[0] !== 'pages/home/home') {
      errors.push(`入口页应为 pages/home/home，当前为 ${json.pages[0]}`)
    }
  }

  if (json.usingComponents) {
    errors.push('app.json 仍包含 usingComponents（应完全移除）')
  }

  const staleBrandHeaderDir = path.join(distRoot, 'components', 'page-brand-header')
  if (fs.existsSync(staleBrandHeaderDir)) {
    errors.push('编译产物中仍有 components/page-brand-header 目录')
  }

  const staleSplashDir = path.join(distRoot, 'pages', 'splash')
  if (fs.existsSync(staleSplashDir)) {
    errors.push('编译产物中仍有 pages/splash 目录，请清缓存后重新编译')
  }

  if (errors.length) {
    console.error(`[patch-weixin-theme] ${rel} 校验失败：`)
    for (const err of errors) console.error(`  - ${err}`)
    return false
  }

  console.log(`[patch-weixin-theme] ${rel} 校验通过，入口页: pages/home/home`)
  return true
}

function main() {
  const distDirs = findMpWeixinDirs()
  if (!distDirs.length) {
    console.error('[patch-weixin-theme] 未找到 mp-weixin 编译目录，请先编译到微信小程序')
    process.exit(1)
  }

  let allOk = true
  for (const distRoot of distDirs) {
    const rel = path.relative(path.join(__dirname, '..'), distRoot)
    const result = patchAppJson(distRoot)
    if (!result.ok) {
      console.warn(`[patch-weixin-theme] 跳过 ${rel}（无 app.json）`)
      allOk = false
      continue
    }
    cleanupStaleDist(distRoot)
    console.log(`[patch-weixin-theme] ${rel} app.json ${result.appChanged ? '已更新' : '无需更新'}`)
    console.log(`[patch-weixin-theme] ${rel} 页面 json 更新 ${result.pageChanged} 个`)
    if (!verifyDist(distRoot)) allOk = false
  }

  if (!allOk) {
    process.exit(1)
  }
}

main()
