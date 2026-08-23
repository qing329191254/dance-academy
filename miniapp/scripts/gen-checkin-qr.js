const QRCode = require('qrcode')
const fs = require('fs')
const path = require('path')

const OUT = path.join(__dirname, '../static/checkin')
fs.mkdirSync(OUT, { recursive: true })

const payload = JSON.stringify({
  t: 'checkin',
  id: '101',
  className: 'HIPHOP',
  date: '2026-08-22',
  time: '16:00-17:15',
  teacher: '金大铭',
  room: '二楼 Room B',
  duration: '75分钟',
})

QRCode.toFile(path.join(OUT, 'checkin-qr.png'), payload, {
  width: 480,
  margin: 2,
  color: { dark: '#1a1028', light: '#ffffff' },
})
  .then(() => console.log('checkin qr generated:', payload))
  .catch((err) => {
    console.error(err)
    process.exit(1)
  })
