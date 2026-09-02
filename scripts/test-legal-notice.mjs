import { applyLegalFromStudio, getLegalInfo } from '../miniapp/common/legal.js'
import { parseStudentNotice, parseInlineParts } from '../miniapp/common/studentNotice.js'

applyLegalFromStudio({
  name: '测试舞室',
  company: '测试教育公司',
  email: 'test@example.com',
  phoneDisplay: '13800138000',
  address: '四川省成都市测试路1号',
  legalUpdateDate: '2026年1月1日',
})

const legal = getLegalInfo()
const checks = [
  ['brand', legal.brand, '测试舞室'],
  ['company', legal.company, '测试教育公司'],
  ['email', legal.email, 'test@example.com'],
  ['phone', legal.phone, '13800138000'],
  ['address', legal.address, '四川省成都市测试路1号'],
  ['updateDate', legal.updateDate, '2026年1月1日'],
]

for (const [key, actual, expected] of checks) {
  if (actual !== expected) {
    throw new Error(`${key} expected ${expected}, got ${actual}`)
  }
}

const boldParts = parseInlineParts('支持**分期付款**，请知悉')
if (boldParts.length !== 3 || !boldParts[1].bold || boldParts[1].text !== '分期付款') {
  throw new Error('parseInlineParts failed')
}

const lines = parseStudentNotice('标题\n\n第一行  保留空格\n**加粗**')
if (lines.length !== 4 || !lines[1].empty || lines[0].parts[0].text !== '标题') {
  throw new Error('parseStudentNotice line break failed')
}

if (parseStudentNotice('').length !== 0 || parseStudentNotice('   ').length !== 0) {
  throw new Error('parseStudentNotice empty failed')
}

console.log('legal + studentNotice tests passed')
