const sharp = require('sharp')
const fs = require('fs')
const path = require('path')

const W = 690
const H = 320
const OUT = path.join(__dirname, '../static/cards')
const LOGO = path.join(__dirname, '../static/logo.png')

fs.mkdirSync(OUT, { recursive: true })

function makeGradientSvg(c1, c2, glow) {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}">
    <defs>
      <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="rgb(${c1.join(',')})"/>
        <stop offset="100%" stop-color="rgb(${c2.join(',')})"/>
      </linearGradient>
      <radialGradient id="r" cx="78%" cy="35%" r="45%">
        <stop offset="0%" stop-color="rgb(${glow.join(',')})" stop-opacity="0.45"/>
        <stop offset="100%" stop-color="rgb(${glow.join(',')})" stop-opacity="0"/>
      </radialGradient>
    </defs>
    <rect width="100%" height="100%" fill="url(#g)"/>
    <rect width="100%" height="100%" fill="url(#r)"/>
  </svg>`
}

async function render(name, c1, c2, glow) {
  const bg = await sharp(Buffer.from(makeGradientSvg(c1, c2, glow))).png().toBuffer()
  const logoMain = await sharp(LOGO).resize(220).png().toBuffer()
  const logoMeta = await sharp(logoMain).metadata()

  const watermark = await sharp(LOGO)
    .resize(300)
    .ensureAlpha()
    .linear(1, -40)
    .png()
    .toBuffer()

  const wmLayer = await sharp({
    create: { width: W, height: H, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } },
  })
    .composite([{ input: watermark, left: -24, top: Math.round(H * 0.06) }])
    .png()
    .toBuffer()

  const wmFaded = await sharp(wmLayer)
    .composite([
      {
        input: Buffer.from([255, 255, 255, 22]),
        raw: { width: 1, height: 1, channels: 4 },
        tile: true,
        blend: 'dest-in',
      },
    ])
    .png()
    .toBuffer()

  const left = W - logoMeta.width - 36
  const top = Math.round((H - logoMeta.height) / 2)

  await sharp(bg)
    .composite([
      { input: wmFaded, left: 0, top: 0 },
      { input: logoMain, left, top },
    ])
    .png()
    .toFile(path.join(OUT, name))

  console.log('generated', name)
}

;(async () => {
  await render('card-group.png', [52, 38, 88], [18, 14, 32], [138, 116, 229])
  await render('card-private.png', [72, 34, 58], [24, 14, 24], [220, 140, 190])
})()
