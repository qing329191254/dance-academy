const PRESETS = {
  avatar: { maxEdge: 800, quality: 0.78, maxBytes: 300 * 1024, label: '头像' },
  hero: { maxEdge: 1600, quality: 0.82, maxBytes: 1024 * 1024, label: '大图' },
  share: { maxEdge: 800, quality: 0.8, maxBytes: 300 * 1024, label: '分享图' },
  default: { maxEdge: 1200, quality: 0.8, maxBytes: 512 * 1024, label: '图片' },
}

export function getImagePreset(purpose = 'default') {
  return PRESETS[purpose] || PRESETS.default
}

function loadImage(file) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const image = new Image()
    image.onload = () => {
      URL.revokeObjectURL(url)
      resolve(image)
    }
    image.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('图片读取失败'))
    }
    image.src = url
  })
}

function canvasToBlob(canvas, type, quality) {
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (!blob) reject(new Error('图片压缩失败'))
        else resolve(blob)
      },
      type,
      quality,
    )
  })
}

/**
 * Compress image before upload. Keeps PNG only when input has transparency-ish
 * needs; otherwise outputs JPEG for smaller size.
 */
export async function compressImageFile(file, purpose = 'default') {
  if (!file || !file.type || !file.type.startsWith('image/')) {
    throw new Error('请选择图片文件')
  }
  // GIF keep as-is but enforce size
  const preset = getImagePreset(purpose)
  if (file.type === 'image/gif') {
    if (file.size > preset.maxBytes) {
      throw new Error(`${preset.label}过大（GIF 请控制在 ${Math.round(preset.maxBytes / 1024)}KB 内）`)
    }
    return file
  }

  const image = await loadImage(file)
  const scale = Math.min(1, preset.maxEdge / Math.max(image.width, image.height))
  const width = Math.max(1, Math.round(image.width * scale))
  const height = Math.max(1, Math.round(image.height * scale))

  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, width, height)
  ctx.drawImage(image, 0, 0, width, height)

  let quality = preset.quality
  let blob = await canvasToBlob(canvas, 'image/jpeg', quality)
  // Step down quality if still too large
  while (blob.size > preset.maxBytes && quality > 0.5) {
    quality -= 0.08
    blob = await canvasToBlob(canvas, 'image/jpeg', quality)
  }
  if (blob.size > preset.maxBytes) {
    throw new Error(
      `${preset.label}压缩后仍过大（${Math.round(blob.size / 1024)}KB），请换更小的原图`,
    )
  }

  const base = (file.name || 'image').replace(/\.[^.]+$/, '')
  return new File([blob], `${base}.jpg`, { type: 'image/jpeg', lastModified: Date.now() })
}
