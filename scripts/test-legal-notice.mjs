import { applyLegalFromStudio, getLegalInfo } from '../miniapp/common/legal.js'
import { parseStudentNotice } from '../miniapp/common/studentNotice.js'

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

const sections = parseStudentNotice('## 一、测试\n第一行\n\n## 二、测试2\n第二行')
if (sections.length !== 2 || sections[0].title !== '一、测试') {
  throw new Error('parseStudentNotice failed')
}

console.log('legal + studentNotice tests passed')
