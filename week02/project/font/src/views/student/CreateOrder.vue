<template>
  <!-- 创建报修单页面容器 -->
  <div class="create-order">
    <el-card>
      <!-- 卡片标题 -->
      <template #header>
        <div class="card-header">
          <span>📝 创建报修单</span>
        </div>
      </template>
      
      <!-- 报修表单：数据绑定 + 校验规则 -->
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <!-- 设备类型选择 -->
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="form.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="💧 水龙头" :value="1" />
            <el-option label="🚽 马桶" :value="2" />
            <el-option label="💡 电灯" :value="3" />
            <el-option label="🪟 窗户" :value="4" />
            <el-option label="🚪 门" :value="5" />
            <el-option label="🛏️ 床" :value="6" />
            <el-option label="🚰 水槽" :value="7" />
            <el-option label="⚡ 电表" :value="8" />
            <el-option label="💧 水表" :value="9" />
          </el-select>
        </el-form-item>
        
        <!-- 问题描述 -->
        <el-form-item label="问题描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细描述问题"
          />
        </el-form-item>
        
        <!-- 优先级选择 -->
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio :label="1">🟢 普通</el-radio>
            <el-radio :label="2">🟡 紧急</el-radio>
            <el-radio :label="3">🔴 非常紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 图片上传（可选） -->
        <el-form-item label="上传图片">
          <el-upload
            action="/api/file/upload"           
            :headers="uploadHeaders"            
            :file-list="fileList"               
            :on-success="handleUploadSuccess"   
            :on-remove="handleRemove"           
            :on-error="handleUploadError"       
            :before-upload="beforeUpload"       
            list-type="picture-card"            
            multiple                            
            :limit="5"                          
            :on-exceed="handleExceed"           
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">最多上传 5 张图片，每张不超过 10MB（可选）</div>
            </template>
          </el-upload>
        </el-form-item>
        
        <!-- 提交 + 重置按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            提交
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 报修单相关接口
import { createOrder } from '@/api/order'
// 工具：获取token、获取用户信息
import { getAccessToken, getUserInfo } from '@/utils/auth'

const router = useRouter()
// 表单引用（用于校验、重置）
const formRef = ref(null)
// 提交加载状态
const loading = ref(false)
// 上传图片列表
const fileList = ref([])
// 获取当前登录学生信息
const userInfo = getUserInfo()

// 报修单表单数据
const form = reactive({
  deviceType: null,       // 设备类型
  description: '',        // 问题描述
  priority: 1,            // 优先级，默认普通
  images: [],             // 上传的图片地址数组
  dormBuilding: userInfo.dormBuilding, // 楼栋（从用户信息自动获取）
  dormRoom: userInfo.dormRoom          // 房间号（从用户信息自动获取）
})

// 表单校验规则（图片可选，不加入rules）
const rules = {
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

// 上传请求头：自动携带token
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${getAccessToken()}`
}))

// 上传前校验：大小 + 格式
const beforeUpload = (file) => {
  // 大小不超过10MB
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  // 只允许图片格式
  const imageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!imageTypes.includes(file.type)) {
    ElMessage.error('只能上传 JPG/PNG/GIF/WebP 格式的图片')
    return false
  }
  return true
}

// 图片上传成功
const handleUploadSuccess = (response, file, uploadFileList) => {
  if (response.code === 200) {
    // 把后端返回的图片url赋值给file.url，用于预览
    file.url = response.data
    fileList.value = uploadFileList
    ElMessage.success('图片上传成功')
  } else {
    // 上传失败，移除当前文件
    fileList.value = uploadFileList.filter(f => f !== file)
  }
}

// 移除已上传图片
const handleRemove = (file, uploadFileList) => {
  fileList.value = uploadFileList
  ElMessage.info('已移除图片')
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败，网络异常')
}

// 超出上传数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传 5 张图片')
}

// ==================== 核心：提交报修单 ====================
const handleSubmit = async () => {
  // 1. 表单校验
  const valid = await formRef.value.validate()
  if (!valid) return

  // 2. 提取所有上传成功的图片url
  form.images = fileList.value.map(item => item.url).filter(url => url)

  loading.value = true
  try {
    // 3. 调用创建报修单接口
    const res = await createOrder(form)
    if (res.code === 200) {
      ElMessage.success('报修单提交成功')
      // 提交成功，跳转到我的报修单列表
      router.push('/student/my-orders')
    } else {
    }
  } catch (error) {
    console.error('提交异常：', error)
  } finally {
    loading.value = false
  }
}

// 重置表单：清空输入 + 清空图片
const handleReset = () => {
  formRef.value.resetFields()
  fileList.value = []
  form.images = []
}
</script>

<style scoped>
.create-order {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

/* 统一上传图片卡片大小 */
:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}
:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>