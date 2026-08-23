const fs = require('fs')
const path = require('path')

const ROOT = path.join(__dirname, '..')

function read(rel) {
  return fs.readFileSync(path.join(ROOT, rel), 'utf8')
}

function write(rel, content) {
  fs.writeFileSync(path.join(ROOT, rel), content, 'utf8')
  console.log('wrote', rel)
}

function addToastComponent(content) {
  if (content.includes('<app-toast')) return content
  return content.replace(/(\s*)<\/view>\s*\n<\/template>/, '$1    <app-toast />\n  </view>\n</template>')
}

// login.vue
write('pages/login/login.vue', addToastComponent(read('pages/login/login.vue')
  .replace(
    /\s*<app-modal[\s\S]*?@confirm="onResultConfirm"\s*\/>/,
    ''
  )
  .replace(
    `import { ref } from 'vue'
import { weixinOneTapLogin, navigateAfterLogin } from '@/common/auth.js'

const showResult = ref(false)
const resultTitle = ref('')
const resultContent = ref('')
const loginOk = ref(false)

async function onLogin() {
  try {
    await weixinOneTapLogin()
    loginOk.value = true
    resultTitle.value = '登录成功'
    resultContent.value = '欢迎回来，即将为您跳转'
    showResult.value = true
  } catch (err) {
    loginOk.value = false
    resultTitle.value = '登录失败'
    resultContent.value = '请稍后重试'
    showResult.value = true
  }
}

function onResultConfirm() {
  if (loginOk.value) {
    navigateAfterLogin()
  }
}`,
    `import { weixinOneTapLogin, navigateAfterLogin } from '@/common/auth.js'
import { showSuccess, showError } from '@/common/toast.js'

async function onLogin() {
  try {
    await weixinOneTapLogin()
    await showSuccess('欢迎回来', 1500)
    navigateAfterLogin()
  } catch (err) {
    showError('登录失败，请稍后重试')
  }
}`
  )))

// profile.vue
write('pages/login/profile.vue', addToastComponent(read('pages/login/profile.vue')
  .replace(
    /\s*<app-modal[\s\S]*?@confirm="goMine"\s*\/>/,
    ''
  )
  .replace(
    `import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { completeProfile, getUser, isLoggedIn } from '@/common/auth.js'`,
    `import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { completeProfile, getUser, isLoggedIn } from '@/common/auth.js'
import { showToast, showSuccess } from '@/common/toast.js'`
  )
  .replace('const showSuccess = ref(false)\n', '')
  .replace(
    `  if (!avatar.value) {
    uni.showToast({ title: '请上传头像', icon: 'none' })
    return
  }
  if (!nickname.value.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  if (!gender.value) {
    uni.showToast({ title: '请选择性别', icon: 'none' })
    return
  }

  completeProfile({
    nickname: nickname.value.trim(),
    avatar: avatar.value,
    gender: gender.value,
    birthday: birthday.value,
  })

  showSuccess.value = true
}

function goMine() {
  uni.switchTab({ url: '/pages/mine/mine' })
}`,
    `  if (!avatar.value) {
    showToast('请上传头像')
    return
  }
  if (!nickname.value.trim()) {
    showToast('请输入昵称')
    return
  }
  if (!gender.value) {
    showToast('请选择性别')
    return
  }

  completeProfile({
    nickname: nickname.value.trim(),
    avatar: avatar.value,
    gender: gender.value,
    birthday: birthday.value,
  })

  showSuccess('资料已保存').then(() => {
    uni.switchTab({ url: '/pages/mine/mine' })
  })
}`
  )))

// book.vue
write('pages/book/book.vue', addToastComponent(read('pages/book/book.vue')
  .replace(
    /\s*<app-modal[\s\S]*?v-model:show="showBookResult"[\s\S]*?\/>/,
    ''
  )
  .replace(
    `import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { bookTabs, getTeacherAvatar } from '@/common/mock.js'`,
    `import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { bookTabs, getTeacherAvatar } from '@/common/mock.js'
import { showSuccess } from '@/common/toast.js'`
  )
  .replace('const showBookResult = ref(false)\nconst bookResultContent = ref(\'\')\n', '')
  .replace(
    /function book\(item\) \{[\s\S]*?\}/,
    `function book(item) {
  const dateTip = active.value === 'group' ? \`\${selectedDateText.value} \` : ''
  showSuccess(\`已预约 \${dateTip}\${item.time} \${item.name}\`)
}`
  )))

// cards.vue
write('pages/mine/cards.vue', addToastComponent(read('pages/mine/cards.vue')
  .replace(
    /\s*<app-modal[\s\S]*?v-model:show="showCoverDone"[\s\S]*?\/>/,
    ''
  )
  .replace(
    `import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { myCards } from '@/common/mock.js'
import { mergeCardList, setCardCover } from '@/common/cardCover.js'
import { ensureLogin } from '@/common/auth.js'`,
    `import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { myCards } from '@/common/mock.js'
import { mergeCardList, setCardCover } from '@/common/cardCover.js'
import { ensureLogin } from '@/common/auth.js'
import { showSuccess } from '@/common/toast.js'`
  )
  .replace('const showCoverDone = ref(false)\n', '')
  .replace('showCoverDone.value = true', 'showSuccess(\'封面已更新\')')
))

// opportunity.vue
write('pages/growth/opportunity.vue', addToastComponent(read('pages/growth/opportunity.vue')
  .replace(
    /\s*<app-modal[\s\S]*?v-model:show="showSuccess"[\s\S]*?\/>/,
    ''
  )
  .replace(
    /\s*<app-modal[\s\S]*?v-model:show="showCopied"[\s\S]*?\/>/,
    ''
  )
  .replace(
    `import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { opportunities, trackMeta } from '@/common/mock.js'`,
    `import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { opportunities, trackMeta } from '@/common/mock.js'
import { showSuccess, showToast } from '@/common/toast.js'`
  )
  .replace('const showSuccess = ref(false)\nconst showCopied = ref(false)\n', '')
  .replace('showSuccess.value = true', 'showSuccess(\'报名成功\')')
  .replace('showCopied.value = true', 'showToast(\'链接已复制\')')
))

// course/detail.vue
write('pages/course/detail.vue', addToastComponent(read('pages/course/detail.vue')
  .replace(
    /\s*<app-modal[\s\S]*?v-model:show="showBuyTip"[\s\S]*?\/>/,
    ''
  )
  .replace(
    `import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { courses } from '@/common/mock.js'`,
    `import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { courses } from '@/common/mock.js'
import { showToast } from '@/common/toast.js'`
  )
  .replace('const showBuyTip = ref(false)\n', '')
  .replace('showBuyTip.value = true', "showToast('请联系门店或课程顾问完成购课')")
))

// mine.vue
write('pages/mine/mine.vue', addToastComponent(read('pages/mine/mine.vue')
  .replace(
    /\s*<app-modal\n      v-model:show="showCheckInResult"[\s\S]*?@confirm="onCheckInConfirm"\s*\/>/,
    ''
  )
  .replace(
    `import { openPage, switchTabPage } from '@/common/navigate.js'`,
    `import { openPage, switchTabPage } from '@/common/navigate.js'
import { showSuccess, showError, showToast } from '@/common/toast.js'`
  )
  .replace('const showCheckInResult = ref(false)\nconst checkInTitle = ref(\'\')\nconst checkInContent = ref(\'\')\n', '')
  .replace(
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
  .replace(
    `uni.showToast({ title: '已退出登录', icon: 'none' })`,
    `showToast('已退出登录')`
  )
))

// home.vue
write('pages/home/home.vue', addToastComponent(read('pages/home/home.vue')
  .replace(
    `import { openPage, switchTabPage } from '@/common/navigate.js'`,
    `import { openPage, switchTabPage } from '@/common/navigate.js'
import { showError } from '@/common/toast.js'`
  )
  .replace(
    `uni.showToast({ title: '图片预览失败', icon: 'none' })`,
    `showError('图片预览失败')`
  )
))

console.log('toast patch done')
