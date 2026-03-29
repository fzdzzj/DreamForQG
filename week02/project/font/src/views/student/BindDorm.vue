<template>
  <!-- 绑定宿舍页面容器：居中布局 -->
  <div class="bind-dorm">
    <el-card class="bind-card">
      <!-- 卡片标题 -->
      <template #header>
        <div class="card-header">
          <span>🏠 绑定宿舍</span>
        </div>
      </template>
      
      <!-- 警告提示：首次登录必须绑定 -->
      <el-alert
        title="首次登录需要绑定宿舍"
        description="绑定宿舍后您才能使用报修功能，请准确填写您的宿舍信息"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 20px"
      />
      
      <!-- 绑定宿舍表单：数据绑定 + 校验规则 -->
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <!-- 楼栋选择下拉框 -->
        <el-form-item label="宿舍楼栋" prop="dormBuilding">
          <el-select 
            v-model="form.dormBuilding" 
            placeholder="请选择宿舍楼栋"
            style="width: 100%"
          >
            <el-option label="A 栋" value="A 栋" />
            <el-option label="B 栋" value="B 栋" />
            <el-option label="C 栋" value="C 栋" />
            <el-option label="D 栋" value="D 栋" />
          </el-select>
        </el-form-item>
        
        <!-- 房间号输入框 -->
        <el-form-item label="房间号" prop="dormRoom">
          <el-input 
            v-model="form.dormRoom" 
            placeholder="请输入房间号（如：101）"
            maxlength="5"
            show-word-limit
          />
        </el-form-item>
        
        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            提交绑定
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// 接口：绑定宿舍
import { bindDorm } from '@/api/student'
// 工具：获取/更新本地用户信息
import { getUserInfo, updateUserInfo } from '@/utils/auth'

const router = useRouter()
// 表单引用（用于校验）
const formRef = ref(null)
// 加载状态
const loading = ref(false)

// 获取当前登录的用户信息
const userInfo = getUserInfo()

// 绑定宿舍表单数据
const form = reactive({
  dormBuilding: '', // 楼栋
  dormRoom: ''      // 房间号
})

// 表单校验规则
const rules = {
  // 楼栋：必选
  dormBuilding: [
    { required: true, message: '请选择宿舍楼栋', trigger: 'change' }
  ],
  // 房间号：必填 + 格式验证（3-4位数字）
  dormRoom: [
    { required: true, message: '请输入房间号', trigger: 'blur' },
    { pattern: /^\d{3,4}$/, message: '房间号格式错误（如：101）', trigger: 'blur' }
  ]
}

// ==================== 核心：提交绑定宿舍 ====================
const handleSubmit = async () => {
  // 1. 先做表单校验
  await formRef.value.validate()
  // 2. 开启加载状态
  loading.value = true
  
  try {
    // 3. 调用后端接口，提交楼栋+房间号
    await bindDorm(form)
    
    // 4. 提示绑定成功
    ElMessage.success('宿舍绑定成功')
    
    // 5. 更新本地存储的用户信息
    updateUserInfo({
      ...userInfo,
      dormBuilding: form.dormBuilding,
      dormRoom: form.dormRoom,
      dormBound: true // 标记已绑定宿舍
    })

    // 6. 强制更新内存中的用户信息（关键）
    userInfo.dormBound = true

    // 7. 延迟跳转，确保 localStorage 写入完成
    setTimeout(()=>{
      router.push('/student/dashboard')
    },100)
    
  } catch (error) {
    console.error('绑定失败:', error)
  } finally {
    // 无论成功失败，关闭加载
    loading.value = false
  }
}
</script>

<style scoped>
/* 页面容器：垂直水平居中 */
.bind-dorm {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

/* 卡片宽度 */
.bind-card {
  width: 500px;
  max-width: 90%;
}

/* 标题居中 */
.card-header {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
}
</style>