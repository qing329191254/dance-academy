import { onBeforeMount, onMounted, onUnmounted, ref } from 'vue'

const MOBILE_MAX = 768
const TABLET_MAX = 1280

function read() {
  if (typeof window === 'undefined') {
    return { isMobile: false, isTablet: false, isCompact: false }
  }
  const isMobile = window.matchMedia(`(max-width: ${MOBILE_MAX}px)`).matches
  const isCompact = window.matchMedia(`(max-width: ${TABLET_MAX}px)`).matches
  const isTablet = isCompact && !isMobile
  return { isMobile, isTablet, isCompact }
}

/**
 * Shared viewport breakpoints for admin layout.
 * - mobile ≤768
 * - tablet 769–1280 (covers iPad Pro portrait 1024 and similar)
 * - compact = mobile or tablet (drawer nav)
 */
export function useBreakpoint() {
  const isMobile = ref(false)
  const isTablet = ref(false)
  const isCompact = ref(false)

  function update() {
    const next = read()
    isMobile.value = next.isMobile
    isTablet.value = next.isTablet
    isCompact.value = next.isCompact
  }

  onBeforeMount(update)

  onMounted(() => {
    update()
    window.addEventListener('resize', update, { passive: true })
    window.addEventListener('orientationchange', update, { passive: true })
  })

  onUnmounted(() => {
    window.removeEventListener('resize', update)
    window.removeEventListener('orientationchange', update)
  })

  return { isMobile, isTablet, isCompact }
}
