<template>
  <!-- 管理员端布局根容器：占满整个屏幕 -->
  <div class="layout">
    <!-- Element Plus 布局容器 -->
    <el-container>
      <!-- 顶部 Header 区域：标题、用户名、消息铃铛、退出按钮 -->
      <el-header class="header">
        <!-- 左侧：系统标题 -->
        <div class="header-left">
          <h2>🏠 宿舍报修系统（管理员）</h2>
        </div>
        <!-- 右侧：消息铃铛 + 用户名 + 退出按钮 -->
        <div class="header-right">
          <!-- 消息铃铛组件 -->
          <MessageBell />
          <!-- 显示当前登录账号 -->
          <span class="username">{{ userInfo.account }}</span>
          <!-- 退出登录按钮 -->
          <el-button type="danger" size="small" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </el-header>
      
      <!-- 下部容器：左侧菜单 + 右侧内容 -->
      <el-container>
        <!-- 左侧侧边栏：宽度固定 200px -->
        <el-aside width="200px" class="aside">
          <!-- 左侧菜单：路由模式，高亮当前页面 -->
          <el-menu
            :default-active="activeMenu"
            router
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/admin/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/admin/orders">
              <el-icon><Document /></el-icon>
              <span>报修单管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/orders-query">
              <el-icon><Search /></el-icon>
              <span>多条件查询</span>
            </el-menu-item>
            <el-menu-item index="/admin/logs">
              <el-icon><Notebook /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 右侧主内容区域：显示当前路由页面 -->
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
// 导入 Vue 计算属性
import { computed } from 'vue'
// 导入路由：获取当前路径、进行页面跳转
import { useRoute, useRouter } from 'vue-router'
// 导入消息提示、确认弹窗
import { ElMessage, ElMessageBox } from 'element-plus'
// 导入菜单图标
import { HomeFilled, Document, Notebook } from '@element-plus/icons-vue'
// 导入工具方法：获取用户信息、清除本地登录数据
import { getUserInfo, removeTokens } from '@/utils/auth'
// 导入退出登录接口
import { logout } from '@/api/auth'
// 导入消息铃铛组件
import MessageBell from '@/components/MessageBell.vue'
// 导入获取刷新令牌方法
import { getRefreshToken } from '@/utils/auth'

// 获取当前路由信息
const route = useRoute()
// 获取路由实例，用于跳转
const router = useRouter()

// 计算属性：获取用户信息（响应式）
const userInfo = computed(() => getUserInfo())

// 计算属性：当前路由路径，用于菜单高亮
const activeMenu = computed(() => route.path)

/**
 * 退出登录逻辑
 * 1. 弹出确认框
 * 2. 调用后端退出接口
 * 3. 清除本地 token
 * 4. 跳转到登录页
 */
const handleLogout = async () => {
  try {
    // 弹出确认退出对话框
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端退出接口，传入 refreshToken
    await logout(getRefreshToken())
    // 清除本地所有登录信息（token、用户信息...）
    removeTokens()
    // 提示成功
    ElMessage.success('退出成功')
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    // 用户点击取消时，不报错
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

/* 顶部导航栏样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #67C23A;
  color: white;
  padding: 0 20px;
}

/* 左侧标题 */
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

/* 用户名样式 */
.username {
  font-size: 14px;
}

/* 左侧菜单背景 */
.aside {
  background: #304156;
}

/* 内容区域背景与内边距 */
.main {
  background: #f0f2f5;
  padding: 20px;
}
</style>