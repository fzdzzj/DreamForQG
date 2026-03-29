<template>
  <!-- 修改报修单页面容器 -->
  <div class="edit-order">
    <el-card>
      <!-- 卡片标题 + 返回按钮 -->
      <template #header>
        <div class="card-header">
          <span>📝 修改报修单</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>

      <!-- 修改表单：结构和创建页基本一致 -->
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="100px"
      >
        <!-- 设备类型选择 -->
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="form.deviceType" style="width:100%">
            <el-option label="💧水龙头" :value="1" />
            <el-option label="🚽马桶" :value="2" />
            <el-option label="💡电灯" :value="3" />
            <el-option label="🪟窗户" :value="4" />
            <el-option label="🚪门" :value="5" />
            <el-option label="🛏️床" :value="6" />
            <el-option label="🚰水槽" :value="7" />
            <el-option label="⚡电表" :value="8" />
            <el-option label="💧水表" :value="9" />
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
            <el-radio :label="1">🟢普通</el-radio>
            <el-radio :label="2">🟡紧急</el-radio>
            <el-radio :label="3">🔴非常紧急</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 图片上传（支持回显、新增、删除） -->
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
              <div class="el-upload__tip">最多 5 张，每张≤10MB（可选）</div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 保存 + 取消按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            保存修改
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 报修单接口：详情、更新
import { getOrderDetail, updateOrder } from '@/api/order'
// 工具：token、用户信息
import { getAccessToken, getUserInfo } from '@/utils/auth'

// 获取当前登录学生信息
const userInfo = getUserInfo()
// 路由：跳转、返回
const router = useRouter()
// 路由：获取当前页面的报修单ID
const route = useRoute()

// 表单引用（校验、重置）
const formRef = ref(null)
// 提交加载状态
const loading = ref(false)
// 上传图片列表（用于回显）
const fileList = ref([])

// 表单数据
const form = reactive({
  id: '',                  // 报修单ID（必须）
  deviceType: null,         // 设备类型
  description: '',          // 问题描述
  priority: 1,              // 优先级
  images: [],               // 图片地址数组
  dormRoom: userInfo.dormRoom,      // 房间号
  dormBuilding: userInfo.dormBuilding// 楼栋
})

// 表单校验规则
const rules = {
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

// 上传图片请求头：自动携带token
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getAccessToken()
}))

// 页面加载时：加载报修单详情并回显
onMounted(() => {
  loadOrderData()
})

// ==================== 核心：加载报修单详情（回显）====================
async function loadOrderData() {
  try {
    // 根据路由上的ID获取详情
    const res = await getOrderDetail(route.params.id)
    const data = res.data
    
    // 给表单赋值
    form.id = data.id
    form.deviceType = Number(data.deviceType)
    form.description = data.description
    form.priority = Number(data.priority)
    
    // 图片回显：必须转为 [{ url: xxx }] 格式才能预览
    const imageList = data.images || []
    form.images = imageList
    fileList.value = imageList.map(url => ({ url }))
    
  } catch (err) {
    ElMessage.error('加载失败')
    console.error(err)
  }
}

// ==================== 图片上传相关逻辑（和创建页一致）====================
// 上传成功
const handleUploadSuccess = (response, file, uploadFileList) => {
  if (response.code === 200) {
    file.url = response.data
    fileList.value = uploadFileList
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败：' + (response.msg || '未知错误'))
    fileList.value = uploadFileList.filter(f => f !== file)
  }
}

// 删除图片
const handleRemove = (file, uploadFileList) => {
  fileList.value = uploadFileList
  ElMessage.info('已移除图片')
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败，网络异常')
}

// 超过5张
const handleExceed = () => {
  ElMessage.warning('最多只能上传 5 张图片')
}

// 上传前校验：大小 + 格式
const beforeUpload = (file) => {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片不能超过 10MB')
    return false
  }
  const types = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!types.includes(file.type)) {
    ElMessage.error('仅支持 JPG/PNG/GIF/WebP 格式')
    return false
  }
  return true
}

// ==================== 核心：提交修改 ====================
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 表单校验
    const valid = await formRef.value.validate()
    if (!valid) return

    // 提取所有图片URL
    form.images = fileList.value
      .map(f => f.url)
      .filter(Boolean)

    loading.value = true
    // 调用修改接口
    const res = await updateOrder(form)

    if (res.code === 200) {
      ElMessage.success('修改成功')
      // 跳回我的报修单
      router.push('/student/my-orders')
    } else {
      ElMessage.error(res.msg || '修改失败')
    }

  } catch (err) {
    console.error('修改异常', err)
    ElMessage.error('修改失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.edit-order {
  max-width: 800px;
  margin: 0 auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 统一上传图片大小 */
:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}
:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>