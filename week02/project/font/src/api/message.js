// 消息通知相关接口
import request from '@/utils/request'

/**
 * @description 获取当前用户未读消息总数
 * @returns {Promise} 返回未读消息数量
 */
export function getUnreadCount() {
  return request.get('/message/unread-count')
}

/**
 * @description 获取消息统计信息（总数量、已读、未读、今天等）
 * @returns {Promise} 返回消息统计数据
 */
export function getMessageStats() {
  return request.get('/message/stats')
}

/**
 * @description 分页/条件查询消息列表
 * @param {Object} params - 查询参数
 * @param {number} [params.pageNum=1] - 页码（可选）
 * @param {number} [params.pageSize=10] - 每页条数（可选）
 * @param {string} [params.type] - 消息类型（可选）
 * @param {string} [params.isRead] - 是否已读（可选）
 * @returns {Promise} 返回消息列表数据
 */
export function getMessages(params) {
  return request.get('/message/list', { params })
}

/**
 * @description 将单条消息标记为已读
 * @param {number} id - 消息ID
 * @returns {Promise} 返回操作结果
 */
export function markAsRead(id) {
  return request.put(`/message/read/${id}`)
}

/**
 * @description 将当前用户所有消息标记为已读
 * @returns {Promise} 返回操作结果
 */
export function markAllAsRead() {
  return request.put('/message/read-all')
}

/**
 * @description 删除单条消息
 * @param {number} id - 消息ID
 * @returns {Promise} 返回删除结果
 */
export function deleteMessage(id) {
  return request.delete(`/message/${id}`)
}

/**
 * @description 批量删除消息
 * @param {number[]} messageIds - 消息ID数组，如 [1,2,3]
 * @returns {Promise} 返回删除结果
 */
export function deleteMessages(messageIds) {
  return request.delete('/message/batch', {
    data: { messageIds }
  })
}

/**
 * @description 批量将消息标记为已读
 * @param {number[]} messageIds - 消息ID数组，如 [1,2,3]
 * @returns {Promise} 返回操作结果
 */
export function markBatchAsRead(messageIds) {
  return request.put('/message/batch/read', {
    messageIds
  })
}