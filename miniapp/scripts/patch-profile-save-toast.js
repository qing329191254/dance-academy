const fs = require('fs')
const path = require('path')

const file = path.join(__dirname, '..', 'pages', 'login', 'profile.vue')
let content = fs.readFileSync(file, 'utf8')
content = content.replace("showSuccessToast('资料已保存')", "showSuccessToast('保存成功')")
fs.writeFileSync(file, content, 'utf8')
console.log('updated profile save toast')
