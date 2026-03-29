// 操作日志相关接口
import request from '@/utils/request'

/**
 * @description 分页/条件查询操作日志列表
 * @param {Object} params - 查询参数
 * @param {number} [params.pageNum=1] - 页码（可选，默认1）
 * @param {number} [params.pageSize=10] - 每页条数（可选，默认10）
 * @param {string} [params.userAccount] - 用户账号（可选）
 * @param {string} [params.startTime] - 开始时间（可选)
 * @param {string} [params.endTime] - 结束时间（可选）
 * @param {string} [params.result] - 操作结果（可选）
 * @returns {Promise} 返回日志列表数据
 */
export function getOperationLogs(params) {
  return request.get('/admin/logs', { params })
}

/**
 * @description 批量删除操作日志
 * @param {number[]} logIds - 日志ID数组，如 [1,2,3]
 * @returns {Promise} 返回删除结果
 */
export function deleteLogs(logIds) {
  return request.delete('/admin/logs', { params: { logIds } })
}