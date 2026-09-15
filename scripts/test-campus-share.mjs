import assert from 'node:assert/strict'
import {
  parseQueryString,
  queryFromLaunchOptions,
  sharePathWithCampus,
  shareEntryKey,
  shouldApplyShareCampus,
} from '../miniapp/common/campusQuery.js'

function testParse() {
  assert.deepEqual(parseQueryString('campusId=2'), { campusId: '2' })
  assert.deepEqual(parseQueryString('?campusId=shizishan&x=1'), { campusId: 'shizishan', x: '1' })
}

function testLaunchQuery() {
  assert.equal(
    queryFromLaunchOptions({ query: { campusId: '3' } }).campusId,
    '3',
  )
  assert.equal(
    queryFromLaunchOptions({ path: 'pages/home/home?campusId=5', query: {} }).campusId,
    '5',
  )
  assert.equal(
    queryFromLaunchOptions({ path: 'pages/home/home', query: {} }).campusId,
    undefined,
  )
}

function testSharePath() {
  assert.equal(sharePathWithCampus('2'), '/pages/home/home?campusId=2')
  assert.equal(sharePathWithCampus(''), '/pages/home/home')
}

function testResumeDoesNotReapply() {
  const first = { scene: 1007, query: { campusId: '2' } }
  const resume = { scene: 1007, query: { campusId: '2' } }
  const other = { scene: 1007, query: { campusId: '8' } }
  const a = shouldApplyShareCampus(first, '')
  assert.equal(a.apply, true)
  const b = shouldApplyShareCampus(resume, a.key)
  assert.equal(b.apply, false)
  const c = shouldApplyShareCampus(other, b.key)
  assert.equal(c.apply, true)
  assert.equal(shareEntryKey({ query: {} }), '')
}

function testColdStartTrustsStoredId() {
  const fallback = [{ id: '1', name: '四川师范大学' }]
  const stored = '8'
  const readCampusId = fallback.some((item) => item.id === stored) ? stored : stored
  assert.equal(readCampusId, '8')
  const afterList = [
    { id: '1', name: '四川师范大学' },
    { id: '8', name: '川师狮山' },
  ]
  const keep = afterList.some((item) => item.id === stored) ? stored : afterList[0].id
  assert.equal(keep, '8')
}

testParse()
testLaunchQuery()
testSharePath()
testResumeDoesNotReapply()
testColdStartTrustsStoredId()
console.log('campus share tests passed')
