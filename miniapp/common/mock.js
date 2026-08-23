export const studio = {
  name: '高校FOR一GET街舞俱乐部',
  location: '四川成都 · 高校街舞俱乐部',
  city: '四川成都',
  address: '四川省成都市',
  latitude: 30.659462,
  longitude: 104.065735,
  businessHours: '营业时间 13:00-22:00',
  phone: '02888881234',
  phoneDisplay: '028-8888-1234',
}

export const brandPhotos = []

export const teachers = [
  { id: 1, name: '金大铭', style: 'HipHop', intro: '校队主力，擅长编舞与舞台表现', avatar: '' },
  { id: 2, name: '龙龙', style: 'Jazz', intro: 'Jazz 体系主教，课程节奏感强', avatar: '' },
  { id: 3, name: '90', style: 'Breaking', intro: 'Breaking 专项，带队比赛经验丰富', avatar: '' },
  { id: 4, name: '小朱', style: 'Waacking', intro: 'Waacking / 女团风，舞台感突出', avatar: '' },
]

export function getTeacherAvatar(name) {
  return teachers.find((t) => t.name === name)?.avatar || ''
}

export const courses = [
  { id: 1, name: 'HipHop 入门', price: 199, level: '零基础', desc: '节奏、律动与基础脚步' },
  { id: 2, name: 'Jazz 二星课', price: 299, level: '进阶', desc: '组合编排与表现力训练' },
  { id: 3, name: 'Breaking 专项', price: 399, level: '进阶', desc: 'Footwork / Freeze / Power' },
]

export const bookTabs = [
  {
    key: 'group',
    name: '团课',
    list: [
      { id: 101, dayOffset: 0, name: 'HIPHOP', time: '16:00-17:15', teacher: '金大铭', room: '二楼 Room B', stars: 3, status: '可预约' },
      { id: 102, dayOffset: 0, name: 'JAZZ二星课', time: '18:00-19:15', teacher: '龙龙', room: '二楼 Room A', stars: 4, status: '可预约' },
      { id: 103, dayOffset: 1, name: 'Breaking 基础', time: '14:00-15:30', teacher: '90', room: '一楼 Studio', stars: 3, status: '可预约' },
      { id: 104, dayOffset: 1, name: 'Waacking', time: '19:00-20:15', teacher: '小朱', room: '二楼 Room B', stars: 4, status: '可预约' },
      { id: 105, dayOffset: 2, name: 'HIPHOP', time: '16:00-17:15', teacher: '金大铭', room: '二楼 Room B', stars: 3, status: '可预约' },
      { id: 106, dayOffset: 3, name: 'JAZZ二星课', time: '18:00-19:15', teacher: '龙龙', room: '二楼 Room A', stars: 4, status: '名额紧张' },
      { id: 107, dayOffset: 5, name: 'Breaking 专项', time: '15:00-16:30', teacher: '90', room: '一楼 Studio', stars: 4, status: '可预约' },
    ],
  },
  {
    key: 'fixed',
    name: '固定班',
    list: [
      { id: 201, name: '周末固定班 · HipHop', time: '周六 14:00-15:30', teacher: '90', room: '一楼 Studio', stars: 3, status: '招生中' },
      { id: 202, name: '周中固定班 · Jazz', time: '周三 19:30-21:00', teacher: '龙龙', room: '二楼 Room A', stars: 4, status: '名额紧张' },
    ],
  },
  {
    key: 'private',
    name: '私教课',
    list: [
      { id: 301, name: '1v1 私教 · 编舞', time: '预约制', teacher: '金大铭', room: '私教室', stars: 5, status: '可预约' },
      { id: 302, name: '1v1 私教 · 基础巩固', time: '预约制', teacher: '小朱', room: '私教室', stars: 4, status: '可预约' },
    ],
  },
]

export const userGrowthProfile = {
  work: {
    line: '勤工俭学',
    current: '兼职',
    level: 'T1',
    path: '兼职 → 实习 → 管理',
    url: '/pages/growth/work',
  },
  dance: {
    line: '舞蹈发展',
    current: '演出',
    level: 'T1',
    path: '演出 → 商演 → 教师',
    url: '/pages/growth/dance',
  },
}

export const growthIntro =
  '高校街舞俱乐部不只是上课——我们把兼职、实习、演出、商演、教师考证等资源嫁接给学生，用两条成长线帮你从学员走向舞台与职场。'

export const workTracks = [
  { key: 'parttime', name: '兼职', level: 'T1', desc: '活动执行、课程助理等校园兼职机会' },
  { key: 'intern', name: '实习', level: 'T2', desc: '品牌运营、教务协同等正式实习岗位' },
  { key: 'manage', name: '管理', level: 'T3', desc: '分馆/项目负责人方向的管理成长' },
]

export const danceTracks = [
  { key: 'show', name: '演出', level: 'T1', desc: '校内晚会、社团联动等演出机会' },
  { key: 'commercial', name: '商演', level: 'T2', desc: '品牌活动、商演邀约与舞台实践' },
  { key: 'teacher', name: '教师', level: 'T3', desc: '教师考证、带班助教与正式任教' },
]

export const opportunities = {
  parttime: [
    { id: 'w1', title: '周末活动执行助理', deadline: '2026-09-05', spots: 6, level: 'T1', summary: '协助活动布场与现场执行，适合新学员积累经验。' },
    { id: 'w2', title: '公开课现场助教', deadline: '2026-09-12', spots: 4, level: 'T1', summary: '协助老师控场、签到与学员引导。' },
  ],
  intern: [
    { id: 'w3', title: '品牌内容运营实习', deadline: '2026-09-20', spots: 2, level: 'T2', summary: '短视频选题、拍摄协助与社群内容更新。' },
  ],
  manage: [
    { id: 'w4', title: '秋季项目组负责人选拔', deadline: '2026-10-01', spots: 1, level: 'T3', summary: '负责一组学员活动统筹，需具备 T3 管理权益。' },
  ],
  show: [
    { id: 'd1', title: '迎新晚会节目海选', deadline: '2026-09-08', spots: 12, level: 'T1', summary: 'HipHop / Jazz 小组节目，通过后进入排练。' },
  ],
  commercial: [
    { id: 'd2', title: '商场品牌快闪商演', deadline: '2026-09-18', spots: 8, level: 'T2', summary: '商演排练 2 次 + 正式演出 1 场。' },
  ],
  teacher: [
    { id: 'd3', title: '年度教师资格考证班', deadline: '2026-10-15', spots: 10, level: 'T3', summary: '面向达到教师成长线 T3 的学员开放报名。' },
  ],
}

export const trackMeta = {
  parttime: { line: '勤工俭学', name: '兼职', level: 'T1' },
  intern: { line: '勤工俭学', name: '实习', level: 'T2' },
  manage: { line: '勤工俭学', name: '管理', level: 'T3' },
  show: { line: '舞蹈发展', name: '演出', level: 'T1' },
  commercial: { line: '舞蹈发展', name: '商演', level: 'T2' },
  teacher: { line: '舞蹈发展', name: '教师', level: 'T3' },
}

export const myCards = [
  {
    id: 1,
    name: '团课 10 次卡',
    type: '团课',
    remain: 6,
    total: 10,
    expire: '2026-12-31',
    cover: '',
  },
  {
    id: 2,
    name: '私教体验卡',
    type: '私教',
    remain: 1,
    total: 2,
    expire: '2026-10-15',
    cover: '',
  },
]

export const myCourses = [
  {
    id: 1,
    name: 'HipHop 入门',
    teacher: '金大铭',
    progress: '3/12 节',
    status: '进行中',
  },
  {
    id: 2,
    name: 'Jazz 二星课',
    teacher: '龙龙',
    progress: '1/16 节',
    status: '进行中',
  },
  {
    id: 3,
    name: 'Breaking 专项',
    teacher: '90',
    progress: '待开课',
    status: '未开始',
  },
]

export const myBookings = [
  {
    id: 1,
    name: 'JAZZ二星课',
    date: '2026-08-22',
    time: '18:00-19:15',
    teacher: '龙龙',
    room: '二楼 Room A',
    status: '待上课',
  },
]

export const myPractice = []
