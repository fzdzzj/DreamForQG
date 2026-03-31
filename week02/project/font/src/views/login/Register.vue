<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="register-title">🏠 注册账号</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="account">
          <el-input 
            v-model="form.account" 
            placeholder="请输入账号" 
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="pwd">
          <el-input 
            v-model="form.pwd" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPwd">
          <el-input 
            v-model="form.confirmPwd" 
            type="password" 
            placeholder="请确认密码" 
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            class="register-btn" 
            @click="handleRegister" 
            :loading="loading"
            size="large"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-link">
        <span>已有账号？</span>
        <el-link @click="handleLogin">立即登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  account: '',
  pwd: '',
  confirmPwd: ''
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { pattern: /^(3125|3225)\d{6}$/, message: '账号格式错误,必须以3125或3225开头,后6位为数字', trigger: 'blur' }
  ],
  pwd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{6,10}$/, message: '密码长度必须在 6-10 位英文数字', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.pwd) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  
  try {
    await register({
      account: form.account,
      pwd: form.pwd,
      role: 1 // 固定为学生角色
    })
    
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

const handleLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 420px;
  padding: 20px;
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #409EFF;
  font-size: 24px;
}

.register-btn {
  width: 100%;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.login-link .el-link {
  margin-left: 5px;
  color: #409EFF;
}
</style>