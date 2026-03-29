<!-- src/views/student/Profile.vue -->
<template>
  <!-- 个人中心页面容器 -->
  <div class="profile">
    <el-card>
      <!-- 卡片标题 -->
      <template #header>
        <div class="card-header">
          <span>👤 个人中心</span>
        </div>
      </template>
      
      <!-- 基本信息展示 -->
      <el-descriptions title="基本信息" :column="1" border>
        <el-descriptions-item label="账号">{{ userInfo.account }}</el-descriptions-item>
        <el-descriptions-item label="宿舍">
          <!-- 已绑定显示宿舍信息 -->
          <span v-if="userInfo.dormBound">
            {{ userInfo.dormBuilding }} {{ userInfo.dormRoom }}
          </span>
          <!-- 未绑定显示标签 -->
          <el-tag v-else type="warning">未绑定</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 宿舍信息模块 -->
      <div class="section">
        <div class="section-header">
          <span>🏠 宿舍信息</span>
          <el-button type="primary" size="small" @click="showEditDorm = true">
            修改宿舍
          </el-button>
        </div>
      </div>
      
      <!-- 账户安全模块 -->
      <div class="section">
        <div class="section-header">
          <span>🔐 账户安全</span>
          <el-button type="primary" size="small" @click="showEditPassword = true">
            修改密码
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- ==================== 修改宿舍弹窗 ==================== -->
    <el-dialog v-model="showEditDorm" title="修改宿舍" width="500px">
      <el-form :model="dormForm" :rules="dormRules" ref="dormFormRef" label-width="100px">
        <el-form-item label="宿舍楼栋" prop="dormBuilding">
          <el-select v-model="dormForm.dormBuilding" placeholder="请选择宿舍楼栋" style="width: 100%">
            <el-option label="A 栋" value="A 栋" />
            <el-option label="B 栋" value="B 栋" />
            <el-option label="C 栋" value="C 栋" />
            <el-option label="D 栋" value="D 栋" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="房间号" prop="dormRoom">
          <el-input v-model="dormForm.dormRoom" placeholder="请输入房间号" maxlength="5" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDorm = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateDorm" :loading="loading">
          确认修改
        </el-button>
      </template>
    </el-dialog>
    
    <!-- ==================== 修改密码弹窗 ==================== -->
    <el-dialog v-model="showEditPassword" title="修改密码" width="500px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="旧密码" prop="oldPwd">
          <el-input v-model="passwordForm.oldPwd" type="password" placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPwd">
          <el-input v-model="passwordForm.newPwd" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditPassword = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePassword" :loading="loading">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// 工具：获取/更新用户信息、清除token
import { getUserInfo, updateUserInfo, removeTokens } from '@/utils/auth'
// 接口：宿舍相关
import { updateDorm, getDormInfo } from '@/api/student'
// 接口：修改密码
import { updatePassword} from '@/api/auth'

const router = useRouter()

// 表单引用
const dormFormRef = ref(null)
const passwordFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 弹窗控制
const showEditDorm = ref(false)
const showEditPassword = ref(false)

// 用户信息
const userInfo = ref(getUserInfo())

// ==================== 修改宿舍表单 ====================
const dormForm = reactive({
  dormBuilding: '',
  dormRoom: ''
})

// 宿舍表单校验规则
const dormRules = {
  dormBuilding: [
    { required: true, message: '请选择宿舍楼栋', trigger: 'change' }
  ],
  dormRoom: [
    { required: true, message: '请输入房间号', trigger: 'blur' },
    { pattern: /^\d{3,4}$/, message: '房间号格式错误（如：101）', trigger: 'blur' }
  ]
}

// ==================== 修改密码表单 ====================
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码表单校验规则
const passwordRules = {
  oldPwd: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{6,10}$/, message: '旧密码长度6-10位，只能包含字母、数字', trigger: 'blur' }
  ],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{6,10}$/, message: '密码长度6-10位，只能包含字母、数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPwd) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// ==================== 加载宿舍信息 ====================
const loadDormInfo = async () => {
  try {
    const res = await getDormInfo()
    if (res.data.isBound === 'true') {
      dormForm.dormBuilding = res.data.dormBuilding
      dormForm.dormRoom = res.data.dormRoom
    }
  } catch (error) {
    console.error('加载宿舍信息失败:', error)
  }
}

// ==================== 提交修改宿舍 ====================
const handleUpdateDorm = async () => {
  await dormFormRef.value.validate()
  loading.value = true
  
  try {
    await updateDorm(dormForm)
    ElMessage.success('宿舍修改成功')
    showEditDorm.value = false
    
    // 更新本地用户信息
    updateUserInfo({
      ...userInfo.value,
      dormBuilding: dormForm.dormBuilding,
      dormRoom: dormForm.dormRoom,
      dormBound: true
    })
    // 重新获取最新信息
    userInfo.value = getUserInfo()

  } catch (error) {
    console.error('修改失败:', error)
  } finally {
    loading.value = false
  }
}

// ==================== 提交修改密码 ====================
const handleUpdatePassword = async () => {
  await passwordFormRef.value.validate()
  loading.value = true
  
  try {
    await updatePassword(passwordForm)
    ElMessage.success('密码修改成功，请重新登录')
    showEditPassword.value = false
    
    // 密码修改成功 → 清除token → 重新登录
    removeTokens()
    router.push('/login')
  } catch (error) {
    console.error('修改失败:', error)
  } finally {
    loading.value = false
  }
}

// 页面加载时获取宿舍信息
onMounted(() => {
  loadDormInfo()
})
</script>

<style scoped>
.profile {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.section {
  margin-top: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}
</style>