const fs = require('fs')
const path = require('path')

const file = path.join(__dirname, '..', 'pages', 'mine', 'cards.vue')
let content = fs.readFileSync(file, 'utf8')

content = content.replace(
  /<app-modal[\s\S]*?v-model:show="showCoverDone"[\s\S]*?\/>/,
  `<app-modal
      v-model:show="showCoverDone"
      title="封面已更新"
      content="卡包封面已保存"
    />`
)

fs.writeFileSync(file, content, 'utf8')
console.log('cards.vue fixed')
