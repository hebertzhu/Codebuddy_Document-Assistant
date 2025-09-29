import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LiteratureDetail from '@/views/LiteratureDetail.vue'
// lazy views
const SettingsView = () => import('@/views/SettingsView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: '文献库 - 文献助手' }
    },
    {
      path: '/literature/:id',
      name: 'literature-detail',
      component: LiteratureDetail,
      meta: { title: '文献详情 - 文献助手' }
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView,
      meta: { title: '设置 - 文献助手' }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

// 路由守卫 - 设置页面标题
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router