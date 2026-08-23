export function getStatusBarHeight() {
  try {
    return uni.getSystemInfoSync().statusBarHeight || 44
  } catch {
    return 44
  }
}
