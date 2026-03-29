<template>
  <!-- 学生首页容器 -->
  <div class="dashboard">
    <!-- 统计卡片行：总报修、待处理、已完成 -->
    <el-row :gutter="20">
      <!-- 总报修单卡片 -->
      <el-col :span="8">
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

      <!-- 待处理报修卡片 -->
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 已完成报修卡片 -->
      <el-col :span="8">
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
    </el-row>
    
    <!-- 最近报修单列表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>📋 最近报修单</span>
      </template>

      <!-- 最近 5 条报修单表格 -->
      <el-table :data="recentOrders" style="width: 100%">
        <el-table-column prop="id" label="单号" width="80" />
        
        <!-- 设备类型：数字转中文 -->
        <el-table-column prop="deviceType" label="设备类型" width="100">
          <template #default="scope">
            <el-tag type="info">
              {{ deviceTypeMap[scope.row.deviceType] || '未知设备' }}
            </el-tag>
          </template>  
        </el-table-column>
       
        <!-- 状态：数字转中文 -->
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="info">
              {{ statusTypeMap[scope.row.status] || '未知状态' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, Clock, CircleCheck } from '@element-plus/icons-vue'
// 接口：获取我的报修单
import { getMyOrders } from '@/api/order'

// 统计数据：总数量、待处理、已完成
const stats = ref({
  total: 0,
  pending: 0,
  completed: 0
})

// 设备类型映射：后端数字 → 前端中文
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

// 报修状态映射：后端数字 → 前端中文
const statusTypeMap = {
  '1': '待处理',
  '2': '已完成',
  '3': '已取消'
}

// 最近报修单（只显示前5条）
const recentOrders = ref([])

// ==================== 核心：加载统计数据 + 最近报修 ====================
const loadStats = async () => {
  try {
    // 1. 请求我的所有报修单
    const res = await getMyOrders()
    const orders = res.data || []

    // 2. 统计数量
    stats.value.total = orders.length
    stats.value.pending = orders.filter(o => o.status === '1').length
    stats.value.completed = orders.filter(o => o.status === '2').length

    // 3. 只取前5条作为最近记录
    recentOrders.value = orders.slice(0, 5)
  } catch (error) {
    console.error('加载失败:', error)
  }
}

// 页面加载时自动获取数据
onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.stat-card {
  margin-bottom: 20px;
}

/* 统计卡片布局：图标 + 数字 + 文字 */
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