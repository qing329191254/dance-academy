/**
 * 修复因编码损坏导致的 Vue 模板闭合标签问题（?/text>、?/view> 等）
 */
const fs = require('fs')
const path = require('path')

const ROOT = path.join(__dirname, '..', 'pages')

const HEADING_FIXES = [
  ['说?/text>', '说明</text>'],
  ['内?/text>', '内容</text>'],
  ['费?/text>', '费用</text>'],
  ['规?/text>', '规范</text>'],
  ['产?/text>', '产权</text>'],
  ['声?/text>', '声明</text>'],
  ['变?/text>', '变更</text>'],
  ['系?/text>', '联系</text>'],
  ['目?/text>', '目的</text>'],
  ['保?/text>', '保护</text>'],
  ['权?/text>', '权利</text>'],
  ['更?/text>', '更新</text>'],
]

function walk(dir, list = []) {
  for (const name of fs.readdirSync(dir)) {
    const full = path.join(dir, name)
    if (fs.statSync(full).isDirectory()) {
      walk(full, list)
    } else if (name.endsWith('.vue')) {
      list.push(full)
    }
  }
  return list
}

function fixFile(filePath) {
  let content = fs.readFileSync(filePath, 'utf8')
  const original = content

  for (const [from, to] of HEADING_FIXES) {
    content = content.split(from).join(to)
  }

  // 段落/普通文案：?/text> 多为 「。</text>」 损坏
  content = content.replace(/([^<])\?\/text>/g, '$1。</text>')
  content = content.replace(/([^<])\?\/view>/g, '$1</view>')

  // 修复未闭合的引号行（placeholder 等）
  content = content.replace(/placeholder="([^"\n]*)\n/g, (match, p1) => {
    if (p1.endsWith('"')) return match
    return `placeholder="${p1}"\n`
  })

  if (content !== original) {
    fs.writeFileSync(filePath, content, 'utf8')
    return true
  }
  return false
}

const files = walk(ROOT)
let count = 0
for (const file of files) {
  if (fixFile(file)) {
    count += 1
    console.log('fixed:', path.relative(ROOT, file))
  }
}
console.log(`done, ${count} files updated`)
