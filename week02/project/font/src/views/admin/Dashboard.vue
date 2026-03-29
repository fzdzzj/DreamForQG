<template>
  <!-- 管理员首页总容器 -->
  <div class="dashboard">
    <!-- 栅格布局：一行分4列，间距20px -->
    <el-row :gutter="20">
      
      <!-- 第1列：总报修单统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总报修单</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 第2列：待维修数量统计 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待维修</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 第3列：已完成数量统计 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 第4列：已取消数量统计 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#F56C6C"><CircleClose /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.cancelled }}</div>
              <div class="stat-label">已取消</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 待处理报修单列表卡片 -->
    <el-card style="margin-top: 20px">
      <!-- 卡片头部插槽 -->
      <template #header>
        <span>📋 待处理报修单</span>
      </template>

      <!-- 表格：显示待处理报修单 -->
      <el-table :data="pendingOrders" style="width: 100%">
        <el-table-column prop="id" label="单号" width="80" />
        <el-table-column prop="dormBuilding" label="楼栋" width="80" />
        <el-table-column prop="dormRoom" label="房间" width="80" />
        
        <!-- 设备类型：数字转中文 -->
        <el-table-column prop="deviceType" label="设备类型" width="100" >
          <template #default="{ row }">
            {{ deviceTypeMap[row.deviceType] || '未知' }}
          </template>
        </el-table-column>
        
        <!-- 优先级：数字转中文 -->
        <el-table-column prop="priority" label="优先级" width="80" >
          <template #default="{ row }">
            {{ priorityMap[row.priority] || '未知' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="180" />
        
        <!-- 操作列：完成按钮 -->
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleComplete(row.id)">
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
// 导入Vue组合API
import { ref, onMounted } from 'vue'
// 导入Element Plus图标
import { Document, Clock, CircleCheck, CircleClose } from '@element-plus/icons-vue'
// 导入报修单接口
import { getAllOrders, updateOrderStatus } from '@/api/order'
// 导入消息提示
import { ElMessage } from 'element-plus'

// ==================== 枚举映射：把后端数字 → 中文文字 ====================
// 设备类型映射
const deviceTypeMap = {
  '1': '水龙头',
  '2': '马桶',
  '3': '电灯',
  '4': '窗户',
  '5': '门',
  '6': '床',
  '7': '水槽',
  '8': '电表',
  '9': '水表'
}
// 优先级映射
const priorityMap = {
  '1': '普通',
  '2': '紧急',
  '3': '非常紧急'
}

// ==================== 响应式数据 ====================
// 统计数据：总数量、待维修、已完成、已取消
const stats = ref({
  total: 0,
  pending: 0,
  completed: 0,
  cancelled: 0
})

// 待处理报修单列表（表格展示）
const pendingOrders = ref([])

// ==================== 核心方法 ====================
/**
 * 加载数据：
 * 1. 获取所有报修单
 * 2. 自动统计 4 个状态数量
 * 3. 筛选出待处理单显示在表格
 */
const loadStats = async () => {
  try {
    const res = await getAllOrders()
    const orders = res.data || []

    // 统计总数
    stats.value.total = orders.length
    // 状态1：待维修
    stats.value.pending = orders.filter(o => o.status === '1').length
    // 状态2：已完成
    stats.value.completed = orders.filter(o => o.status === '2').length
    // 状态3：已取消
    stats.value.cancelled = orders.filter(o => o.status === '3').length

    // 取前5条待维修单显示在表格
    pendingOrders.value = orders.filter(o => o.status === '1').slice(0, 5)
  } catch (error) {
    console.error('加载失败:', error)
  }
}

/**
 * 点击【完成】按钮
 * 调用接口将报修单状态改为 2（已完成）
 * 然后重新加载数据刷新页面
 */
const handleComplete = async (id) => {
  try {
    await updateOrderStatus(id, { status: 2 })
    ElMessage.success('操作成功')
    loadStats() // 重新加载数据
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// ==================== 生命周期 ====================
// 页面一加载就执行：获取统计数据
onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 40px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>