import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 导入本地存储工具类：用于获取token、角色、用户信息、宿舍绑定状态
import {
  getToken,
  getUserRole,
  getUserInfo,
  needBindDorm,
} from '@/utils/auth'

// 导入学生端、管理员端的公共布局组件（侧边栏+顶部导航+内容区域）
import StudentLayout from '@/views/student/Layout.vue'
import AdminLayout from '@/views/admin/Layout.vue'

// 路由配置数组：所有页面的路径、组件、权限、标题都在这里定义
const routes = [
  // 公共消息模块：学生 / 管理员都能访问的消息中心
  {
    path: '/message',
    name: 'Message',
    meta: { requiresAuth: true },  // 需要登录才能访问
    children: [
      {
        path: 'list',
        name: 'MessageList',
        component: () => import('@/views/common/MessageList.vue'),
        meta: { title: '消息中心' }
      }
    ]
  },

  // ==================== 登录注册模块 ====================
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      requiresAuth: false,  // 不需要登录就能访问
      title: '登录'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: {
      requiresAuth: false,
      title: '注册'
    }
  },

  // ==================== 默认重定向 ====================
  // 访问根路径 / 时，自动跳转到学生首页
  {
    path: '/',
    redirect: '/login'
  },

  // ==================== 学生端路由 ====================
  {
    path: '/student',
    name: 'Student',
    component: StudentLayout,
    meta: {
      requiresAuth: true,   // 需要登录
      role: '1',            // 角色必须是学生（1=学生）
      title: '学生端'
    },
    // 学生端子页面
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: {
          title: '首页',
          requiresBindDorm: true  // 需要绑定宿舍才能访问
        }
      },
      {
        path: 'bind-dorm',
        name: 'BindDorm',
        component: () => import('@/views/student/BindDorm.vue'),
        meta: {
          title: '绑定宿舍',
          requiresBindDorm: false  // 绑定页面不需要检查绑定状态
        }
      },
      {
        path: 'create-order',
        name: 'CreateOrder',
        component: () => import('@/views/student/CreateOrder.vue'),
        meta: {
          title: '创建报修单',
          requiresBindDorm: true,
          requiresAuth: true
        }
      },
      {
        path: 'my-orders',
        name: 'MyOrders',
        component: () => import('@/views/student/MyOrders.vue'),
        meta: {
          title: '我的报修单',
          requiresBindDorm: true,
          requiresAuth: true
        }
      },
      {
        path: 'edit-order/:id',
        name: 'EditOrder',
        component: () => import('@/views/student/EditOrder.vue'),
        meta: {
          title: '编辑报修单',
          requiresBindDorm: true,
          requiresAuth: true
        }
      },
      {
        path: 'order-detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/student/OrderDetail.vue'),
        meta: {
          title: '报修单详情',
          requiresBindDorm: true,
          requiresAuth: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/student/Profile.vue'),
        meta: {
          title: '个人中心',
          requiresBindDorm: true,
          requiresAuth: true
        }
      }
    ]
  },

  // ==================== 管理员端路由 ====================
  {
    path: '/admin',
    name: 'Admin',
    component: AdminLayout,
    meta: {
      requiresAuth: true,   // 需要登录
      role: '2',            // 角色必须是管理员（2=管理员）
      title: '管理端'
    },
    // 管理员子页面
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/admin/OrderList.vue'),
        meta: { title: '报修单管理' }
      },
      {
        path: 'orders-query',
        name: 'OrderQuery',
        component: () => import('@/views/admin/OrderQuery.vue'),
        meta: { title: '多条件查询' }
      },
      {
        path: 'order/:id',
        name: 'AdminOrderDetail',
        component: () => import('@/views/admin/OrderDetail.vue'),
        meta: { title: '报修单详情' }
      },
      {
        path: 'logs',
        name: 'LogList',
        component: () => import('@/views/admin/LogList.vue'),
        meta: { title: '操作日志' }
      }
    ]
  },

  // ==================== 404 页面 ====================
  // 匹配所有不存在的路径，统一跳转到404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '页面未找到' }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),  // 使用history模式（URL不带#）
  routes                         // 挂载上面定义的所有路由
})

// ==================== 全局路由守卫（进入页面前执行）====================
// 所有页面跳转都会先走这里，做：登录检查、权限检查、宿舍绑定检查、角色校验
router.beforeEach((to, from, next) => {
  // 从本地获取登录状态、用户角色、用户信息
  const token = getToken()
  const role = getUserRole()
  const userInfo = getUserInfo()

  // 设置浏览器标签页标题
  document.title = to.meta.title
    ? `${to.meta.title} - 宿舍报修系统`
    : '宿舍报修系统'

  // ========== 1. 登录校验：需要登录但未登录 → 跳登录 ==========
  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  // ========== 2. 学生宿舍绑定校验：只有学生角色才需要检查 ==========
  if (role === '1') {
    // 未绑定宿舍，且访问需要绑定的页面
    if (needBindDorm()) {
      // 允许访问：绑定页、登录页
      // 其他页面一律强制跳转到绑定宿舍页
      if (to.path !== '/student/bind-dorm' && to.path !== '/login') {
        console.log("未绑定宿舍且访问需要绑定的页面");

        ElMessage.warning('请先绑定宿舍')
        next('/student/bind-dorm')
        return
      }
    }

    // 已绑定宿舍，还想访问绑定页 → 直接跳首页
    if (!needBindDorm() && to.path === '/student/bind-dorm') {
      next('/student/dashboard')
      return
    }
  }

  // ========== 3. 角色权限校验：学生不能访问管理员页，管理员不能访问学生页 ==========
  if (to.meta.role && to.meta.role !== role) {
    ElMessage.error('权限不足，无法访问此页面')

    // 根据当前角色跳转到对应首页
    if (role === '1') {
      next('/student/dashboard')
    } else if (role === '2') {
      next('/admin/dashboard')
    } else {
      next('/login')
    }
    return
  }

  // ========== 4. 已登录用户访问登录页 → 自动跳首页 ==========
  if (token && to.path === '/login') {
    ElMessage.info('您已登录')

    if (role === '1') {
      next('/student/dashboard')
    } else if (role === '2') {
      next('/admin/dashboard')
    } else {
      next('/')
    }
    return
  }

  // ========== 5. 所有校验都通过 → 放行 ==========
  next()
})

// ==================== 路由后置守卫（进入页面后执行）====================
router.afterEach((to, from) => {
  // 每次页面跳转后，滚动到页面顶部
  window.scrollTo(0, 0)

  // 控制台打印跳转记录，方便调试
  console.log(`路由跳转：${from.path} → ${to.path}`)
})

// 导出路由，在 main.js 中挂载到 Vue 实例
export default router