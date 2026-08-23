const fs = require('fs')
const path = require('path')

const file = path.join(__dirname, '..', 'pages', 'mine', 'mine.vue')
let content = fs.readFileSync(file, 'utf8')

content = content.replace(
  "import { openPage, switchTabPage } from '@/common/navigate.js'",
  "import { openPage, switchTabPage } from '@/common/navigate.js'\nimport { showSuccess, showError } from '@/common/toast.js'"
)

content = content.replace(
  /\s*<app-modal\n      v-model:show="showCheckInResult"[\s\S]*?@confirm="onCheckInConfirm"\s*\/>/,
  ''
)

content = content.replace(
  'const showCheckInResult = ref(false)\nconst checkInTitle = ref(\'\')\nconst checkInContent = ref(\'\')\n',
  ''
)

if (!content.includes('<app-toast')) {
  content = content.replace(
    /(\s*)<\/view>\s*\n<\/template>/,
    '$1    <app-toast />\n  </view>\n</template>'
  )
}

content = content.replace(
  `function doLogout() {
  logout()
  refreshUser()
  uni.showToast({ title: '已退出登录', icon: 'none' })
}`,
  `function doLogout() {
  logout()
  refreshUser()
  showSuccess('退出成功')
}`
)

content = content.replace(
  `function startScan() {
  startCheckInScan({
    onResult(outcome) {
      checkInTitle.value = outcome.ok ? '签到成功' : '签到失败'
      checkInContent.value = outcome.message
      showCheckInResult.value = true
      if (outcome.ok) refreshPracticeCount()
    },
  })
}

function onCheckInConfirm() {
  showCheckInResult.value = false
}`,
  `function startScan() {
  startCheckInScan({
    onResult(outcome) {
      if (outcome.ok) {
        showSuccess(outcome.message || '签到成功')
        refreshPracticeCount()
        return
      }
      showError(outcome.message || '签到失败')
    },
  })
}`
)

fs.writeFileSync(file, content, 'utf8')
console.log('mine.vue patched')
