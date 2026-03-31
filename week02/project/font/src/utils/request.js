// 引入 axios 请求库，用于发送 HTTP 请求
import axios from 'axios'
// 引入 Element Plus 的消息提示组件，用于统一错误提示
import { ElMessage } from 'element-plus'
// 引入路由实例，用于 token 过期时跳转到登录页
import router from '@/router'
// 从本地认证工具中导入 token 相关操作方法
import {
  getAccessToken,    // 获取本地存储的 accessToken（用于接口鉴权）
  getRefreshToken,   // 获取本地存储的 refreshToken（用于刷新 token）
  setAccessToken,    // 刷新成功后，保存新的 accessToken
  removeTokens       // 登录失效时，清空所有 token 和用户信息
} from '@/utils/auth'
// 引入后端刷新 token 的接口方法
import { refreshTokens } from '@/api/auth'

// ==================== 创建 axios 实例 ====================
// 创建一个自定义的 axios 实例，统一配置基础路径和超时时间
const request = axios.create({
  baseURL: '/api',    // 所有请求自动加上 /api 前缀（对应 vite 代理）
  timeout: 15000      // 请求超时时间：15 秒，超过则自动断开
})

// ==================== 刷新令牌全局状态管理 ====================
// 标记：是否正在执行刷新 token 操作
// 作用：防止多个请求同时触发刷新，导致多次调用刷新接口
let isRefreshing = false

// 请求等待队列
// 作用：当正在刷新 token 时，其他 401 请求先存起来，不直接报错
// 等 token 刷新成功后，再把这些请求重新发送一次
let requestQueue = []

/**
 * 执行等待队列中的所有请求
 * @param {String} newToken - 刷新后得到的新 accessToken
 * 作用：遍历所有等待的请求，塞入新 token，然后重新发送
 */
function executeQueue(newToken) {
  requestQueue.forEach(cb => cb(newToken))
  // 执行完毕后清空队列
  requestQueue = []
}

// ==================== 请求拦截器 ====================
// 发送请求之前自动执行
request.interceptors.request.use(
  config => {
    // 从本地获取 accessToken
    const token = getAccessToken()
    // 如果 token 存在，则在请求头中自动携带 Authorization
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    // 返回处理后的配置，发送请求
    return config
  },
  // 请求发送失败（极少出现），直接抛出错误
  error => Promise.reject(error)
)

// ==================== 响应拦截器（核心：401 无感刷新）====================
// 接口返回结果后自动执行
request.interceptors.response.use(
  // 响应成功：直接返回后端的 data 数据，不用每次写 res.data
  response => response.data,

  // 响应失败：处理错误、401 token 过期、无感刷新
  error => {
    // 从错误对象中解构出 响应数据response、请求配置config
    const { response, config } = error

    // 情况1：网络错误（无响应、断网、请求超时）
    if (!response) {
      ElMessage.error('网络异常，请稍后重试')
      return Promise.reject(error)
    }

    // 情况2：非 401 错误（500服务器错误、403权限不足、404不存在）
    // 直接提示错误信息，不做 token 刷新
    if (response.status !== 401) {
      const msg = response.data?.message || response.data?.msg || '服务器异常'
      ElMessage.error(msg)
      return Promise.reject(error)
    }

    // =============================================================
    // 下面代码 只处理 401 错误（token 无效 / 过期）
    // =============================================================

    // 子情况A：当前请求就是【刷新 token】接口也返回 401
    // 说明 refreshToken 也过期了 → 彻底登录失效
    if (config.url.includes('/auth/refresh')) {
      removeTokens()               // 清空所有登录信息
      router.push('/login')        // 强制跳回登录页
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(error)
    }

    // 子情况B：正在刷新 token 中
    // 不重复刷新，把当前请求塞进队列等待
    if (isRefreshing) {
      // 返回一个 Promise，让当前请求“悬停等待”
      return new Promise(resolve => {
        // 把“等待执行的函数”推入队列
        // 重点：resolve 就是等刷新完成后，让这个请求继续执行
        requestQueue.push(newToken => {
          // 给当前请求换上新 token
          config.headers['Authorization'] = `Bearer ${newToken}`
          // 放行！重新发送请求
          resolve(request(config))
        })
      })
    }

    // 子情况C：开始第一次刷新 token
    // 锁定状态，防止其他请求重复进入
    isRefreshing = true

    // 获取本地的 refreshToken
    const refreshToken = getRefreshToken()

    // 如果没有 refreshToken → 登录失效
    if (!refreshToken) {
      removeTokens()
      router.push('/login')
      isRefreshing = false
      return Promise.reject(error)
    }

    // ==================== 执行刷新 token ====================
    refreshTokens({ refreshToken })
      .then(res => {
        // 刷新成功，拿到新的 accessToken
        const { accessToken } = res
        // 保存新 token 到本地
        setAccessToken(accessToken)
        // 执行队列：把之前等待的所有请求重新发送
        executeQueue(accessToken)
        // 给当前失败的请求换上新 token
        config.headers['Authorization'] = `Bearer ${accessToken}`
        // 重新发送当前请求
        return request(config)
      })
      .catch(err => {
        // 刷新失败（refreshToken 也过期）
        ElMessage.error('登录已过期，请重新登录')
        removeTokens()
        router.push('/login')
        return Promise.reject(err)
      })
      .finally(() => {
        // 无论成功失败，都解除刷新状态
        isRefreshing = false
      })

    // 抛出错误，进入上面的 then/catch 流程
    return Promise.reject(error)
  }
)

// 导出封装好的 axios 实例，全局使用
export default request