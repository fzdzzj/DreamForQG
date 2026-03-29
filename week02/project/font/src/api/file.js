import request from '@/utils/request'

/**
 * @description 上传单个文件
 * @param {File} file - 要上传的文件对象（input 选择的文件）
 * @returns {Promise} 返回上传结果，包含文件地址
 */
export function uploadFile(file) {
  // 构建 FormData 格式数据
  const formData = new FormData()
  formData.append('file', file)

  // 发送文件上传请求，设置 multipart/form-data 请求头
  return request.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}