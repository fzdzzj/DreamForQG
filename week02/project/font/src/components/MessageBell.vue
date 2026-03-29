<!-- src/components/MessageBell.vue -->
<template>
  <!-- 消息角标组件：用于显示未读消息数量，数值为0时自动隐藏徽章 -->
  <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-bell">
    <!-- 文本按钮：点击后跳转到消息列表页面 -->
    <el-button text @click="handleViewMessages">
      <!-- 铃铛图标，设置图标大小为20px -->
      <el-icon size="20"><Bell /></el-icon>
    </el-button>
  </el-badge>
</template>

<script setup>
// 导入Vue组合式API
import { ref, onMounted, onUnmounted } from 'vue'
// 导入路由跳转方法
import { useRouter } from 'vue-router'
// 导入Element Plus铃铛图标
import { Bell } from '@element-plus/icons-vue'
// 导入获取未读消息数的接口
import { getUnreadCount } from '@/api/message'

// 创建路由实例，用于页面跳转
const router = useRouter()

// 定义响应式变量：存储未读消息的总数量，默认值为0
const unreadCount = ref(0)

// 定义定时器变量：用于存储轮询定时器的ID，初始值为null
let timer = null

/**
 * 异步加载未读消息数量
 * 调用接口获取最新未读消息数，并更新到页面显示
 */
const loadUnreadCount = async () => {
  try {
    // 调用接口请求未读消息数量
    const res = await getUnreadCount()
    // 将接口返回的数据赋值给未读消息数量变量
    unreadCount.value = res.data
  } catch (error) {
    // 接口请求失败时，在控制台打印错误信息
    console.error('加载未读数失败:', error)
  }
}

/**
 * 点击铃铛图标触发的事件
 * 功能：跳转到消息列表页面，查看所有消息
 */
const handleViewMessages = () => {
  router.push('/message/list')
}

/**
 * 生命周期钩子：组件挂载完成后执行
 * 1. 立即加载一次未读消息数
 * 2. 开启定时器，每50秒自动刷新一次未读消息数量
 */
onMounted(() => {
  loadUnreadCount()
  timer = setInterval(loadUnreadCount, 50000)
})

/**
 * 生命周期钩子：组件卸载时执行
 * 清除定时器，防止页面销毁后定时器继续执行造成内存泄漏
 */
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.message-bell {
  cursor: pointer;
}

:deep(.el-badge__content) {
  background-color: #F56C6C;
}
</style>