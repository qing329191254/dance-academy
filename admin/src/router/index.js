import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: '/dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '工作台' } },
      { path: '/studio', component: () => import('../views/Studio.vue'), meta: { title: '门店信息' } },
      { path: '/media', component: () => import('../views/Media.vue'), meta: { title: '轮播与相册' } },
      { path: '/teachers', component: () => import('../views/Teachers.vue'), meta: { title: '老师管理' } },
      { path: '/teacher-reviews', component: () => import('../views/TeacherReviews.vue'), meta: { title: '学员评价' } },
      { path: '/courses', component: () => import('../views/Courses.vue'), meta: { title: '课程产品' } },
      { path: '/schedules', component: () => import('../views/Schedules.vue'), meta: { title: '课表管理' } },
      { path: '/bookings', component: () => import('../views/Bookings.vue'), meta: { title: '预约管理' } },
      { path: '/users', component: () => import('../views/Users.vue'), meta: { title: '学员管理' } },
      { path: '/cards', component: () => import('../views/Cards.vue'), meta: { title: '卡包发放' } },
      { path: '/opportunities', component: () => import('../views/Opportunities.vue'), meta: { title: '成长机会' } },
      { path: '/applies', component: () => import('../views/Applies.vue'), meta: { title: '报名审核' } },
      { path: '/practice', component: () => import('../views/Practice.vue'), meta: { title: '签到记录' } },
      { path: '/checkin-pending', component: () => import('../views/CheckinPending.vue'), meta: { title: '待确认签到' } },
      { path: '/teacher-attendance', component: () => import('../views/TeacherAttendance.vue'), meta: { title: '教师考勤' } },
      { path: '/employee-duty', component: () => import('../views/EmployeeDuty.vue'), meta: { title: '员工值班' } },
      { path: '/class-archives', component: () => import('../views/ClassArchives.vue'), meta: { title: '课堂档案' } },
      { path: '/feedback', component: () => import('../views/Feedback.vue'), meta: { title: '意见反馈' } },
      { path: '/schools', component: () => import('../views/Schools.vue'), meta: { title: '学校管理', superOnly: true } },
      { path: '/admins', component: () => import('../views/Admins.vue'), meta: { title: '管理员', superOnly: true } },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/'
  }
  if (to.meta.superOnly || to.path.startsWith('/admins') || to.path.startsWith('/schools')) {
    const profile = JSON.parse(localStorage.getItem('admin_profile') || 'null')
    if (profile && !profile.superAdmin) {
      return '/dashboard'
    }
  }
  return true
})

export default router
