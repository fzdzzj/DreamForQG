import request from '@/utils/request'

/**
 * @description 管理员 - 报修单多条件分页查询
 * @param {OrderQueryDTO} data - 多条件查询参数
 * @param {number} [data.id] - 报修单ID（可选）
 * @param {string} [data.status] - 报修状态（可选）1-待处理 2-已完成 3-已取消
 * @param {string} [data.dormBuilding] - 宿舍楼栋（可选）例：A栋
 * @param {string} [data.dormRoom] - 宿舍房间号（可选）例：101
 * @param {string} [data.startTime] - 开始时间（可选）格式：yyyy-MM-dd HH:mm:ss
 * @param {string} [data.endTime] - 结束时间（可选）格式：yyyy-MM-dd HH:mm:ss
 * @param {string} [data.priority] - 优先级（可选）1-普通 2-紧急 3-非常紧急
 * @param {string} [data.deviceType] - 设备类型（可选）1-水龙头 2-马桶 3-电灯 4-床 5-门 6-水槽 7-电表 8-水表
 * @param {number} [data.pageNum=1] - 页码（可选，默认1）
 * @param {number} [data.pageSize=10] - 每页条数（可选，默认10）
 * @returns {Promise} 返回符合条件的报修单分页列表
 */
export function queryOrders(data) {
  return request.post('/admin/orders/query', data)
}

/**
 * @description 学生 - 创建报修单
 * @param {Object} data - 创建报修单参数
 * @param {string} data.dormBuilding - 宿舍楼栋（必传）例：A栋
 * @param {string} data.dormRoom - 宿舍房间号（必传）例：101
 * @param {string} data.deviceType - 设备类型（必传）1-水龙头 2-马桶 3-电灯 4-床 5-门 6-水槽 7-电表 8-水表
 * @param {string} data.description - 问题描述（必传）详细说明故障情况
 * @param {string} data.priority - 优先级（必传）1-普通 2-紧急 3-非常紧急
 * @returns {Promise} 返回创建成功的报修单信息
 */
export function createOrder(data) {
  return request.post('/student/order', data)
}

/**
 * @description 获取当前学生的报修单列表
 * @returns {Promise} 我的报修单列表
 */
export function getMyOrders() {
  return request.get('/student/orders')
}

/**
 * @description 获取报修单详情（学生端）
 * @param {number} id - 报修单ID
 * @returns {Promise} 报修单详情
 */
export function getOrderDetail(id) {
  return request.get(`/student/order/${id}`)
}

/**
 * @description 学生 - 取消报修单
 * @param {number} id - 报修单ID
 * @returns {Promise} 取消结果
 */
export function cancelOrder(id) {
  return request.put(`/student/order/${id}/cancel`)
}

/**
 * @description 管理员分页获取所有报修单
 * @param {Object} params - 分页/查询参数
 * @returns {Promise} 所有报修单列表
 */
export function getAllOrders() {
  return request.get('/admin/orders')
}

/**
 * @description 管理员更新报修单状态
 * @param {number|string} id - 报修单ID
 * @param {Object} data - 状态数据
 * @returns {Promise} 更新结果
 */
export function updateOrderStatus(id, data) {
  return request.put(`/admin/order/${id}/status`, data)
}

/**
 * @description 管理员删除报修单
 * @param {number} id - 报修单ID
 * @returns {Promise} 删除结果
 */
export function deleteOrder(id) {
  return request.delete(`/admin/order/${id}`)
}

/**
 * @description 管理员获取报修单详情
 * @param {number} id - 报修单ID
 * @returns {Promise} 报修单详情
 */
export function getOrderDetailAdmin(id) {
  return request.get(`/admin/order/${id}`)
}

/**
 * @description 学生修改报修单信息
 * @param {Object} data - 报修单信息
 * @returns {Promise} 修改结果
 */
/**
 * @description 学生 - 修改报修单
 * @param {UpdateOrderDTO} data - 修改报修单参数
 * @param {number} data.id - 报修单ID（必传）
 * @param {string} data.dormBuilding - 宿舍楼栋（必传）示例：A栋
 * @param {string} data.dormRoom - 宿舍房间号（必传）示例：101
 * @param {string} data.deviceType - 设备类型（必传）1-水龙头 2-马桶 3-电灯 4-床 5-门 6-水槽 7-电表 8-水表
 * @param {string} data.description - 问题描述（必传）故障详细说明
 * @param {string} data.priority - 报修优先级（必传）1-普通 2-紧急 3-非常紧急
 * @param {Array<string>} [data.images] - 图片URL列表（可选）
 * @returns {Promise} 返回修改结果
 */
export function updateOrder(data) {
  return request.put('/student/order', data)
}