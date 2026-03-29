<template>
  <!-- 学生端整体布局外壳 -->
  <div class="layout">
    
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <!-- 左侧：系统标题 + 宿舍信息 -->
        <div class="header-left">
          <h2>🏠 宿舍报修系统（学生）</h2>
          <!-- 只有绑定宿舍后才显示 -->
          <span v-if="userInfo.dormBound" class="dorm-info">
            📍 {{ userInfo.dormBuilding }} {{ userInfo.dormRoom }}
          </span>
        </div>

        <!-- 右侧：消息铃铛 + 用户名 + 退出登录 -->
        <div class="header-right">
          <!-- 消息铃铛组件 -->
          <MessageBell />
          <span class="username">{{ userInfo.account }}</span>
          <el-button type="danger" size="small" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </el-header>
      
      <!-- 下部容器：左侧菜单 + 右侧内容 -->
      <el-container>
        <!-- 左侧侧边栏菜单 -->
        <el-aside width="200px" class="aside">
          <el-menu
            :default-active="activeMenu"  
            router                       
            background-color="#304156"    
            text-color="#bfcbd9"          
            active-text-color="#409EFF"   
          >
            <el-menu-item index="/student/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/student/profile">
              <el-icon><UserFilled /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <el-menu-item index="/student/create-order">
              <el-icon><Edit /></el-icon>
              <span>创建报修单</span>
            </el-menu-item>
            <el-menu-item index="/student/my-orders">
              <el-icon><Document /></el-icon>
              <span>我的报修单</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 右侧主内容区域：页面内容在这里渲染 -->
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled, Edit, Document, UserFilled } from '@element-plus/icons-vue'

// 工具类：获取用户信息、清除token
import { getUserInfo, removeTokens } from '@/utils/auth'
// 退出登录接口
import { logout } from '@/api/auth'
// 获取刷新token
import { getRefreshToken } from '../../utils/auth'
// 消息铃铛组件
import MessageBell from '@/components/MessageBell.vue'

const route = useRoute()
const router = useRouter()

// 计算属性：获取当前登录用户信息
const userInfo = computed(() => getUserInfo())

// 当前路由 = 菜单高亮项
const activeMenu = computed(() => route.path)

// ==================== 退出登录逻辑 ====================
const handleLogout = async () => {
  try {
    // 弹出确认框
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端退出接口
    await logout(getRefreshToken())
    // 清除本地 token
    removeTokens()
    ElMessage.success('退出成功')
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    // 用户点取消则不处理
    if (error !== 'cancel') {
      console.error('退出失败:', error)
    }
  }
}
</script>

<style scoped>
/* 布局占满全屏 */
.layout {
  height: 100vh;
}

/* 顶部导航样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #409EFF;
  color: white;
  padding: 0 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
}

/* 右侧用户区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-size: 14px;
}

/* 左侧菜单背景 */
.aside {
  background: #304156;
}

/* 主内容区背景 */
.main {
  background: #f0f2f5;
  padding: 20px;
}

/* 宿舍信息样式 */
.dorm-info {
  margin-left: 15px;
  font-size: 13px;
  opacity: 0.9;
}
</style>