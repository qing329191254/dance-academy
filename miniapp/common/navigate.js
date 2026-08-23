function preloadOne(url) {
  return new Promise((resolve) => {
    if (typeof uni.preloadPage !== 'function') {
      resolve(false)
      return
    }
    uni.preloadPage({
      url,
      success: () => resolve(true),
      fail: () => resolve(false),
    })
  })
}

export function openPage(url) {
  preloadOne(url).finally(() => {
    uni.navigateTo({ url })
  })
}

export function switchTabPage(url) {
  uni.switchTab({ url })
}
