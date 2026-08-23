const fs = require('fs')
const path = require('path')

const file = path.join(__dirname, '..', 'pages', 'mine', 'mine.vue')
let content = fs.readFileSync(file, 'utf8')

content = content.replace(
  /uni\.showToast\(\{ title: '[^']*', icon: 'none' \}\)/,
  "uni.showToast({ title: '\u5df2\u9000\u51fa\u767b\u5f55', icon: 'none' })"
)

fs.writeFileSync(file, content, 'utf8')
console.log('fixed mine logout toast')
