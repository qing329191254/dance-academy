function preloadOne(url) {
  try {
    if (typeof uni.preloadPage === 'function') {
      uni.preloadPage({ url })
    }
  } catch (e) {}
}

export function openPage(url) {
  preloadOne(url)
  uni.navigateTo({
    url,
    fail() {
      uni.redirectTo({
        url,
        fail() {
          uni.reLaunch({ url })
        },
      })
    },
  })
}

export function switchTabPage(url) {
  uni.switchTab({
    url,
    fail() {
      uni.reLaunch({ url })
    },
  })
}

export function openBookTab(tab) {
  try {
    if (tab) uni.setStorageSync('pendingBookTab', tab)
  } catch (e) {}
  switchTabPage('/pages/book/book')
}
