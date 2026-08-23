const fs = require('fs')
const path = require('path')

function walk(dir, list = []) {
  for (const name of fs.readdirSync(dir)) {
    const full = path.join(dir, name)
    if (fs.statSync(full).isDirectory()) walk(full, list)
    else if (name.endsWith('.vue')) list.push(full)
  }
  return list
}

const ROOT = path.join(__dirname, '..')
const issues = []

for (const file of walk(ROOT)) {
  const rel = path.relative(ROOT, file)
  if (rel.includes('node_modules')) continue
  const content = fs.readFileSync(file, 'utf8')
  const template = content.match(/<template>([\s\S]*?)<\/template>/)?.[1] || ''
  if (template.includes('?/text>') || template.includes('?/view>')) {
    issues.push(`${rel}: broken closing tag`)
  }
  const badAttrs = template.match(/\b(?:title|content|placeholder)="[^"\n]*$/gm)
  if (badAttrs) issues.push(`${rel}: unclosed attr ${badAttrs[0]}`)
}

if (issues.length) {
  console.log(issues.join('\n'))
  process.exit(1)
}
console.log('all vue templates look syntactically ok')
