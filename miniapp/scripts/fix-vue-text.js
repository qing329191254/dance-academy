const fs = require('fs')
const path = require('path')

const fixes = [
  ['pages/book/book.vue', [
    [/私教需与老师协商后预约具体日期时[^\n<]*/g, '私教需与老师协商后预约具体日期时间'],
    [/<text v-for="n in 5"[^>]*>[^<]*<\/text>/g, '<text v-for="n in 5" :key="n" :class="n <= item.stars ? \'on\' : \'off\'">★</text>'],
  ]],
  ['pages/mine/cards.vue', [
    [/换封[^<]*/g, '换封面'],
    [/title="封面已更[^"]*"/g, 'title="封面已更新"'],
    [/content="卡包封面已保[^"]*"/g, 'content="卡包封面已保存"'],
  ]],
  ['pages/mine/practice.vue', [
    [/已签[^<]*<\/text>/g, '已签到</text>'],
    [/去扫码签[^<]*/g, '去扫码签到'],
  ]],
  ['pages/growth/work.vue', [
    [/报名[^<]*<\/text>/g, '报名。</text>'],
  ]],
  ['pages/growth/dance.vue', [
    [/<text class="lead muted">[\s\S]*?<\/text>/, '<text class="lead muted">舞蹈发展成长线：演出练胆 → 商演实践 → 教师考证与任教。</text>'],
  ]],
]

const ROOT = path.join(__dirname, '..')
for (const [rel, rules] of fixes) {
  const file = path.join(ROOT, rel)
  let content = fs.readFileSync(file, 'utf8')
  for (const [pattern, replacement] of rules) {
    content = content.replace(pattern, replacement)
  }
  fs.writeFileSync(file, content, 'utf8')
  console.log('fixed', rel)
}
