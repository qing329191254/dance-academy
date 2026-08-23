const fs = require('fs')
const path = require('path')

require('./restore-mine.js')

const file = path.join(__dirname, '..', 'pages', 'mine', 'mine.vue')
let content = fs.readFileSync(file, 'utf8')

content = content.replace(
  /\n    <app-modal\n      v-model:show="showLogoutDone"\n      title="退出成功"\n      content="您已退出登录"\n    \/>\n/,
  '\n'
)

content = content.replace(
  'const showLogoutConfirm = ref(false)\nconst showLogoutDone = ref(false)',
  'const showLogoutConfirm = ref(false)'
)

content = content.replace(
  `function doLogout() {
  logout()
  refreshUser()
  showLogoutDone.value = true
}`,
  `function doLogout() {
  logout()
  refreshUser()
  uni.showToast({ title: '已退出登录', icon: 'none' })
}`
)

fs.writeFileSync(file, content, 'utf8')
console.log('mine.vue restored with logout toast')
