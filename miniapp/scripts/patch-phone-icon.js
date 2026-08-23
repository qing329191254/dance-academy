const fs = require('fs')
const path = require('path')

const files = [
  path.join(__dirname, '..', 'pages', 'home', 'home.vue'),
  path.join(__dirname, 'restore-home.js'),
  path.join(__dirname, 'restore-vue-files.js'),
]

const from = '<view class="phone" @click.stop="callStudio">电</view>'
const to = `<view class="phone" @click.stop="callStudio">
            <image class="phone-icon" src="/static/nav/phone.png" mode="aspectFit" />
          </view>`

const styleFrom = `.phone {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #8a74e5;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
}`

const styleTo = `.phone {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #8a74e5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.phone-icon {
  width: 36rpx;
  height: 36rpx;
}`

for (const file of files) {
  if (!fs.existsSync(file)) continue
  let content = fs.readFileSync(file, 'utf8')
  if (!content.includes(from)) {
    console.log('skip (already patched?):', file)
    continue
  }
  content = content.replace(from, to)
  content = content.replace(styleFrom, styleTo)
  fs.writeFileSync(file, content, 'utf8')
  console.log('patched', file)
}
