export const studio = {
  name: '高校FOR-GET舞室',
  location: '四川成都 · 高校FOR-GET舞室',
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

export const trialCourse = {
  name: '体验课',
  price: '9.9',
  unit: '节',
  tag: '新人专享',
  summary: '一次到店，感受课堂氛围与老师风格',
  desc: '适合第一次来舞室的同学。用一节体验课了解教室、音乐和上课节奏，再决定适合自己的课程体系。',
  highlights: ['一节团课体验', '到店即可上课', '可咨询老师选课建议'],
}

export const courseSystem = [
  {
    key: 'fixed',
    name: '精品固定班',
    summary: '固定时段、固定老师，按体系进阶',
    desc: '每周固定上课时间，跟着同一位老师系统训练。适合想长期学、把基础打扎实的同学。',
    highlights: ['固定班次与教室', '按阶段进阶', '适合持续出勤'],
    actionLabel: '查看固定班课表',
    actionTab: 'fixed',
  },
  {
    key: 'pass',
    name: '次通卡',
    summary: '按次计费，团课灵活通刷',
    desc: '买次卡后可预约团课，时间更灵活。适合课表不固定、想按自己节奏来上课的同学。',
    highlights: ['按次扣课', '团课通刷', '约满即来、更自由'],
    actionLabel: '咨询购卡',
    actionTab: '',
  },
  {
    key: 'private',
    name: '私教',
    summary: '1 对 1，针对个人问题专项突破',
    desc: '根据你的基础、目标和赛程单独排课。适合想快速提升、准备比赛或需要纠错巩固的同学。',
    highlights: ['1 对 1 授课', '内容可定制', '时间需与老师协商'],
    actionLabel: '预约私教',
    actionTab: 'private',
  },
  {
    key: 'custom',
    name: '定制课程 · 赛事商演',
    summary: '编舞定制、比赛集训与商演排练',
    desc: '为社团、比赛、商演或品牌活动定制编舞与排练计划。可按人数、风格和上场时间单独沟通。',
    highlights: ['编舞定制', '赛事集训', '商演排练'],
    actionLabel: '预约咨询',
    actionTab: '',
  },
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
  'FOR-GET不仅是上课，我们把兼职、实习、就业演出、商演、考证等多样化资源嫁接给大家，用舞蹈发展、勤工俭学两条成长线，帮你从学员走向舞台与职场，愿你的大学，因为有FG而更好。'

export const workTracks = [
  { key: 'parttime', name: '兼职', level: 'T1', desc: '活动执行、课程助理等校园兼职机会' },
  { key: 'intern', name: '实习', level: 'T2', desc: '教务部、招新部、宣传部等正式实习岗位' },
  { key: 'manage', name: '管理', level: 'T3', desc: '单项目/分校区/品牌负责人等深度方向' },
]

export const danceTracks = [
  { key: 'show', name: '演出', level: 'T1', desc: '校园表演、学期派对、MV拍摄等机会' },
  { key: 'commercial', name: '商演', level: 'T2', desc: 'FG舞队:品牌活动、商演、邀约与大型赛事' },
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
