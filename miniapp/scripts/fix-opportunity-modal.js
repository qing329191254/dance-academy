const fs = require('fs')
const path = require('path')

const files = [
  'pages/growth/opportunity.vue',
]

const ROOT = path.join(__dirname, '..')

for (const rel of files) {
  const file = path.join(ROOT, rel)
  let content = fs.readFileSync(file, 'utf8')

  content = content.replace(
    /<app-modal[\s\S]*?v-model:show="showSuccess"[\s\S]*?\/>/,
    `<app-modal
      v-model:show="showSuccess"
      title="报名成功"
      content="已进入后台报名数据，筛选结果将通知学员"
    />`
  )

  content = content.replace(
    /<app-modal[\s\S]*?v-model:show="showCopied"[\s\S]*?\/>/,
    `<app-modal
      v-model:show="showCopied"
      title="链接已复制"
      content="可发送给同学一起报名"
    />`
  )

  content = content.replace(/不涉及收费支付[^<]*/g, '不涉及收费支付。')

  fs.writeFileSync(file, content, 'utf8')
  console.log('fixed', rel)
}
