import { createRouter, createWebHistory } from 'vue-router'
import GeneratedPage from '../pages/GeneratedPage.vue'
import { pageMap } from '../generated/pages'

const defaultPath = '/pages/PP_02_Property_Dashboard'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: defaultPath },
    {
      path: '/pages/:pageKey',
      component: GeneratedPage,
      beforeEnter: (to) => {
        const pageKey = String(to.params.pageKey || '')
        return pageMap.has(pageKey) ? true : defaultPath
      },
    },
    { path: '/:pathMatch(.*)*', redirect: defaultPath },
  ],
})
