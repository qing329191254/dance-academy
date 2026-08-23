const TAB_PAGES = [
  '/pages/book/book',
  '/pages/growth/index',
  '/pages/mine/mine',
]

const COMMON_SUB_PAGES = [
  '/pages/brand/brand',
  '/pages/course/list',
  '/pages/teachers/teachers',
  '/pages/teachers/detail',
]

function preloadOne(url) {
  return new Promise((resolve) => {
    if (typeof uni.preloadPage !== 'function') {
      resolve()
      return
    }
    uni.preloadPage({
      url,
      success: () => resolve(),
      fail: () => resolve(),
    })
  })
}

export function preloadTabPages() {
  TAB_PAGES.forEach((url) => {
    if (typeof uni.preloadPage === 'function') {
      uni.preloadPage({ url })
    }
  })
}

export function preloadCommonSubPages() {
  COMMON_SUB_PAGES.forEach((url) => {
    if (typeof uni.preloadPage === 'function') {
      uni.preloadPage({ url })
    }
  })
}

export function preloadTabPagesAsync() {
  const urls = [...TAB_PAGES, ...COMMON_SUB_PAGES]
  return Promise.all(urls.map((url) => preloadOne(url)))
}
