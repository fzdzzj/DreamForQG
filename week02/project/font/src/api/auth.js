// 导入封装好的 axios 请求工具
import request from '@/utils/request'

/**
 * @description 用户登录
 * @param {Object} data - 登录参数
 * @param {string} data.account - 账号 
 * @param {string} data.pwd - 密码
 * @returns {Promise} 返回userInfo和Token
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * @description 用户注册
 * @param {Object} data - 注册参数
 * @param {string} data.account - 账号 
 * @param {string} data.pwd - 密码
 * @returns {Promise} 返回请求结果
 */
export function register(data) {
  return request.post('/auth/register', data)
}

/**
 * @description 刷新 Token
 * @param {Object} data - 刷新参数
 * @param {string} data.refreshToken - 刷新令牌
 * @returns {Promise} 返回accessToken
 */
export function refreshTokens(data) {
  return request.put('/auth/refresh', data)
}

/**
 * @description 修改用户密码
 * @param {Object} data - 密码修改参数
 * @param {string} data.oldPwd - 旧密码
 * @param {string} data.newPwd - 新密码
 * @returns {Promise} 返回请求结果
 */
export function updatePassword(data) {
  return request.put('/auth/update-password', data)
}

/**
 * @description 退出登录
 * @param {string} refreshToken - 刷新令牌
 * @returns {Promise} 返回请求结果
 */
export function logout(refreshToken) {
  return request.post('/auth/logout', { refreshToken })
}