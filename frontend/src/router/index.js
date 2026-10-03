
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/UserLogin.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/approval',
    name: 'Approval',
    component: () => import('@/views/admin/ApprovalPage.vue'),
    meta: { requiresAuth: true, requiredRole: 2 }
  },
   {
    path: '/user-management',
    name: 'UserManagement',
    component:() => import('@/views/admin/UserManagement.vue'),
    meta: { requiresAuth: true,  requiredRole: 2}
  },
  {
    path: '/facility-management',
    name: 'FacilityManagement',
    component: () => import('@/views/admin/FacilityAdmin/FacilityManagement.vue'),
    meta: { requiresAuth: true , allowedRoles: [1, 2] }
  },
  {
    path: '/facility',
    name: 'FacilityList',
    component: () => import('@/views/FacilityList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reservation-list',
    name: 'ReservationList',
    component: () => import('@/views/ReservationList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reservation-management',
    name: 'ReservationManagement',
    component: () => import('@/views/admin/FacilityAdmin/ReservationManagement.vue'),
    meta: { requiresAuth: true, requiredRole: 1 }
  },
  {
    path: '/sport-recordandhealth-report',
    name: 'SportRecordAndHealthReport',
    component: () => import('@/views/SportRecordAndHealthReport.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfilePage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/announce-manage',
    name: 'AnnounceManage',
    component: () => import('@/views/admin/AnnounceManage.vue'),
    meta: { requiresAuth: true, allowedRoles: [1, 2] }
  },
  {
  path: '/facility-statistics',
  name: 'FacilityStatistics',
  component: () => import('@/views/FacilityStatistics.vue'),
  meta: { requiresAuth: true, allowedRoles: [1, 2]  }
}
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})
router.push({ path: '/', query: { showAnnouncement: 'true' } })
// 路由守卫 - 不使用 next 回调，不使用未使用的参数
router.beforeEach((to) => {
  const userInfoStr = localStorage.getItem('userInfo')
  const isLogin = userInfoStr !== null

  // 如果需要登录但未登录
  if (to.meta.requiresAuth && !isLogin) {
    return '/login'
  }

  // 如果需要特定角色
  if (to.meta.requiredRole && isLogin) {
    const userInfo = JSON.parse(userInfoStr)
    if (userInfo.role !== to.meta.requiredRole) {
      return '/'
    }
  }

  // 如果已登录且访问登录页，跳转到首页
  if (to.path === '/login' && isLogin) {
    return '/'
  }

  return true
})

export default router
