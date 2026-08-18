import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { nextTick } from 'vue'

/**
 * 路由权限分级说明：
 * - 无 meta.auth：公开访问（首页、商家、商品详情、搜索等）
 * - meta.auth: true：需要登录（购物车、订单、个人中心等）
 * - meta.merchant: true：需要商家权限（商家中心）
 * - meta.admin: true：需要管理员权限（管理后台）
 */
const routes = [
  // ========== 公开访问页面（无需登录）==========
  { path: '/', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
  { path: '/test', component: () => import('@/views/Test.vue'), meta: { title: '测试页面' } },
  { path: '/stores', component: () => import('@/views/StoreList.vue'), meta: { title: '商家列表' } },
  { path: '/store/:id', component: () => import('@/views/StoreDetail.vue'), meta: { title: '店铺详情' } },
  { path: '/dish/:id', component: () => import('@/views/DishDetail.vue'), meta: { title: '菜品详情' } },
  { path: '/search', component: () => import('@/views/Search.vue'), meta: { title: '搜索' } },
  { path: '/pay-result', component: () => import('@/views/PayResult.vue'), meta: { title: '支付结果', auth: true } },
  
  // ========== 认证页面 ==========
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },
  { path: '/forgot-password', component: () => import('@/views/ForgotPassword.vue'), meta: { title: '忘记密码' } },
  { path: '/merchant-register', component: () => import('@/views/MerchantRegister.vue'), meta: { title: '商家入驻' } },
  { path: '/rider-register', component: () => import('@/views/RiderRegister.vue'), meta: { title: '骑手注册' } },
  
  // ========== 需要登录的页面 ==========
  { path: '/cart', component: () => import('@/views/Cart.vue'), meta: { title: '购物车', auth: true } },
  { path: '/order/confirm', component: () => import('@/views/OrderConfirm.vue'), meta: { title: '确认订单', auth: true } },
  { path: '/user', component: () => import('@/views/User.vue'), meta: { title: '个人中心', auth: true } },
  { path: '/user/orders', component: () => import('@/views/UserOrders.vue'), meta: { title: '我的订单', auth: true } },
  { path: '/user/orders/:id', component: () => import('@/views/UserOrders.vue'), meta: { title: '订单详情', auth: true } },
  { path: '/user/address', component: () => import('@/views/UserAddress.vue'), meta: { title: '收货地址', auth: true } },
  { path: '/notifications', component: () => import('@/views/Notifications.vue'), meta: { title: '通知中心', auth: true } },
  
  { path: '/rider/home', component: () => import('@/views/rider/RiderHome.vue'), meta: { title: '骑手中心', auth: true } },
  { path: '/rider/grab', component: () => import('@/views/rider/GrabOrders.vue'), meta: { title: '抢单大厅', auth: true } },
  { path: '/rider/delivery', component: () => import('@/views/rider/DeliveryOrders.vue'), meta: { title: '配送订单', auth: true } },
  { path: '/rider/earnings', component: () => import('@/views/rider/RiderEarnings.vue'), meta: { title: '收益明细', auth: true } },
  
  // ========== 需要商家权限的页面 ==========
  { path: '/merchant', component: () => import('@/views/merchant/Dashboard.vue'), meta: { title: '商家中心', merchant: true } },
  { path: '/merchant/categories', component: () => import('@/views/merchant/Categories.vue'), meta: { title: '分类管理', merchant: true } },
  { path: '/merchant/dishes', component: () => import('@/views/merchant/Dishes.vue'), meta: { title: '菜品管理', merchant: true } },
  { path: '/merchant/orders', component: () => import('@/views/merchant/Orders.vue'), meta: { title: '订单管理', merchant: true } },
  { path: '/merchant/reviews', component: () => import('@/views/merchant/Reviews.vue'), meta: { title: '评论管理', merchant: true } },
  { path: '/merchant/delivery', component: () => import('@/views/merchant/DeliverySettings.vue'), meta: { title: '配送设置', merchant: true } },
  { path: '/merchant/earnings', component: () => import('@/views/merchant/Earnings.vue'), meta: { title: '收益明细', merchant: true } },
  
  // ========== 需要管理员权限的页面 ==========
  { path: '/admin', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '管理后台', admin: true } },
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title || 'BZY外卖') + ' - BZY外卖'
  const user = useUserStore()
  if (to.meta.auth && !user.token) return next('/login')
  if (to.meta.merchant && user.role !== 1 && user.role !== 2) return next('/')
  if (to.meta.admin && user.role !== 2) return next('/')
  next()
})

router.afterEach(async (to, from) => {
  await nextTick()
  window.scrollTo(0, 0)
  
  // 清理认证页面的样式
  const authPages = ['/login', '/register', '/merchant-register', '/rider-register']
  const isFromAuthPage = authPages.includes(from.path)
  const isToAuthPage = authPages.includes(to.path)
  
  if (isFromAuthPage && !isToAuthPage) {
    // 从认证页面切换到普通页面，清理样式
    document.body.style.overflow = ''
  }
  
  // 延迟一点时间确保 DOM 完全渲染
  setTimeout(() => {
    const contentWrapper = document.querySelector('.content-wrapper')
    if (contentWrapper) {
      contentWrapper.scrollTop = 0
      // 确保滚动容器正确初始化
      contentWrapper.style.overflowY = 'auto'
      // 触发布局重排，确保滚动生效
      void contentWrapper.offsetHeight
    }
  }, isFromAuthPage && !isToAuthPage ? 100 : 50)
})

export default router
