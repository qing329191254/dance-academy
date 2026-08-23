export const PAGE_BG = '#111111'
export const SPLASH_BG = '#000000'

export function applyPageBackground(color = PAGE_BG) {
  if (typeof uni.setBackgroundColor !== 'function') return
  uni.setBackgroundColor({
    backgroundColor: color,
    backgroundColorTop: color,
    backgroundColorBottom: color,
  })
}