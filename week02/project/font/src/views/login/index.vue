<template>
  <!-- 登录页面外层容器：全屏居中布局 -->
  <div class="login-container">
    <!-- 登录卡片 -->
    <el-card class="login-card">
      <!-- 页面标题 -->
      <h2 class="login-title">🏠 宿舍报修系统</h2>

      <!-- 登录表单：绑定数据、校验规则、表单引用 -->
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <!-- 账号输入框 -->
        <el-form-item prop="account">
          <el-input 
            v-model="form.account" 
            placeholder="请输入账号"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <!-- 密码输入框：支持显示密码、回车登录 -->
        <el-form-item prop="pwd">
          <el-input 
            v-model="form.pwd" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 登录按钮：加载状态、点击提交 -->
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-btn" 
            @click="handleLogin" 
            :loading="loading"
            size="large"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 测试账号提示 -->
      <div class="tips">
        <p>学生账号：3125004123 密码：123456</p>
        <p>管理员账号：0025004128 密码：123456</p>
      </div>
      
      <!-- 注册链接 -->
      <div class="register-link">
        <span>还没有账号？</span>
        <el-link @click="handleRegister">立即注册</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 引入 Vue 3 响应式 API
import { ref, reactive } from 'vue'
// 引入路由：页面跳转
import { useRouter } from 'vue-router'
// 引入 Element Plus 消息提示
import { ElMessage } from 'element-plus'
// 引入登录接口
import { login } from '@/api/auth'
// 引入本地存储工具：保存 token、用户信息、清除 token
import { setAccessToken, setRefreshToken, setUserInfo ,removeTokens} from '@/utils/auth'

// 路由实例
const router = useRouter()
// 表单引用：用于表单校验
const formRef = ref(null)
// 登录按钮加载状态
const loading = ref(false)

// 登录表单数据（响应式）
const form = reactive({
  account: '',  // 账号
  pwd: ''       // 密码
})

// 表单校验规则
const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' },{
    pattern: /^(3125|3225|0025)\d{6}$/, message: '账号格式错误,必须以3125开头,后6位为数字', trigger: 'blur'
  }],
  pwd: [{ required: true, message: '请输入密码', trigger: 'blur' },{
    pattern: /^[a-zA-Z0-9]{6,10}$/, message: '密码格式错误,长度6-10位，只能包含字母、数字', trigger: 'blur'
  }]
}

// ==================== 核心：登录逻辑 ====================
const handleLogin = async () => {
  // 1. 先执行表单校验
  await formRef.value.validate()
  // 2. 开启加载状态
  loading.value = true
  
  try {
    // 登录前先清除旧的 token，避免冲突
    removeTokens()
    
    // 3. 调用登录接口，提交账号密码
    const res = await login(form)
    
    // 4. 保存后端返回的令牌（用于接口鉴权）
    setAccessToken(res.data.accessToken)
    setRefreshToken(res.data.refreshToken)
    
    // 5. 保存用户信息到本地
    setUserInfo(res.data.user)
    
    // 6. 提示登录成功
    ElMessage.success('登录成功')

    // ==================== 角色判断 + 页面跳转 ====================
    // role=1 是学生
    if(res.data.user.role=='1'){
      // 判断学生是否需要绑定宿舍
      if(res.data.needBindDorm){
        router.push('/student/bind-dorm')
      }else{
        router.push('/student/dashboard')
      }
    } else {
      // 角色不是1 → 管理员，直接跳后台
      router.push('/admin/dashboard')
    }
    
  } catch (error) {
    // 登录失败：打印错误 + 提示用户
    console.error('登录失败:', error)
    ElMessage.error('登录失败：' + (error.message || '账号或密码错误'))
  } finally {
    // 无论成功失败，都关闭加载状态
    loading.value = false
  }
}

// 跳转到注册页面
const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
/* 页面容器：渐变背景 + 垂直水平居中 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 登录卡片宽度 */
.login-card {
  width: 420px;
  padding: 20px;
}

/* 标题样式 */
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #409EFF;
  font-size: 24px;
}

/* 登录按钮全屏宽 */
.login-btn {
  width: 100%;
}

/* 测试账号提示框 */
.tips {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
}

.tips p {
  margin: 5px 0;
}

/* 注册链接样式 */
.register-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.register-link .el-link {
  margin-left: 5px;
  color: #409EFF;
}
</style>