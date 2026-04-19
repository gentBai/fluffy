import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'routes',
        name: 'Routes',
        component: () => import('../views/Routes.vue')
      },
      {
        path: 'services',
        name: 'Services',
        component: () => import('../views/Services.vue')
      },
      {
        path: 'auth',
        name: 'Auth',
        component: () => import('../views/Auth.vue')
      },
      {
        path: 'rate-limit',
        name: 'RateLimit',
        component: () => import('../views/RateLimit.vue')
      },
      {
        path: 'blacklist',
        name: 'Blacklist',
        component: () => import('../views/Blacklist.vue')
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('../views/Logs.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
