// 学生宿舍相关接口
import request from '@/utils/request'

/**
 * @description 学生首次绑定宿舍
 * @param {Object} data - 绑定宿舍参数
 * @param {string} data.dormBuilding - 宿舍楼栋（必传）例：A栋
 * @param {string} data.dormRoom - 宿舍房间号（必传）例：101
 * @returns {Promise} 返回绑定结果
 */
export function bindDorm(data) {
  return request.post('/student/dorm', data)
}

/**
 * @description 学生修改已绑定的宿舍信息
 * @param {Object} data - 修改宿舍参数
 * @param {string} data.dormBuilding - 新宿舍楼栋（必传）例：B栋
 * @param {string} data.dormRoom - 新宿舍房间号（必传）例：202
 * @returns {Promise} 返回修改结果
 */
export function updateDorm(data) {
  return request.put('/student/dorm', data)
}

/**
 * @description 获取当前学生已绑定的宿舍信息
 * @returns {Promise} 返回宿舍详情（楼栋、房间号、绑定时间等）
 */
export function getDormInfo() {
  return request.get('/student/dorm/info')
}

/**
 * @description 检查学生宿舍绑定状态
 * @returns {Promise} 返回绑定状态（已绑定/未绑定）
 */
export function getDormStatus() {
  return request.get('/student/dorm/status')
}