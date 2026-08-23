const fs = require('fs')
const path = require('path')

const ROOT = path.join(__dirname, '..')

function write(rel, content) {
  fs.writeFileSync(path.join(ROOT, rel), content, 'utf8')
  console.log('fixed', rel)
}

let book = fs.readFileSync(path.join(ROOT, 'pages/book/book.vue'), 'utf8')
const bookStart = book.indexOf('function book(item)')
const bookEnd = book.indexOf('</script>', bookStart)
const bookFunc = `function book(item) {
  const dateTip = active.value === 'group' ? \`\${selectedDateText.value} \` : ''
  showSuccess(\`已预约 \${dateTip}\${item.time} \${item.name}\`)
}
`
book = book.slice(0, bookStart) + bookFunc + book.slice(bookEnd)
write('pages/book/book.vue', book)

let profile = fs.readFileSync(path.join(ROOT, 'pages/login/profile.vue'), 'utf8')
if (!profile.includes("from '@/common/toast.js'")) {
  profile = profile.replace(
    "import { completeProfile, getUser, isLoggedIn } from '@/common/auth.js'\r\n",
    "import { completeProfile, getUser, isLoggedIn } from '@/common/auth.js'\r\nimport { showToast, showSuccess as showSuccessToast } from '@/common/toast.js'\r\n"
  )
}
profile = profile.replace("const showSuccess = ref(false)\r\n", '')
const submitStart = profile.indexOf('function submit()')
const submitEnd = profile.indexOf('</script>', submitStart)
const submitFunc = `function submit() {
  if (!avatar.value) {
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

  showSuccessToast('资料已保存').then(() => {
    uni.switchTab({ url: '/pages/mine/mine' })
  })
}
`
profile = profile.slice(0, submitStart) + submitFunc + profile.slice(submitEnd)
write('pages/login/profile.vue', profile)

console.log('fixes done')
