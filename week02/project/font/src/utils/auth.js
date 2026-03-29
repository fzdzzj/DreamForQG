/**
 * 本地存储工具类
 * 用于 Token、用户信息、登录状态、宿舍绑定状态的存储与获取
 */

// 存储 Access Token（接口鉴权短令牌）
export function setAccessToken(token) {
  localStorage.setItem('accessToken', token)
}

// 获取 Access Token
export function getAccessToken() {
  return localStorage.getItem('accessToken')
}

// 存储 Refresh Token（刷新令牌，用于续期）
export function setRefreshToken(token) {
  localStorage.setItem('refreshToken', token)
}

// 获取 Refresh Token
export function getRefreshToken() {
  return localStorage.getItem('refreshToken')
}

// ============ 兼容旧版本代码（避免报错）============
// 兼容旧版获取 Token 方法
export function getToken() {
  return getAccessToken()
}

// 兼容旧版存储 Token 方法
export function setToken(token) {
  setAccessToken(token)
}

// 兼容旧版删除 Token 方法
export function removeToken() {
  removeTokens()
}

// 删除所有 Token 及相关登录信息
export function removeTokens() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('tokenExpire')
}

// 存储用户信息（对象自动转为 JSON 字符串）
export function setUserInfo(userInfo) {
  localStorage.setItem('userInfo', JSON.stringify(userInfo))
}

// 获取用户信息（JSON 字符串转为对象）
export function getUserInfo() {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : null
}

// 单独清空用户信息
export function clearUserInfo() {
  localStorage.removeItem('userInfo')
}

// 判断用户是否已登录（依据是否存在 Access Token）
export function isLoggedIn() {
  return !!getAccessToken()
}

// 获取用户角色（学生/管理员）
export function getUserRole() {
  const userInfo = getUserInfo()
  return userInfo ? userInfo.role : null
}

// 检查学生是否需要绑定宿舍
// 未绑定宿舍时返回 true，需要跳转到绑定页面
export function needBindDorm() {
  const userInfo = getUserInfo()
  return !userInfo || !userInfo.dormBound
}

// 更新用户信息（绑定宿舍后更新本地存储）
export function updateUserInfo(userInfo) {
  setUserInfo(userInfo)
}

// 退出登录：清空所有登录态信息
export function logout() {
  removeTokens()
}