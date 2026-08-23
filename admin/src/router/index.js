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
      { path: '/courses', component: () => import('../views/Courses.vue'), meta: { title: '课程管理' } },
      { path: '/schedules', component: () => import('../views/Schedules.vue'), meta: { title: '课表管理' } },
      { path: '/bookings', component: () => import('../views/Bookings.vue'), meta: { title: '预约管理' } },
      { path: '/users', component: () => import('../views/Users.vue'), meta: { title: '学员管理' } },
      { path: '/cards', component: () => import('../views/Cards.vue'), meta: { title: '卡包发放' } },
      { path: '/opportunities', component: () => import('../views/Opportunities.vue'), meta: { title: '成长机会' } },
      { path: '/applies', component: () => import('../views/Applies.vue'), meta: { title: '报名审核' } },
      { path: '/practice', component: () => import('../views/Practice.vue'), meta: { title: '签到记录' } },
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
  return true
})

export default router
